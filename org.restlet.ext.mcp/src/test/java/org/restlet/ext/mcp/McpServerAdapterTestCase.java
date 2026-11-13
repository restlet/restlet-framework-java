/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;

class McpServerAdapterTestCase {
    private final McpServerAdapter server = new McpServerAdapter();

    static void addEcho(McpServerAdapter adapter) {
        adapter.getTools().add(McpTestFeatures.echo());
    }

    /** A legacy call through the adapter's core, as interceptors see it. */
    private static final String LIST = "{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"tools/list\"}";

    @AfterEach
    void cleanup() throws Exception {
        server.stop();
    }

    static Request request(String body) {
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost/mcp",
                        new StringRepresentation(body, MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.VERSION);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        return request;
    }

    private Response send(String body) {
        return server.handle(request(body));
    }

    static Map<String, Object> envelope(Response response) throws Exception {
        return McpJsonDefaults.getMapper()
                .readValue(response.getEntity().getText(), new TypeRef<Map<String, Object>>() {});
    }

    @Test
    void nativeDispatchToolsFiltersAndContext() throws Exception {
        addEcho(server);
        AtomicBoolean filtered = new AtomicBoolean();
        server.getToolFilters()
                .add(
                        (context, tool) -> {
                            filtered.set(true);
                            return Mono.just(Boolean.TRUE);
                        });
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            context.attributes().put("intercepted", true);
                            return chain.proceed(message, context);
                        });
        server.start();
        McpRequestContext context = new McpRequestContext(null, McpProtocol.MODERN_VERSION);
        var listed =
                server.dispatch(McpTestFeatures.request("tools/list", 1, Map.of()), context)
                        .orElseThrow();
        assertNull(listed.error());
        assertTrue(filtered.get());
        assertEquals(true, context.attributes().get("intercepted"));
        var result =
                server.dispatch(
                                McpTestFeatures.request(
                                        "tools/call",
                                        2,
                                        Map.of(
                                                "name",
                                                "echo",
                                                "arguments",
                                                Map.of("text", "native"))),
                                context)
                        .orElseThrow();
        assertNull(result.error());
        assertTrue(McpJsonDefaults.getMapper().writeValueAsString(result).contains("echo:native"));
        assertThrows(IllegalStateException.class, () -> server.setMaxMessageBytes(9));
        server.getTools().clear();
        assertTrue(
                McpJsonDefaults.getMapper()
                        .writeValueAsString(
                                server.dispatch(
                                                McpTestFeatures.request("tools/list", 3, Map.of()),
                                                context)
                                        .orElseThrow())
                        .contains("echo"));
    }

    /**
     * A context made without a revision is a modern one: interceptors and tool filters of a
     * dispatched request see the revision it is served in.
     */
    @Test
    void defaultContextReportsTheModernRevision() throws Exception {
        addEcho(server);
        List<String> seen = new CopyOnWriteArrayList<>();
        server.getToolFilters()
                .add(
                        (context, tool) -> {
                            seen.add("filter:" + context.protocolVersion());
                            return Mono.just(Boolean.TRUE);
                        });
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            seen.add("interceptor:" + context.protocolVersion());
                            return chain.proceed(message, context);
                        });
        server.start();

        assertNull(
                server.dispatch(
                                McpTestFeatures.request("tools/list", 1, Map.of()),
                                new McpRequestContext(null))
                        .orElseThrow()
                        .error());

        assertEquals(
                List.of(
                        "interceptor:" + McpProtocol.MODERN_VERSION,
                        "filter:" + McpProtocol.MODERN_VERSION),
                seen);
    }

    @Test
    void sdkOwnsMethodErrorsAndNotificationResponses() throws Exception {
        server.start();
        Response response = send("{\"jsonrpc\":\"2.0\",\"id\":\"x\",\"method\":\"unknown\"}");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Map<String, Object> decoded = envelope(response);
        assertEquals("x", decoded.get("id"));
        assertEquals(-32601, ((Map<?, ?>) decoded.get("error")).get("code"));
        response =
                send(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"logging/setLevel\",\"params\":{\"level\":\"info\"}}");
        assertEquals(-32601, ((Map<?, ?>) envelope(response).get("error")).get("code"));
        response = send("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}");
        assertEquals(Map.of(), envelope(response).get("result"));
        for (String notification :
                List.of("notifications/initialized", "notifications/roots/list_changed")) {
            response = send("{\"jsonrpc\":\"2.0\",\"method\":\"" + notification + "\"}");
            assertEquals(Status.SUCCESS_ACCEPTED, response.getStatus());
            assertNull(response.getEntity());
        }
    }

    @Test
    void nativeResourcesTemplatesPromptsAndCapabilities() throws Exception {
        server.getResources().add(McpTestFeatures.resource("test://fixed", "resource"));
        server.getResourceTemplates()
                .add(McpTestFeatures.template("test://items/{id}", "template"));
        server.getPrompts().add(McpTestFeatures.prompt("greet", "greeting", "hello"));
        server.start();
        McpRequestContext context = new McpRequestContext(null, McpProtocol.MODERN_VERSION);
        for (String method :
                List.of("resources/list", "resources/templates/list", "prompts/list")) {
            assertNull(
                    server.dispatch(McpTestFeatures.request(method, 1, Map.of()), context)
                            .orElseThrow()
                            .error());
        }
        for (String uri : List.of("test://fixed", "test://items/1")) {
            var response =
                    server.dispatch(
                                    McpTestFeatures.request(
                                            "resources/read", 2, Map.of("uri", uri)),
                                    context)
                            .orElseThrow();
            assertNull(response.error());
            assertTrue(McpJsonDefaults.getMapper().writeValueAsString(response).contains(uri));
        }
        var prompt =
                server.dispatch(
                                McpTestFeatures.request(
                                        "prompts/get",
                                        3,
                                        Map.of("name", "greet", "arguments", Map.of())),
                                context)
                        .orElseThrow();
        assertNull(prompt.error());
        assertTrue(McpJsonDefaults.getMapper().writeValueAsString(prompt).contains("hello"));
    }

    @Test
    void distinguishesParseAndInvalidEnvelopeErrorsWithNullId() throws Exception {
        server.start();
        for (String body :
                List.of(
                        "[]",
                        "null",
                        "{}",
                        "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":null}",
                        "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1,\"params\":[]}")) {
            Response response = send(body);
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
            Map<String, Object> result = envelope(response);
            assertNull(result.get("id"));
            assertEquals(-32600, ((Map<?, ?>) result.get("error")).get("code"));
        }
        Map<String, Object> malformed = envelope(send("{broken"));
        assertEquals(-32700, ((Map<?, ?>) malformed.get("error")).get("code"));
    }

    @Test
    void boundsEncodedResponses() throws Exception {
        server.setMaxMessageBytes(256);
        server.getInterceptors()
                .add(
                        (message, context, chain) ->
                                Mono.just(
                                        JsonRpc.JSONRPCResponse.result(
                                                1, Map.of("text", "x".repeat(256)))));
        server.start();
        Response response = send(LIST.formatted(1));
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    void exactMessageLimitIncludingUnknownLength() throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}";
        int bytes = body.getBytes(StandardCharsets.UTF_8).length;
        server.setMaxMessageBytes(bytes);
        server.start();
        assertEquals(Status.SUCCESS_OK, send(body).getStatus());
        assertEquals(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, send(body + " ").getStatus());
        Request streamed = request(body);
        streamed.setEntity(
                new InputRepresentation(
                        new ByteArrayInputStream((body + " ").getBytes(StandardCharsets.UTF_8)),
                        MediaType.APPLICATION_JSON));
        assertEquals(
                Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, server.handle(streamed).getStatus());
        streamed.setEntity(
                new InputRepresentation(
                        new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                        MediaType.APPLICATION_JSON));
        assertEquals(Status.SUCCESS_OK, server.handle(streamed).getStatus());
    }

    @Test
    void rejectsMalformedUtf8() throws Exception {
        server.start();
        Request streamed = request("");
        streamed.setEntity(
                new ByteArrayRepresentation(
                        new byte[] {(byte) 0xc3, (byte) 0x28}, MediaType.APPLICATION_JSON));
        assertEquals(
                -32700, ((Map<?, ?>) envelope(server.handle(streamed)).get("error")).get("code"));
    }

    @Test
    void rejectsUnsupportedVersionsSessionsMethodsMediaAndOrigins() throws Exception {
        server.getAllowedOrigins().add("https://trusted.test");
        server.start();
        Request request = request("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
        request.getHeaders().set(McpProtocol.VERSION_HEADER, "2026-07-28");
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, server.handle(request).getStatus());
        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.VERSION);
        request.getHeaders().set("Mcp-Session-Id", "not-supported");
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, server.handle(request).getStatus());
        request.getHeaders().removeAll("Mcp-Session-Id", true);
        request.getHeaders().set("Origin", "https://evil.test");
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, server.handle(request).getStatus());
        request.getHeaders().set("Origin", "https://trusted.test");
        assertEquals(Status.SUCCESS_OK, server.handle(request).getStatus());
        request.getHeaders().add("Origin", "https://trusted.test");
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, server.handle(request).getStatus());
        request.getHeaders().removeAll("Origin", true);
        request.getHeaders().set("Accept", "application/json");
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, server.handle(request).getStatus());
        request.getHeaders().set("Accept", "application/json, text/event-stream;q=0");
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, server.handle(request).getStatus());
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        request.setEntity("{}", MediaType.TEXT_PLAIN);
        assertEquals(
                Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, server.handle(request).getStatus());
        request.setMethod(Method.GET);
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, server.handle(request).getStatus());
    }

    /**
     * A legacy initialize is answered from server/discover: tools, resources and prompts only,
     * without change notifications, subscriptions or a session.
     */
    @Test
    void legacyInitializeAdvertisesOnlyWhatBothErasShare() throws Exception {
        addEcho(server);
        server.getResources().add(McpTestFeatures.resource("test://fixed", "resource"));
        server.getPrompts().add(McpTestFeatures.prompt("greet", "greeting", "hello"));
        server.setInstructions("Be brief");
        server.start();
        Response response =
                send(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                                + "\"params\":{\"protocolVersion\":\"2025-11-25\",\"capabilities\":"
                                + "{\"roots\":{},\"sampling\":{}},"
                                + "\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}");
        assertNull(response.getHeaders().getFirstValue(McpProtocol.SESSION_HEADER, true));
        Map<?, ?> result = (Map<?, ?>) envelope(response).get("result");
        assertEquals(McpProtocol.VERSION, result.get("protocolVersion"));
        assertEquals("Be brief", result.get("instructions"));
        assertEquals("restlet", ((Map<?, ?>) result.get("serverInfo")).get("name"));
        Map<?, ?> capabilities = (Map<?, ?>) result.get("capabilities");
        assertEquals(Set.of("tools", "resources", "prompts"), capabilities.keySet());
        assertEquals(Map.of("listChanged", false), capabilities.get("tools"));
        assertEquals(
                Map.of("subscribe", false, "listChanged", false), capabilities.get("resources"));
        assertEquals(
                -32601,
                ((Map<?, ?>)
                                envelope(
                                                send(
                                                        "{\"jsonrpc\":\"2.0\",\"id\":2,"
                                                                + "\"method\":\"resources/subscribe\","
                                                                + "\"params\":{\"uri\":\"test://fixed\"}}"))
                                        .get("error"))
                        .get("code"));
    }

    /**
     * The same registrations serve both eras: interceptors and tool filters see each request once,
     * on the modern types, with the revision it came in.
     */
    @Test
    void policyRunsOnceForBothEras() throws Exception {
        addEcho(server);
        server.getTools()
                .add(
                        io.modelcontextprotocol.modern.server.feature.McpAsyncTool.of(
                                io.modelcontextprotocol.modern.McpSchema.Tool.builder(
                                                "hidden", Map.of("type", "object"))
                                        .build(),
                                (ctx, request) -> Mono.empty()));
        List<String> seen = new CopyOnWriteArrayList<>();
        server.getToolFilters()
                .add(
                        (context, tool) -> {
                            seen.add("filter:" + context.protocolVersion());
                            return Mono.just(!"hidden".equals(tool.name()));
                        });
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            assertTrue(message instanceof JsonRpc.JSONRPCRequest);
                            seen.add(
                                    ((JsonRpc.JSONRPCRequest) message).method()
                                            + ":"
                                            + context.protocolVersion());
                            return chain.proceed(message, context);
                        });
        server.start();

        Map<?, ?> legacy = (Map<?, ?>) envelope(send(LIST.formatted(1))).get("result");
        var modern =
                server.dispatch(
                                McpTestFeatures.request("tools/list", 2, Map.of()),
                                new McpRequestContext(null, McpProtocol.MODERN_VERSION))
                        .orElseThrow();

        assertEquals(1, ((List<?>) legacy.get("tools")).size());
        assertFalse(legacy.containsKey("resultType"));
        assertTrue(McpJsonDefaults.getMapper().writeValueAsString(modern).contains("\"echo\""));
        assertFalse(McpJsonDefaults.getMapper().writeValueAsString(modern).contains("hidden"));
        String classic = McpProtocol.VERSION;
        String current = McpProtocol.MODERN_VERSION;
        assertEquals(
                List.of(
                        "tools/list:" + classic,
                        "filter:" + classic,
                        "filter:" + classic,
                        "tools/list:" + current,
                        "filter:" + current,
                        "filter:" + current),
                seen);
    }

    /**
     * A legacy request that fails past the edge, in an interceptor or at the deadline, is answered
     * with its id, which a stdio client needs to match the error to its call.
     */
    @Test
    void legacyFailuresPastTheEdgeKeepTheRequestId() throws Exception {
        addEcho(server);
        server.setRequestTimeout(Duration.ofMillis(300));
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            if ("tools/list".equals(((JsonRpc.JSONRPCRequest) message).method())) {
                                throw new IllegalStateException("interceptor failed");
                            }
                            return Mono.never();
                        });
        server.start();

        Response failed = send(LIST.formatted(7));
        Response late =
                send(
                        "{\"jsonrpc\":\"2.0\",\"id\":\"late\",\"method\":\"tools/call\","
                                + "\"params\":{\"name\":\"echo\",\"arguments\":{\"text\":\"x\"}}}");

        for (var answered : List.of(Map.entry(failed, (Object) 7), Map.entry(late, "late"))) {
            assertEquals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, answered.getKey().getStatus());
            Map<String, Object> error = envelope(answered.getKey());
            assertEquals(answered.getValue(), error.get("id"));
            assertEquals(-32603, ((Map<?, ?>) error.get("error")).get("code"));
        }
    }

    @Test
    void initializeRequiresSupportedBodyVersionAndPostInitializeHeader() throws Exception {
        server.start();
        Request initialize =
                request(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                                + "\"params\":{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},"
                                + "\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}");
        initialize.getHeaders().removeAll(McpProtocol.VERSION_HEADER, true);
        Response response = server.handle(initialize);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertNull(response.getHeaders().getFirstValue("Mcp-Session-Id", true));
        assertTrue(response.getEntity().getText().contains("2025-11-25"));
        assertEquals(
                Status.CLIENT_ERROR_BAD_REQUEST,
                send(initialize.getEntity().getText().replace("2025-11-25", "2026-07-28"))
                        .getStatus());
        Request ping = request("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}");
        ping.getHeaders().removeAll(McpProtocol.VERSION_HEADER, true);
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, server.handle(ping).getStatus());
    }

    @Test
    void concurrentAdmissionIncludesBlockingCallbackAndStopInterruptsIt() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        server.setMaxConcurrentCalls(1);
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            entered.countDown();
                            try {
                                new CountDownLatch(1).await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                exited.countDown();
                            }
                            return Mono.error(new IllegalStateException("interrupted"));
                        });
        server.start();
        var first = CompletableFuture.supplyAsync(() -> send(LIST.formatted(1)));
        assertTrue(entered.await(3, TimeUnit.SECONDS));
        assertEquals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, send(LIST.formatted(2)).getStatus());
        server.stop();
        assertTrue(exited.await(3, TimeUnit.SECONDS));
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                first.get(3, TimeUnit.SECONDS).getStatus());
        assertFalse(server.isStarted());
        assertThrows(IllegalStateException.class, server::start);
    }

    @Test
    void trickledBodyHoldsACallSlotAndIsCancelledAtTheDeadline() throws Exception {
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        InputStream trickle =
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        reading.countDown();
                        try {
                            new CountDownLatch(1).await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        throw new InterruptedIOException("cancelled");
                    }

                    @Override
                    public void close() {
                        closed.countDown();
                    }
                };
        server.setMaxConcurrentCalls(1);
        server.setRequestTimeout(Duration.ofMillis(500));
        server.start();
        Request slow = request("");
        slow.setEntity(new InputRepresentation(trickle, MediaType.APPLICATION_JSON));
        var first = CompletableFuture.supplyAsync(() -> server.handle(slow));
        assertTrue(reading.await(3, TimeUnit.SECONDS));
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                send("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}").getStatus());
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                first.get(3, TimeUnit.SECONDS).getStatus());
        assertTrue(closed.await(3, TimeUnit.SECONDS));
        assertEquals(
                Status.SUCCESS_OK,
                send("{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"ping\"}").getStatus());
    }

    @Test
    void abnormalRequestDisconnectCancelsOwnedCallback() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            entered.countDown();
                            return Mono.<JsonRpc.JSONRPCResponse>never()
                                    .doOnCancel(exited::countDown);
                        });
        server.start();
        Request request = request(LIST.formatted(1));
        var result = CompletableFuture.supplyAsync(() -> server.handle(request));
        assertTrue(entered.await(3, TimeUnit.SECONDS));
        request.abort();
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                result.get(3, TimeUnit.SECONDS).getStatus());
        assertTrue(exited.await(3, TimeUnit.SECONDS));
    }

    @Test
    void rejectsInvalidLimitsAndAbsentLifecycle() {
        assertThrows(IllegalArgumentException.class, () -> server.setMaxConcurrentCalls(0));
        assertThrows(
                IllegalArgumentException.class, () -> server.setMaxMessageBytes(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> server.setRequestTimeout(Duration.ZERO));
        assertNotNull(server.getServerInfo());
        assertThrows(
                IllegalStateException.class,
                () ->
                        server.dispatch(
                                McpTestFeatures.request("server/discover", 1, Map.of()),
                                new McpRequestContext(null)));
    }
}
