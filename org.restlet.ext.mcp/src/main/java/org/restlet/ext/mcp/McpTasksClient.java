/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.ToolParamHeaderAnnotations;
import io.modelcontextprotocol.modern.client.McpAsyncClient;
import io.modelcontextprotocol.modern.client.McpAsyncElicitationHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The client side of the Tasks extension ({@link McpTask#EXTENSION_ID}) of MCP revision {@link
 * McpProtocol#MODERN_VERSION}: tool calls the server may run as tasks, and {@code tasks/get},
 * {@code tasks/update} and {@code tasks/cancel}. The SDK's client, {@link
 * McpClientAdapter#getModernClient()}, serves the rest of the revision; it can't declare the
 * extension, which this client declares on each request, with the elicitation capabilities of the
 * adapter's handlers.
 *
 * <p>The questions of an {@code input_required} result or task go to the adapter's form and URL
 * elicitation handlers, and the call is retried, or the task updated, with their answers, within
 * the round limit. A question no handler covers, a handler completing empty, or the round limit
 * hands the result or task back to the caller as it is.
 *
 * <p>Obtained from {@link McpClientAdapter#getTasksClient()}, which owns its lifecycle.
 * Experimental: the SDK's modern API and the Tasks extension are previews.
 */
public final class McpTasksClient {
    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};
    private static final String META = "_meta";
    private static final String TASK_ID = "taskId";

    /** The shortest wait between two polls of a task, in milliseconds. */
    public static final long MIN_POLL_MS = 100;

    /** The longest wait between two polls of a task, in milliseconds. */
    public static final long MAX_POLL_MS = 30_000;

    private static final long DEFAULT_POLL_MS = 1_000;

    private final McpModernHttpTransport transport;
    private final McpJsonMapper mapper;
    private final Map<String, Object> capabilities;
    private final McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> formElicitation;
    private final McpAsyncElicitationHandler<McpSchema.ElicitUrlRequest> urlElicitation;
    private final int maxInputRounds;
    private final AtomicLong ids = new AtomicLong();

    McpTasksClient(
            McpModernHttpTransport transport,
            McpJsonMapper mapper,
            McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> formElicitation,
            McpAsyncElicitationHandler<McpSchema.ElicitUrlRequest> urlElicitation,
            int maxInputRounds) {
        this.transport = transport;
        this.mapper = mapper;
        this.formElicitation = formElicitation;
        this.urlElicitation = urlElicitation;
        this.maxInputRounds = maxInputRounds;
        McpSchema.ClientCapabilities.Builder declared =
                McpSchema.ClientCapabilities.builder()
                        .extensions(Map.of(McpTask.EXTENSION_ID, Map.of()));
        if (formElicitation != null || urlElicitation != null) {
            declared.elicitation(
                    new McpSchema.ClientCapabilities.Elicitation(
                            formElicitation == null
                                    ? null
                                    : new McpSchema.ClientCapabilities.Elicitation.Form(),
                            urlElicitation == null
                                    ? null
                                    : new McpSchema.ClientCapabilities.Elicitation.Url()));
        }
        this.capabilities = mapper.convertValue(declared.build(), MAP);
    }

    /**
     * Calls a tool, which the server may run as a task: an {@link McpTaskResult} is returned then,
     * for {@link #awaitTask} or the caller's own polling. The arguments the tool's {@code
     * x-mcp-header} annotations name are mirrored into {@code Mcp-Param-*} headers, as the server
     * requires, from the definition given, such as one listed with {@link
     * McpAsyncClient#listTools()}.
     *
     * @param request The call.
     * @param tool The tool's definition, or null for a tool without {@code x-mcp-header}
     *     annotations.
     * @return The tool's result, an {@code input_required} result left unanswered, or a task.
     */
    public Mono<McpSchema.CallToolOutcome> callTool(
            McpSchema.CallToolRequest request, McpSchema.Tool tool) {
        Objects.requireNonNull(request, "request");
        return Mono.defer(() -> call(request, paramHeaders(request, tool), 0));
    }

    /** One round of a call, then the next with the answers to its questions, if any. */
    private Mono<McpSchema.CallToolOutcome> call(
            McpSchema.CallToolRequest request, Map<String, String> headers, int round) {
        return result(McpSchema.METHOD_TOOLS_CALL, params(request), request.name(), headers)
                .map(this::callOutcome)
                .flatMap(
                        outcome -> {
                            if (!(outcome instanceof McpSchema.InputRequiredResult required)
                                    || round >= maxInputRounds) {
                                return Mono.just(outcome);
                            }
                            return answers(required.inputRequests())
                                    .map(Optional::of)
                                    .defaultIfEmpty(Optional.empty())
                                    .flatMap(
                                            answers ->
                                                    answers.isEmpty()
                                                            ? Mono.just(outcome)
                                                            : call(
                                                                    new McpSchema.CallToolRequest(
                                                                            request.name(),
                                                                            request.arguments(),
                                                                            answers.get(),
                                                                            required.requestState(),
                                                                            request.meta()),
                                                                    headers,
                                                                    round + 1));
                        });
    }

    private McpSchema.CallToolOutcome callOutcome(Map<String, Object> result) {
        Object type = result.get("resultType");
        if (McpSchema.ResultType.INPUT_REQUIRED.equals(type)) {
            return mapper.convertValue(result, McpSchema.InputRequiredResult.class);
        }
        if (McpTaskResult.RESULT_TYPE.equals(type)) {
            return new McpTaskResult(McpTask.fromFields(result));
        }
        return mapper.convertValue(result, McpSchema.CallToolResult.class);
    }

    /**
     * Gets the state of a task, with {@code tasks/get}.
     *
     * @param taskId The task identifier.
     * @return The state.
     */
    public Mono<McpTask> getTask(String taskId) {
        Objects.requireNonNull(taskId, TASK_ID);
        return result("tasks/get", taskParams(taskId), taskId, Map.of()).map(McpTask::fromFields);
    }

    /**
     * Answers the questions a task waits on, with {@code tasks/update}.
     *
     * @param taskId The task identifier.
     * @param inputResponses The answers, keyed as the task's {@code inputRequests}.
     * @return Completes once the server took them.
     */
    public Mono<Void> updateTask(String taskId, Map<String, Object> inputResponses) {
        Objects.requireNonNull(taskId, TASK_ID);
        Objects.requireNonNull(inputResponses, "inputResponses");
        Map<String, Object> params = taskParams(taskId);
        params.put("inputResponses", inputResponses);
        return result("tasks/update", params, taskId, Map.of()).then();
    }

    /**
     * Asks a task to stop, with {@code tasks/cancel}. Cancellation is cooperative.
     *
     * @param taskId The task identifier.
     * @return Completes once the server took the request.
     */
    public Mono<Void> cancelTask(String taskId) {
        Objects.requireNonNull(taskId, TASK_ID);
        return result("tasks/cancel", taskParams(taskId), taskId, Map.of()).then();
    }

    /**
     * Waits for a task to end, polling {@code tasks/get} at its {@code pollIntervalMs}, bounded to
     * between {@value #MIN_POLL_MS} ms and {@value #MAX_POLL_MS} ms, one second by default. The
     * questions of an {@code input_required} task go to the elicitation handlers, and their answers
     * to {@code tasks/update}, within the round limit; otherwise the task is returned as it is.
     * Cancelling the returned {@code Mono} stops the polling, not the task: see {@link
     * #cancelTask}.
     *
     * @param task The task, as {@code tools/call} answered it.
     * @return The tool's result once completed, or the task still waiting on input. A failed task
     *     fails with an {@link McpException} carrying its error; a cancelled one with an internal
     *     error.
     */
    public Mono<McpSchema.CallToolOutcome> awaitTask(McpTaskResult task) {
        Objects.requireNonNull(task, "task");
        return awaitTask(task, MIN_POLL_MS);
    }

    /**
     * Waits for a task, polling no more often than {@code minPollMs}: one loop step per poll, so a
     * long task grows neither the stack nor the chain.
     */
    Mono<McpSchema.CallToolOutcome> awaitTask(McpTaskResult task, long minPollMs) {
        return Mono.just(new Step(task.task(), 0, false))
                .expand(step -> next(step, minPollMs))
                .last()
                .flatMap(this::ending);
    }

    /** A state of a task being waited for, and whether the wait stops at it. */
    private record Step(McpTask task, int round, boolean stop) {}

    /** The step after this one, or none once the task ended or the wait stops. */
    private Mono<Step> next(Step step, long minPollMs) {
        McpTask task = step.task();
        if (step.stop() || task.status().isTerminal()) {
            return Mono.empty();
        }
        if (task.status() == McpTask.Status.WORKING) {
            return poll(task, minPollMs).map(next -> new Step(next, step.round(), false));
        }
        if (step.round() >= maxInputRounds) {
            return Mono.empty();
        }
        Map<String, Object> asked = new LinkedHashMap<>();
        asked.put("resultType", McpSchema.ResultType.INPUT_REQUIRED);
        asked.put("inputRequests", task.inputRequests());
        return answers(
                        mapper.convertValue(asked, McpSchema.InputRequiredResult.class)
                                .inputRequests())
                .flatMap(
                        answers ->
                                updateTask(task.taskId(), answers)
                                        .then(poll(task, minPollMs))
                                        .map(next -> new Step(next, step.round() + 1, false)))
                // No answers: the task is handed back as it waits.
                .defaultIfEmpty(new Step(task, step.round(), true));
    }

    /** What the wait ends with at its last step. */
    private Mono<McpSchema.CallToolOutcome> ending(Step step) {
        McpTask task = step.task();
        return switch (task.status()) {
            case COMPLETED ->
                    Mono.just(mapper.convertValue(task.result(), McpSchema.CallToolResult.class));
            case FAILED ->
                    Mono.error(
                            new McpException(
                                    mapper.convertValue(
                                            task.error(),
                                            JsonRpc.JSONRPCResponse.JSONRPCError.class)));
            case CANCELLED ->
                    Mono.error(
                            new McpException(
                                    McpSchema.ErrorCodes.INTERNAL_ERROR,
                                    "MCP task cancelled: " + task.taskId()));
            case WORKING, INPUT_REQUIRED -> Mono.just(new McpTaskResult(task));
        };
    }

    /** The task's next state, after its poll interval. */
    private Mono<McpTask> poll(McpTask task, long minPollMs) {
        long interval =
                task.pollIntervalMs() == null
                        ? DEFAULT_POLL_MS
                        : Math.clamp(task.pollIntervalMs(), minPollMs, MAX_POLL_MS);
        return Mono.delay(Duration.ofMillis(interval)).then(getTask(task.taskId()));
    }

    private static Map<String, Object> taskParams(String taskId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(TASK_ID, taskId);
        return params;
    }

    /**
     * The handlers' answers to questions, by their keys; empty when a question has no handler or a
     * handler gives no answer.
     */
    private Mono<Map<String, Object>> answers(Map<String, McpSchema.InputRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            return Mono.empty();
        }
        List<Mono<Map.Entry<String, Object>>> answering = new java.util.ArrayList<>();
        for (Map.Entry<String, McpSchema.InputRequest> question : questions.entrySet()) {
            Mono<McpSchema.ElicitResult> answer = answer(question.getValue());
            if (answer == null) {
                return Mono.empty();
            }
            answering.add(answer.map(result -> Map.entry(question.getKey(), result)));
        }
        return Flux.concat(answering)
                .collectList()
                .flatMap(
                        answered -> {
                            if (answered.size() < answering.size()) {
                                return Mono.empty();
                            }
                            Map<String, Object> responses = new LinkedHashMap<>();
                            answered.forEach(
                                    entry ->
                                            responses.put(
                                                    entry.getKey(),
                                                    mapper.convertValue(entry.getValue(), MAP)));
                            return Mono.just(responses);
                        });
    }

    /** The answer of the handler covering a question, or null if none does. */
    private Mono<McpSchema.ElicitResult> answer(McpSchema.InputRequest question) {
        if (question instanceof McpSchema.ElicitInputRequest elicit) {
            if (elicit.params() instanceof McpSchema.ElicitFormRequest form
                    && formElicitation != null) {
                return Mono.defer(() -> formElicitation.elicit(form));
            }
            if (elicit.params() instanceof McpSchema.ElicitUrlRequest url
                    && urlElicitation != null) {
                return Mono.defer(() -> urlElicitation.elicit(url));
            }
        }
        return null;
    }

    /**
     * Sends a request and returns its result; an error response fails with an {@link McpException}.
     * Each subscription sends its own request, with its own id.
     */
    private Mono<Map<String, Object>> result(
            String method, Map<String, Object> given, String name, Map<String, String> headers) {
        return Mono.defer(
                () -> {
                    Map<String, Object> params = new LinkedHashMap<>(given);
                    Map<String, Object> meta = new LinkedHashMap<>();
                    if (given.get(META) instanceof Map<?, ?> map) {
                        map.forEach((key, value) -> meta.put(String.valueOf(key), value));
                    }
                    meta.putIfAbsent(
                            McpSchema.MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
                    meta.putIfAbsent(McpSchema.MetaKeys.CLIENT_CAPABILITIES, capabilities);
                    params.put(META, meta);
                    long id = ids.incrementAndGet();
                    // The stream completes after the response: taking the last one doesn't
                    // cancel it.
                    return transport
                            .send(
                                    new JsonRpc.JSONRPCRequest(method, id, params),
                                    McpProtocol.MODERN_VERSION,
                                    name,
                                    headers,
                                    false)
                            .ofType(JsonRpc.JSONRPCResponse.class)
                            .filter(response -> McpModernHttpTransport.sameId(response.id(), id))
                            .next()
                            .switchIfEmpty(
                                    Mono.error(
                                            () -> new IOException("MCP request got no response")))
                            .map(
                                    response -> {
                                        if (response.error() != null) {
                                            throw new McpException(response.error());
                                        }
                                        return mapper.convertValue(response.result(), MAP);
                                    });
                });
    }

    /** The params of a request, as a modifiable map. */
    private Map<String, Object> params(Object request) {
        return new LinkedHashMap<>(mapper.convertValue(request, MAP));
    }

    /** The {@code Mcp-Param-*} headers of a call: the arguments the tool's annotations name. */
    private Map<String, String> paramHeaders(
            McpSchema.CallToolRequest request, McpSchema.Tool tool) {
        if (tool == null || request.arguments() == null || !tool.name().equals(request.name())) {
            return Map.of();
        }
        // The arguments as the server sees them, in JSON: a UUID or an enum is a string.
        Map<String, Object> arguments = mapper.convertValue(request.arguments(), MAP);
        Map<String, String> headers = new LinkedHashMap<>();
        for (ToolParamHeaderAnnotations.Annotation annotation :
                ToolParamHeaderAnnotations.parse(tool).valid()) {
            String value = headerValue(argument(arguments, annotation.path()));
            if (value != null) {
                headers.put(annotation.name(), value);
            }
        }
        return headers;
    }

    /**
     * An argument as a header value: a number in plain decimal notation, such as 10000000 rather
     * than 1.0E7, as the server expects; a string or boolean as is; else null.
     */
    private static String headerValue(Object value) {
        return switch (value) {
            case String text -> text;
            case Boolean flag -> flag.toString();
            case Number number -> {
                try {
                    yield new BigDecimal(number.toString()).toPlainString();
                } catch (NumberFormatException _) {
                    yield null; // NaN or infinite: not a JSON number.
                }
            }
            case null, default -> null;
        };
    }

    private static Object argument(Map<String, Object> arguments, List<String> path) {
        Object value = arguments;
        for (String step : path) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(step);
        }
        return value;
    }
}
