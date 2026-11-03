/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;
import software.amazon.smithy.java.aws.client.restjson.RestJsonClientProtocol;
import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.client.core.MessageExchange;
import software.amazon.smithy.java.client.core.error.TransportException;
import software.amazon.smithy.java.client.http.HttpMessageExchange;
import software.amazon.smithy.java.client.http.JavaHttpClientTransport;
import software.amazon.smithy.java.client.rpcv2.RpcV2CborProtocol;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.dynamicclient.DocumentException;
import software.amazon.smithy.java.dynamicclient.DynamicClient;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.http.api.HttpRequest;
import software.amazon.smithy.java.http.api.HttpResponse;
import software.amazon.smithy.java.http.api.HttpVersion;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;

/**
 * R5 feasibility spike: runs pinned smithy-java 1.8.0 dynamic clients, with their own protocol
 * codecs, over a Restlet {@link Client} through the SDK's {@link ClientTransport} seam. The
 * transport below is a test fixture, not the production adapter.
 */
class SmithySdkTransportSpikeTestCase {
    static final String MODEL =
            """
            $version: "2"
            namespace spike

            use aws.protocols#restJson1
            use smithy.protocols#rpcv2Cbor

            @restJson1
            service JsonSpike {
                version: "1"
                operations: [Echo, Download]
                errors: [Rejected]
            }

            @rpcv2Cbor
            service CborSpike {
                version: "1"
                operations: [Echo]
                errors: [Rejected]
            }

            @http(method: "POST", uri: "/echo/{id}")
            operation Echo {
                input := {
                    @required
                    @httpLabel
                    id: String

                    @httpQuery("q")
                    q: String

                    @httpHeader("x-tag")
                    tag: String

                    @httpHeader("If-Match")
                    ifMatch: String

                    @httpHeader("Accept")
                    accept: String

                    message: String
                }
                output := {
                    @httpHeader("x-tag")
                    tag: String

                    message: String
                }
            }

            @readonly
            @http(method: "GET", uri: "/download")
            operation Download {
                output := {
                    @required
                    @httpPayload
                    body: Payload
                }
            }

            @streaming
            blob Payload

            @error("client")
            @httpError(409)
            structure Rejected {
                reason: String
            }
            """;

    private static final ShapeId JSON_SPIKE = ShapeId.from("spike#JsonSpike");
    private static final ShapeId CBOR_SPIKE = ShapeId.from("spike#CborSpike");

    /** Transports created by the factory, to see which one the SDK discovered. */
    static final AtomicInteger DISCOVERED = new AtomicInteger();

    private final List<Captured> captured = new ArrayList<>();
    private final CountDownLatch firstChunkRead = new CountDownLatch(1);
    private final AtomicBoolean restWritten = new AtomicBoolean();
    private static Model model;
    private HttpServer peer;
    private Client restlet;

    /** A request exactly as the peer received it. */
    private record Captured(
            String method, String target, Map<String, List<String>> headers, byte[] body) {}

    @BeforeAll
    static void assembleModel() {
        // Discovering the trait models on the class path takes seconds: once per class.
        ClassLoader loader = SmithySdkTransportSpikeTestCase.class.getClassLoader();
        model =
                Model.assembler(loader)
                        .discoverModels(loader)
                        .addUnparsedModel("spike.smithy", MODEL)
                        .assemble()
                        .unwrap();
    }

