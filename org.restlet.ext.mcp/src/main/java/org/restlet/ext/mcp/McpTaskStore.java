/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * The application's tasks of the MCP Tasks extension, behind {@code tasks/get}, {@code
 * tasks/update} and {@code tasks/cancel}, for {@link McpServerAdapter#setTaskStore}. The tasks and
 * their execution stay the application's, so a durable engine can back them; the adapter serves the
 * methods, checks their params and {@code Mcp-Name} header, and advertises the extension.
 *
 * <p>A task belongs to the principal that created it, {@link McpRequestContext#principal()} of the
 * {@code tools/call} that returned it. Every method gets the context of the caller and must treat a
 * task of another principal as unknown, so that its existence never leaks. Experimental.
 */
public interface McpTaskStore {

    /**
     * The state of a task.
     *
     * @param taskId The task identifier.
     * @param context The caller's context, with its principal.
     * @return The state, or empty for an unknown task or one of another principal.
     */
    Mono<McpTask> get(String taskId, McpRequestContext context);

    /**
     * Delivers answers to the questions an {@code input_required} task waits on. Answers to other
     * keys, or to a task that isn't waiting, are ignored.
     *
     * @param taskId The task identifier.
     * @param inputResponses The answers, keyed as the task's {@code inputRequests}.
     * @param context The caller's context, with its principal.
     * @return True, or false for an unknown task or one of another principal.
     */
    Mono<Boolean> update(
            String taskId, Map<String, Object> inputResponses, McpRequestContext context);

    /**
     * Asks a task to stop. Cancellation is cooperative: the task may still end otherwise.
     *
     * @param taskId The task identifier.
     * @param context The caller's context, with its principal.
     * @return True, or false for an unknown task or one of another principal.
     */
    Mono<Boolean> cancel(String taskId, McpRequestContext context);
}
