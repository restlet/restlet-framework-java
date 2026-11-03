/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.restlet.util.Series;
import software.amazon.smithy.java.aws.client.restjson.RestJsonClientProtocol;
import software.amazon.smithy.java.client.http.JavaHttpClientTransport;
import software.amazon.smithy.java.client.rpcv2.RpcV2CborProtocol;
import software.amazon.smithy.java.core.error.ModeledException;
import software.amazon.smithy.java.core.schema.Schema;
import software.amazon.smithy.java.core.schema.SchemaIndex;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.TypeRegistry;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.dynamicclient.DocumentException;
import software.amazon.smithy.java.dynamicclient.DynamicClient;
import software.amazon.smithy.java.dynamicclient.DynamicOperation;
import software.amazon.smithy.java.dynamicschemas.SchemaConverter;
import software.amazon.smithy.java.dynamicschemas.StructDocument;
import software.amazon.smithy.java.framework.model.UnknownOperationException;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.server.Operation;
import software.amazon.smithy.java.server.RequestContext;
import software.amazon.smithy.java.server.Route;
import software.amazon.smithy.java.server.Service;
import software.amazon.smithy.java.server.core.Handler;
import software.amazon.smithy.java.server.core.HandlerAssembler;
import software.amazon.smithy.java.server.core.HttpJob;
import software.amazon.smithy.java.server.core.ProtocolResolver;
import software.amazon.smithy.java.server.core.ServiceMatcher;
import software.amazon.smithy.java.server.core.ServiceProtocolResolutionRequest;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.TopDownIndex;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.ShapeId;

/**
 * Feasibility spike for an experimental Smithy server: hosts smithy-java 1.8.0 server protocols
 * (restJson1, RPCv2 CBOR) and their handler chain in a Restlet, behind a Restlet router, running
 * each call on its own request thread instead of the SDK's single-thread orchestrator. The host and
 * the model-driven service below are test fixtures, not the production adapter.
 */
class SmithyServerSpikeTestCase {
    private static final ShapeId JSON_SPIKE = ShapeId.from("spike#JsonSpike");
    private static final ShapeId CBOR_SPIKE = ShapeId.from("spike#CborSpike");
    private static Model model;

    private Server server;
    private Client http2;

    @BeforeAll
    static void assembleModel() {
        ClassLoader loader = SmithyServerSpikeTestCase.class.getClassLoader();
        model =
                Model.assembler(loader)
                        .discoverModels(loader)
                        .addUnparsedModel("spike.smithy", SmithySdkTransportSpikeTestCase.MODEL)
                        .assemble()
                        .unwrap();
    }

