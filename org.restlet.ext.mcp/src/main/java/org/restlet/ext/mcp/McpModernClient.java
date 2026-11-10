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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A client of MCP revision {@link McpProtocol#MODERN_VERSION}, on the SDK's modern types, over
 * HTTP: discovery, listings, tool calls, resource reads and prompts, with streamed progress, {@code
 * input_required} retries and {@code subscriptions/listen}. Each request carries the protocol
 * version and the client capabilities in its {@code _meta}, as the revision requires. Cancelling a
 * returned {@code Mono} or {@code Flux} aborts its HTTP exchange, which the server sees as a
 * cancellation.
 *
 * <p>Obtained from {@link McpClientAdapter#getModernClient()}, which owns its lifecycle.
 * Experimental: the SDK's modern API is a preview.
 */
public final class McpModernClient {
    private static final Logger LOGGER = Logger.getLogger(McpModernClient.class.getName());
    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};

    private final McpModernHttpTransport transport;
    private final McpJsonMapper mapper;
    private final Map<String, Object> capabilities;
    private final McpInputHandler inputHandler;
    private final int maxInputRounds;
    private final AtomicLong ids = new AtomicLong();

    /** The tools last listed, by name, whose {@code x-mcp-header} arguments calls mirror. */
    private final Map<String, McpSchema.Tool> tools = new ConcurrentHashMap<>();

    McpModernClient(
            McpModernHttpTransport transport,
            McpJsonMapper mapper,
            McpSchema.ClientCapabilities capabilities,
            McpInputHandler inputHandler,
            int maxInputRounds) {
        this.transport = transport;
        this.mapper = mapper;
        this.capabilities = mapper.convertValue(capabilities, MAP);
        this.inputHandler = inputHandler;
        this.maxInputRounds = maxInputRounds;
    }

    /**
     * Discovers the server: its supported versions, capabilities and instructions.
     *
     * @return The discovery result.
     */
    public Mono<McpSchema.DiscoverResult> discover() {
        return result(McpSchema.METHOD_SERVER_DISCOVER, new LinkedHashMap<>(), null, Map.of(), null)
                .map(result -> mapper.convertValue(result, McpSchema.DiscoverResult.class));
    }

    /**
     * Lists tools, and remembers their definitions: a later call mirrors the arguments their {@code
     * x-mcp-header} annotations name into {@code Mcp-Param-*} headers, as the server requires. A
     * call to an annotated tool this client has not listed is sent without them, and the server
     * refuses it with a header mismatch. Listing the first page forgets the tools listed before.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of tools.
     */
    public Mono<McpSchema.ListToolsResult> listTools(String cursor) {
        return page(McpSchema.METHOD_TOOLS_LIST, cursor, McpSchema.ListToolsResult.class)
                .doOnNext(
                        page -> {
                            if (cursor == null) {
                                tools.clear();
                            }
                            page.tools().forEach(tool -> tools.put(tool.name(), tool));
                        });
    }

    /**
     * Calls a tool; see {@link #callTool(McpSchema.CallToolRequest, Consumer)}.
     *
     * @param request The call.
     * @return The tool's result, or an {@code input_required} result.
     */
    public Mono<McpSchema.CallToolOutcome> callTool(McpSchema.CallToolRequest request) {
        return callTool(request, null);
    }

    /**
     * Calls a tool. An {@code input_required} result is answered by the input handler, and the call
     * retried, within the round limit; without a handler, or past the limit, it is returned.
     *
     * @param request The call.
     * @param progress Receives the progress notifications of the call, or null for none.
     * @return The tool's result, or an {@code input_required} result.
     */
    public Mono<McpSchema.CallToolOutcome> callTool(
            McpSchema.CallToolRequest request, Consumer<McpSchema.ProgressParams> progress) {
        Objects.requireNonNull(request, "request");
        // Worked out when subscribed, from the tools listed by then.
        return Mono.defer(
                () -> {
                    Map<String, String> headers = paramHeaders(request);
                    return withInput(
                            request.inputResponses(),
                            request.requestState(),
                            (responses, state) ->
                                    outcome(
                                            McpSchema.METHOD_TOOLS_CALL,
                                            new McpSchema.CallToolRequest(
                                                    request.name(),
                                                    request.arguments(),
                                                    responses,
                                                    state,
                                                    request.meta()),
                                            request.name(),
                                            headers,
                                            progress,
                                            McpSchema.CallToolResult.class,
                                            McpSchema.CallToolOutcome.class),
                            0);
                });
    }

    /**
     * Lists resources.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of resources.
     */
    public Mono<McpSchema.ListResourcesResult> listResources(String cursor) {
        return page(McpSchema.METHOD_RESOURCES_LIST, cursor, McpSchema.ListResourcesResult.class);
    }

    /**
     * Lists resource templates.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of resource templates.
     */
    public Mono<McpSchema.ListResourceTemplatesResult> listResourceTemplates(String cursor) {
        return page(
                McpSchema.METHOD_RESOURCES_TEMPLATES_LIST,
                cursor,
                McpSchema.ListResourceTemplatesResult.class);
    }

    /**
     * Reads a resource, answering {@code input_required} results as {@link
     * #callTool(McpSchema.CallToolRequest, Consumer)} does.
     *
     * @param request The read.
     * @param progress Receives the progress notifications of the read, or null for none.
     * @return The resource's contents, or an {@code input_required} result.
     */
    public Mono<McpSchema.ReadResourceOutcome> readResource(
            McpSchema.ReadResourceRequest request, Consumer<McpSchema.ProgressParams> progress) {
        Objects.requireNonNull(request, "request");
        return withInput(
                request.inputResponses(),
                request.requestState(),
                (responses, state) ->
                        outcome(
                                McpSchema.METHOD_RESOURCES_READ,
                                new McpSchema.ReadResourceRequest(
                                        request.uri(), responses, state, request.meta()),
                                request.uri(),
                                Map.of(),
                                progress,
                                McpSchema.ReadResourceResult.class,
                                McpSchema.ReadResourceOutcome.class),
                0);
    }

    /**
     * Lists prompts.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of prompts.
     */
    public Mono<McpSchema.ListPromptsResult> listPrompts(String cursor) {
        return page(McpSchema.METHOD_PROMPTS_LIST, cursor, McpSchema.ListPromptsResult.class);
    }

    /**
     * Gets a prompt, answering {@code input_required} results as {@link
     * #callTool(McpSchema.CallToolRequest, Consumer)} does.
     *
     * @param request The prompt and its arguments.
     * @param progress Receives the progress notifications of the request, or null for none.
     * @return The prompt, or an {@code input_required} result.
     */
    public Mono<McpSchema.GetPromptOutcome> getPrompt(
            McpSchema.GetPromptRequest request, Consumer<McpSchema.ProgressParams> progress) {
        Objects.requireNonNull(request, "request");
        return withInput(
                request.inputResponses(),
                request.requestState(),
                (responses, state) ->
                        outcome(
                                McpSchema.METHOD_PROMPTS_GET,
                                new McpSchema.GetPromptRequest(
                                        request.name(),
                                        request.arguments(),
                                        responses,
                                        state,
                                        request.meta()),
                                request.name(),
                                Map.of(),
                                progress,
                                McpSchema.GetPromptResult.class,
                                McpSchema.GetPromptOutcome.class),
                0);
    }

    /**
     * Listens to the changes a filter selects: tools, prompts and resources list changes, and the
     * updates of the resources named. The stream emits each change notification as it comes, the
     * acknowledgement excepted, and completes when the server ends it, such as when it stops.
     * Cancelling it ends the listen stream. It has no time limit. Notifications a slow subscriber
     * has not taken yet are buffered; they are small, and a server sends them only on changes.
     *
     * @param filter The changes to listen to.
     * @return The change notifications.
     */
    public Flux<JsonRpc.JSONRPCNotification> listen(McpSchema.SubscriptionFilter filter) {
        return Flux.defer(() -> listening(filter));
    }

    private Flux<JsonRpc.JSONRPCNotification> listening(McpSchema.SubscriptionFilter filter) {
        Map<String, Object> params = params(new McpSchema.SubscriptionsListenRequest(filter, null));
        JsonRpc.JSONRPCRequest rpc =
                new JsonRpc.JSONRPCRequest(
                        McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                        ids.incrementAndGet(),
                        stamp(params));
        return transport
                .send(rpc, null, Map.of(), true)
                .handle(
                        (message, sink) -> {
                            if (message instanceof JsonRpc.JSONRPCNotification notification
                                    && !McpSchema.METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED
                                            .equals(notification.method())) {
                                sink.next(notification);
                            } else if (message instanceof JsonRpc.JSONRPCResponse response
                                    && response.error() != null) {
                                sink.error(new McpException(response.error()));
                            }
                        });
    }

    /** Retries a request with the input its handler gives, within the round limit. */
    private <O extends McpSchema.Result> Mono<O> withInput(
            Map<String, Object> responses,
            String state,
            BiFunction<Map<String, Object>, String, Mono<O>> attempt,
            int round) {
        return attempt.apply(responses, state)
                .flatMap(
                        outcome -> {
                            if (!(outcome instanceof McpSchema.InputRequiredResult required)
                                    || inputHandler == null
                                    || round >= maxInputRounds) {
                                return Mono.just(outcome);
                            }
                            return inputHandler
                                    .respond(required)
                                    .map(Optional::of)
                                    .defaultIfEmpty(Optional.empty())
                                    .flatMap(
                                            next ->
                                                    next.isPresent()
                                                            ? withInput(
                                                                    next.get(),
                                                                    required.requestState(),
                                                                    attempt,
                                                                    round + 1)
                                                            : Mono.just(outcome));
                        });
    }

    /** A request whose result is complete, or input required. */
    private <O extends McpSchema.Result> Mono<O> outcome(
            String method,
            Object request,
            String name,
            Map<String, String> headers,
            Consumer<McpSchema.ProgressParams> progress,
            Class<? extends O> complete,
            Class<O> type) {
        return result(method, params(request), name, headers, progress)
                .map(
                        result ->
                                type.cast(
                                        McpSchema.ResultType.INPUT_REQUIRED.equals(
                                                        result.get("resultType"))
                                                ? mapper.convertValue(
                                                        result, McpSchema.InputRequiredResult.class)
                                                : mapper.convertValue(result, complete)));
    }

    private <R> Mono<R> page(String method, String cursor, Class<R> type) {
        return result(
                        method,
                        params(new McpSchema.PaginatedRequest(cursor, null)),
                        null,
                        Map.of(),
                        null)
                .map(result -> mapper.convertValue(result, type));
    }

    /**
     * Sends a request, passing its progress on, and returns its result; an error response fails
     * with an {@link McpException}.
     */
    private Mono<Map<String, Object>> result(
            String method,
            Map<String, Object> params,
            String name,
            Map<String, String> headers,
            Consumer<McpSchema.ProgressParams> progress) {
        return Mono.defer(() -> exchange(method, params, name, headers, progress));
    }

    private Mono<Map<String, Object>> exchange(
            String method,
            Map<String, Object> given,
            String name,
            Map<String, String> headers,
            Consumer<McpSchema.ProgressParams> progress) {
        // Each subscription sends its own request, with its own id.
        Map<String, Object> params = new LinkedHashMap<>(given);
        params.put("_meta", new LinkedHashMap<>(meta(given)));
        long id = ids.incrementAndGet();
        Object token = progress == null ? null : "restlet-" + id;
        if (token != null) {
            meta(params).put(McpSchema.MetaKeys.PROGRESS_TOKEN, token);
        }
        stamp(params);
        // The stream completes after the response: taking the last one doesn't cancel it.
        return transport
                .send(new JsonRpc.JSONRPCRequest(method, id, params), name, headers, false)
                .doOnNext(message -> progress(message, token, progress))
                .ofType(JsonRpc.JSONRPCResponse.class)
                .filter(response -> McpModernHttpTransport.sameId(response.id(), id))
                .last()
                .onErrorMap(
                        java.util.NoSuchElementException.class,
                        e -> new IOException("MCP request got no response", e))
                .switchIfEmpty(Mono.error(() -> new IOException("MCP request got no response")))
                .map(
                        response -> {
                            if (response.error() != null) {
                                throw new McpException(response.error());
                            }
                            return mapper.convertValue(response.result(), MAP);
                        });
    }

    private void progress(
            JsonRpc.JSONRPCMessage message,
            Object token,
            Consumer<McpSchema.ProgressParams> progress) {
        if (token != null
                && message instanceof JsonRpc.JSONRPCNotification notification
                && McpSchema.METHOD_NOTIFICATION_PROGRESS.equals(notification.method())) {
            try {
                McpSchema.ProgressParams params =
                        mapper.convertValue(notification.params(), McpSchema.ProgressParams.class);
                if (token.equals(params.progressToken())) {
                    progress.accept(params);
                }
            } catch (RuntimeException e) {
                // A malformed notification, or a failing consumer, doesn't fail the call.
                LOGGER.log(Level.FINE, "MCP progress notification dropped", e);
            }
        }
    }

    /** The params of a request, with a modifiable {@code _meta} merging what it had. */
    private Map<String, Object> params(Object request) {
        Map<String, Object> params = new LinkedHashMap<>(mapper.convertValue(request, MAP));
        Map<String, Object> meta = new LinkedHashMap<>();
        if (params.get("_meta") instanceof Map<?, ?> given) {
            given.forEach((key, value) -> meta.put(String.valueOf(key), value));
        }
        params.put("_meta", meta);
        return params;
    }

    /**
     * Adds what the revision requires in each request's {@code _meta}: the protocol version and the
     * client capabilities, unless the request gives them.
     */
    private Map<String, Object> stamp(Map<String, Object> params) {
        meta(params).putIfAbsent(McpSchema.MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
        meta(params).putIfAbsent(McpSchema.MetaKeys.CLIENT_CAPABILITIES, capabilities);
        return params;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> meta(Map<String, Object> params) {
        return (Map<String, Object>)
                params.computeIfAbsent("_meta", ignored -> new LinkedHashMap<String, Object>());
    }

    /**
     * The {@code Mcp-Param-*} headers of a call: the arguments the tool's valid {@code
     * x-mcp-header} annotations name, when the tool was listed.
     */
    private Map<String, String> paramHeaders(McpSchema.CallToolRequest request) {
        McpSchema.Tool tool = tools.get(request.name());
        if (tool == null || request.arguments() == null) {
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

    /** Ends the exchanges in flight and listen streams. */
    void close() throws IOException {
        transport.close();
    }
}
