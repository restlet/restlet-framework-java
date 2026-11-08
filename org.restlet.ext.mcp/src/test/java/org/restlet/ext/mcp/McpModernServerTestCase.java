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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.ServerSentEvent;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;

/**
 * MCP {@code 2026-07-28} served by {@link McpServerAdapter} next to {@code 2025-11-25}: bridged
 * registrations, modern tools, interceptors, filters, limits and protocol errors.
 */
class McpModernServerTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** The 2025-11-25 registrations are served to 2026-07-28 clients, converted. */
    @Test
    void bridgedRegistrationsAnswerModernClients() throws Exception {
        McpServerAdapter adapter = adapter();
        addResourcesAndPrompts(adapter);
        adapter.start();

        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        assertEquals(List.of(MODERN), discover.get("supportedVersions"));
        Map<?, ?> capabilities = (Map<?, ?>) discover.get("capabilities");
        for (String kind : List.of("tools", "resources", "prompts")) {
            assertTrue(capabilities.containsKey(kind), kind + " in " + capabilities);
        }

        List<?> tools = (List<?>) result(call(adapter, "tools/list", Map.of(), null)).get("tools");
        assertEquals(
                List.of("echo", "progress", "approve"),
                tools.stream().map(tool -> ((Map<?, ?>) tool).get("name")).toList());
        assertEquals(
                "echo:hi",
                firstText(
                        result(
                                call(
                                        adapter,
                                        "tools/call",
                                        Map.of("name", "echo", "arguments", Map.of("text", "hi")),
                                        "echo"))));
        for (String uri : List.of("test://fixed", "test://items/7")) {
            Map<String, Object> read =
                    result(call(adapter, "resources/read", Map.of("uri", uri), uri));
            assertEquals(uri, ((Map<?, ?>) ((List<?>) read.get("contents")).get(0)).get("uri"));
        }
        Map<String, Object> prompt =
                result(call(adapter, "prompts/get", Map.of("name", "greet"), "greet"));
        assertEquals("greeting", prompt.get("description"));
    }

    /** A modern tool streams its progress over SSE, then the result answering the request. */
    @Test
    void aModernToolStreamsItsProgressBeforeItsResult() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "progress", "arguments", Map.of()),
                                "progress",
                                meta));

        assertEquals(200, response.getStatus().getCode());
        assertEquals(MediaType.TEXT_EVENT_STREAM, response.getEntity().getMediaType());
        List<Map<String, Object>> events = events(body(response));
        assertEquals(2, events.size(), events.toString());
        assertEquals("notifications/progress", events.get(0).get("method"));
        assertEquals("half", ((Map<?, ?>) events.get(0).get("params")).get("message"));
        assertEquals(1, ((Number) events.get(1).get("id")).intValue());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) events.get(1).get("result");
        assertEquals("done", firstText(result));
    }

    /**
     * An input_required result seals its state with the request-state key: the retry reaches
     * another instance with the same key, but not one with another key.
     */
    @Test
    void anInputRequiredRetryReachesAnInstanceSharingTheKey() throws Exception {
        byte[] key = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
        McpServerAdapter first = adapter();
        first.setRequestStateKey(key);
        first.start();
        McpServerAdapter second = adapter();
        second.setRequestStateKey(key);
        second.start();
        McpServerAdapter other = adapter();
        other.setRequestStateKey(
                "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.US_ASCII));
        other.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));

        Map<String, Object> asked =
                result(
                        first.handle(
                                modern(
                                        "tools/call",
                                        Map.of("name", "approve", "arguments", Map.of()),
                                        "approve",
                                        meta)));
        assertEquals("input_required", asked.get("resultType"), asked.toString());
        String state = (String) asked.get("requestState");
        assertNotNull(state);
        assertFalse(state.contains("s1"), "the state must be sealed, not sent in clear");

        Map<String, Object> retry = new LinkedHashMap<>();
        retry.put("name", "approve");
        retry.put("arguments", Map.of());
        retry.put("requestState", state);
        retry.put(
                "inputResponses",
                Map.of("confirm", Map.of("action", "accept", "content", Map.of())));
        Map<String, Object> answered =
                result(second.handle(modern("tools/call", retry, "approve", meta)));
        assertEquals("approved:s1", firstText(answered));

        Response refused = other.handle(modern("tools/call", retry, "approve", meta));
        assertNotNull(envelope(refused).get("error"), body(refused));
    }

    /** Interceptors see 2026-07-28 requests, may refuse them, and see their final result. */
    @Test
    void interceptorsWrapModernRequests() throws Exception {
        McpServerAdapter adapter = adapter();
        List<String> seen = new CopyOnWriteArrayList<>();
        adapter.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            seen.add(context.protocolVersion());
                            if (message instanceof McpSchema.JSONRPCRequest request
                                    && request.params() instanceof Map<?, ?> params
                                    && "approve".equals(params.get("name"))) {
                                return Mono.just(
                                        new McpSchema.JSONRPCResponse(
                                                "2.0",
                                                request.id(),
                                                null,
                                                new McpSchema.JSONRPCResponse.JSONRPCError(
                                                        -32001, "Not allowed", null)));
                            }
                            return chain.proceed(message, context)
                                    .doOnNext(response -> seen.add("final:" + response.id()));
                        });
        adapter.start();

        Response refused =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "approve", "arguments", Map.of()),
                                "approve",
                                meta()));
        assertEquals(
                -32001,
                ((Number) ((Map<?, ?>) envelope(refused).get("error")).get("code")).intValue());
        assertEquals(
                "echo:x",
                firstText(
                        result(
                                call(
                                        adapter,
                                        "tools/call",
                                        Map.of("name", "echo", "arguments", Map.of("text", "x")),
                                        "echo"))));
        assertEquals(List.of(MODERN, MODERN, "final:1"), seen);
    }

    /** The adapter's tool filters apply to 2026-07-28 listings, modern tools included. */
    @Test
    void toolFiltersApplyToModernListings() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getToolFilters().add((context, tool) -> Mono.just(!tool.name().equals("approve")));
        adapter.start();

        List<?> tools = (List<?>) result(call(adapter, "tools/list", Map.of(), null)).get("tools");

        assertEquals(
                List.of("echo", "progress"),
                tools.stream().map(tool -> ((Map<?, ?>) tool).get("name")).toList());
    }

    /** The same adapter still serves 2025-11-25 clients. */
    @Test
    void legacyClientsAreStillServed() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();

        Response response =
                adapter.handle(
                        McpServerAdapterTestCase.request(
                                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":"
                                        + "{\"name\":\"echo\",\"arguments\":{\"text\":\"old\"}}}"));

        Map<String, Object> envelope = McpServerAdapterTestCase.envelope(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) envelope.get("result");
        assertEquals("echo:old", firstText(result));
        assertFalse(result.containsKey("resultType"), "2025-11-25 results have no resultType");
    }

    /** The call limit counts 2026-07-28 requests: a call over it is unavailable. */
    @Test
    void theCallLimitAppliesToModernRequests() throws Exception {
        McpServerAdapter adapter = adapter();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("hold", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.fromCallable(
                                                () -> {
                                                    entered.countDown();
                                                    release.await(10, TimeUnit.SECONDS);
                                                    return McpAsyncResponse.<CallToolOutcome>result(
                                                            text("held"));
                                                })));
        adapter.setMaxConcurrentCalls(1);
        adapter.start();
        Map<String, Object> hold = Map.of("name", "hold", "arguments", Map.of());

        CompletableFuture<Response> held =
                CompletableFuture.supplyAsync(
                        () -> adapter.handle(modern("tools/call", hold, "hold", meta())));
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        Response refused = adapter.handle(modern("tools/list", Map.of(), null, meta()));
        release.countDown();

        assertEquals(503, refused.getStatus().getCode(), body(refused));
        assertEquals("held", firstText(result(held.get(10, TimeUnit.SECONDS))));
    }

    /** Protocol errors use the 2026-07-28 codes and HTTP statuses; a notification gets 202. */
    @Test
    void protocolErrorsAndNotifications() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setMaxMessageBytes(4096);
        adapter.start();

        Request notification =
                post(
                        "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/cancelled\",\"params\":"
                                + "{\"requestId\":1,\"_meta\":{\""
                                + MetaKeys.PROTOCOL_VERSION
                                + "\":\""
                                + MODERN
                                + "\"}}}",
                        "notifications/cancelled",
                        null);
        Response accepted = adapter.handle(notification);
        assertEquals(202, accepted.getStatus().getCode());
        assertTrue(accepted.getEntity() == null || accepted.getEntity().getSize() <= 0);

        Response mismatch =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "echo", "arguments", Map.of()),
                                "other",
                                meta()));
        assertEquals(400, mismatch.getStatus().getCode());
        assertEquals(-32020, code(mismatch));

        Response unknown = adapter.handle(modern("nothing/here", Map.of(), null, meta()));
        assertEquals(404, unknown.getStatus().getCode());
        assertEquals(-32601, code(unknown));

        Response oversized =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of(
                                        "name",
                                        "echo",
                                        "arguments",
                                        Map.of("text", "x".repeat(8192))),
                                "echo",
                                meta()));
        assertEquals(413, oversized.getStatus().getCode());
    }

    /** A tool's x-mcp-header arguments are checked against their Mcp-Param headers. */
    @Test
    void paramHeadersAreValidatedForAnnotatedTools() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder(
                                                "region",
                                                Map.of(
                                                        "type",
                                                        "object",
                                                        "properties",
                                                        Map.of(
                                                                "region",
                                                                Map.of(
                                                                        "type",
                                                                        "string",
                                                                        "x-mcp-header",
                                                                        "Region"))))
                                        .build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>result(
                                                        text("ok")))));
        adapter.start();
        Map<String, Object> params = Map.of("name", "region", "arguments", Map.of("region", "eu"));

        Request matching = modern("tools/call", params, "region", meta());
        matching.getHeaders().set("Mcp-Param-Region", "eu");
        assertEquals("ok", firstText(result(adapter.handle(matching))));

        Request differing = modern("tools/call", params, "region", meta());
        differing.getHeaders().set("Mcp-Param-Region", "us");
        assertHeaderMismatch(adapter.handle(differing));
        assertHeaderMismatch(adapter.handle(modern("tools/call", params, "region", meta())));
    }

    /** Checks a 400 header-mismatch error, reading the body once. */
    private static void assertHeaderMismatch(Response response) throws Exception {
        String body = body(response);
        assertEquals(400, response.getStatus().getCode(), body);
        Map<String, Object> envelope =
                MAPPER.readValue(body, new TypeRef<Map<String, Object>>() {});
        assertEquals(-32020, ((Number) ((Map<?, ?>) envelope.get("error")).get("code")).intValue());
    }

    /** A client gone mid-stream cancels the tool's stream and frees the call slot. */
    @Test
    void aClientGoneMidStreamCancelsTheTool() throws Exception {
        McpServerAdapter adapter = adapter();
        CountDownLatch cancelled = new CountDownLatch(1);
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("endless", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(
                                                                                1, null, "started")
                                                                        .then(
                                                                                Mono
                                                                                        .<CallToolOutcome>
                                                                                                never()
                                                                                        .doOnCancel(
                                                                                                cancelled
                                                                                                        ::countDown))))));
        adapter.setMaxConcurrentCalls(1);
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "endless", "arguments", Map.of()),
                                "endless",
                                meta));
        java.io.OutputStream gone =
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        throw new java.io.IOException("client gone");
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws java.io.IOException {
                        throw new java.io.IOException("client gone");
                    }
                };
        try {
            response.getEntity().write(gone);
        } catch (java.io.IOException _) {
            // The writer reports the broken client.
        }

        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the tool's stream was not cancelled");
        assertEquals(
                "echo:freed",
                firstText(
                        result(
                                callWhenFree(
                                        adapter,
                                        "tools/call",
                                        Map.of(
                                                "name",
                                                "echo",
                                                "arguments",
                                                Map.of("text", "freed")),
                                        "echo"))));
    }

    /** A stream cut short by the timeout ends with an error answering the request. */
    @Test
    void aTimeoutDuringAStreamEndsItWithAnError() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("stuck", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(
                                                                                1, null, "started")
                                                                        .then(
                                                                                Mono
                                                                                        .<CallToolOutcome>
                                                                                                never())))));
        adapter.setRequestTimeout(java.time.Duration.ofMillis(500));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "stuck", "arguments", Map.of()),
                                "stuck",
                                meta));
        List<Map<String, Object>> events =
                CompletableFuture.supplyAsync(
                                () -> {
                                    try {
                                        return events(body(response));
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                })
                        .get(10, TimeUnit.SECONDS);

        assertEquals(2, events.size(), events.toString());
        assertEquals("notifications/progress", events.get(0).get("method"));
        assertEquals(1, ((Number) events.get(1).get("id")).intValue(), events.toString());
        assertNotNull(events.get(1).get("error"), events.toString());
    }

    /** The input_required state is bound to the user: another user can't replay it. */
    @Test
    void inputRequiredStateIsBoundToTheUser() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));
        Request ask =
                modern(
                        "tools/call",
                        Map.of("name", "approve", "arguments", Map.of()),
                        "approve",
                        meta);
        ask.getClientInfo().setUser(new org.restlet.security.User("alice"));
        String state = (String) result(adapter.handle(ask)).get("requestState");
        Map<String, Object> retry = new LinkedHashMap<>();
        retry.put("name", "approve");
        retry.put("arguments", Map.of());
        retry.put("requestState", state);
        retry.put(
                "inputResponses",
                Map.of("confirm", Map.of("action", "accept", "content", Map.of())));

        Request asBob = modern("tools/call", retry, "approve", meta);
        asBob.getClientInfo().setUser(new org.restlet.security.User("bob"));
        Response refused = adapter.handle(asBob);
        assertNotNull(envelope(refused).get("error"), body(refused));

        Request asAlice = modern("tools/call", retry, "approve", meta);
        asAlice.getClientInfo().setUser(new org.restlet.security.User("alice"));
        assertEquals("approved:s1", firstText(result(adapter.handle(asAlice))));
    }

    /** A 2026-07-28 request from an origin that isn't allowed is forbidden. */
    @Test
    void anUnallowedOriginIsForbidden() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getAllowedOrigins().add("https://allowed.example");
        adapter.start();

        Request request = modern("tools/list", Map.of(), null, meta());
        request.getHeaders().set("Origin", "https://evil.example");

        assertEquals(403, adapter.handle(request).getStatus().getCode());
    }

    /** A tool name can't be used by both a 2025-11-25 tool and a modern one. */
    @Test
    void aDuplicateToolNameFailsTheStart() {
        McpServerAdapter adapter = adapter();
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("echo", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>result(
                                                        text("modern")))));

        IllegalArgumentException error =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class, adapter::start);
        assertTrue(error.getMessage().contains("echo"), error.getMessage());
    }

    /** An interceptor sees and may change the final result of a streamed call. */
    @Test
    void anInterceptorChangesTheResultOfAStreamedCall() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getInterceptors()
                .add(
                        (message, context, chain) ->
                                chain.proceed(message, context)
                                        .map(
                                                response ->
                                                        new McpSchema.JSONRPCResponse(
                                                                response.jsonrpc(),
                                                                response.id(),
                                                                Map.of(
                                                                        "content",
                                                                        List.of(
                                                                                Map.of(
                                                                                        "type",
                                                                                        "text",
                                                                                        "text",
                                                                                        "intercepted")),
                                                                        "resultType",
                                                                        "complete"),
                                                                null)));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        List<Map<String, Object>> events =
                events(
                        body(
                                adapter.handle(
                                        modern(
                                                "tools/call",
                                                Map.of("name", "progress", "arguments", Map.of()),
                                                "progress",
                                                meta))));

        assertEquals(2, events.size(), events.toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) events.get(1).get("result");
        assertEquals("intercepted", firstText(result));
    }

    /** A call timing out before its answer is unavailable, with an error naming the request. */
    @Test
    void aTimedOutCallNamesTheRequest() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("silent", Map.of("type", "object")).build(),
                                (ctx, request) -> Mono.never()));
        adapter.setRequestTimeout(java.time.Duration.ofMillis(500));
        adapter.start();

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "silent", "arguments", Map.of()),
                                "silent",
                                meta()));
        String body = body(response);

        assertEquals(503, response.getStatus().getCode(), body);
        Map<String, Object> envelope =
                MAPPER.readValue(body, new TypeRef<Map<String, Object>>() {});
        assertEquals(1, ((Number) envelope.get("id")).intValue(), body);
    }

    /** The context an interceptor forwards is the one the handlers get. */
    @Test
    void handlersGetTheContextAnInterceptorForwards() throws Exception {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        adapter.getTools()
                .add(
                        new McpStatelessServerFeatures.AsyncToolSpecification(
                                McpServerAdapterTestCase.echoTool(),
                                (transport, call) ->
                                        Mono.just(
                                                McpSchema.CallToolResult.builder()
                                                        .addTextContent(
                                                                String.valueOf(
                                                                        ((McpRequestContext)
                                                                                        transport
                                                                                                .get(
                                                                                                        McpProtocol
                                                                                                                .CONTEXT_KEY))
                                                                                .attributes()
                                                                                .get("tenant")))
                                                        .build())));
        adapter.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            McpRequestContext forwarded =
                                    new McpRequestContext(
                                            context.transportRequest().orElse(null),
                                            context.protocolVersion());
                            forwarded.attributes().put("tenant", "acme");
                            return chain.proceed(message, forwarded);
                        });
        adapter.start();

        assertEquals(
                "acme",
                firstText(
                        result(
                                call(
                                        adapter,
                                        "tools/call",
                                        Map.of("name", "echo", "arguments", Map.of("text", "x")),
                                        "echo"))));
    }

    /** A streamed result an interceptor suppresses still ends with an error answering the call. */
    @Test
    void aSuppressedStreamedResultStillAnswersTheCall() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getInterceptors()
                .add(
                        (message, context, chain) ->
                                chain.proceed(message, context).then(Mono.empty()));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        List<Map<String, Object>> events =
                events(
                        body(
                                adapter.handle(
                                        modern(
                                                "tools/call",
                                                Map.of("name", "progress", "arguments", Map.of()),
                                                "progress",
                                                meta))));

        assertEquals(2, events.size(), events.toString());
        assertEquals(1, ((Number) events.get(1).get("id")).intValue(), events.toString());
        assertNotNull(events.get(1).get("error"), events.toString());
    }

    /** A client gone before the first event, at the head's flush, also frees the call slot. */
    @Test
    void aClientGoneAtTheFirstFlushCancelsTheTool() throws Exception {
        McpServerAdapter adapter = adapter();
        CountDownLatch cancelled = new CountDownLatch(1);
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("endless", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(
                                                                                1, null, "started")
                                                                        .then(
                                                                                Mono
                                                                                        .<CallToolOutcome>
                                                                                                never()
                                                                                        .doOnCancel(
                                                                                                cancelled
                                                                                                        ::countDown))))));
        adapter.setMaxConcurrentCalls(1);
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "endless", "arguments", Map.of()),
                                "endless",
                                meta));
        java.io.OutputStream unflushable =
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        // Never reached: the flush fails first.
                    }

                    @Override
                    public void flush() throws java.io.IOException {
                        throw new java.io.IOException("client gone");
                    }
                };
        try {
            response.getEntity().write(unflushable);
        } catch (java.io.IOException _) {
            // The write reports the broken client.
        }

        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the tool's stream was not cancelled");
        assertEquals(
                "echo:freed",
                firstText(
                        result(
                                callWhenFree(
                                        adapter,
                                        "tools/call",
                                        Map.of(
                                                "name",
                                                "echo",
                                                "arguments",
                                                Map.of("text", "freed")),
                                        "echo"))));
    }

    /** A streamed response released without being written cancels the tool and frees its slot. */
    @Test
    void aReleasedUnwrittenStreamCancelsTheTool() throws Exception {
        McpServerAdapter adapter = adapter();
        CountDownLatch cancelled = new CountDownLatch(1);
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("endless", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(
                                                                                1, null, "started")
                                                                        .then(
                                                                                Mono
                                                                                        .<CallToolOutcome>
                                                                                                never()
                                                                                        .doOnCancel(
                                                                                                cancelled
                                                                                                        ::countDown))))));
        adapter.setMaxConcurrentCalls(1);
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "endless", "arguments", Map.of()),
                                "endless",
                                meta));
        response.getEntity().release();

        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the tool's stream was not cancelled");
        assertEquals(
                "echo:freed",
                firstText(
                        result(
                                callWhenFree(
                                        adapter,
                                        "tools/call",
                                        Map.of(
                                                "name",
                                                "echo",
                                                "arguments",
                                                Map.of("text", "freed")),
                                        "echo"))));
    }

    /** Modern resources, templates and prompts are served next to the bridged ones. */
    @Test
    void modernResourcesAndPromptsAreServed() throws Exception {
        McpServerAdapter adapter = adapter();
        addResourcesAndPrompts(adapter);
        addModernResourcesAndPrompts(adapter);
        adapter.start();

        List<?> resources =
                (List<?>) result(call(adapter, "resources/list", Map.of(), null)).get("resources");
        assertEquals(
                List.of("test://fixed", "test://modern"),
                resources.stream().map(each -> ((Map<?, ?>) each).get("uri")).toList());
        List<?> templates =
                (List<?>)
                        result(call(adapter, "resources/templates/list", Map.of(), null))
                                .get("resourceTemplates");
        assertEquals(
                List.of("test://items/{id}", "test://things/{id}"),
                templates.stream().map(each -> ((Map<?, ?>) each).get("uriTemplate")).toList());
        for (String uri : List.of("test://modern", "test://things/3")) {
            Map<String, Object> read =
                    result(call(adapter, "resources/read", Map.of("uri", uri), uri));
            Map<?, ?> content = (Map<?, ?>) ((List<?>) read.get("contents")).get(0);
            assertEquals(uri, content.get("uri"));
            assertEquals("modern", content.get("text"));
        }
        List<?> prompts =
                (List<?>) result(call(adapter, "prompts/list", Map.of(), null)).get("prompts");
        assertEquals(
                List.of("greet", "welcome"),
                prompts.stream().map(each -> ((Map<?, ?>) each).get("name")).toList());
        Map<String, Object> prompt =
                result(call(adapter, "prompts/get", Map.of("name", "welcome"), "welcome"));
        assertEquals("modern welcome", prompt.get("description"));
    }

    /** A URI, URI template or prompt name used twice across both lists fails the start. */
    @Test
    void aDuplicateResourceOrPromptFailsTheStart() {
        McpServerAdapter uri = adapter();
        addResourcesAndPrompts(uri);
        uri.getModernResources()
                .add(
                        new McpModernResource(
                                io.modelcontextprotocol.modern.McpSchema.Resource.builder(
                                                "test://fixed", "again")
                                        .build(),
                                modernRead()));
        McpServerAdapter template = adapter();
        addResourcesAndPrompts(template);
        template.getModernResourceTemplates()
                .add(
                        new McpModernResourceTemplate(
                                io.modelcontextprotocol.modern.McpSchema.ResourceTemplate.builder(
                                                "test://items/{id}", "again")
                                        .build(),
                                modernRead()));
        McpServerAdapter prompt = adapter();
        addResourcesAndPrompts(prompt);
        prompt.getModernPrompts().add(modernPrompt("greet"));

        for (var each :
                Map.of(uri, "test://fixed", template, "items/{id}", prompt, "greet").entrySet()) {
            IllegalArgumentException error =
                    org.junit.jupiter.api.Assertions.assertThrows(
                            IllegalArgumentException.class, each.getKey()::start);
            assertTrue(error.getMessage().contains(each.getValue()), error.getMessage());
        }
    }

    /**
     * A listen stream is acknowledged, then carries the changes the application publishes; it does
     * not take a call slot, and ends with its result when the adapter stops.
     */
    @Test
    void aListenStreamCarriesChangesUntilTheAdapterStops() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setMaxConcurrentCalls(1);
        adapter.start();

        Response response = adapter.handle(listen());
        assertEquals(200, response.getStatus().getCode());
        assertEquals(MediaType.TEXT_EVENT_STREAM, response.getEntity().getMediaType());
        EventStreamReader reader = open(response);
        assertEquals(
                io.modelcontextprotocol.modern.McpSchema
                        .METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED,
                next(reader).get("method"));

        assertNotNull(result(call(adapter, "tools/list", Map.of(), null)).get("tools"));
        adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());
        assertEquals(
                io.modelcontextprotocol.modern.McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED,
                next(reader).get("method"));

        adapters.remove(adapter);
        adapter.stop();
        Map<String, Object> last = next(reader);
        assertEquals(1, ((Number) last.get("id")).intValue(), last.toString());
        assertTrue(last.containsKey("result"), last.toString());
        // Nothing follows the result, such as an error cutting the stream short.
        assertEquals(
                null,
                CompletableFuture.supplyAsync(
                                () -> {
                                    try {
                                        ServerSentEvent event = reader.readEvent();
                                        while (event != null
                                                && (event.getData() == null
                                                        || event.getData().isEmpty())) {
                                            event = reader.readEvent();
                                        }
                                        return event == null ? null : event.getData();
                                    } catch (java.io.IOException e) {
                                        throw new IllegalStateException(e);
                                    }
                                })
                        .get(10, TimeUnit.SECONDS));
    }

    /** Listen streams beyond the limit are refused, without affecting calls. */
    @Test
    void theListenLimitRefusesAnExtraStream() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setMaxListenStreams(1);
        adapter.start();

        Response first = adapter.handle(listen());
        EventStreamReader reader = open(first);
        assertEquals(
                io.modelcontextprotocol.modern.McpSchema
                        .METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED,
                next(reader).get("method"));
        Response second = adapter.handle(listen());

        assertEquals(503, second.getStatus().getCode(), body(second));
        assertNotNull(result(call(adapter, "tools/list", Map.of(), null)).get("tools"));
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("progress", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(1, 2.0, "half")
                                                                        .then(
                                                                                Mono.just(
                                                                                        text(
                                                                                                "done")))))));
        adapter.getModernTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("approve", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                request.requestState() == null
                                                        ? McpAsyncResponse.<CallToolOutcome>result(
                                                                InputRequiredResult.builder()
                                                                        .elicit(
                                                                                "confirm",
                                                                                ElicitFormRequest
                                                                                        .builder(
                                                                                                "Proceed?",
                                                                                                Map
                                                                                                        .of(
                                                                                                                "type",
                                                                                                                "object",
                                                                                                                "properties",
                                                                                                                Map
                                                                                                                        .of()))
                                                                                        .build())
                                                                        .requestState("s1")
                                                                        .build())
                                                        : McpAsyncResponse.<CallToolOutcome>result(
                                                                text(
                                                                        "approved:"
                                                                                + request
                                                                                        .requestState())))));
        adapters.add(adapter);
        return adapter;
    }

    private static void addResourcesAndPrompts(McpServerAdapter adapter) {
        adapter.getResources()
                .add(
                        new McpStatelessServerFeatures.AsyncResourceSpecification(
                                McpSchema.Resource.builder("test://fixed", "fixed").build(),
                                (context, request) -> Mono.just(contents(request.uri()))));
        adapter.getResourceTemplates()
                .add(
                        new McpStatelessServerFeatures.AsyncResourceTemplateSpecification(
                                McpSchema.ResourceTemplate.builder("test://items/{id}", "items")
                                        .build(),
                                (context, request) -> Mono.just(contents(request.uri()))));
        adapter.getPrompts()
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
    }

    private static void addModernResourcesAndPrompts(McpServerAdapter adapter) {
        adapter.getModernResources()
                .add(
                        new McpModernResource(
                                io.modelcontextprotocol.modern.McpSchema.Resource.builder(
                                                "test://modern", "modern")
                                        .build(),
                                modernRead()));
        adapter.getModernResourceTemplates()
                .add(
                        new McpModernResourceTemplate(
                                io.modelcontextprotocol.modern.McpSchema.ResourceTemplate.builder(
                                                "test://things/{id}", "things")
                                        .build(),
                                modernRead()));
        adapter.getModernPrompts().add(modernPrompt("welcome"));
    }

    private static McpAsyncResource modernRead() {
        return McpAsyncResource.of(
                (ctx, request) -> {
                    io.modelcontextprotocol.modern.McpSchema.ReadResourceOutcome outcome =
                            io.modelcontextprotocol.modern.McpSchema.ReadResourceResult.builder(
                                            List.of(
                                                    new io.modelcontextprotocol.modern.McpSchema
                                                            .TextResourceContents(
                                                            request.uri(),
                                                            "text/plain",
                                                            "modern",
                                                            null)))
                                    .build();
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    private static McpAsyncPrompt modernPrompt(String name) {
        return McpAsyncPrompt.of(
                io.modelcontextprotocol.modern.McpSchema.Prompt.builder(name).build(),
                (ctx, request) -> {
                    io.modelcontextprotocol.modern.McpSchema.GetPromptOutcome outcome =
                            new io.modelcontextprotocol.modern.McpSchema.GetPromptResult(
                                    "modern " + name,
                                    List.of(
                                            new io.modelcontextprotocol.modern.McpSchema
                                                    .PromptMessage(
                                                    io.modelcontextprotocol.modern.McpSchema.Role
                                                            .USER,
                                                    TextContent.builder("hi").build())),
                                    null,
                                    null);
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    /** A {@code subscriptions/listen} request for tools list changes. */
    private static Request listen() {
        return modern(
                io.modelcontextprotocol.modern.McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                Map.of("notifications", Map.of("toolsListChanged", true)),
                null,
                meta());
    }

    /** Writes a streamed response in the background, to be read event by event. */
    private static EventStreamReader open(Response response) throws Exception {
        var input = new java.io.PipedInputStream(1 << 16);
        var output = new java.io.PipedOutputStream(input);
        Thread.ofVirtual()
                .start(
                        () -> {
                            try (output) {
                                response.getEntity().write(output);
                            } catch (java.io.IOException _) {
                                // The reader went away.
                            }
                        });
        return new EventStreamReader(input);
    }

    /** The next message of a stream, skipping heartbeats. */
    private static Map<String, Object> next(EventStreamReader reader) throws Exception {
        return CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                for (ServerSentEvent event = reader.readEvent();
                                        event != null;
                                        event = reader.readEvent()) {
                                    if (event.getData() != null && !event.getData().isEmpty()) {
                                        return MAPPER.readValue(
                                                event.getData(),
                                                new TypeRef<Map<String, Object>>() {});
                                    }
                                }
                                throw new IllegalStateException("The stream ended");
                            } catch (java.io.IOException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                .get(10, TimeUnit.SECONDS);
    }

    private static McpSchema.ReadResourceResult contents(String uri) {
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(uri, "text/plain", "text", null)), null);
    }

    private static CallToolResult text(String text) {
        return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
    }

    private static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }

    private static Response call(
            McpServerAdapter adapter, String method, Map<String, Object> params, String name) {
        return adapter.handle(modern(method, params, name, meta()));
    }

    /**
     * Calls once the call slot an ended exchange held is free again: its worker frees it just after
     * the handler sees the cancellation.
     */
    private static Response callWhenFree(
            McpServerAdapter adapter, String method, Map<String, Object> params, String name)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        Response response = call(adapter, method, params, name);
        while (response.getStatus().getCode() == 503 && System.nanoTime() < deadline) {
            response.getEntity().release();
            java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
            response = call(adapter, method, params, name);
        }
        return response;
    }

    /** A 2026-07-28 request with its _meta and headers. */
    private static Request modern(
            String method, Map<String, Object> params, String name, Map<String, Object> meta) {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", method);
        envelope.put("params", withMeta);
        try {
            return post(MAPPER.writeValueAsString(envelope), method, name);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Request post(String body, String method, String name) {
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost/mcp",
                        new StringRepresentation(body, MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, MODERN);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        request.getHeaders().set("Mcp-Method", method);
        if (name != null) {
            request.getHeaders().set("Mcp-Name", name);
        }
        return request;
    }

    private static String body(Response response) throws Exception {
        if (response.getEntity() == null) {
            return "";
        }
        var bytes = new ByteArrayOutputStream();
        response.getEntity().write(bytes);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> envelope(Response response) throws Exception {
        return MAPPER.readValue(body(response), new TypeRef<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(Response response) throws Exception {
        Map<String, Object> envelope = envelope(response);
        assertTrue(envelope.containsKey("result"), envelope.toString());
        return (Map<String, Object>) envelope.get("result");
    }

    private static int code(Response response) throws Exception {
        return ((Number) ((Map<?, ?>) envelope(response).get("error")).get("code")).intValue();
    }

    private static String firstText(Map<String, Object> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }

    private static List<Map<String, Object>> events(String body) throws Exception {
        List<Map<String, Object>> events = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (line.startsWith("data:")) {
                events.add(
                        MAPPER.readValue(
                                line.substring(5).trim(), new TypeRef<Map<String, Object>>() {}));
            }
        }
        return events;
    }
}
