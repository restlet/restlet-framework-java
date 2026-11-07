/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema.ErrorCodes;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.server.McpParamHeaders;
import io.modelcontextprotocol.modern.server.McpServer;
import io.modelcontextprotocol.modern.server.McpTransportResponse;
import io.modelcontextprotocol.modern.server.transport.McpHeaderDecoder;
import io.modelcontextprotocol.server.transport.HeaderAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.ServerSentEvent;
import org.restlet.data.Status;
import org.restlet.ext.mcp.McpInterceptor;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpRequestContext;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.EventStreamRepresentation;
import org.restlet.representation.Representation;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * One MCP {@code 2026-07-28} HTTP exchange, as the SDK's servlet transport handles it: POST only,
 * the {@code Mcp-Method}, {@code MCP-Protocol-Version} and {@code Mcp-Name} headers checked against
 * the body, 202 for a notification, one JSON reply with an error mapped to its HTTP status, or an
 * SSE stream when the SDK streams notifications before the result.
 *
 * <p>The exchange runs in an admitted worker, within the adapter's call limit and timeout. The
 * adapter's interceptors wrap the request and the final result or error, on the {@code 2025-11-25}
 * message types. Notifications streamed before the result pass without them, and so does the
 * in-band error ending a stream cut short by a timeout, a disconnect or a stop. Streamed messages
 * reach the response through a bounded queue, so a slow client holds back the SDK, and leaving the
 * stream early cancels the SDK's.
 */
public final class McpModernExchange {

    /** Runs the adapter's interceptors around a terminal step. */
    @FunctionalInterface
    public interface Interceptors {

        /**
         * Runs the interceptor chain.
         *
         * @param message The message, on the {@code 2025-11-25} types.
         * @param context The request context.
         * @param terminal The step after the last interceptor.
         * @return The final response, empty for a notification.
         */
        Mono<McpSchema.JSONRPCResponse> run(
                McpSchema.JSONRPCMessage message,
                McpRequestContext context,
                McpInterceptor.Chain terminal);
    }

    /** Streamed messages a slow client may leave pending before the SDK is held back. */
    private static final int QUEUE_CAPACITY = 64;

    /** Marks the end of a stream in the queue. */
    private static final byte[] END = new byte[0];

    private static final Map<Integer, Status> ERROR_STATUSES =
            Map.of(
                    ErrorCodes.PARSE_ERROR, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.INVALID_REQUEST, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.INVALID_PARAMS, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.HEADER_MISMATCH, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.MISSING_REQUIRED_CLIENT_CAPABILITY, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.UNSUPPORTED_PROTOCOL_VERSION, Status.CLIENT_ERROR_BAD_REQUEST,
                    ErrorCodes.METHOD_NOT_FOUND, Status.CLIENT_ERROR_NOT_FOUND);

    private final McpServer sdk;
    private final McpJsonMapper mapper;
    private final McpWorkers workers;
    private final Duration timeout;
    private final int maxMessageBytes;
    private final Interceptors interceptors;
    private final Supplier<Mono<Boolean>> stopping;

    /**
     * Creates the exchange handler of an adapter.
     *
     * @param sdk The SDK's modern server.
     * @param mapper The JSON mapper.
     * @param workers The adapter's admission workers.
     * @param timeout The request timeout.
     * @param maxMessageBytes The largest request or response message.
     * @param interceptors The adapter's interceptor chain.
     * @param stopping Emits when the adapter stops.
     */
    public McpModernExchange(
            McpServer sdk,
            McpJsonMapper mapper,
            McpWorkers workers,
            Duration timeout,
            int maxMessageBytes,
            Interceptors interceptors,
            Supplier<Mono<Boolean>> stopping) {
        this.sdk = sdk;
        this.mapper = mapper;
        this.workers = workers;
        this.timeout = timeout;
        this.maxMessageBytes = maxMessageBytes;
        this.interceptors = interceptors;
        this.stopping = stopping;
    }

