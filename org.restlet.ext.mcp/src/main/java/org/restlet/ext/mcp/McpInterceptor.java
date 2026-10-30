/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/** MCP-native envelope interception around the single SDK dispatcher. Notifications stay empty. */
@FunctionalInterface
public interface McpInterceptor {
    Mono<McpSchema.JSONRPCResponse> intercept(
            McpSchema.JSONRPCMessage message, McpRequestContext context, Chain chain);

    @FunctionalInterface
    interface Chain {
        Mono<McpSchema.JSONRPCResponse> proceed(
                McpSchema.JSONRPCMessage message, McpRequestContext context);
    }
}
