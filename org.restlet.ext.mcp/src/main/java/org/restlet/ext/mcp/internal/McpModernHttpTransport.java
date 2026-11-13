/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.modern.JsonRpc;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.ServerSentEvent;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.Representation;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

/**
 * Sends MCP {@code 2026-07-28} requests over a borrowed, started Restlet HTTP connector, one POST
 * each, and streams back what answers them: the notifications the server sends first, then the
 * response. Calls are admitted within the call limit and timeout; listen streams within their own
 * limit, without timeout. Messages are read as they are demanded, so a slow subscriber holds back
 * the server rather than filling memory. Cancelling a request aborts its HTTP exchange, which the
 * server sees as a cancellation. It never retries or reconnects.
 */
public final class McpModernHttpTransport {
    private static final String BASE64_PREFIX = "=?base64?";
    private static final String BASE64_SUFFIX = "?=";

    /**
     * The characters an event may have beyond its message: field names, comments and the other
     * fields. Each message is limited in bytes on its own.
     */
    private static final int MAX_FRAMING_CHARS = 8192;

    private final Client client;
    private final String target;
    private final McpJsonMapper mapper;
    private final int maxMessageBytes;
    private final Consumer<Request> customizer;
    private final McpWorkers calls;
    private final McpWorkers listeners;
    private final Set<Request> requests = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    /**
     * @param client The borrowed, started HTTP connector, not following redirects.
     * @param target The MCP endpoint URI.
     * @param mapper The JSON mapper.
     * @param maxMessageBytes The size limit of each message, sent or received.
     * @param maxConcurrentCalls The number of calls in flight at once.
     * @param timeout The time limit of each call, listen streams excepted.
     * @param customizer Adds to each request, such as credentials.
     */
    public McpModernHttpTransport(
            Client client,
            String target,
            McpJsonMapper mapper,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration timeout,
            Consumer<Request> customizer) {
        this.client = Objects.requireNonNull(client, "client");
        this.target = Objects.requireNonNull(target, "target");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.maxMessageBytes = maxMessageBytes;
        this.customizer = Objects.requireNonNull(customizer, "customizer");
        calls = new McpWorkers(maxConcurrentCalls, timeout);
        listeners = new McpWorkers(McpProtocol.DEFAULT_MAX_LISTEN_STREAMS, null, timeout);
    }

    /**
     * Sends a request, and streams back the messages answering it: notifications, then its
     * response, after which the stream completes. Responses to other requests are dropped, and the
     * stream fails if it ends without its response. Cancelling the stream aborts the exchange.
     *
     * @param rpc The request.
     * @param name The value of the {@code Mcp-Name} header, or null.
     * @param params The values of the {@code Mcp-Param-*} headers, by name.
     * @param listen Whether this is a {@code subscriptions/listen} stream, without time limit.
     * @return The messages answering the request.
     */
    public Flux<JsonRpc.JSONRPCMessage> send(
            JsonRpc.JSONRPCRequest rpc, String name, Map<String, String> params, boolean listen) {
        return Flux.<JsonRpc.JSONRPCMessage>create(
                        sink -> {
                            AtomicReference<Request> active = new AtomicReference<>();
                            Demand demand = new Demand();
                            sink.onRequest(demand::add);
                            Emitter emitter =
                                    message -> {
                                        demand.take();
                                        sink.next(message);
                                    };
                            Disposable running =
                                    (listen ? listeners : calls)
                                            .submit(
                                                    () ->
                                                            exchange(
                                                                    rpc, name, params, active,
                                                                    emitter),
                                                    () -> abort(active.get()))
                                            .subscribe(null, sink::error, sink::complete);
                            sink.onCancel(running);
                        })
                // What follows runs off the worker, which then frees its slot without waiting. The
                // worker reads only what this bounded queue has room for.
                .publishOn(Schedulers.boundedElastic());
    }

    private void exchange(
            JsonRpc.JSONRPCRequest rpc,
            String name,
            Map<String, String> params,
            AtomicReference<Request> active,
            Emitter sink)
            throws IOException {
        if (closed) {
            throw new IOException("MCP client is closed");
        }
        Request request = request(rpc, name, params);
        active.set(request);
        requests.add(request);
        Response response = null;
        try {
            if (closed || Thread.currentThread().isInterrupted()) {
                request.abort();
                throw new IOException("MCP exchange cancelled");
            }
            response = client.handle(request);
            Representation entity = response.getEntity();
            MediaType type = entity == null ? null : entity.getMediaType();
            if (type != null && MediaType.TEXT_EVENT_STREAM.isCompatible(type)) {
                stream(entity, rpc.id(), sink);
            } else if (type != null && MediaType.APPLICATION_JSON.isCompatible(type)) {
                sink.emit(single(entity, response, rpc.id()));
            } else {
                throw new McpHttpStatusException(
                        "MCP HTTP failure: " + response.getStatus(),
                        response.getStatus().getCode(),
                        response.getStatus().getThrowable());
            }
        } finally {
            if (response != null && response.getEntity() != null) {
                response.getEntity().release();
            }
            requests.remove(request);
        }
    }

