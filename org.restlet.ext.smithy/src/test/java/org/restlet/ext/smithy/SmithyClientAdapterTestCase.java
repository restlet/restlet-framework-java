/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.smithy.internal.RestletTransportFactory;
import org.restlet.representation.ByteArrayRepresentation;
import software.amazon.smithy.java.auth.api.identity.IdentityResolver;
import software.amazon.smithy.java.aws.auth.api.identity.AwsCredentialsIdentity;
import software.amazon.smithy.java.aws.client.auth.scheme.sigv4.SigV4AuthScheme;
import software.amazon.smithy.java.aws.client.core.settings.RegionSetting;
import software.amazon.smithy.java.aws.client.restjson.RestJsonClientProtocol;
import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.client.core.MessageExchange;
import software.amazon.smithy.java.client.core.error.TransportException;
import software.amazon.smithy.java.client.core.error.TransportSocketTimeout;
import software.amazon.smithy.java.client.http.HttpContext;
import software.amazon.smithy.java.client.rpcv2.RpcV2CborProtocol;
import software.amazon.smithy.java.core.schema.Schema;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.dynamicclient.DocumentException;
import software.amazon.smithy.java.dynamicclient.DynamicClient;
import software.amazon.smithy.java.dynamicschemas.SchemaConverter;
import software.amazon.smithy.java.http.api.HttpRequest;
import software.amazon.smithy.java.http.api.HttpResponse;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.retries.api.RetryStrategy;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;

