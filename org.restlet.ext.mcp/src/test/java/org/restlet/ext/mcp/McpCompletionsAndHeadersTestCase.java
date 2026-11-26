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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpSyncPrompt;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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

/**
 * What MCP Java SDK 2.2.0 brings to {@link McpServerAdapter}: completions of prompt arguments and
 * resource template variables, and the refusal of repeated {@code Mcp-Method} and {@code Mcp-Name}
 * headers.
 */
class McpCompletionsAndHeadersTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** A prompt completes its arguments, async or blocking, and discover advertises it. */
    @Test
    void promptsCompleteTheirArguments() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getPrompts()
                .add(
                        McpAsyncPrompt.of(
                                        McpSchema.Prompt.builder("greet").arguments(who()).build(),
                                        (ctx, request) -> Mono.error(new IllegalStateException()))
                                .withCompletionHandler(
                                        (ctx, request) ->
                                                Mono.just(
                                                        McpSchema.CompleteResult.of(
                                                                new McpSchema.CompleteResult
                                                                        .Completion(
                                                                        List.of(
                                                                                request.argument()
                                                                                                .value()
                                                                                        + "lice"),
                                                                        1,
                                                                        false)))));
        adapter.getPrompts()
                .add(
                        McpBlocking.prompt(
                                McpSyncPrompt.of(
                                                McpSchema.Prompt.builder("farewell")
                                                        .arguments(who())
                                                        .build(),
                                                (ctx, request) -> {
                                                    throw new IllegalStateException();
                                                })
                                        .withCompletionHandler(
                                                (ctx, request) ->
                                                        McpSchema.CompleteResult.of(
                                                                new McpSchema.CompleteResult
                                                                        .Completion(
                                                                        List.of("bob"),
                                                                        1,
                                                                        false)))));
        adapter.start();

        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        assertTrue(
                ((Map<?, ?>) discover.get("capabilities")).containsKey("completions"),
                discover.toString());
        assertEquals(List.of("alice"), values(complete(adapter, prompt("greet"), "who", "a")));
        assertEquals(List.of("bob"), values(complete(adapter, prompt("farewell"), "who", "b")));
    }

    /** A resource template completes its variables. */
    @Test
    void resourceTemplatesCompleteTheirVariables() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getResourceTemplates()
                .add(
                        new McpResourceTemplate(
                                        McpSchema.ResourceTemplate.builder(
                                                        "test://items/{id}", "items")
                                                .build(),
                                        McpAsyncResource.of(
                                                (ctx, request) ->
                                                        Mono.error(new IllegalStateException())))
                                .withCompletionHandler(
                                        McpBlocking.completion(
                                                (ctx, request) ->
                                                        McpSchema.CompleteResult.of(
                                                                new McpSchema.CompleteResult
                                                                        .Completion(
                                                                        List.of(
                                                                                request.argument()
                                                                                                .value()
                                                                                        + "7"),
                                                                        1,
                                                                        false)))));
        adapter.start();

        assertEquals(
                List.of("item-7"),
                values(
                        complete(
                                adapter,
                                Map.of("type", "ref/resource", "uri", "test://items/{id}"),
                                "id",
                                "item-")));
    }

    /** Without a completion handler, nothing completes and none is advertised. */
    @Test
    void withoutHandlersNothingCompletes() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getPrompts()
                .add(
                        McpAsyncPrompt.of(
                                McpSchema.Prompt.builder("plain").build(),
                                (ctx, request) -> Mono.error(new IllegalStateException())));
        adapter.start();

        Map<String, Object> discover = result(call(adapter, "server/discover", Map.of(), null));
        assertTrue(
                !((Map<?, ?>) discover.get("capabilities")).containsKey("completions"),
                discover.toString());
    }

    /** A request sending Mcp-Method or Mcp-Name more than once is refused, as the SDK does. */
    @Test
    void repeatedMcpHeadersAreRefused() throws Exception {
        McpServerAdapter adapter = adapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.start();

        Request twiceMethod =
                modern("tools/call", Map.of("name", "echo", "arguments", Map.of()), "echo");
        twiceMethod.getHeaders().add("Mcp-Method", "tools/call");
        assertEquals(
                "Mcp-Method header has more than one value", error(adapter.handle(twiceMethod)));

        Request twiceName =
                modern("tools/call", Map.of("name", "echo", "arguments", Map.of()), "echo");
        twiceName.getHeaders().add("Mcp-Name", "other");
        assertEquals("Mcp-Name header has more than one value", error(adapter.handle(twiceName)));
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        return adapter;
    }

    private static Map<String, Object> prompt(String name) {
        return Map.of("type", "ref/prompt", "name", name);
    }

    private static Map<String, Object> complete(
            McpServerAdapter adapter, Map<String, Object> ref, String argument, String value)
            throws Exception {
        return result(
                call(
                        adapter,
                        "completion/complete",
                        Map.of("ref", ref, "argument", Map.of("name", argument, "value", value)),
                        null));
    }

    private static List<McpSchema.PromptArgument> who() {
        return List.of(new McpSchema.PromptArgument("who", null, "Who", false));
    }

    private static List<?> values(Map<String, Object> result) {
        return (List<?>) ((Map<?, ?>) result.get("completion")).get("values");
    }

    private static Response call(
            McpServerAdapter adapter, String method, Map<String, Object> params, String name) {
        return adapter.handle(modern(method, params, name));
    }

    private static Request modern(String method, Map<String, Object> params, String name) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
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

    private static Map<String, Object> envelope(Response response) throws Exception {
        var bytes = new ByteArrayOutputStream();
        if (response.getEntity() != null) {
            response.getEntity().write(bytes);
        }
        return MAPPER.readValue(
                bytes.toString(StandardCharsets.UTF_8), new TypeRef<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(Response response) throws Exception {
        Map<String, Object> envelope = envelope(response);
        assertTrue(envelope.containsKey("result"), envelope.toString());
        return (Map<String, Object>) envelope.get("result");
    }

    private static String error(Response response) throws Exception {
        Map<?, ?> error = (Map<?, ?>) envelope(response).get("error");
        assertEquals(-32020, ((Number) error.get("code")).intValue(), String.valueOf(error));
        return (String) error.get("message");
    }
}