    /**
     * Handles a {@code 2026-07-28} POST whose method, origin, media types and size the adapter
     * already checked.
     *
     * @param request The request.
     * @param response The response.
     */
    public void handle(Request request, Response response) {
        var exchange =
                new Exchange(request, new McpRequestContext(request, McpProtocol.MODERN_VERSION));
        Mono<Void> stopped =
                stopping.get()
                        .flatMap(ignored -> Mono.error(new IOException("MCP server stopped")));
        Mono<Void> disconnected =
                Mono.fromCompletionStage(request.getDisconnectSignal())
                        .flatMap(
                                cause ->
                                        Mono.error(
                                                new IOException("MCP peer disconnected", cause)));
        Disposable running =
                Mono.firstWithSignal(
                                stopped,
                                disconnected,
                                workers.submit(exchange::run, () -> request.getEntity().release()))
                        .subscribe(null, exchange::fail, exchange::end);
        Reply reply;
        try {
            reply =
                    exchange.head.get(
                            timeout.toNanos() + TimeUnit.SECONDS.toNanos(1), TimeUnit.NANOSECONDS);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            running.dispose();
            reply = unavailable();
        } catch (ExecutionException | TimeoutException _) {
            running.dispose();
            reply = unavailable();
        }
        reply.apply(response, exchange, running, heartbeat());
    }

    /** Comment heartbeats on a stream, as the SDK's servlet transport sends: at most 30 s apart. */
    private Duration heartbeat() {
        Duration half = timeout.dividedBy(2);
        Duration max = Duration.ofSeconds(30);
        return half.compareTo(max) < 0 ? half : max;
    }

    private Reply unavailable() {
        return json(
                Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                McpMessages.error(null, ErrorCodes.INTERNAL_ERROR, "MCP exchange unavailable"));
    }

    /** The state of one exchange, shared by its worker and the thread writing the response. */
    private final class Exchange {
        private final Request request;
        private final McpRequestContext context;
        private final McpTransportContext transport;
        private final CompletableFuture<Reply> head = new CompletableFuture<>();
        private final BlockingQueue<byte[]> events = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        private volatile boolean finished;

        /** The id of the request, once read; answers a stream cut short. */
        private volatile Object requestId;

        private Exchange(Request request, McpRequestContext context) {
            this.request = request;
            this.context = context;
            this.transport = McpTransportContext.create(Map.of(McpProtocol.CONTEXT_KEY, context));
        }

        /** Runs in the admitted worker: reads, checks and dispatches the message. */
        private void run() throws IOException, InterruptedException {
            JsonRpc.JSONRPCMessage message;
            try (InputStream input = request.getEntity().getStream()) {
                byte[] body = McpMessages.read(input, maxMessageBytes);
                message = JsonRpc.deserializeMessage(mapper, McpMessages.utf8(body));
            } catch (McpMessages.MessageLimitException _) {
                head.complete(new Reply(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, null, false));
                return;
            } catch (JsonRpc.InvalidMessageException e) {
                head.complete(
                        error(e.id(), ErrorCodes.INVALID_REQUEST, "Invalid JSON-RPC message"));
                return;
            } catch (IOException _) {
                head.complete(error(null, ErrorCodes.PARSE_ERROR, "Parse error"));
                return;
            }
            if (message instanceof JsonRpc.JSONRPCNotification notification) {
                notification(notification);
            } else if (message instanceof JsonRpc.JSONRPCRequest rpc) {
                call(rpc);
            } else {
                head.complete(
                        error(null, ErrorCodes.INVALID_REQUEST, "Requests or notifications only"));
            }
        }

        private void notification(JsonRpc.JSONRPCNotification notification) {
            interceptors
                    .run(
                            mapper.convertValue(notification, McpSchema.JSONRPCNotification.class),
                            context,
                            (next, nextContext) ->
                                    sdk.handleNotification(
                                                    transport,
                                                    mapper.convertValue(
                                                            next,
                                                            JsonRpc.JSONRPCNotification.class))
                                            .then(Mono.empty()))
                    .blockOptional(timeout);
            head.complete(new Reply(Status.SUCCESS_ACCEPTED, null, false));
        }