    @BeforeEach
    void registerConnectors() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void stop() throws Exception {
        if (http2 != null) {
            http2.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    /** Starts an HTTP/1.1 and HTTP/2 (h2c) server with the Smithy host under "/api". */
    private String start(Map<String, Function<StructDocument, Document>> handlers)
            throws Exception {
        var services =
                List.<Service>of(
                        new ModelService(model, JSON_SPIKE, handlers),
                        new ModelService(model, CBOR_SPIKE, handlers));
        Router router = new Router();
        // The host receives every path below "/api", like the gRPC adapter's routes.
        router.attach(
                "/api", new SmithyHost(services), org.restlet.routing.Template.MODE_STARTS_WITH);
        Context context = new Context();
        context.getParameters().set("http.transport.protocol", "HTTP2");
        server = new Server(context, Protocol.HTTP, 0, router);
        server.start();
        return "http://localhost:" + server.getActualPort() + "/api";
    }

    private static Map<String, Function<StructDocument, Document>> echo() {
        Map<String, Function<StructDocument, Document>> handlers = new HashMap<>();
        handlers.put(
                "Echo",
                input -> {
                    if ("reject".equals(member(input, "tag"))) {
                        throw rejected("no");
                    }
                    return Document.of(
                            Map.of(
                                    "tag", Document.of(member(input, "tag")),
                                    "message",
                                            Document.of(
                                                    member(input, "message")
                                                            + "|"
                                                            + member(input, "id")
                                                            + "|"
                                                            + member(input, "q"))));
                });
        return handlers;
    }

    private static String member(StructDocument input, String name) {
        Document value = input.getMember(name);
        return value == null ? null : value.asString();
    }

    private static ModeledException rejected(String reason) {
        ServiceShape service = model.expectShape(JSON_SPIKE, ServiceShape.class);
        Schema schema =
                new SchemaConverter(model)
                        .getSchema(model.expectShape(ShapeId.from("spike#Rejected")));
        return Document.of(Map.of("reason", Document.of(reason)))
                .asShape(
                        new DocumentException.SchemaGuidedExceptionBuilder(
                                service.getId(), schema));
    }

    private static Document echoInput(String tag) {
        return echoInput(tag, "a b+c");
    }

    private static Document echoInput(String tag, String id) {
        return Document.of(
                Map.of(
                        "id", Document.of(id),
                        "q", Document.of("x=y&z é"),
                        "tag", Document.of(tag),
                        "message", Document.of("hello ünïcode")));
    }

    private static DynamicClient jdkJsonClient(String endpoint) {
        return DynamicClient.builder()
                .model(model)
                .serviceId(JSON_SPIKE)
                .protocol(new RestJsonClientProtocol(JSON_SPIKE))
                .transport(
                        new JavaHttpClientTransport(
                                java.net.http.HttpClient.newBuilder()
                                        .version(java.net.http.HttpClient.Version.HTTP_1_1)
                                        .build()))
                .endpoint(endpoint)
                .build();
    }

    /** Smithy's own JDK client calls the hosted restJson1 service, through a Restlet router. */
    @Test
    void restJsonRoundTripBehindARouter() throws Exception {
        String endpoint = start(echo());
        try (var client = jdkJsonClient(endpoint)) {
            Document output = client.call("Echo", echoInput("t1"));
            assertEquals("hello ünïcode|a b+c|x=y&z é", output.getMember("message").asString());
            assertEquals("t1", output.getMember("tag").asString());
        }
    }

    /**
     * Pins an upstream limit: the restJson1 server matches labels on the decoded path, so an
     * encoded "/" in a label splits it into two segments and no operation matches.
     */
    @Test
    void encodedSlashInALabelIsNotMatchedUpstream() throws Exception {
        String endpoint = start(echo());
        try (var client = jdkJsonClient(endpoint)) {
            var error =
                    assertThrows(
                            software.amazon.smithy.java.core.error.CallException.class,
                            () -> client.call("Echo", echoInput("t1", "a/b")));
            assertTrue(error.getMessage().contains("404"), error::getMessage);
        }
    }

    @Test
    void modeledErrorReachesTheClient() throws Exception {
        String endpoint = start(echo());
        try (var client = jdkJsonClient(endpoint)) {
            var error =
                    assertThrows(
                            DocumentException.class,
                            () -> client.call("Echo", echoInput("reject")));
            assertEquals("no", error.getContents().getMember("reason").asString());
        }
    }

    @Test
    void unknownOperationIsNotFound() throws Exception {
        String endpoint = start(echo());
        Client client = new Client(new Context(), Protocol.HTTP);
        client.start();
        try {
            Response response =
                    client.handle(new Request(org.restlet.data.Method.POST, endpoint + "/nowhere"));
            assertEquals(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        } finally {
            client.stop();
        }
    }

    /** RPCv2 CBOR between a Restlet HTTP/2 client and the hosted service, over h2c. */
    @Test
    void rpcV2CborOverHttp2() throws Exception {
        String endpoint = start(echo());
        Context clientContext = new Context();
        clientContext.getParameters().set("httpClientTransportMode", "HTTP2");
        http2 = new Client(clientContext, Protocol.HTTP);
        http2.start();
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(CBOR_SPIKE)
                        .protocol(new RpcV2CborProtocol(CBOR_SPIKE))
                        .transport(new SmithySdkTransportSpikeTestCase.RestletTransport(http2))
                        .endpoint(endpoint)
                        .build()) {
            Document output = client.call("Echo", echoInput("t2"));
            assertEquals("hello ünïcode|a b+c|x=y&z é", output.getMember("message").asString());
        }
    }

    /**
     * Blocking operations run on their own request threads: two calls that each wait for the other
     * to start both complete. On the SDK's single-thread orchestrator, they would deadlock.
     */
    @Test
    void blockingOperationsDoNotSerializeCalls() throws Exception {
        CountDownLatch bothEntered = new CountDownLatch(2);
        Map<String, Function<StructDocument, Document>> handlers = new HashMap<>();
        handlers.put(
                "Echo",
                input -> {
                    bothEntered.countDown();
                    try {
                        assertTrue(bothEntered.await(10, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return Document.of(Map.of("message", Document.of("done")));
                });
        String endpoint = start(handlers);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                var client = jdkJsonClient(endpoint)) {
            List<Future<Document>> calls = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                calls.add(executor.submit(() -> client.call("Echo", echoInput("x"))));
            }
            for (Future<Document> call : calls) {
                assertEquals(
                        "done", call.get(15, TimeUnit.SECONDS).getMember("message").asString());
            }
        }
    }

    /** A streaming payload starts reaching the client before the operation's stream ends. */
    @Test
    void streamingOutputIsNotBufferedWhole() throws Exception {
        CountDownLatch firstChunkRead = new CountDownLatch(1);
        AtomicBoolean restProduced = new AtomicBoolean();
        Map<String, Function<StructDocument, Document>> handlers = new HashMap<>();
        Schema payload =
                new SchemaConverter(model)
                        .getSchema(model.expectShape(ShapeId.from("spike#Payload")));
        handlers.put(
                "Download",
                input -> {
                    InputStream body =
                            new InputStream() {
                                private int sent;

                                @Override
                                public int read() {
                                    byte[] one = new byte[1];
                                    return read(one, 0, 1) < 0 ? -1 : one[0] & 0xff;
                                }

                                @Override
                                public int read(byte[] buffer, int offset, int length) {
                                    if (sent == 64 * 1024) {
                                        try {
                                            firstChunkRead.await(10, TimeUnit.SECONDS);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                        restProduced.set(true);
                                    }
                                    int total = 4 * 1024 * 1024 + 64 * 1024;
                                    if (sent >= total) {
                                        return -1;
                                    }
                                    int count =
                                            Math.min(
                                                    length,
                                                    (sent < 64 * 1024 ? 64 * 1024 : total) - sent);
                                    sent += count;
                                    return count;
                                }
                            };
                    return Document.of(
                            Map.of(
                                    "body",
                                    Document.of(
                                            payload,
                                            DataStream.ofInputStream(
                                                    body, "application/octet-stream"))));
                });
        String endpoint = start(handlers);
        Client restletClient = new Client(new Context(), Protocol.HTTP);
        restletClient.start();
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(JSON_SPIKE)
                        .protocol(new RestJsonClientProtocol(JSON_SPIKE))
                        .transport(
                                new SmithySdkTransportSpikeTestCase.RestletTransport(restletClient))
                        .endpoint(endpoint)
                        .build()) {
            Document output = client.call("Download");
            try (InputStream body = output.getMember("body").asDataStream().asInputStream()) {
                assertEquals(64 * 1024, body.readNBytes(64 * 1024).length);
                assertFalse(restProduced.get(), "the whole payload was buffered before sending");
                firstChunkRead.countDown();
                assertEquals(4 * 1024 * 1024, body.readAllBytes().length);
            }
        } finally {
            firstChunkRead.countDown();
        }
    }

    /** The spike host: SDK protocol resolution and handlers, run on the Restlet request thread. */
    private static final class SmithyHost extends Restlet {
        private final ProtocolResolver resolver;
        private final List<Handler> handlers;

        private SmithyHost(List<Service> services) {
            resolver =
                    new ProtocolResolver(
                            new ServiceMatcher(
                                    List.of(
                                            Route.builder()
                                                    .pathPrefix("/")
                                                    .services(services)
                                                    .build())));
            handlers = new HandlerAssembler().assembleHandlers(services);
        }

        @Override
        public void handle(Request request, Response response) {
            super.handle(request, response);
            // The SDK resolves operations on the path below the route that matched.
            String remaining = request.getResourceRef().getRemainingPart(false, true);
            URI uri = URI.create(remaining.startsWith("/") ? remaining : "/" + remaining);
            var headers = HttpHeaders.ofModifiable();
            for (Header header : request.getHeaders()) {
                headers.addHeader(header.getName(), header.getValue());
            }
            Representation entity = request.getEntity();
            if (entity != null
                    && entity.getMediaType() != null
                    && !headers.hasHeader("content-type")) {
                headers.addHeader("content-type", entity.getMediaType().toString());
            }
            var smithyRequest =
                    new software.amazon.smithy.java.server.core.HttpRequest(
                            headers, uri, request.getMethod().getName());
            HttpJob job;
            try {
                var resolution =
                        resolver.resolve(
                                new ServiceProtocolResolutionRequest(
                                        uri,
                                        headers,
                                        smithyRequest.context(),
                                        smithyRequest.method()));
                job =
                        new HttpJob(
                                resolution.operation(),
                                resolution.protocol(),
                                smithyRequest,
                                new software.amazon.smithy.java.server.core.HttpResponse(
                                        HttpHeaders.ofModifiable()));
            } catch (UnknownOperationException e) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            try {
                smithyRequest.setDataStream(
                        entity == null || !entity.isAvailable()
                                ? DataStream.ofEmpty()
                                : DataStream.ofInputStream(
                                        entity.getStream(),
                                        headers.contentType(),
                                        entity.getSize()));
            } catch (IOException e) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST, e);
                return;
            }
            run(job);
            write(job, response);
        }

        /** Runs the handlers as the SDK's orchestrator does, but joining on this thread. */
        private void run(HttpJob job) {
            var done = new ArrayDeque<Handler>();
            for (Handler handler : handlers) {
                if (job.isFailure() || job.isCompleted()) {
                    break;
                }
                done.push(handler);
                join(job, handler.before(job));
            }
            while (!done.isEmpty()) {
                join(job, done.pop().after(job));
            }
            if (job.isFailure()) {
                join(job, job.chosenProtocol().serializeError(job, job.getFailure()));
            }
        }

        private static void join(HttpJob job, CompletableFuture<Void> step) {
            try {
                step.join();
            } catch (CompletionException e) {
                job.setFailure(e.getCause());
            } catch (RuntimeException e) {
                job.setFailure(e);
            }
        }

        private static void write(HttpJob job, Response response) {
            var smithyResponse = job.response();
            response.setStatus(org.restlet.data.Status.valueOf(smithyResponse.getStatusCode()));
            var raw = new Series<Header>(Header.class);
            smithyResponse.headers().forEachEntry(raw::add);
            response.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_RESPONSE_HEADERS, raw);
            DataStream body = smithyResponse.getSerializedValue();
            if (body != null && body.contentLength() != 0) {
                var entity =
                        new OutputRepresentation(
                                body.contentType() == null
                                        ? null
                                        : MediaType.valueOf(body.contentType()),
                                body.contentLength() < 0
                                        ? Representation.UNKNOWN_SIZE
                                        : body.contentLength()) {
                            @Override
                            public void write(java.io.OutputStream out) throws IOException {
                                copy(body.asInputStream(), out);
                            }
                        };
                response.setEntity(entity);
            }
        }

        /**
         * Copies a payload, flushing whenever the source has nothing more available: the connector
         * only flushes full buffers once it holds more than its high water mark, so a producer
         * pausing mid-stream would otherwise hold back what it already produced.
         */
        private static void copy(InputStream in, java.io.OutputStream out) throws IOException {
            try (in) {
                byte[] buffer = new byte[16 * 1024];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                    if (in.available() == 0) {
                        out.flush();
                    }
                }
            }
        }
    }

    /** A server service built from the model, like smithy-java's ProxyService, without codegen. */
    private static final class ModelService implements Service {
        private final Map<String, Operation<StructDocument, StructDocument>> operations =
                new HashMap<>();
        private final List<Operation<? extends SerializableStruct, ? extends SerializableStruct>>
                all = new ArrayList<>();
        private final Schema schema;
        private final SchemaIndex index;
        private final TypeRegistry errors;

        private ModelService(
                Model model,
                ShapeId serviceId,
                Map<String, Function<StructDocument, Document>> handlers) {
            var converter = new SchemaConverter(model);
            var service = model.expectShape(serviceId, ServiceShape.class);
            var registry = TypeRegistry.builder();
            for (ShapeId error : service.getErrorsSet()) {
                register(error, serviceId, registry, model, converter);
            }
            errors = registry.build();
            for (var shape : TopDownIndex.of(model).getContainedOperations(service)) {
                String name = shape.getId().getName();
                var api =
                        DynamicOperation.create(
                                shape,
                                converter,
                                model,
                                service,
                                errors,
                                (error, builder) ->
                                        register(error, serviceId, builder, model, converter));
                Schema output = converter.getSchema(model.expectShape(shape.getOutputShape()));
                Function<StructDocument, Document> handler = handlers.get(name);
                Operation<StructDocument, StructDocument> operation =
                        Operation.of(
                                name,
                                (StructDocument input, RequestContext context) -> {
                                    if (handler == null) {
                                        throw new UnsupportedOperationException(name);
                                    }
                                    return StructDocument.of(
                                            output, handler.apply(input), serviceId);
                                },
                                api,
                                this);
                operations.put(name, operation);
                all.add(operation);
            }
            schema = converter.getSchema(service);
            index = converter.getSchemaIndex();
        }

        private static void register(
                ShapeId error,
                ShapeId serviceId,
                TypeRegistry.Builder registry,
                Model model,
                SchemaConverter converter) {
            Schema errorSchema = converter.getSchema(model.expectShape(error));
            registry.putType(
                    error,
                    ModeledException.class,
                    () ->
                            new DocumentException.SchemaGuidedExceptionBuilder(
                                    serviceId, errorSchema));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends SerializableStruct, O extends SerializableStruct>
                Operation<I, O> getOperation(String name) {
            return (Operation<I, O>) operations.get(name);
        }

        @Override
        public List<Operation<? extends SerializableStruct, ? extends SerializableStruct>>
                getAllOperations() {
            return all;
        }

        @Override
        public Schema schema() {
            return schema;
        }

        @Override
        public TypeRegistry typeRegistry() {
            return errors;
        }

        @Override
        public SchemaIndex schemaIndex() {
            return index;
        }
    }
}
