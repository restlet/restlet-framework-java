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
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    static McpSchema.Tool echoTool() {
        return McpSchema.Tool.builder(
                        "echo",
                        Map.of(
                                "type",
                                "object",
                                "properties",
                                Map.of("text", Map.of("type", "string"))))
                .description("Echoes text")
                .build();
    }

    static void addEcho(McpServerAdapter adapter) {
        adapter.getTools()
                .add(
                        new McpStatelessServerFeatures.AsyncToolSpecification(
                                echoTool(),
                                (context, call) ->
                                        Mono.just(
                                                McpSchema.CallToolResult.builder()
                                                        .addTextContent(
                                                                "echo:"
                                                                        + call.arguments()
                                                                                .get("text"))
                                                        .isError(false)
                                                        .build())));
    }

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
                        (context, values) -> {
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
        McpRequestContext context = new McpRequestContext(null);
        var listed =
                server.dispatch(
                                new McpSchema.JSONRPCRequest("2.0", "tools/list", 1, Map.of()),
                                context)
                        .orElseThrow();
        assertNull(listed.error());
        assertTrue(filtered.get());
        assertEquals(true, context.attributes().get("intercepted"));
        var result =
                server.dispatch(
                                new McpSchema.JSONRPCRequest(
                                        "2.0",
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
                                                new McpSchema.JSONRPCRequest(
                                                        "2.0", "tools/list", 3, Map.of()),
                                                context)
                                        .orElseThrow())
                        .contains("echo"));
    }

    @Test
    void sdkOwnsMethodErrorsAndNotificationResponses() throws Exception {
        server.start();
        Response response = send("{\"jsonrpc\":\"2.0\",\"id\":\"x\",\"method\":\"unknown\"}");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        var decoded =
                McpSchema.deserializeJsonRpcMessage(
                        McpJsonDefaults.getMapper(), response.getEntity().getText());
        assertTrue(
                decoded instanceof McpSchema.JSONRPCResponse rpc
                        && rpc.error().code() == -32601
                        && "x".equals(rpc.id()));
        response = send("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(Status.SUCCESS_ACCEPTED, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    void nativeResourcesTemplatesPromptsAndCapabilities() throws Exception {
        server.getResources()
                .add(
                        new McpStatelessServerFeatures.AsyncResourceSpecification(
                                McpSchema.Resource.builder("test://fixed", "fixed")
                                        .mimeType("text/plain")
                                        .build(),
                                (context, request) ->
                                        Mono.just(
                                                new McpSchema.ReadResourceResult(
                                                        List.of(
                                                                new McpSchema.TextResourceContents(
                                                                        request.uri(),
                                                                        "text/plain",
                                                                        "resource",
                                                                        null)),
                                                        null))));
        server.getResourceTemplates()
                .add(
                        new McpStatelessServerFeatures.AsyncResourceTemplateSpecification(
                                McpSchema.ResourceTemplate.builder("test://items/{id}", "items")
                                        .build(),
                                (context, request) ->
                                        Mono.just(
                                                new McpSchema.ReadResourceResult(
                                                        List.of(
                                                                new McpSchema.TextResourceContents(
                                                                        request.uri(),
                                                                        "text/plain",
                                                                        "template",
                                                                        null)),
                                                        null))));
        server.getPrompts()
                .add(
                        new McpStatelessServerFeatures.AsyncPromptSpecification(
                                McpSchema.Prompt.builder("greet").build(),
                                (context, request) ->
                                        Mono.just(
                                                new McpSchema.GetPromptResult(
                                                        "greeting",
                                                        List.of(
                                                                new McpSchema.PromptMessage(
                                                                        McpSchema.Role.USER,
                                                                        McpSchema.TextContent
                                                                                .builder("hello")
                                                                                .build())),
                                                        null))));
        server.start();
        McpRequestContext context = new McpRequestContext(null);
        for (String method :
                List.of("resources/list", "resources/templates/list", "prompts/list")) {
            assertNull(
                    server.dispatch(
                                    new McpSchema.JSONRPCRequest("2.0", method, 1, Map.of()),
                                    context)
                            .orElseThrow()
                            .error());
        }
        for (String uri : List.of("test://fixed", "test://items/1")) {
            var response =
                    server.dispatch(
                                    new McpSchema.JSONRPCRequest(
                                            "2.0", "resources/read", 2, Map.of("uri", uri)),
                                    context)
                            .orElseThrow();
            assertNull(response.error());
            assertTrue(McpJsonDefaults.getMapper().writeValueAsString(response).contains(uri));
        }
        var prompt =
                server.dispatch(
                                new McpSchema.JSONRPCRequest(
                                        "2.0",
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
                                        new McpSchema.JSONRPCResponse(
                                                "2.0", 1, Map.of("text", "x".repeat(256)), null)));
        server.start();
        Response response = send("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
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
        var first =
                CompletableFuture.supplyAsync(
                        () -> send("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}"));
        assertTrue(entered.await(3, TimeUnit.SECONDS));
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                send("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}").getStatus());
        server.stop();
        assertTrue(exited.await(3, TimeUnit.SECONDS));
        assertEquals(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                first.get(3, TimeUnit.SECONDS).getStatus());
        assertFalse(server.isStarted());
        assertThrows(IllegalStateException.class, server::start);
    }

    @Test
    void abnormalRequestDisconnectCancelsOwnedCallback() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        server.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            entered.countDown();
                            return Mono.<McpSchema.JSONRPCResponse>never()
                                    .doOnCancel(exited::countDown);
                        });
        server.start();
        Request request = request("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
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
                                new McpSchema.JSONRPCRequest("2.0", "ping", 1, Map.of()),
                                new McpRequestContext(null)));
    }
}
