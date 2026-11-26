/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * A JSON-RPC method an MCP extension adds, for {@link McpServerAdapter#getMethods()}, such as
 * {@code skills/list} or {@code tasks/get}. It goes through the same pipeline as the methods the
 * adapter serves itself: interceptors, the {@code _meta} version and client capability checks, the
 * sealed {@code requestState} of {@code input_required} results, the error mapping and the HTTP
 * status. Only {@code 2026-07-28} clients reach it: the legacy edge answers it -32601.
 *
 * <p>Registering a method doesn't advertise its extension; see {@link
 * McpServerAdapter#getExtensions()}. Experimental: the SDK's modern API is a preview.
 *
 * @param name The JSON-RPC method name.
 * @param nameParam The params field the {@code Mcp-Name} header must mirror, or null when the
 *     method takes no {@code Mcp-Name} header.
 * @param inputRequired Whether the method may answer {@code input_required} and accept its {@code
 *     requestState} on retry.
 * @param handler The handler.
 */
public record McpMethod(String name, String nameParam, boolean inputRequired, Handler handler) {

    /** Answers the requests of an extension method. */
    @FunctionalInterface
    public interface Handler {
        /**
         * Answers a request. An {@link io.modelcontextprotocol.modern.McpException} is answered
         * with its error; any other error is answered with an internal error.
         *
         * @param context The SDK's request context; the adapter's {@link McpRequestContext} is its
         *     transport context's {@link McpProtocol#CONTEXT_KEY} entry.
         * @param params The request params, {@code _meta} included, with an opened {@code
         *     requestState} on a retry of an input-required method.
         * @return The response, a result or a stream ending in one.
         */
        Mono<? extends McpAsyncResponse<? extends McpSchema.Result>> handle(
                io.modelcontextprotocol.modern.server.McpRequestContext context,
                Map<String, Object> params);
    }

    /**
     * Creates a method.
     *
     * @param name The JSON-RPC method name.
     * @param nameParam The params field the {@code Mcp-Name} header must mirror, or null.
     * @param inputRequired Whether the method may answer {@code input_required}.
     * @param handler The handler.
     */
    public McpMethod {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(handler, "handler");
        if (name.isBlank()) {
            throw new IllegalArgumentException("An MCP method name must not be blank");
        }
        if (nameParam != null && nameParam.isBlank()) {
            throw new IllegalArgumentException("An MCP name param must not be blank");
        }
    }

    /**
     * Creates a method without an {@code Mcp-Name} header, which doesn't answer {@code
     * input_required}.
     *
     * @param name The JSON-RPC method name.
     * @param handler The handler.
     * @return The method.
     */
    public static McpMethod of(String name, Handler handler) {
        return new McpMethod(name, null, false, handler);
    }

    /**
     * Returns this method with the params field the {@code Mcp-Name} header must mirror, such as
     * {@code taskId} for {@code tasks/get}.
     *
     * @param value The params field.
     * @return A copy of this method.
     */
    public McpMethod withNameParam(String value) {
        return new McpMethod(
                name, Objects.requireNonNull(value, "nameParam"), inputRequired, handler);
    }

    /**
     * Returns this method, answering {@code input_required} when {@code value} is true.
     *
     * @param value Whether the method may answer {@code input_required}.
     * @return A copy of this method.
     */
    public McpMethod withInputRequired(boolean value) {
        return new McpMethod(name, nameParam, value, handler);
    }
}
