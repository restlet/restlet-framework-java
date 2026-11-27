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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CacheScope;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;

/** The cache hints, {@code ttlMs} and {@code cacheScope}, of the results of an adapter. */
class McpCacheHintsTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** Without hints, no result may be reused. */
    @Test
    void resultsAreNotCacheableByDefault() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();

        for (String method : McpCacheHint.METHODS) {
            assertHint(0, "private", result(adapter, method, Map.of()), method);
        }
        assertHint(0, "private", read(adapter, "test://fixed"), "resources/read");
    }

    /** Each method gets its own hint, discover included. */
    @Test
    void eachMethodGetsItsHint() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setCacheHint("server/discover", McpCacheHint.publicFor(Duration.ofMinutes(1)));
        adapter.setCacheHint("tools/list", McpCacheHint.publicFor(Duration.ofSeconds(1)));
        adapter.setCacheHint("resources/list", McpCacheHint.privateFor(Duration.ofSeconds(2)));
        adapter.setCacheHint(
                "resources/templates/list", McpCacheHint.publicFor(Duration.ofSeconds(3)));
        adapter.setCacheHint("prompts/list", new McpCacheHint(4000, CacheScope.PRIVATE));
        adapter.start();

        assertHint(60_000, "public", result(adapter, "server/discover", Map.of()), "discover");
        assertHint(1000, "public", result(adapter, "tools/list", Map.of()), "tools");
        assertHint(2000, "private", result(adapter, "resources/list", Map.of()), "resources");
        assertHint(
                3000, "public", result(adapter, "resources/templates/list", Map.of()), "templates");
        assertHint(4000, "private", result(adapter, "prompts/list", Map.of()), "prompts");
        // A read keeps its handler's hint: none here.
        assertHint(0, "private", read(adapter, "test://fixed"), "resources/read");
    }

    /** Filtered, the tools list depends on the request: it stays private. */
    @Test
    void filteredToolsStayPrivate() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getToolFilters().add((context, tool) -> Mono.just(Boolean.TRUE));
        adapter.setCacheHint("tools/list", McpCacheHint.publicFor(Duration.ofSeconds(5)));
        adapter.setCacheHint("prompts/list", McpCacheHint.publicFor(Duration.ofSeconds(5)));
        adapter.start();

        assertHint(5000, "private", result(adapter, "tools/list", Map.of()), "tools");
        assertHint(5000, "public", result(adapter, "prompts/list", Map.of()), "prompts");
    }

    /**
     * A read carries the hint its handler built it with, but the answer to a retry after
     * input_required is never reused.
     */
    @Test
    void readsCarryTheirHandlersHint() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getResources()
                .add(
                        new McpResource(
                                McpSchema.Resource.builder("test://hinted", "hinted").build(),
                                hinted()));
        adapter.getResources()
                .add(
                        new McpResource(
                                McpSchema.Resource.builder("test://asking", "asking").build(),
                                asking()));
        adapter.start();

        assertHint(7000, "public", read(adapter, "test://hinted"), "hinted");

        Map<String, Object> meta = meta();
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));
        Map<String, Object> asked = result(adapter, "resources/read", uri("test://asking"), meta);
        assertEquals("input_required", asked.get("resultType"), asked.toString());
        Map<String, Object> retry = uri("test://asking");
        retry.put("requestState", asked.get("requestState"));
        retry.put(
                "inputResponses",
                Map.of("confirm", Map.of("action", "accept", "content", Map.of())));
        Map<String, Object> answered = result(adapter, "resources/read", retry, meta);
        assertTrue(answered.containsKey("contents"), answered.toString());
        assertHint(0, "private", answered, "retry");
    }

    /** 2025-11-25 clients get their results without hints. */
    @Test
    void legacyClientsGetNoHints() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getResources()
                .add(
                        new McpResource(
                                McpSchema.Resource.builder("test://hinted", "hinted").build(),
                                hinted()));
        for (String method : McpCacheHint.METHODS) {
            adapter.setCacheHint(method, McpCacheHint.publicFor(Duration.ofMinutes(1)));
        }
        adapter.start();

        for (String call :
                List.of(
                        "\"method\":\"tools/list\"",
                        "\"method\":\"resources/list\"",
                        "\"method\":\"resources/templates/list\"",
                        "\"method\":\"prompts/list\"",
                        "\"method\":\"resources/read\",\"params\":{\"uri\":\"test://hinted\"}")) {
            Map<String, Object> envelope =
                    McpServerAdapterTestCase.envelope(
                            adapter.handle(
                                    McpServerAdapterTestCase.request(
                                            "{\"jsonrpc\":\"2.0\",\"id\":1," + call + "}")));
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) envelope.get("result");
            assertTrue(result != null, call + ": " + envelope);
            assertFalse(result.containsKey("ttlMs"), call + ": " + result);
            assertFalse(result.containsKey("cacheScope"), call + ": " + result);
        }
    }

    /** Only discover and the listings take a hint, until the adapter starts. */
    @Test
    void hintsAreCheckedAndFixedOnStart() throws Exception {
        McpServerAdapter adapter = adapter();
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.setCacheHint("tools/call", McpCacheHint.NONE));
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.setCacheHint(null, McpCacheHint.NONE));
        McpCacheHint hint = McpCacheHint.privateFor(Duration.ofSeconds(1));
        adapter.setCacheHint("tools/list", hint);
        assertEquals(hint, adapter.getCacheHint("tools/list"));
        adapter.setCacheHint("tools/list", null);
        assertNull(adapter.getCacheHint("tools/list"));
        adapter.start();
        assertThrows(IllegalStateException.class, () -> adapter.setCacheHint("tools/list", hint));
    }

    /** A hint has a time to live that is not negative, and a scope. */
    @Test
    void hintsAreValid() {
        assertThrows(IllegalArgumentException.class, () -> new McpCacheHint(-1, CacheScope.PUBLIC));
        assertThrows(NullPointerException.class, () -> new McpCacheHint(1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> McpCacheHint.publicFor(Duration.ofMillis(-1)));
        assertEquals(
                new McpCacheHint(1500, CacheScope.PRIVATE),
                McpCacheHint.privateFor(Duration.ofMillis(1500)));
        assertEquals(new McpCacheHint(0, CacheScope.PRIVATE), McpCacheHint.NONE);
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getResources().add(McpTestFeatures.resource("test://fixed", "text"));
        adapter.getResourceTemplates().add(McpTestFeatures.template("test://items/{id}", "text"));
        adapter.getPrompts().add(McpTestFeatures.prompt("greet", "greeting", "hello"));
        adapters.add(adapter);
        return adapter;
    }

    /** A read publicly cacheable for 7 seconds. */
    private static McpAsyncResource hinted() {
        return McpAsyncResource.of(
                (ctx, request) -> {
                    McpSchema.ReadResourceOutcome outcome =
                            McpSchema.ReadResourceResult.builder(contents(request.uri()))
                                    .ttlMs(7000)
                                    .cacheScope(CacheScope.PUBLIC)
                                    .build();
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    /** A read asking a confirmation first, then publicly cacheable for 7 seconds. */
    private static McpAsyncResource asking() {
        return McpAsyncResource.of(
                (ctx, request) -> {
                    McpSchema.ReadResourceOutcome outcome =
                            request.requestState() == null
                                    ? McpSchema.InputRequiredResult.builder()
                                            .elicit(
                                                    "confirm",
                                                    McpSchema.ElicitFormRequest.builder(
                                                                    "Proceed?",
                                                                    Map.of(
                                                                            "type",
                                                                            "object",
                                                                            "properties",
                                                                            Map.of()))
                                                            .build())
                                            .requestState("s1")
                                            .build()
                                    : McpSchema.ReadResourceResult.builder(contents(request.uri()))
                                            .ttlMs(7000)
                                            .cacheScope(CacheScope.PUBLIC)
                                            .build();
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    private static List<McpSchema.ResourceContents> contents(String uri) {
        return List.of(new McpSchema.TextResourceContents(uri, "text/plain", "hinted", null));
    }

    private static void assertHint(
            long ttlMs, String scope, Map<String, Object> result, String what) {
        assertEquals(ttlMs, ((Number) result.get("ttlMs")).longValue(), what + ": " + result);
        assertEquals(scope, result.get("cacheScope"), what + ": " + result);
    }

    private static Map<String, Object> uri(String uri) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("uri", uri);
        return params;
    }

    private static Map<String, Object> read(McpServerAdapter adapter, String uri) throws Exception {
        return result(adapter, "resources/read", uri(uri), meta());
    }

    private static Map<String, Object> result(
            McpServerAdapter adapter, String method, Map<String, Object> params) throws Exception {
        return result(adapter, method, params, meta());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(
            McpServerAdapter adapter,
            String method,
            Map<String, Object> params,
            Map<String, Object> meta)
            throws Exception {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", method);
        envelope.put("params", withMeta);
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost/mcp",
                        new StringRepresentation(
                                MAPPER.writeValueAsString(envelope), MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, MODERN);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        request.getHeaders().set("Mcp-Method", method);
        if (params.get("uri") instanceof String name) {
            request.getHeaders().set("Mcp-Name", name);
        }
        Response response = adapter.handle(request);
        Map<String, Object> answer =
                MAPPER.readValue(
                        response.getEntity().getText(), new TypeRef<Map<String, Object>>() {});
        assertTrue(answer.containsKey("result"), answer.toString());
        return (Map<String, Object>) answer.get("result");
    }

    private static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }
}