        private void call(JsonRpc.JSONRPCRequest rpc) throws InterruptedException {
            requestId = rpc.id();
            String mismatch = mismatch(rpc);
            if (mismatch != null) {
                head.complete(error(rpc.id(), ErrorCodes.HEADER_MISMATCH, mismatch));
                return;
            }
            Optional<McpSchema.JSONRPCResponse> last =
                    interceptors
                            .run(
                                    mapper.convertValue(rpc, McpSchema.JSONRPCRequest.class),
                                    context,
                                    (next, nextContext) -> Mono.fromCallable(() -> terminal(next)))
                            .blockOptional(timeout);
            if (last.isEmpty()) {
                // An interceptor answered nothing: accepted, as on the 2025-11-25 path.
                head.complete(new Reply(Status.SUCCESS_ACCEPTED, null, false));
                return;
            }
            byte[] encoded;
            try {
                encoded = McpMessages.encode(mapper, last.get(), maxMessageBytes);
            } catch (IOException _) {
                // Too large to send, as on the 2025-11-25 path.
                if (!head.complete(new Reply(Status.SERVER_ERROR_INTERNAL, null, false))) {
                    events.put(failure("MCP response too large"));
                }
                return;
            }
            if (!head.complete(json(status(last.get()), encoded))) {
                events.put(encoded);
            }
        }

        /** Dispatches to the SDK after the interceptors, streaming what comes before the result. */
        private McpSchema.JSONRPCResponse terminal(McpSchema.JSONRPCMessage next)
                throws IOException, InterruptedException {
            JsonRpc.JSONRPCRequest rpc = mapper.convertValue(next, JsonRpc.JSONRPCRequest.class);
            McpTransportContext sdkContext = transport;
            if (McpSchema.METHOD_TOOLS_CALL.equals(rpc.method())) {
                // The tools feature validates x-mcp-header arguments against these.
                sdkContext =
                        transport.putAll(
                                Map.of(
                                        McpParamHeaders.TRANSPORT_CONTEXT_KEY,
                                        McpHeaderDecoder.paramHeaders(headers())));
            }
            McpTransportResponse reply = sdk.handleBlocking(sdkContext, rpc).block(timeout);
            if (reply instanceof McpTransportResponse.Result result) {
                return classic(result.response());
            }
            if (reply instanceof McpTransportResponse.Error failure) {
                return classic(failure.response());
            }
            if (!(reply instanceof McpTransportResponse.Streaming streaming)) {
                throw new IOException("MCP request got no response");
            }
            head.complete(new Reply(Status.SUCCESS_OK, null, true));
            JsonRpc.JSONRPCResponse last = null;
            // Closing the stream cancels the SDK's, should the loop end early.
            try (Stream<JsonRpc.JSONRPCMessage> messages = streaming.messages().toStream()) {
                for (Iterator<JsonRpc.JSONRPCMessage> it = messages.iterator(); it.hasNext(); ) {
                    JsonRpc.JSONRPCMessage each = it.next();
                    if (each instanceof JsonRpc.JSONRPCResponse response) {
                        last = response;
                    } else {
                        events.put(McpMessages.encode(mapper, each, maxMessageBytes));
                    }
                }
            }
            if (last == null) {
                throw new IOException("MCP stream ended without a response");
            }
            return classic(last);
        }

        private McpSchema.JSONRPCResponse classic(JsonRpc.JSONRPCResponse response) {
            return mapper.convertValue(response, McpSchema.JSONRPCResponse.class);
        }

        /** The servlet transport's header checks, on Restlet headers. */
        private String mismatch(JsonRpc.JSONRPCRequest rpc) {
            String method = header("Mcp-Method");
            if (method == null) {
                return "Missing required header: Mcp-Method";
            }
            if (!method.equals(rpc.method())) {
                return "Mcp-Method header does not match request method";
            }
            Map<?, ?> params = rpc.params() instanceof Map<?, ?> map ? map : Map.of();
            if (params.get("_meta") instanceof Map<?, ?> meta
                    && meta.get(MetaKeys.PROTOCOL_VERSION) instanceof String version
                    && !version.equals(header(McpProtocol.VERSION_HEADER))) {
                return "MCP-Protocol-Version header does not match _meta protocol version";
            }
            String key =
                    switch (rpc.method()) {
                        case "tools/call", "prompts/get" -> "name";
                        case "resources/read" -> "uri";
                        default -> null;
                    };
            if (key != null && params.get(key) instanceof String expected) {
                String name = header("Mcp-Name");
                if (name == null) {
                    return "Missing required header: Mcp-Name";
                }
                String decoded = McpHeaderDecoder.decodeValue(name);
                if (decoded == null) {
                    return "Mcp-Name header has a malformed value";
                }
                if (!expected.equals(decoded)) {
                    return "Mcp-Name header does not match request name/uri";
                }
            }
            return null;
        }

