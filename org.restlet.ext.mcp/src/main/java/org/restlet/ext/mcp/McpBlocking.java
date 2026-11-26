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
import io.modelcontextprotocol.modern.server.McpAsyncNotifier;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.McpSyncNotifier;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.McpSyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpSyncResource;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.restlet.ext.mcp.internal.McpCallScopes;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Turns blocking handlers, the SDK's sync tools, resources and prompts, and blocking extension
 * methods, into the registrations of an {@link McpServerAdapter}. A blocking handler runs on a
 * virtual thread, never on a shared pool: the one serving its request, or one of its own for a
 * streaming body (so its progress is sent while it runs) and for a call an interceptor moved off a
 * virtual thread. Restlet's thread-local variables of the connector's call are set around it
 * (current request and response, context, application and virtual host; see {@link
 * McpRequestContext#withRestletLocals}), and the adapter's {@link McpServerAdapter#getCallScopes()
 * call scopes} too, so it may block on I/O and read thread-bound context. Experimental: the SDK's
 * modern API is a preview.
 */
public final class McpBlocking {

    /** Runs the blocking handlers of requests that may not block their caller's thread. */
    private static final Scheduler VIRTUAL =
            Schedulers.fromExecutorService(
                    Executors.newVirtualThreadPerTaskExecutor(), "mcp-blocking");

    private McpBlocking() {}

    /** A blocking extension method handler. */
    @FunctionalInterface
    public interface MethodHandler {
        /**
         * Answers a request; see {@link McpMethod.Handler}.
         *
         * @param context The SDK's request context.
         * @param params The request params.
         * @return The response.
         */
        McpSyncResponse<? extends McpSchema.Result> handle(
                McpRequestContext context, Map<String, Object> params);
    }

    /**
     * A tool with a blocking handler.
     *
     * @param tool The SDK's sync tool.
     * @return The tool to register.
     */
    public static McpAsyncTool tool(McpSyncTool tool) {
        Objects.requireNonNull(tool, "tool");
        return McpAsyncTool.of(
                tool.definition(),
                tool.requiredCapabilities(),
                (ctx, request) -> respond(ctx, () -> tool.handler().call(ctx, request)));
    }

    /**
     * A resource read handler that blocks, for {@link McpResource} or {@link McpResourceTemplate}.
     *
     * @param resource The SDK's sync resource.
     * @return The read handler to register.
     */
    public static McpAsyncResource resource(McpSyncResource resource) {
        Objects.requireNonNull(resource, "resource");
        return McpAsyncResource.of(
                resource.requiredCapabilities(),
                (ctx, request) -> respond(ctx, () -> resource.handler().read(ctx, request)));
    }

    /**
     * A prompt with a blocking handler.
     *
     * @param prompt The SDK's sync prompt.
     * @return The prompt to register.
     */
    public static McpAsyncPrompt prompt(McpSyncPrompt prompt) {
        Objects.requireNonNull(prompt, "prompt");
        return McpAsyncPrompt.of(
                prompt.definition(),
                prompt.requiredCapabilities(),
                (ctx, request) -> respond(ctx, () -> prompt.handler().get(ctx, request)));
    }

    /**
     * An extension method with a blocking handler, without an {@code Mcp-Name} param; see {@link
     * McpMethod#withNameParam} and {@link McpMethod#withInputRequired}.
     *
     * @param name The JSON-RPC method name.
     * @param handler The blocking handler.
     * @return The method to register.
     */
    public static McpMethod method(String name, MethodHandler handler) {
        Objects.requireNonNull(handler, "handler");
        return McpMethod.of(name, (ctx, params) -> respond(ctx, () -> handler.handle(ctx, params)));
    }

    /** Runs a blocking handler within the request's scopes, and adapts its response. */
    private static <O> Mono<McpAsyncResponse<O>> respond(
            McpRequestContext ctx, Callable<? extends McpSyncResponse<? extends O>> call) {
        return blocking(ctx, () -> McpCallScopes.call(ctx, call))
                .map(response -> async(ctx, response));
    }

    private static <O> McpAsyncResponse<O> async(
            McpRequestContext ctx, McpSyncResponse<? extends O> response) {
        if (response instanceof McpSyncResponse.Result<? extends O> result) {
            return McpAsyncResponse.result(result.result());
        }
        McpSyncResponse.Body<? extends O> body =
                ((McpSyncResponse.Streaming<? extends O>) response).body();
        // On a thread of its own: the worker forwards the stream's events while the body runs.
        return McpAsyncResponse.streaming(
                notifier -> {
                    Callable<O> run = () -> body.run(new Notifier(notifier));
                    return Mono.fromCallable(() -> McpCallScopes.call(ctx, run))
                            .subscribeOn(VIRTUAL);
                });
    }

    /**
     * Runs a blocking call: on the calling thread when the request may block it and that thread is
     * virtual, as the adapter's workers are, else on a virtual thread of its own, such as when an
     * interceptor resumed on a Reactor thread.
     */
    private static <T> Mono<T> blocking(McpRequestContext ctx, Callable<T> call) {
        return Mono.defer(
                () -> {
                    Mono<T> mono = Mono.fromCallable(call);
                    return ctx.isBlocking() && Thread.currentThread().isVirtual()
                            ? mono
                            : mono.subscribeOn(VIRTUAL);
                });
    }

    /** The SDK's sync notifier on its async one, whose sends complete once queued. */
    private record Notifier(McpAsyncNotifier delegate) implements McpSyncNotifier {
        @Override
        public void progress(double progress, Double total, String message) {
            delegate.progress(progress, total, message).block();
        }

        @Override
        public void notify(String method, Object params) {
            delegate.notify(method, params).block();
        }
    }
}