    @BeforeEach
    void startPeer() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        peer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        peer.createContext("/", this::serve);
        peer.start();
        restlet = new Client(new Context(), Protocol.HTTP);
        restlet.start();
    }

    @AfterEach
    void stopPeer() throws Exception {
        firstChunkRead.countDown();
        if (peer != null) {
            peer.stop(0);
        }
        if (restlet != null) {
            restlet.stop();
        }
    }

    private void serve(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();
        var headers = new TreeMap<String, List<String>>();
        exchange.getRequestHeaders()
                .forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), values));
        synchronized (captured) {
            captured.add(
                    new Captured(
                            exchange.getRequestMethod(),
                            exchange.getRequestURI().getRawPath()
                                    + (exchange.getRequestURI().getRawQuery() == null
                                            ? ""
                                            : "?" + exchange.getRequestURI().getRawQuery()),
                            headers,
                            body));
        }
        String path = exchange.getRequestURI().getRawPath();
        if (path.startsWith("/download")) {
            exchange.getResponseHeaders().add("content-type", "application/octet-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(new byte[64 * 1024]);
                out.flush();
                try {
                    firstChunkRead.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                restWritten.set(true);
                out.write(new byte[4 * 1024 * 1024]);
            }
            return;
        }
        String tag = exchange.getRequestHeaders().getFirst("x-tag");
        if ("reject".equals(tag)) {
            byte[] error =
                    "{\"__type\":\"spike#Rejected\",\"reason\":\"no\"}"
                            .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("content-type", "application/json");
            exchange.sendResponseHeaders(409, error.length);
            exchange.getResponseBody().write(error);
            exchange.close();
            return;
        }
        if (tag != null) {
            exchange.getResponseHeaders().add("x-tag", tag);
        }
        exchange.getResponseHeaders().add("content-type", "application/json");
        exchange.sendResponseHeaders(200, body.length == 0 ? -1 : body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private String endpoint() {
        return "http://localhost:" + peer.getAddress().getPort();
    }

    private DynamicClient json(ClientTransport<?, ?> transport) {
        return DynamicClient.builder()
                .model(model)
                .serviceId(JSON_SPIKE)
                .protocol(new RestJsonClientProtocol(JSON_SPIKE))
                .transport(transport)
                .endpoint(endpoint())
                .build();
    }

    private static Document echoInput(String tag) {
        return Document.of(
                Map.of(
                        "id", Document.of("a b/c+d"),
                        "q", Document.of("x=y&z é"),
                        "tag", Document.of(tag),
                        "ifMatch", Document.of("\"v1\""),
                        "accept", Document.of("application/json;q=0.9, */*;q=0.1"),
                        "message", Document.of("hello ünïcode")));
    }

    /**
     * The bytes that Smithy serialized, and that a signer would sign, reach the peer unchanged:
     * method, raw target, every header Smithy set, and the body. The JDK transport passes the same
     * checks, so they test the expectations, not only the Restlet transport.
     */
    @Test
    void restJsonRequestBytesReachThePeerUnchanged() throws Exception {
        for (boolean overRestlet : new boolean[] {true, false}) {
            captured.clear();
            var transport =
                    overRestlet
                            ? new RestletTransport(restlet)
                            : new RecordingJdkTransport(
                                    java.net.http.HttpClient.newBuilder()
                                            .version(java.net.http.HttpClient.Version.HTTP_1_1)
                                            .build());
            try (var client = json(transport)) {
                Document output = client.call("Echo", echoInput("t1"));
                assertEquals("hello ünïcode", output.getMember("message").asString());
                assertEquals("t1", output.getMember("tag").asString());
            }
            HttpRequest sent =
                    overRestlet
                            ? ((RestletTransport) transport).last
                            : ((RecordingJdkTransport) transport).last;
            byte[] expected =
                    overRestlet
                            ? ((RestletTransport) transport).lastBody
                            : ((RecordingJdkTransport) transport).lastBody;
            Captured received = captured.getFirst();
            String transportName = overRestlet ? "restlet" : "jdk";
            assertEquals(sent.method(), received.method(), transportName);
            String target =
                    sent.uri().getPath()
                            + (sent.uri().getQuery() == null ? "" : "?" + sent.uri().getQuery());
            assertEquals(target, received.target(), transportName);
            sent.headers()
                    .forEachEntry(
                            (name, value) ->
                                    assertEquals(
                                            List.of(value),
                                            received.headers().get(name.toLowerCase(Locale.ROOT)),
                                            transportName + " header " + name));
            assertArrayEquals(expected, received.body(), transportName);
        }
    }

    @Test
    void modeledErrorKeepsItsShapeAndMembers() {
        try (var client = json(new RestletTransport(restlet))) {
            var error =
                    assertThrows(
                            DocumentException.class,
                            () -> client.call("Echo", echoInput("reject")));
            assertEquals("no", error.getContents().getMember("reason").asString());
        }
    }

    /** The response body is a stream: the call returns before the peer has sent all of it. */
    @Test
    void streamingResponseIsNotBufferedWhole() throws Exception {
        try (var client = json(new RestletTransport(restlet))) {
            Document output = client.call("Download");
            assertFalse(restWritten.get(), "the whole response was buffered before returning");
            try (InputStream body = output.getMember("body").asDataStream().asInputStream()) {
                assertEquals(64 * 1024, body.readNBytes(64 * 1024).length);
                firstChunkRead.countDown();
                assertEquals(4 * 1024 * 1024, body.readAllBytes().length);
            }
        }
    }

    @Test
    void connectionFailureIsATransportException() throws Exception {
        int port;
        try (var socket = new java.net.ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(JSON_SPIKE)
                        .protocol(new RestJsonClientProtocol(JSON_SPIKE))
                        .transport(new RestletTransport(restlet))
                        .endpoint("http://localhost:" + port)
                        .build()) {
            assertInstanceOf(
                    TransportException.class,
                    assertThrows(
                            RuntimeException.class, () -> client.call("Echo", echoInput("x"))));
        }
    }

    /** RPCv2 CBOR over a Restlet HTTP/2 (h2c) server and an HTTP/2 Restlet client. */
    @Test
    void rpcV2CborOverHttp2() throws Exception {
        AtomicReference<String> protocol = new AtomicReference<>();
        AtomicReference<String> smithyProtocol = new AtomicReference<>();
        Context serverContext = new Context();
        serverContext.getParameters().set("http.transport.protocol", "HTTP2");
        Server server =
                new Server(
                        serverContext,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                protocol.set(request.getProtocol().getVersion());
                                smithyProtocol.set(
                                        request.getHeaders()
                                                .getFirstValue("smithy-protocol", true));
                                try {
                                    byte[] body = request.getEntity().getStream().readAllBytes();
                                    response.setEntity(
                                            new ByteArrayRepresentation(
                                                    body, MediaType.valueOf("application/cbor")));
                                    response.getHeaders().add("smithy-protocol", "rpc-v2-cbor");
                                } catch (IOException e) {
                                    response.setStatus(
                                            org.restlet.data.Status.SERVER_ERROR_INTERNAL, e);
                                }
                            }
                        });
        server.start();
        Context clientContext = new Context();
        clientContext.getParameters().set("httpClientTransportMode", "HTTP2");
        Client http2 = new Client(clientContext, Protocol.HTTP);
        http2.start();
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(CBOR_SPIKE)
                        .protocol(new RpcV2CborProtocol(CBOR_SPIKE))
                        .transport(new RestletTransport(http2))
                        .endpoint("http://localhost:" + server.getActualPort())
                        .build()) {
            Document output = client.call("Echo", echoInput("t2"));
            assertEquals("hello ünïcode", output.getMember("message").asString());
            assertEquals("2.0", protocol.get());
            assertEquals("rpc-v2-cbor", smithyProtocol.get());
        } finally {
            http2.stop();
            server.stop();
        }
    }

    /**
     * The HTTP/2 client may still be writing the request body when the response ends the SDK call
     * and the SDK releases its pooled body: the transport sends its own copy.
     */
    @Test
    void responseBeforeTheUploadEndsCompletesTheCall() throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().set("http.transport.protocol", "HTTP2");
        Server server =
                new Server(
                        serverContext,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                // Answers at once with an empty CBOR map, without reading.
                                response.setEntity(
                                        new ByteArrayRepresentation(
                                                new byte[] {(byte) 0xa0},
                                                MediaType.valueOf("application/cbor")));
                                response.getHeaders().add("smithy-protocol", "rpc-v2-cbor");
                            }
                        });
        server.start();
        Context clientContext = new Context();
        clientContext.getParameters().set("httpClientTransportMode", "HTTP2");
        Client http2 = new Client(clientContext, Protocol.HTTP);
        http2.start();
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(CBOR_SPIKE)
                        .protocol(new RpcV2CborProtocol(CBOR_SPIKE))
                        .transport(new RestletTransport(http2))
                        .endpoint("http://localhost:" + server.getActualPort())
                        .build()) {
            for (int i = 0; i < 3; i++) {
                Document output =
                        client.call(
                                "Echo",
                                Document.of(
                                        Map.of(
                                                "id", Document.of("big"),
                                                "message", Document.of("x".repeat(1024 * 1024)))));
                assertNull(output.getMember("message"));
            }
        } finally {
            http2.stop();
            server.stop();
        }
    }

    /**
     * Closing an SDK client closes its transport, which leaves a borrowed Restlet client running.
     */
    @Test
    void closingTheSdkClientLeavesTheBorrowedClientRunning() {
        var transport = new RestletTransport(restlet);
        try (var client = json(transport)) {
            client.call("Echo", echoInput("t3"));
        }
        assertTrue(restlet.isStarted());
    }

    /**
     * Without an explicit transport, the SDK picks the first discovered factory whose message
     * exchange matches the protocol, after sorting the factories by ascending priority. The JDK
     * transport is always present, so the Restlet factory must use a negative priority.
     */
    @Test
    void discoveredFactoriesAreSortedByAscendingPriority() {
        var names =
                ClientTransportFactory.load(getClass().getClassLoader()).stream()
                        .map(ClientTransportFactory::name)
                        .toList();
        assertEquals(List.of("restlet", "http-java"), names);
        int before = DISCOVERED.get();
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(JSON_SPIKE)
                        .protocol(new RestJsonClientProtocol(JSON_SPIKE))
                        .endpoint(endpoint())
                        .build()) {
            assertEquals("t4", client.call("Echo", echoInput("t4")).getMember("tag").asString());
        }
        assertEquals(before + 1, DISCOVERED.get(), names::toString);
    }

    private static byte[] copy(DataStream body) {
        var buffer = body.asByteBuffer().duplicate();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /** The JDK transport, recording the request it was given. */
    private static final class RecordingJdkTransport
            implements ClientTransport<HttpRequest, HttpResponse> {
        private final JavaHttpClientTransport delegate;
        private volatile HttpRequest last;
        private volatile byte[] lastBody;

        private RecordingJdkTransport(java.net.http.HttpClient client) {
            delegate = new JavaHttpClientTransport(client);
        }

        @Override
        public HttpResponse send(
                software.amazon.smithy.java.context.Context context, HttpRequest r) {
            last = r;
            lastBody = copy(r.body());
            return delegate.send(context, r);
        }

        @Override
        public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
            return HttpMessageExchange.INSTANCE;
        }
    }

    /** The spike transport: a Smithy HTTP exchange over a borrowed Restlet client. */
    static final class RestletTransport implements ClientTransport<HttpRequest, HttpResponse> {
        private final Client client;
        private final boolean owned;
        volatile HttpRequest last;
        volatile byte[] lastBody;

        RestletTransport(Client client) {
            this(client, false);
        }

        private RestletTransport(Client client, boolean owned) {
            this.client = client;
            this.owned = owned;
        }

        @Override
        public HttpResponse send(
                software.amazon.smithy.java.context.Context context, HttpRequest request) {
            last = request;
            try {
                // The SDK releases a pooled request body when the call ends, while the HTTP/2
                // client may still be writing it after an early response: send a local copy.
                byte[] body = request.body() == null ? new byte[0] : copy(request.body());
                lastBody = body;
                Request call =
                        new Request(Method.valueOf(request.method()), request.uri().toString());
                if (body.length > 0) {
                    String type = request.headers().contentType();
                    call.setEntity(
                            new ByteArrayRepresentation(
                                    body, type == null ? null : MediaType.valueOf(type)));
                }
                // Written as serialized, and signed, by the SDK: standard headers included.
                var raw = new Series<Header>(Header.class);
                request.headers().forEachEntry(raw::add);
                call.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_REQUEST_HEADERS, raw);
                Response response = client.handle(call);
                org.restlet.data.Status status = response.getStatus();
                if (status.isConnectorError()) {
                    throw ClientTransport.remapExceptions(
                            status.getThrowable() == null
                                    ? new IOException(status.toString())
                                    : status.getThrowable());
                }
                var headers = HttpHeaders.ofModifiable();
                for (Header header : response.getHeaders()) {
                    headers.addHeader(header.getName(), header.getValue());
                }
                Representation entity = response.getEntity();
                DataStream output;
                if (entity == null || !entity.isAvailable()) {
                    output = DataStream.ofEmpty();
                } else {
                    output =
                            DataStream.ofInputStream(
                                    entity.getStream(),
                                    entity.getMediaType() == null
                                            ? null
                                            : entity.getMediaType().toString(),
                                    entity.getSize());
                }
                HttpVersion version =
                        response.getProtocol() != null
                                        && "2.0".equals(response.getProtocol().getVersion())
                                ? HttpVersion.HTTP_2
                                : HttpVersion.HTTP_1_1;
                return HttpResponse.of(version, status.getCode(), headers, output);
            } catch (IOException | RuntimeException e) {
                throw ClientTransport.remapExceptions(e);
            }
        }

        @Override
        public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
            return HttpMessageExchange.INSTANCE;
        }

        @Override
        public void close() {
            if (owned) {
                try {
                    client.stop();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /** Factory discovered through the SDK's service loader; owns the client it creates. */
    public static final class RestletTransportFactory
            implements ClientTransportFactory<HttpRequest, HttpResponse> {
        @Override
        public String name() {
            return "restlet";
        }

        @Override
        public byte priority() {
            // The SDK picks the lowest priority first, contrary to its Javadoc: -1 beats the
            // JDK transport (0) that every HTTP protocol module brings along.
            return -1;
        }

        @Override
        public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
            return HttpMessageExchange.INSTANCE;
        }

        @Override
        public ClientTransport<HttpRequest, HttpResponse> createTransport(
                Document settings, Document pluginSettings) {
            DISCOVERED.incrementAndGet();
            Client client = new Client(new Context(), Protocol.HTTP);
            try {
                client.start();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return new RestletTransport(client, true);
        }
    }
}