class SmithyClientAdapterTestCase {
    static final String MODEL =
            """
            $version: "2"
            namespace test

            use aws.auth#sigv4
            use aws.protocols#restJson1
            use smithy.protocols#rpcv2Cbor

            @restJson1
            service JsonService {
                version: "1"
                operations: [Echo, Download, Upload]
                errors: [Rejected]
            }

            @restJson1
            @sigv4(name: "test")
            service SignedService {
                version: "1"
                operations: [Echo, Upload]
                errors: [Rejected]
            }

            @rpcv2Cbor
            service CborService {
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

                    @httpHeader("ETag")
                    etag: String

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

            @idempotent
            @http(method: "PUT", uri: "/upload")
            operation Upload {
                input := {
                    @httpHeader("x-tag")
                    tag: String

                    @required
                    @httpPayload
                    body: Payload
                }
                output := {
                    @httpHeader("x-received")
                    received: String
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

    static final ShapeId JSON_SERVICE = ShapeId.from("test#JsonService");
    private static final ShapeId SIGNED_SERVICE = ShapeId.from("test#SignedService");
    private static final ShapeId CBOR_SERVICE = ShapeId.from("test#CborService");

    private static Model model;
    private final List<Captured> captured = new ArrayList<>();
    private final CountDownLatch firstChunkRead = new CountDownLatch(1);
    private final CountDownLatch released = new CountDownLatch(1);
    private final AtomicBoolean restWritten = new AtomicBoolean();
    private HttpServer peer;
    private Client restlet;

    /** A request exactly as the peer received it. */
    private record Captured(
            String method, String target, Map<String, List<String>> headers, byte[] body) {
        String header(String name) {
            List<String> values = headers.get(name);
            return values == null ? null : values.getFirst();
        }
    }

    @BeforeAll
    static void assembleModel() {
        // Discovering the trait models on the class path takes seconds: once per class.
        ClassLoader loader = SmithyClientAdapterTestCase.class.getClassLoader();
        model =
                Model.assembler(loader)
                        .discoverModels(loader)
                        .addUnparsedModel("test.smithy", MODEL)
                        .assemble()
                        .unwrap();
    }

    @BeforeEach
    void startPeer() throws Exception {
        peer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        peer.createContext("/", this::serve);
        peer.start();
        restlet = borrowable(new Context());
        restlet.start();
    }

    @AfterEach
    void stopPeer() throws Exception {
        firstChunkRead.countDown();
        released.countDown();
        peer.stop(0);
        restlet.stop();
    }

    private static Client borrowable(Context context) {
        context.getParameters().set("followRedirects", "false");
        return new Client(context, Protocol.HTTP);
    }

    private void serve(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();
        var headers = new TreeMap<String, List<String>>();
        exchange.getRequestHeaders()
                .forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), values));
        int attempt;
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
            attempt = captured.size();
        }
        String path = exchange.getRequestURI().getRawPath();
        String tag = exchange.getRequestHeaders().getFirst("x-tag");
        if (path.startsWith("/download")) {
            exchange.getResponseHeaders().add("content-type", "application/octet-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(new byte[64 * 1024]);
                out.flush();
                await(firstChunkRead);
                restWritten.set(true);
                out.write(new byte[4 * 1024 * 1024]);
            }
            return;
        }
        if ("retry".equals(tag) && attempt == 1) {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
            return;
        }
        if (path.startsWith("/upload")) {
            exchange.getResponseHeaders().add("x-received", received(body));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }
        if ("slow".equals(tag)) {
            await(released);
        } else if ("redirect".equals(tag)) {
            exchange.getResponseHeaders().add("location", "/echo/elsewhere");
            exchange.sendResponseHeaders(307, -1);
            exchange.close();
            return;
        } else if ("reject".equals(tag)) {
            byte[] error =
                    "{\"__type\":\"test#Rejected\",\"reason\":\"no\"}"
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
        exchange.getResponseHeaders().add("ETag", "\"e1\"");
        exchange.getResponseHeaders().add("content-type", "application/json");
        exchange.sendResponseHeaders(200, body.length == 0 ? -1 : body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** The length and SHA-256 of a body. */
    private static String received(byte[] body) {
        return body.length + ":" + sha256(body);
    }

    private static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String endpoint() {
        return "http://localhost:" + peer.getAddress().getPort();
    }

    private DynamicClient.Builder json(ClientTransport<?, ?> transport) {
        return DynamicClient.builder()
                .model(model)
                .serviceId(JSON_SERVICE)
                .protocol(new RestJsonClientProtocol(JSON_SERVICE))
                .transport(transport)
                .endpoint(endpoint());
    }

    private DynamicClient signed(ClientTransport<?, ?> transport) {
        return DynamicClient.builder()
                .model(model)
                .serviceId(SIGNED_SERVICE)
                .protocol(new RestJsonClientProtocol(SIGNED_SERVICE))
                .transport(transport)
                .endpoint(endpoint())
                .putSupportedAuthSchemes(new SigV4AuthScheme("test"))
                .addIdentityResolver(
                        IdentityResolver.of(AwsCredentialsIdentity.create("AKID", "SECRET")))
                .putConfig(RegionSetting.REGION, "us-east-1")
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

    private static Document upload(DataStream body) {
        Schema payload =
                new SchemaConverter(model)
                        .getSchema(model.expectShape(ShapeId.from("test#Payload")));
        return Document.of(Map.of("body", Document.of(payload, body)));
    }

    /** Asserts that a request reached the peer exactly as the SDK handed it to the transport. */
    private static void assertUnchanged(Recording.Sent sent, Captured received) {
        assertEquals(sent.request().method(), received.method());
        String target =
                sent.request().uri().getPath()
                        + (sent.request().uri().getQuery() == null
                                ? ""
                                : "?" + sent.request().uri().getQuery());
        assertEquals(target, received.target());
        sent.request()
                .headers()
                .forEachEntry(
                        (name, value) ->
                                assertEquals(
                                        List.of(value),
                                        received.headers().get(name.toLowerCase(Locale.ROOT)),
                                        "header " + name));
        assertArrayEquals(sent.body(), received.body());
    }

    /**
     * Method, raw target, every header the SDK set (standard ones included) and body bytes reach
     * the peer unchanged, and standard response headers come back.
     */
    @Test
    void requestBytesReachThePeerUnchanged() {
        var transport = new Recording(new SmithyClientAdapter(restlet));
        try (var client = json(transport).build()) {
            Document output = client.call("Echo", echoInput("t1"));
            assertEquals("hello ünïcode", output.getMember("message").asString());
            assertEquals("t1", output.getMember("tag").asString());
            assertEquals("\"e1\"", output.getMember("etag").asString());
        }
        assertUnchanged(transport.sent.getFirst(), captured.getFirst());
    }

    /** A SigV4-signed request reaches the peer as signed. */
    @Test
    void signedRequestReachesThePeerAsSigned() {
        var transport = new Recording(new SmithyClientAdapter(restlet));
        try (var client = signed(transport)) {
            assertEquals("t6", client.call("Echo", echoInput("t6")).getMember("tag").asString());
        }
        Captured received = captured.getFirst();
        assertUnchanged(transport.sent.getFirst(), received);
        assertTrue(received.header("authorization").startsWith("AWS4-HMAC-SHA256 "));
        assertNotNull(received.header("x-amz-date"));
    }

    /** A body of unknown length is streamed, chunked over HTTP/1.1, and arrives intact. */
    @Test
    void uploadOfUnknownLengthIsStreamed() {
        byte[] payload = new byte[3 * 1024 * 1024 + 7];
        new Random(1).nextBytes(payload);
        var transport = new Recording(new SmithyClientAdapter(restlet));
        try (var client = json(transport).build()) {
            Document output =
                    client.call(
                            "Upload",
                            upload(
                                    DataStream.ofInputStream(
                                            new java.io.ByteArrayInputStream(payload))));
            assertEquals(received(payload), output.getMember("received").asString());
        }
        assertEquals("chunked", captured.getFirst().header("transfer-encoding"));
        assertNull(captured.getFirst().header("content-length"));
    }

    /** A retried signed upload replays the same body, signed again, without copying it. */
    @Test
    void retriedSignedUploadReplaysItsBody() {
        byte[] payload = new byte[256 * 1024];
        new Random(2).nextBytes(payload);
        var transport = new Recording(new SmithyClientAdapter(restlet));
        try (var client = signed(transport)) {
            Document input =
                    Document.of(
                            Map.of(
                                    "tag",
                                    Document.of("retry"),
                                    "body",
                                    upload(DataStream.ofBytes(payload)).getMember("body")));
            assertEquals(
                    received(payload),
                    client.call("Upload", input).getMember("received").asString());
        }
        assertEquals(2, captured.size());
        assertEquals(2, transport.sent.size());
        for (int i = 0; i < 2; i++) {
            Captured received = captured.get(i);
            assertUnchanged(transport.sent.get(i), received);
            assertArrayEquals(payload, received.body());
            assertTrue(received.header("authorization").startsWith("AWS4-HMAC-SHA256 "));
            assertNotNull(received.header("x-amz-date"));
        }
    }

    /** Uploads and RPCv2 CBOR calls over HTTP/2, with a body of unknown length. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void http2(boolean cbor) throws Exception {
        AtomicReference<String> protocol = new AtomicReference<>();
        Server server =
                http2Server(
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                protocol.set(request.getProtocol().getVersion());
                                try {
                                    byte[] body = request.getEntity().getStream().readAllBytes();
                                    if (cbor) {
                                        response.setEntity(
                                                new ByteArrayRepresentation(
                                                        body,
                                                        MediaType.valueOf("application/cbor")));
                                        response.getHeaders().add("smithy-protocol", "rpc-v2-cbor");
                                    } else {
                                        response.getHeaders().add("x-received", received(body));
                                    }
                                } catch (IOException e) {
                                    response.setStatus(
                                            org.restlet.data.Status.SERVER_ERROR_INTERNAL, e);
                                }
                            }
                        });
        try (var transport = new SmithyClientAdapter(http2Context())) {
            String endpoint = "http://localhost:" + server.getActualPort();
            if (cbor) {
                try (var client =
                        DynamicClient.builder()
                                .model(model)
                                .serviceId(CBOR_SERVICE)
                                .protocol(new RpcV2CborProtocol(CBOR_SERVICE))
                                .transport(transport)
                                .endpoint(endpoint)
                                .build()) {
                    assertEquals(
                            "hello ünïcode",
                            client.call("Echo", echoInput("t2")).getMember("message").asString());
                }
            } else {
                byte[] payload = new byte[1024 * 1024 + 3];
                new Random(3).nextBytes(payload);
                try (var client = json(transport).endpoint(endpoint).build()) {
                    Document output =
                            client.call(
                                    "Upload",
                                    upload(
                                            DataStream.ofInputStream(
                                                    new java.io.ByteArrayInputStream(payload))));
                    assertEquals(received(payload), output.getMember("received").asString());
                }
            }
            assertEquals("2.0", protocol.get());
        } finally {
            server.stop();
        }
    }

    /**
     * An HTTP/2 peer may answer before the request body is written, which ends the SDK call and
     * releases its pooled body while the client is still writing it.
     */
    @Test
    void responseBeforeTheUploadEndsCompletesTheCall() throws Exception {
        Server server =
                http2Server(
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
        try (var transport = new SmithyClientAdapter(http2Context());
                var client =
                        DynamicClient.builder()
                                .model(model)
                                .serviceId(CBOR_SERVICE)
                                .protocol(new RpcV2CborProtocol(CBOR_SERVICE))
                                .transport(transport)
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
            server.stop();
        }
    }

    /** The response body is a stream: the call returns before the peer has sent all of it. */
    @Test
    void streamingResponseIsNotBufferedWhole() throws Exception {
        try (var client = json(new SmithyClientAdapter(restlet)).build()) {
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
    void modeledErrorKeepsItsShapeAndMembers() {
        try (var client = json(new SmithyClientAdapter(restlet)).build()) {
            var error =
                    assertThrows(
                            DocumentException.class,
                            () -> client.call("Echo", echoInput("reject")));
            assertEquals("no", error.getContents().getMember("reason").asString());
        }
    }

    /** Redirects reach the SDK, which neither the transport nor the client follows. */
    @Test
    void redirectsAreNotFollowed() {
        try (var client =
                json(new SmithyClientAdapter(restlet))
                        .retryStrategy(RetryStrategy.noRetries())
                        .build()) {
            assertThrows(RuntimeException.class, () -> client.call("Echo", echoInput("redirect")));
        }
        assertEquals(1, captured.size());
    }

    @Test
    void connectionFailureIsATransportException() throws Exception {
        int port;
        try (var socket = new java.net.ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        try (var client =
                json(new SmithyClientAdapter(restlet))
                        .endpoint("http://localhost:" + port)
                        .retryStrategy(RetryStrategy.noRetries())
                        .build()) {
            assertInstanceOf(
                    TransportException.class,
                    assertThrows(
                            RuntimeException.class, () -> client.call("Echo", echoInput("x"))));
        }
    }

    /** The SDK's request timeout, or the transport's default one, aborts the request. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void requestTimeoutAbortsTheRequest(boolean sdkSetting) {
        var transport = new SmithyClientAdapter(restlet);
        var builder = json(transport).retryStrategy(RetryStrategy.noRetries());
        if (sdkSetting) {
            transport.setRequestTimeout(Duration.ofMinutes(1));
            builder.putConfig(HttpContext.HTTP_REQUEST_TIMEOUT, Duration.ofMillis(300));
        } else {
            transport.setRequestTimeout(Duration.ofMillis(300));
        }
        long start = System.nanoTime();
        try (var client = builder.build()) {
            assertInstanceOf(
                    TransportSocketTimeout.class,
                    assertThrows(
                            RuntimeException.class, () -> client.call("Echo", echoInput("slow"))));
        }
        assertTrue(Duration.ofNanos(System.nanoTime() - start).toSeconds() < 5);
    }

    /** Interrupting the calling thread aborts the request and keeps the interrupt. */
    @Test
    void interruptionAbortsTheRequest() throws Exception {
        CompletableFuture<Throwable> failure = new CompletableFuture<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        try (var client =
                json(new SmithyClientAdapter(restlet))
                        .retryStrategy(RetryStrategy.noRetries())
                        .build()) {
            Thread caller =
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        try {
                                            client.call("Echo", echoInput("slow"));
                                            failure.complete(null);
                                        } catch (Throwable e) {
                                            interrupted.set(Thread.currentThread().isInterrupted());
                                            failure.complete(e);
                                        }
                                    });
            while (captured.isEmpty()) {
                Thread.onSpinWait();
            }
            caller.interrupt();
            assertInstanceOf(TransportException.class, failure.get(5, TimeUnit.SECONDS));
            assertTrue(interrupted.get());
        }
    }

    @Test
    void borrowedClientMustNotFollowRedirects() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmithyClientAdapter(new Client(new Context(), Protocol.HTTP)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmithyClientAdapter(restlet).setRequestTimeout(Duration.ZERO));
    }

    /** Closing the SDK client closes the transport, which stops only a client it owns. */
    @Test
    void closingStopsOnlyAnOwnedClient() {
        var borrowing = new SmithyClientAdapter(restlet);
        try (var client = json(borrowing).build()) {
            client.call("Echo", echoInput("t3"));
        }
        assertFalse(borrowing.isClientOwned());
        assertTrue(restlet.isStarted());
        var owning = new SmithyClientAdapter();
        try (var client = json(owning).build()) {
            client.call("Echo", echoInput("t4"));
        }
        assertTrue(owning.isClientOwned());
        assertTrue(owning.getClient().isStopped());
    }

    /** Without an explicit transport, SDK clients discover this one first, with its own client. */
    @Test
    void sdkClientsDiscoverTheTransport() {
        var names =
                ClientTransportFactory.load(getClass().getClassLoader()).stream()
                        .map(ClientTransportFactory::name)
                        .toList();
        assertEquals(List.of("restlet", "http-java"), names);
        try (var client =
                DynamicClient.builder()
                        .model(model)
                        .serviceId(JSON_SERVICE)
                        .protocol(new RestJsonClientProtocol(JSON_SERVICE))
                        .endpoint(endpoint())
                        .build()) {
            assertEquals("t5", client.call("Echo", echoInput("t5")).getMember("tag").asString());
        }
        assertNotEquals("Java-http-client", captured.getFirst().header("user-agent").split("/")[0]);
    }

    /** The factory reads the same {@code httpConfig} settings as the SDK's JDK transport. */
    @Test
    void factoryReadsTheHttpConfigSettings() throws Exception {
        try (var transport =
                (SmithyClientAdapter)
                        new RestletTransportFactory()
                                .createTransport(
                                        Document.of(
                                                Map.of(
                                                        "requestTimeoutMs",
                                                        Document.of(1500),
                                                        "connectTimeoutMs",
                                                        Document.of(2500))),
                                        Document.of(
                                                Map.of(
                                                        "httpConfig",
                                                        Document.of(
                                                                Map.of(
                                                                        "httpVersion",
                                                                        Document.of("h2"),
                                                                        "requestTimeoutMs",
                                                                        Document.of(99))))))) {
            assertTrue(transport.isClientOwned());
            assertEquals(Duration.ofMillis(1500), transport.getRequestTimeout());
            var parameters = transport.getClient().getContext().getParameters();
            assertEquals("HTTP2", parameters.getFirstValue("httpClientTransportMode"));
            assertEquals("2500", parameters.getFirstValue("connectTimeout"));
            assertEquals("false", parameters.getFirstValue("followRedirects"));
        }
    }

    private static Context http2Context() {
        Context context = new Context();
        context.getParameters().set("httpClientTransportMode", "HTTP2");
        return context;
    }

    private static Server http2Server(Restlet handler) throws Exception {
        Context context = new Context();
        context.getParameters().set("http.transport.protocol", "HTTP2");
        Server server = new Server(context, Protocol.HTTP, 0, handler);
        server.start();
        return server;
    }

    /** Records each request handed to the transport, and its body, while the call is active. */
    private static final class Recording implements ClientTransport<HttpRequest, HttpResponse> {
        private final SmithyClientAdapter delegate;
        private final List<Recording.Sent> sent = new ArrayList<>();

        private record Sent(HttpRequest request, byte[] body) {}

        private Recording(SmithyClientAdapter delegate) {
            this.delegate = delegate;
        }

        @Override
        public HttpResponse send(
                software.amazon.smithy.java.context.Context context, HttpRequest request) {
            byte[] body = new byte[0];
            if (request.body() != null && request.body().hasByteBuffer()) {
                var buffer = request.body().asByteBuffer().duplicate();
                body = new byte[buffer.remaining()];
                buffer.get(body);
            }
            synchronized (sent) {
                sent.add(new Sent(request, body));
            }
            return delegate.send(context, request);
        }

        @Override
        public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
            return delegate.messageExchange();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
