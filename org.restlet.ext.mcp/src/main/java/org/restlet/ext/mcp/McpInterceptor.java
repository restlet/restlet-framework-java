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
import reactor.core.publisher.Mono;

/**
 * MCP-native envelope interception around the server, on the SDK's {@code 2026-07-28} message
 * types. Requests of legacy clients are translated to those types before the interceptors run, so
 * one interceptor serves both eras; {@link McpRequestContext#protocolVersion()} tells which one a
 * request came in. Notifications stay empty.
 */
@FunctionalInterface
public interface McpInterceptor {
    Mono<JsonRpc.JSONRPCResponse> intercept(
            JsonRpc.JSONRPCMessage message, McpRequestContext context, Chain chain);

    @FunctionalInterface
    interface Chain {
        Mono<JsonRpc.JSONRPCResponse> proceed(
                JsonRpc.JSONRPCMessage message, McpRequestContext context);
    }
}
