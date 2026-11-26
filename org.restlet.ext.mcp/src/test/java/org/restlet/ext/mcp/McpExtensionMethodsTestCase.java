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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;

/**
 * Extension methods and advertised extensions of {@link McpServerAdapter}: registration, the
 * pipeline they share with MCP's own methods, and the legacy edge.
 */
class McpExtensionMethodsTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;
    private static final String SKILLS = "io.modelcontextprotocol/skills";

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** An extension method answers its JSON result, with the adapter's server info. */
    @Test
    void anExtensionMethodAnswersItsResult() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.start();

        Response response = call(adapter, "skills/list", Map.of(), null);

        assertEquals(200, response.getStatus().getCode());
        Map<String, Object> result = result(response);
        assertEquals(List.of("resultType", "skills", "_meta"), List.copyOf(result.keySet()));
        assertEquals("complete", result.get("resultType"));
        assertEquals(List.of(Map.of("uri", "skill://deploy")), result.get("skills"));
        assertTrue(
                ((Map<?, ?>) result.get("_meta")).containsKey(MetaKeys.SERVER_INFO),
                result.toString());
    }

    /** Advertised extensions appear in server/discover, with or without methods. */
    @Test
    void advertisedExtensionsAppearInDiscover() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getExtensions().put(SKILLS, Map.of("directoryRead", true));
        adapter.getExtensions().put("io.modelcontextprotocol/ui", Map.of());
        adapter.start();

        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        Map<?, ?> extensions =
                (Map<?, ?>) ((Map<?, ?>) discover.get("capabilities")).get("extensions");
        assertEquals(Map.of("directoryRead", true), extensions.get(SKILLS));
        assertEquals(Map.of(), extensions.get("io.modelcontextprotocol/ui"));
        assertEquals(2, extensions.size(), extensions.toString());
    }

    /** A registered method isn't advertised by itself. */
    @Test
    void aMethodDoesNotAdvertiseItsExtension() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.start();

        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        assertFalse(
                ((Map<?, ?>) discover.get("capabilities")).containsKey("extensions"),
                discover.toString());
    }

    /** The Mcp-Name header of a method declaring a name param must be present and match. */
    @Test
    void theMcpNameHeaderIsCheckedOnMethodsDeclaringIt() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(taskGet());
        adapter.start();
        Map<String, Object> params = Map.of("taskId", "t-1");

        Response missing = call(adapter, "tasks/get", params, null);
        assertEquals(400, missing.getStatus().getCode());
        assertEquals(-32020, code(missing));

        Response mismatched = call(adapter, "tasks/get", params, "t-2");
        assertEquals(400, mismatched.getStatus().getCode());
        assertEquals(-32020, code(mismatched));

        assertEquals("working", result(call(adapter, "tasks/get", params, "t-1")).get("status"));
        String encoded =
                "=?base64?"
                        + Base64.getEncoder().encodeToString("t-1".getBytes(StandardCharsets.UTF_8))
                        + "?=";
        assertEquals("working", result(call(adapter, "tasks/get", params, encoded)).get("status"));
    }

    /** A method declaring no name param takes no Mcp-Name header. */
    @Test
    void aMethodWithoutNameParamNeedsNoMcpNameHeader() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.start();

        Response response = call(adapter, "skills/list", Map.of("name", "ignored"), null);
        assertEquals(200, response.getStatus().getCode(), body(response));
    }

    /** Interceptors run around extension methods and may refuse them. */
    @Test
    void interceptorsWrapExtensionMethods() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.getMethods().add(taskGet());
        List<String> seen = new CopyOnWriteArrayList<>();
        adapter.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            if (message instanceof JsonRpc.JSONRPCRequest request) {
                                seen.add(request.method());
                                if ("tasks/get".equals(request.method())) {
                                    return Mono.just(
                                            JsonRpc.JSONRPCResponse.error(
                                                    request.id(),
                                                    new JsonRpc.JSONRPCResponse.JSONRPCError(
                                                            -32602, "Task not found")));
                                }
                            }
                            return chain.proceed(message, context);
                        });
        adapter.start();

        assertEquals(200, call(adapter, "skills/list", Map.of(), null).getStatus().getCode());
        Response refused = call(adapter, "tasks/get", Map.of("taskId", "t-1"), "t-1");
        assertEquals(-32602, code(refused));
        assertEquals(List.of("skills/list", "tasks/get"), seen);
    }

    /**
     * An input-required method seals the state of its input_required result and opens it on the
     * retry.
     */
    @Test
    void anInputRequiredMethodSealsAndOpensItsState() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(confirm().withInputRequired(true));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));

        Map<String, Object> asked =
                result(adapter.handle(modern("com.example/confirm", Map.of(), null, meta)));
        assertEquals("input_required", asked.get("resultType"), asked.toString());
        String state = (String) asked.get("requestState");
        assertNotNull(state);
        assertFalse(state.contains("s1"), "the state must be sealed, not sent in clear");

        Map<String, Object> retry = new LinkedHashMap<>();
        retry.put("requestState", state);
        retry.put(
                "inputResponses",
                Map.of("confirm", Map.of("action", "accept", "content", Map.of())));
        Map<String, Object> answered =
                result(adapter.handle(modern("com.example/confirm", retry, null, meta)));
        assertEquals("s1", answered.get("confirmed"));
    }

    /**
     * The sealed state of a method declaring a name param is bound to that param's value: it opens
     * on a retry for the same value, not for another, over HTTP and through dispatch.
     */
    @Test
    void aSealedStateIsBoundToTheNameParamValue() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(confirm().withNameParam("taskId").withInputRequired(true));
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));

        Map<String, Object> asked =
                result(
                        adapter.handle(
                                modern(
                                        "com.example/confirm",
                                        Map.of("taskId", "t-1"),
                                        "t-1",
                                        meta)));
        String state = (String) asked.get("requestState");
        assertNotNull(state, asked.toString());

        Map<String, Object> same = retry("t-1", state);
        assertEquals(
                "s1",
                result(adapter.handle(modern("com.example/confirm", same, "t-1", meta)))
                        .get("confirmed"));

        Map<String, Object> other = retry("t-2", state);
        Response refused = adapter.handle(modern("com.example/confirm", other, "t-2", meta));
        assertEquals(-32602, code(refused), body(refused));

        Map<String, Object> dispatched = new LinkedHashMap<>(other);
        dispatched.put("_meta", meta);
        JsonRpc.JSONRPCResponse response =
                adapter.dispatch(
                                new JsonRpc.JSONRPCRequest(
                                        "2.0", "com.example/confirm", 2, dispatched),
                                new McpRequestContext(null))
                        .orElseThrow();
        assertNotNull(response.error(), String.valueOf(response.result()));
        assertEquals(-32602, response.error().code());
    }

    private static Map<String, Object> retry(String taskId, String state) {
        Map<String, Object> retry = new LinkedHashMap<>();
        retry.put("taskId", taskId);
        retry.put("requestState", state);
        retry.put(
                "inputResponses",
                Map.of("confirm", Map.of("action", "accept", "content", Map.of())));
        return retry;
    }

    /** A method not declared input-required can't answer input_required. */
    @Test
    void aMethodNotInputRequiredCannotAnswerInputRequired() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(confirm());
        adapter.start();
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));

        Response response = adapter.handle(modern("com.example/confirm", Map.of(), null, meta));
        assertEquals(-32603, code(response));
    }

    /** A legacy client can't reach an extension method. */
    @Test
    void legacyClientsDoNotReachExtensionMethods() throws Exception {
        McpServerAdapter adapter = adapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.start();

        Response response =
                adapter.handle(
                        McpServerAdapterTestCase.request(
                                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"skills/list\","
                                        + "\"params\":{}}"));
        Map<String, Object> envelope = McpServerAdapterTestCase.envelope(response);
        assertEquals(-32601, ((Map<?, ?>) envelope.get("error")).get("code"));
    }

    /** A method MCP itself defines can't be registered as an extension method. */
    @Test
    void mcpMethodsCannotBeRegistered() {
        for (String name : List.of("tools/call", "server/discover", "notifications/tasks")) {
            McpServerAdapter adapter = adapter();
            adapter.getMethods().add(McpMethod.of(name, McpExtensionMethodsTestCase::skills));
            assertThrows(IllegalArgumentException.class, adapter::start, name);
        }
    }

    /** A method can't be registered twice. */
    @Test
    void aMethodCannotBeRegisteredTwice() {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        assertThrows(IllegalArgumentException.class, adapter::start);
    }

    /** An extension id must be a prefixed _meta key. */
    @Test
    void anExtensionIdMustBePrefixed() {
        McpServerAdapter adapter = adapter();
        adapter.getExtensions().put("skills", Map.of());
        assertThrows(IllegalArgumentException.class, adapter::start);
    }

    /** Registrations are fixed once the adapter starts. */
    @Test
    void registrationsAreFixedAfterStart() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getMethods().add(McpMethod.of("skills/list", McpExtensionMethodsTestCase::skills));
        adapter.start();
        adapter.getMethods().clear();
        adapter.getExtensions().put(SKILLS, Map.of());

        assertEquals(200, call(adapter, "skills/list", Map.of(), null).getStatus().getCode());
        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        assertFalse(((Map<?, ?>) discover.get("capabilities")).containsKey("extensions"));
    }

    /** A method and a JSON result check their arguments. */
    @Test
    void methodsAndJsonResultsCheckTheirArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> McpMethod.of(" ", McpExtensionMethodsTestCase::skills));
        assertThrows(
                IllegalArgumentException.class,
                () -> McpMethod.of("a/b", McpExtensionMethodsTestCase::skills).withNameParam(""));
        assertThrows(NullPointerException.class, () -> McpMethod.of("a/b", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> McpJsonResult.complete(Map.of("resultType", "task")));
        assertThrows(IllegalArgumentException.class, () -> McpJsonResult.of(" ", Map.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> McpJsonResult.of("input_required", Map.of("requestState", "s1")));

        McpJsonResult task = McpJsonResult.of("task", Map.of("_meta", Map.of("k", "v")));
        assertEquals("task", task.resultType());
        assertEquals(Map.of("k", "v"), task.meta());
        assertEquals(task, McpJsonResult.of("task", Map.of("_meta", Map.of("k", "v"))));
        assertEquals(task.hashCode(), task.fields().hashCode());
        assertTrue(task.toString().contains("task"));
        assertEquals(null, McpJsonResult.complete(Map.of()).meta());
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        return adapter;
    }

    private static Mono<McpAsyncResponse<McpJsonResult>> skills(
            io.modelcontextprotocol.modern.server.McpRequestContext ctx,
            Map<String, Object> params) {
        return Mono.just(
                McpAsyncResponse.result(
                        McpJsonResult.complete(
                                Map.of("skills", List.of(Map.of("uri", "skill://deploy"))))));
    }

    private static McpMethod taskGet() {
        return McpMethod.of(
                        "tasks/get",
                        (ctx, params) ->
                                Mono.just(
                                        McpAsyncResponse.result(
                                                McpJsonResult.complete(
                                                        Map.of(
                                                                "taskId",
                                                                params.get("taskId"),
                                                                "status",
                                                                "working")))))
                .withNameParam("taskId");
    }

    private static McpMethod confirm() {
        return McpMethod.of(
                "com.example/confirm",
                (ctx, params) ->
                        Mono.just(
                                params.get("requestState") == null
                                        ? McpAsyncResponse.<McpSchema.Result>result(
                                                InputRequiredResult.builder()
                                                        .elicit(
                                                                "confirm",
                                                                ElicitFormRequest.builder(
                                                                                "Proceed?",
                                                                                Map.of(
                                                                                        "type",
                                                                                        "object",
                                                                                        "properties",
                                                                                        Map.of()))
                                                                        .build())
                                                        .requestState("s1")
                                                        .build())
                                        : McpAsyncResponse.<McpSchema.Result>result(
                                                McpJsonResult.complete(
                                                        Map.of(
                                                                "confirmed",
                                                                params.get("requestState"))))));
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
}
