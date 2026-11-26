/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

/**
 * Binds application context around each blocking handler of an {@link McpServerAdapter}, for {@link
 * McpServerAdapter#getCallScopes()}: thread-bound state such as {@code ScopedValue}s, {@code
 * ThreadLocal}s, the MDC or a tracing span, which the handler then reads on its own thread. An
 * interceptor can't do this, as the handler runs after its {@code chain.proceed} returned. The
 * scopes apply to the handlers made blocking with {@link McpBlocking}, the first scope outermost.
 * Experimental.
 */
@FunctionalInterface
public interface McpCallScope {

    /** The handler call a scope wraps. */
    @FunctionalInterface
    interface Body {
        /**
         * Runs the handler.
         *
         * @throws Exception If the handler fails.
         */
        void run() throws Exception;
    }

    /**
     * Runs the handler within this scope. A scope must run the body exactly once, on the calling
     * thread, and let its failure through.
     *
     * @param context The request's context: its transport request and response, user and
     *     attributes.
     * @param body The handler call.
     * @throws Exception If the body or the scope fails.
     */
    void run(McpRequestContext context, Body body) throws Exception;
}
