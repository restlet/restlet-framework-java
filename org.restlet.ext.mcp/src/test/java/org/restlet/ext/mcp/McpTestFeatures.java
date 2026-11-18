/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/** Registrations and requests shared by the tests, on the SDK's modern types. */
final class McpTestFeatures {

    private McpTestFeatures() {}

    /** A tool answering {@code echo:<text>}. */
    static McpAsyncTool echo() {
        return McpAsyncTool.of(
                McpSchema.Tool.builder(
                                "echo",
                                Map.of(
                                        "type",
                                        "object",
                                        "properties",
                                        Map.of("text", Map.of("type", "string"))))
                        .description("Echoes text")
                        .build(),
                (ctx, request) ->
                        Mono.just(
                                McpAsyncResponse.<McpSchema.CallToolOutcome>result(
                                        text(
                                                "echo:"
                                                        + (request.arguments() == null
                                                                ? null
                                                                : request.arguments()
                                                                        .get("text"))))));
    }

    /** A tool result with one text content. */
    static McpSchema.CallToolResult text(String text) {
        return McpSchema.CallToolResult.builder()
                .addContent(McpSchema.TextContent.builder(text).build())
                .build();
    }

    /** A resource reading {@code text} at its URI. */
    static McpResource resource(String uri, String text) {
        return new McpResource(McpSchema.Resource.builder(uri, uri).build(), read(text));
    }

    /** A resource template reading {@code text} at each URI it matches. */
    static McpResourceTemplate template(String uriTemplate, String text) {
        return new McpResourceTemplate(
                McpSchema.ResourceTemplate.builder(uriTemplate, uriTemplate).build(), read(text));
    }

    private static McpAsyncResource read(String text) {
        return McpAsyncResource.of(
                (ctx, request) -> {
                    McpSchema.ReadResourceOutcome outcome =
                            McpSchema.ReadResourceResult.builder(
                                            List.of(
                                                    new McpSchema.TextResourceContents(
                                                            request.uri(),
                                                            "text/plain",
                                                            text,
                                                            null)))
                                    .build();
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    /** A prompt described {@code description}, with one user message {@code text}. */
    static McpAsyncPrompt prompt(String name, String description, String text) {
        return McpAsyncPrompt.of(
                McpSchema.Prompt.builder(name).build(),
                (ctx, request) -> {
                    McpSchema.GetPromptOutcome outcome =
                            new McpSchema.GetPromptResult(
                                    description,
                                    List.of(
                                            new McpSchema.PromptMessage(
                                                    McpSchema.Role.USER,
                                                    McpSchema.TextContent.builder(text).build())),
                                    null,
                                    null);
                    return Mono.just(McpAsyncResponse.result(outcome));
                });
    }

    /** The {@code _meta} a 2026-07-28 request requires, without client capabilities. */
    static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(McpSchema.MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
        meta.put(McpSchema.MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }

    /** A 2026-07-28 request, with the {@code _meta} the revision requires. */
    static JsonRpc.JSONRPCRequest request(String method, Object id, Map<String, Object> params) {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta());
        return new JsonRpc.JSONRPCRequest(method, id, withMeta);
    }
}
