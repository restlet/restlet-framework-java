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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpSyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpSyncResource;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;

/**
 * Blocking handlers made with {@link McpBlocking}: their thread, Restlet's current response and the
 * adapter's call scopes around them.
 */
class McpBlockingTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;
    private static final ScopedValue<String> CALLER = ScopedValue.newInstance();
    private static final ThreadLocal<String> TRACE = new ThreadLocal<>();

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /**
     * A blocking tool runs on a virtual thread, with Restlet's current request and the scopes'
     * context, the first scope outermost, and nothing left on the thread afterwards.
     */
    @Test
    void aBlockingToolRunsWithinTheScopes() throws Exception {
        List<String> order = new CopyOnWriteArrayList<>();
        AtomicReference<Request> current = new AtomicReference<>();
        AtomicReference<Thread> thread = new AtomicReference<>();
        McpServerAdapter adapter = adapter();
        adapter.getCallScopes()
                .add(
                        (context, body) -> {
                            order.add("outer");
                            ScopedValue.where(CALLER, context.principal().orElse("anonymous"))
                                    .call(
                                            () -> {
                                                body.run();
                                                return null;
                                            });
                        });
        adapter.getCallScopes()
                .add(
                        (context, body) -> {
                            order.add("inner:" + CALLER.orElse("unbound"));
                            TRACE.set("span-1");
                            try {
                                body.run();
                            } finally {
                                TRACE.remove();
                            }
                        });
        adapter.getTools()
                .add(
                        McpBlocking.tool(
                                McpSyncTool.of(
                                        Tool.builder("whoami", Map.of("type", "object")).build(),
                                        (ctx, request) -> {
                                            current.set(Request.getCurrent());
                                            thread.set(Thread.currentThread());
                                            return McpSyncResponse.<CallToolOutcome>result(
                                                    McpTestFeatures.text(
                                                            CALLER.get() + "/" + TRACE.get()));
                                        })));
        adapter.start();

        Request request =
                call("tools/call", Map.of("name", "whoami", "arguments", Map.of()), "whoami");
        request.getClientInfo().setUser(new org.restlet.security.User("alice"));
        Map<String, Object> result = result(adapter.handle(request));

        assertEquals("alice/span-1", firstText(result));
        assertEquals(List.of("outer", "inner:alice"), order);
        assertSame(request, current.get());
        assertTrue(thread.get().isVirtual(), thread.get().toString());
        assertNull(TRACE.get());
    }

    /** Blocking resources, prompts and extension methods run within the scopes too. */
    @Test
    void blockingResourcesPromptsAndMethodsRunWithinTheScopes() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getCallScopes()
                .add(
                        (context, body) ->
                                ScopedValue.where(CALLER, "scoped")
                                        .call(
                                                () -> {
                                                    body.run();
                                                    return null;
                                                }));
        adapter.getResources()
                .add(
                        new McpResource(
                                McpSchema.Resource.builder("test://who", "who").build(),
                                McpBlocking.resource(
                                        McpSyncResource.of(
                                                (ctx, request) ->
                                                        McpSyncResponse
                                                                .<McpSchema.ReadResourceOutcome>
                                                                        result(
                                                                                read(
                                                                                        request
                                                                                                .uri()))))));
        adapter.getPrompts()
                .add(
                        McpBlocking.prompt(
                                McpSyncPrompt.of(
                                        McpSchema.Prompt.builder("greet").build(),
                                        (ctx, request) ->
                                                McpSyncResponse.<McpSchema.GetPromptOutcome>result(
                                                        new McpSchema.GetPromptResult(
                                                                "hello " + CALLER.get(),
                                                                List.of(),
                                                                null,
                                                                null)))));
        adapter.getMethods()
                .add(
                        McpBlocking.method(
                                "com.example/who",
                                (ctx, params) ->
                                        McpSyncResponse.result(
                                                McpJsonResult.complete(
                                                        Map.of("who", CALLER.get())))));
        adapter.start();

        Map<String, Object> read =
                result(
                        adapter.handle(
                                call("resources/read", Map.of("uri", "test://who"), "test://who")));
        assertEquals("scoped", ((Map<?, ?>) ((List<?>) read.get("contents")).get(0)).get("text"));
        Map<String, Object> prompt =
                result(adapter.handle(call("prompts/get", Map.of("name", "greet"), "greet")));
        assertEquals("hello scoped", prompt.get("description"));
        Map<String, Object> who = result(adapter.handle(call("com.example/who", Map.of(), null)));
        assertEquals("scoped", who.get("who"));
    }

    /** A streaming blocking tool sends its progress before its result, within the scopes. */
    @Test
    void aStreamingBlockingToolRunsWithinTheScopes() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getCallScopes()
                .add(
                        (context, body) ->
                                ScopedValue.where(CALLER, "streamed")
                                        .call(
                                                () -> {
                                                    body.run();
                                                    return null;
                                                }));
        adapter.getTools()
                .add(
                        McpBlocking.tool(
                                McpSyncTool.of(
                                        Tool.builder("steps", Map.of("type", "object")).build(),
                                        (ctx, request) ->
                                                McpSyncResponse.<CallToolOutcome>streaming(
                                                        notifier -> {
                                                            notifier.progress(1, 2.0, "half");
                                                            return McpTestFeatures.text(
                                                                    CALLER.get());
                                                        }))));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");

        Response response =
                adapter.handle(
                        call(
                                "tools/call",
                                Map.of("name", "steps", "arguments", Map.of()),
                                "steps",
                                meta));
        List<Map<String, Object>> events = new ArrayList<>();
        for (String line : body(response).split("\n")) {
            if (line.startsWith("data:")) {
                events.add(MAPPER.readValue(line.substring(5).trim(), new TypeRef<>() {}));
            }
        }
        assertEquals(2, events.size(), events.toString());
        assertEquals("notifications/progress", events.get(0).get("method"));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) events.get(1).get("result");
        assertEquals("streamed", firstText(result));
    }

    /** A scope that doesn't run the handler, or runs it twice, fails the call. */
    @Test
    void aScopeMustRunTheHandlerOnce() throws Exception {
        for (McpCallScope broken :
                List.<McpCallScope>of(
                        (context, body) -> {},
                        (context, body) -> {
                            body.run();
                            body.run();
                        })) {
            McpServerAdapter adapter = adapter();
            adapter.getCallScopes().add(broken);
            adapter.getTools().add(echo());
            adapter.start();
            assertEquals(
                    -32603,
                    code(
                            adapter.handle(
                                    call(
                                            "tools/call",
                                            Map.of("name", "echo", "arguments", Map.of()),
                                            "echo"))));
        }
    }

    /** The error of a blocking handler is answered, and dispatch runs one without transport. */
    @Test
    void errorsAndDispatchWithoutTransport() throws Exception {
        AtomicReference<Response> current = new AtomicReference<>();
        McpServerAdapter adapter = adapter();
        adapter.getTools()
                .add(
                        McpBlocking.tool(
                                McpSyncTool.of(
                                        Tool.builder("refuse", Map.of("type", "object")).build(),
                                        (ctx, request) -> {
                                            throw McpException.invalidParams("refused");
                                        })));
        adapter.getTools()
                .add(
                        McpBlocking.tool(
                                McpSyncTool.of(
                                        Tool.builder("current", Map.of("type", "object")).build(),
                                        (ctx, request) -> {
                                            current.set(Response.getCurrent());
                                            return McpSyncResponse.<CallToolOutcome>result(
                                                    McpTestFeatures.text("ok"));
                                        })));
        adapter.start();

        assertEquals(
                -32602,
                code(
                        adapter.handle(
                                call(
                                        "tools/call",
                                        Map.of("name", "refuse", "arguments", Map.of()),
                                        "refuse"))));

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "current");
        params.put("arguments", Map.of());
        params.put("_meta", meta());
        JsonRpc.JSONRPCResponse dispatched =
                adapter.dispatch(
                                new JsonRpc.JSONRPCRequest("2.0", "tools/call", 1, params),
                                new McpRequestContext(null))
                        .orElseThrow();
        assertNull(dispatched.error());
        assertNull(current.get());
    }

    private static McpSchema.ReadResourceResult read(String uri) {
        return McpSchema.ReadResourceResult.builder(
                        List.of(
                                new McpSchema.TextResourceContents(
                                        uri, "text/plain", CALLER.get(), null)))
                .build();
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        return adapter;
    }

    private static io.modelcontextprotocol.modern.server.feature.McpAsyncTool echo() {
        return McpBlocking.tool(
                McpSyncTool.of(
                        Tool.builder("echo", Map.of("type", "object")).build(),
                        (ctx, request) ->
                                McpSyncResponse.<CallToolOutcome>result(
                                        McpTestFeatures.text("echo"))));
    }

    private static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }

    private static Request call(String method, Map<String, Object> params, String name) {
        return call(method, params, name, meta());
    }

    private static Request call(
            String method, Map<String, Object> params, String name, Map<String, Object> meta) {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", method);
        envelope.put("params", withMeta);
        String body;
        try {
            body = MAPPER.writeValueAsString(envelope);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
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
        var bytes = new ByteArrayOutputStream();
        if (response.getEntity() != null) {
            response.getEntity().write(bytes);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(Response response) throws Exception {
        Map<String, Object> envelope = MAPPER.readValue(body(response), new TypeRef<>() {});
        assertTrue(envelope.containsKey("result"), envelope.toString());
        return (Map<String, Object>) envelope.get("result");
    }

    private static int code(Response response) throws Exception {
        Map<String, Object> envelope = MAPPER.readValue(body(response), new TypeRef<>() {});
        return ((Number) ((Map<?, ?>) envelope.get("error")).get("code")).intValue();
    }

    private static String firstText(Map<String, Object> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }
}
