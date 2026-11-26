/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.restlet.ext.mcp.McpCallScope;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpRequestContext;

/**
 * Runs a blocking handler with Restlet's current response set and the adapter's call scopes around
 * it. Internal to the MCP extension.
 */
public final class McpCallScopes {

    /** The SDK transport context key of the adapter's call scopes. */
    public static final String SCOPES_KEY = "org.restlet.ext.mcp.callScopes";

    private McpCallScopes() {}

    /**
     * The SDK transport context entries carrying the adapter's call scopes.
     *
     * @param scopes The scopes, first outermost.
     * @return The entries.
     */
    public static Map<String, Object> of(List<McpCallScope> scopes) {
        return scopes.isEmpty() ? Map.of() : Map.of(SCOPES_KEY, List.copyOf(scopes));
    }

    /**
     * Calls a blocking handler within its request's scopes.
     *
     * @param ctx The SDK's request context.
     * @param call The handler call.
     * @param <T> The handler's result.
     * @return The handler's result.
     * @throws Exception If the handler or a scope fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T call(
            io.modelcontextprotocol.modern.server.McpRequestContext ctx, Callable<T> call)
            throws Exception {
        McpRequestContext context =
                ctx.transportContext().get(McpProtocol.CONTEXT_KEY)
                                instanceof McpRequestContext restlet
                        ? restlet
                        : new McpRequestContext(null);
        List<McpCallScope> scopes =
                ctx.transportContext().get(SCOPES_KEY) instanceof List<?> list
                        ? (List<McpCallScope>) list
                        : List.of();
        return context.withRestletLocals(() -> run(scopes, 0, context, call));
    }

    private static <T> T run(
            List<McpCallScope> scopes, int index, McpRequestContext context, Callable<T> call)
            throws Exception {
        if (index == scopes.size()) {
            return call.call();
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        AtomicBoolean ran = new AtomicBoolean();
        scopes.get(index)
                .run(
                        context,
                        () -> {
                            if (!ran.compareAndSet(false, true)) {
                                throw new IllegalStateException(
                                        "An MCP call scope ran the handler twice");
                            }
                            try {
                                result.set(run(scopes, index + 1, context, call));
                            } catch (Exception e) {
                                failure.set(e);
                                throw e;
                            }
                        });
        if (!ran.get()) {
            throw new IllegalStateException("An MCP call scope didn't run the handler");
        }
        if (failure.get() != null) {
            // A scope that swallowed the handler's failure must not turn it into no result.
            throw failure.get();
        }
        return result.get();
    }
}