    private Request request(JsonRpc.JSONRPCRequest rpc, String name, Map<String, String> params)
            throws IOException {
        Request request =
                new Request(
                        Method.POST,
                        target,
                        new ByteArrayRepresentation(
                                McpMessages.encode(mapper, rpc, maxMessageBytes),
                                MediaType.APPLICATION_JSON));
        customizer.accept(request);
        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.MODERN_VERSION);
        request.getHeaders().set("Mcp-Method", rpc.method());
        if (name != null) {
            request.getHeaders().set("Mcp-Name", headerValue(name));
        }
        params.forEach(
                (header, value) ->
                        request.getHeaders().set("Mcp-Param-" + header, headerValue(value)));
        request.getClientInfo()
                .setAcceptedMediaTypes(
                        List.of(
                                new Preference<>(MediaType.APPLICATION_JSON),
                                new Preference<>(MediaType.TEXT_EVENT_STREAM)));
        return request;
    }

    /** A JSON reply: the response, success or error, whatever the HTTP status. */
    private JsonRpc.JSONRPCMessage single(Representation entity, Response response, Object id)
            throws IOException {
        byte[] body;
        try (InputStream input = entity.getStream()) {
            body = McpMessages.read(input, maxMessageBytes);
        }
        JsonRpc.JSONRPCMessage message;
        try {
            message = JsonRpc.deserializeMessage(mapper, McpMessages.utf8(body));
        } catch (IOException | RuntimeException e) {
            throw new McpHttpStatusException(
                    "MCP HTTP failure: " + response.getStatus(), response.getStatus().getCode(), e);
        }
        if (!(message instanceof JsonRpc.JSONRPCResponse reply) || !sameId(reply.id(), id)) {
            if (response.getStatus().isError()) {
                // Not an answer to this request, such as a legacy server's error without an id.
                throw new McpHttpStatusException(
                        "MCP HTTP failure: " + response.getStatus(),
                        response.getStatus().getCode(),
                        null);
            }
            throw new IOException("MCP JSON reply must be the response to its request");
        }
        if (reply.error() != null && response.getStatus().isServerError()) {
            // Keeps the status, which tells a failing server from a legacy one.
            throw new McpServerErrorException(reply.error(), response.getStatus().getCode());
        }
        return message;
    }

    /**
     * An event stream: each message as it comes, until the response to the request. Responses to
     * other requests are dropped. The stream must be valid UTF-8, and each message within the
     * message size limit, in bytes.
     */
    private void stream(Representation entity, Object id, Emitter sink) throws IOException {
        try (InputStream input = entity.getStream();
                EventStreamReader reader =
                        new EventStreamReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                                .newDecoder()
                                                .onMalformedInput(CodingErrorAction.REPORT)
                                                .onUnmappableCharacter(
                                                        CodingErrorAction.REPORT)))) {
            // Characters bound the event as read, its framing included; bytes bound its message.
            reader.setMaxEventSize(
                    (int) Math.min(Integer.MAX_VALUE, (long) maxMessageBytes + MAX_FRAMING_CHARS));
            for (ServerSentEvent event = reader.readEvent();
                    event != null;
                    event = reader.readEvent()) {
                String data = event.getData();
                if (data == null || data.isEmpty()) {
                    continue; // A heartbeat.
                }
                if (utf8Length(data) > maxMessageBytes) {
                    throw new McpMessages.MessageLimitException(
                            "MCP message exceeds " + maxMessageBytes + " bytes");
                }
                JsonRpc.JSONRPCMessage message = JsonRpc.deserializeMessage(mapper, data);
                boolean response = message instanceof JsonRpc.JSONRPCResponse;
                boolean ours = response && sameId(((JsonRpc.JSONRPCResponse) message).id(), id);
                if (!response || ours) {
                    sink.emit(message); // Another request's response is dropped.
                }
                if (ours) {
                    return;
                }
            }
        }
        throw new IOException("MCP event stream ended without a response");
    }

    /** The number of UTF-8 bytes of a well-formed string. */
    private static long utf8Length(String value) {
        long bytes = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x80) {
                bytes += 1;
            } else if (c < 0x800 || Character.isSurrogate(c)) {
                bytes += 2; // Each surrogate: 2 of its supplementary character's 4 bytes.
            } else {
                bytes += 3;
            }
        }
        return bytes;
    }

    /**
     * Whether two JSON-RPC ids are the same, across their JSON number forms, exactly: 1 and 1.0
     * are, 1 and 1.5 aren't.
     *
     * @param a An id.
     * @param b Another id.
     * @return True if they are the same.
     */
    public static boolean sameId(Object a, Object b) {
        return Objects.equals(McpMessages.idKey(a), McpMessages.idKey(b));
    }

    /**
     * A header value as MCP sends it: as is when it is printable ASCII without surrounding spaces,
     * else UTF-8 in base64 between {@code =?base64?} and {@code ?=}.
     *
     * @param value The value.
     * @return The header value.
     */
    public static String headerValue(String value) {
        boolean plain =
                !value.startsWith(BASE64_PREFIX)
                        && value.strip().equals(value)
                        && value.chars().allMatch(c -> c >= 0x20 && c <= 0x7e);
        return plain
                ? value
                : BASE64_PREFIX
                        + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8))
                        + BASE64_SUFFIX;
    }

    private static void abort(Request request) {
        if (request != null) {
            request.abort();
        }
    }

    /** Hands a message on, once it is demanded. */
    @FunctionalInterface
    private interface Emitter {
        void emit(JsonRpc.JSONRPCMessage message) throws IOException;
    }

    /** The messages demanded and not handed on yet. Cancelling interrupts a worker waiting. */
    private static final class Demand {
        private long requested;

        synchronized void add(long count) {
            requested = Operators.addCap(requested, count);
            notifyAll();
        }

        synchronized void take() throws InterruptedIOException {
            try {
                while (requested == 0) {
                    wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException("MCP exchange cancelled");
            }
            if (requested != Long.MAX_VALUE) {
                requested--;
            }
        }
    }

    /** Aborts the exchanges in flight and refuses new ones. */
    public void close() throws IOException {
        closed = true;
        requests.forEach(Request::abort);
        try {
            calls.close();
        } finally {
            listeners.close();
        }
    }
}