        private String header(String name) {
            return request.getHeaders().getFirstValue(name, true);
        }

        /** The request headers, as the SDK's header decoder reads them. */
        private HeaderAccessor headers() {
            return new HeaderAccessor() {
                @Override
                public List<String> getHeader(String name) {
                    return Arrays.asList(request.getHeaders().getValuesArray(name, true));
                }

                @Override
                public List<String> getHeaderNames() {
                    return new ArrayList<>(request.getHeaders().getNames());
                }
            };
        }

        /** An in-band error answering the request, for a stream that can't end with its result. */
        private byte[] failure(String message) {
            try {
                return McpMessages.encode(
                        mapper,
                        McpMessages.error(requestId, ErrorCodes.INTERNAL_ERROR, message),
                        maxMessageBytes);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }

        /**
         * Ends the exchange after a failure: unavailable when nothing was answered yet, or an
         * in-band error once streaming. The stream always ends, whatever happens here.
         */
        private void fail(Throwable cause) {
            try {
                if (head.isDone()) {
                    byte[] error = failure("MCP exchange failed");
                    if (!events.offer(error)) {
                        // A stalled client: what it hasn't read yet gives way to the answer.
                        events.clear();
                        events.offer(error);
                    }
                } else {
                    head.complete(unavailable());
                }
            } catch (RuntimeException _) {
                // The stream ends below anyway.
            } finally {
                finish();
            }
        }

        /** Ends the exchange once the worker is done; it answered, unless it was cut short. */
        private void end() {
            try {
                if (!head.isDone()) {
                    head.complete(unavailable());
                }
            } finally {
                finish();
            }
        }

        /** Marks the end of the stream, for the writer to stop once it has written the rest. */
        private void finish() {
            finished = true;
            events.offer(END);
        }

        /**
         * Takes the next streamed message, or null at the end of the stream: after its end marker,
         * or once the worker finished and nothing is left, should the marker not fit the queue.
         */
        private byte[] next() throws InterruptedException {
            for (; ; ) {
                byte[] event = events.poll(100, TimeUnit.MILLISECONDS);
                if (event == END || (event == null && finished && events.isEmpty())) {
                    return null;
                }
                if (event != null) {
                    return event;
                }
            }
        }
    }

    /** A JSON-RPC error reply, built as a map since the id may be null. */
    private Reply error(Object id, int code, String message) {
        return json(
                ERROR_STATUSES.getOrDefault(code, Status.SUCCESS_OK),
                McpMessages.error(id, code, message));
    }

    private Reply json(Status status, Object message) {
        try {
            return json(status, McpMessages.encode(mapper, message, maxMessageBytes));
        } catch (IOException _) {
            return new Reply(Status.SERVER_ERROR_INTERNAL, null, false);
        }
    }

    private static Reply json(Status status, byte[] body) {
        return new Reply(status, body, false);
    }

    private static Status status(McpSchema.JSONRPCResponse response) {
        return response.error() == null
                ? Status.SUCCESS_OK
                : ERROR_STATUSES.getOrDefault(response.error().code(), Status.SUCCESS_OK);
    }

    /** The reply to write: a status and an optional JSON body, or an SSE stream. */
    private record Reply(Status status, byte[] body, boolean streaming) {

        private void apply(
                Response response, Exchange exchange, Disposable running, Duration heartbeat) {
            response.setStatus(status);
            if (streaming) {
                response.setEntity(stream(exchange, running, heartbeat));
            } else if (body != null) {
                response.setEntity(new ByteArrayRepresentation(body, MediaType.APPLICATION_JSON));
            } else {
                response.setEntity((Representation) null);
            }
        }

        private static Representation stream(
                Exchange exchange, Disposable running, Duration heartbeat) {
            return new EventStreamRepresentation(
                    sink -> {
                        try {
                            for (byte[] event = exchange.next();
                                    event != null;
                                    event = exchange.next()) {
                                sink.send(
                                        ServerSentEvent.builder()
                                                .event("message")
                                                .data(new String(event, StandardCharsets.UTF_8))
                                                .build());
                            }
                        } catch (InterruptedException _) {
                            Thread.currentThread().interrupt();
                            running.dispose();
                        } catch (IOException e) {
                            running.dispose();
                            throw e;
                        }
                    },
                    heartbeat);
        }
    }
}
