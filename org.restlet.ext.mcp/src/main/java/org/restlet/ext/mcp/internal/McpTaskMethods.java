/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import java.util.List;
import java.util.Map;
import org.restlet.ext.mcp.McpJsonResult;
import org.restlet.ext.mcp.McpMethod;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpRequestContext;
import org.restlet.ext.mcp.McpTaskStore;
import reactor.core.publisher.Mono;

/**
 * The {@code tasks/get}, {@code tasks/update} and {@code tasks/cancel} methods of the MCP Tasks
 * extension, on the application's store. Internal to the MCP extension.
 */
public final class McpTaskMethods {

    /** The params field the {@code Mcp-Name} header of the task methods mirrors. */
    public static final String TASK_ID = "taskId";

    private McpTaskMethods() {}

    /**
     * The task methods on a store.
     *
     * @param store The application's tasks.
     * @return The methods.
     */
    public static List<McpMethod> of(McpTaskStore store) {
        return List.of(
                method(
                        "tasks/get",
                        (taskId, params, context) ->
                                store.get(taskId, context)
                                        .switchIfEmpty(Mono.error(notFound()))
                                        .map(task -> McpJsonResult.complete(task.toFields()))),
                method(
                        "tasks/update",
                        (taskId, params, context) -> {
                            Object responses = params.get("inputResponses");
                            if (!(responses instanceof Map<?, ?> answers)) {
                                return Mono.error(
                                        McpException.invalidParams(
                                                "inputResponses must be an object"));
                            }
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typed = (Map<String, Object>) answers;
                            return acknowledged(store.update(taskId, typed, context));
                        }),
                method(
                        "tasks/cancel",
                        (taskId, params, context) -> acknowledged(store.cancel(taskId, context))));
    }

    /** Answers a task method once its task id is checked. */
    @FunctionalInterface
    private interface TaskHandler {
        Mono<McpJsonResult> handle(
                String taskId, Map<String, Object> params, McpRequestContext context);
    }

    private static McpMethod method(String name, TaskHandler handler) {
        return McpMethod.of(
                        name,
                        (ctx, params) -> {
                            if (!(params.get(TASK_ID) instanceof String taskId)
                                    || taskId.isBlank()) {
                                return Mono.error(McpException.invalidParams("taskId is required"));
                            }
                            McpRequestContext context =
                                    ctx.transportContext().get(McpProtocol.CONTEXT_KEY)
                                                    instanceof McpRequestContext restlet
                                            ? restlet
                                            : new McpRequestContext(null);
                            return handler.handle(taskId, params, context)
                                    .map(McpAsyncResponse::result);
                        })
                .withNameParam(TASK_ID);
    }

    /** An empty complete result, or "Task not found" when the store didn't know the task. */
    private static Mono<McpJsonResult> acknowledged(Mono<Boolean> known) {
        return known.defaultIfEmpty(Boolean.FALSE)
                .flatMap(
                        found ->
                                found
                                        ? Mono.just(McpJsonResult.complete(Map.of()))
                                        : Mono.error(notFound()));
    }

    private static McpException notFound() {
        return McpException.invalidParams("Task not found");
    }
}
