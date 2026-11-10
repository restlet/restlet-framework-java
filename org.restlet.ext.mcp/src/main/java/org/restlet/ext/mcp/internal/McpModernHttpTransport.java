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
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

/**
 * Sends MCP {@code 2026-07-28} requests over a borrowed, started Restlet HTTP connector, one POST
 * each, and streams back what answers them: the notifications the server sends first, then the
 * response. Calls are admitted within the call limit and timeout; listen streams within their own
 * limit, without timeout. Cancelling a request aborts its HTTP exchange, which the server sees as a
 * cancellation. It never retries or reconnects.
 */
public final class McpModernHttpTransport {
    private static final String BASE64_PREFIX = "=?base64?";
    private static final String BASE64_SUFFIX = "?=";

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
     * @param maxListenStreams The number of listen streams open at once.
     * @param timeout The time limit of each call, listen streams excepted.
     * @param customizer Adds to each request, such as credentials.
     */
    public McpModernHttpTransport(
            Client client,
            String target,
            McpJsonMapper mapper,
            int maxMessageBytes,
            int maxConcurrentCalls,
            int maxListenStreams,
            Duration timeout,
            Consumer<Request> customizer) {
        this.client = Objects.requireNonNull(client, "client");
        this.target = Objects.requireNonNull(target, "target");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.maxMessageBytes = maxMessageBytes;
        this.customizer = Objects.requireNonNull(customizer, "customizer");
        calls = new McpWorkers(maxConcurrentCalls, timeout);
        listeners = new McpWorkers(maxListenStreams, null, timeout);
    }

    /**
     * Sends a request, and streams back the messages answering it: notifications, then its
     * response, after which the stream completes. Cancelling the stream aborts the exchange.
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
                            Disposable running =
                                    (listen ? listeners : calls)
                                            .submit(
                                                    () -> exchange(rpc, name, params, active, sink),
                                                    () -> abort(active.get()))
                                            .subscribe(null, sink::error, sink::complete);
                            sink.onCancel(running);
                        })
                // What follows runs off the worker, which then frees its slot without waiting.
                .publishOn(Schedulers.boundedElastic());
    }

    private void exchange(
            JsonRpc.JSONRPCRequest rpc,
            String name,
            Map<String, String> params,
            AtomicReference<Request> active,
            FluxSink<JsonRpc.JSONRPCMessage> sink)
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
                sink.next(single(entity, response));
            } else {
                throw new IOException(
                        "MCP HTTP failure: " + response.getStatus(),
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
    private JsonRpc.JSONRPCMessage single(Representation entity, Response response)
            throws IOException {
        byte[] body;
        try (InputStream input = entity.getStream()) {
            body = McpMessages.read(input, maxMessageBytes);
        }
        try {
            return JsonRpc.deserializeMessage(mapper, McpMessages.utf8(body));
        } catch (IOException | RuntimeException e) {
            throw new IOException("MCP HTTP failure: " + response.getStatus(), e);
        }
    }

    /** An event stream: each message as it comes, until the response to the request. */
    private void stream(Representation entity, Object id, FluxSink<JsonRpc.JSONRPCMessage> sink)
            throws IOException {
        try (InputStream input = entity.getStream();
                EventStreamReader reader = new EventStreamReader(input)) {
            reader.setMaxEventSize(maxMessageBytes);
            for (ServerSentEvent event = reader.readEvent();
                    event != null;
                    event = reader.readEvent()) {
                String data = event.getData();
                if (data == null || data.isEmpty()) {
                    continue; // A heartbeat.
                }
                JsonRpc.JSONRPCMessage message = JsonRpc.deserializeMessage(mapper, data);
                sink.next(message);
                if (message instanceof JsonRpc.JSONRPCResponse response
                        && sameId(response.id(), id)) {
                    return;
                }
            }
        }
        throw new IOException("MCP event stream ended without a response");
    }

    /**
     * Whether two JSON-RPC ids are the same, across their JSON integer forms.
     *
     * @param a An id.
     * @param b Another id.
     * @return True if they are the same.
     */
    public static boolean sameId(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return x.longValue() == y.longValue();
        }
        return Objects.equals(a, b);
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
