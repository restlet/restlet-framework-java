/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.EventStreamReader;
import reactor.core.publisher.Mono;

/**
 * Native SDK transport using a borrowed, started Restlet HTTP connector. It never owns an HTTP
 * engine or retries/reconnects a POST. The borrowed connector must disable redirects.
 */
public final class RestletMcpClientTransport implements McpClientTransport {
    private final Client client;
    private final String target;
    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final McpWorkers workers;
    private final Set<Request> requests = ConcurrentHashMap.newKeySet();
    private final int maxMessageBytes;
    private final Duration timeout;
    private final Consumer<Request> customizer;
    private volatile Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
            incoming;
    private volatile boolean closed;

    public RestletMcpClientTransport(Client client, String target) {
        this(
                client,
                target,
                McpProtocol.DEFAULT_MAX_MESSAGE_BYTES,
                McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS,
                Duration.ofSeconds(30),
                request -> {});
    }

    public RestletMcpClientTransport(
            Client client,
            String target,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration timeout,
            Consumer<Request> customizer) {
        this.client = Objects.requireNonNull(client, "client");
        Reference reference = new Reference(target);
        if (!"http".equals(reference.getScheme()) && !"https".equals(reference.getScheme())) {
            throw new IllegalArgumentException("MCP HTTP target must use http or https");
        }
        if (maxMessageBytes <= 0 || maxMessageBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid MCP message byte limit");
        }
        this.target = target;
        this.maxMessageBytes = maxMessageBytes;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.customizer = Objects.requireNonNull(customizer, "customizer");
        workers = new McpWorkers(maxConcurrentCalls, timeout);
    }

    @Override
    public synchronized Mono<Void> connect(
            Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
        if (closed || incoming != null) {
            return Mono.error(new IllegalStateException("MCP transport is single-use"));
        }
        if (!client.isStarted()
                || client.getContext() == null
                || !"false"
                        .equalsIgnoreCase(
                                client.getContext()
                                        .getParameters()
                                        .getFirstValue("followRedirects", "true"))) {
            return Mono.error(
                    new IllegalStateException(
                            "Borrowed MCP client must be started with followRedirects=false"));
        }
        incoming = Objects.requireNonNull(handler, "handler");
        return Mono.empty();
    }

    @Override
    public List<String> protocolVersions() {
        return List.of(McpProtocol.VERSION);
    }

    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        var active = new AtomicReference<Request>();
        return workers.submit(
                () -> exchange(message, active),
                () -> {
                    Request request = active.get();
                    if (request != null) {
                        request.abort();
                    }
                });
    }

    private void exchange(McpSchema.JSONRPCMessage message, AtomicReference<Request> active)
            throws IOException {
        if (closed || incoming == null) {
            throw new IOException("MCP transport is not connected");
        }
        byte[] bytes = McpMessages.encode(mapper, message, maxMessageBytes);
        Request request =
                new Request(
                        Method.POST,
                        target,
                        new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        customizer.accept(request);
        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.VERSION);
        request.getClientInfo()
                .setAcceptedMediaTypes(
                        List.of(
                                new Preference<>(MediaType.APPLICATION_JSON),
                                new Preference<>(MediaType.TEXT_EVENT_STREAM)));
        active.set(request);
        requests.add(request);
        if (closed || Thread.currentThread().isInterrupted()) {
            requests.remove(request);
            request.abort();
            throw new IOException("MCP exchange cancelled");
        }
        Response response = null;
        try {
            response = client.handle(request);
            if (!response.getStatus().isSuccess()) {
                throw new IOException(
                        "MCP HTTP failure: " + response.getStatus(),
                        response.getStatus().getThrowable());
            }
            if (response.getHeaders().getFirstValue("Mcp-Session-Id", true) != null) {
                throw new IOException(
                        "MCP session responses are not supported by the stateless profile");
            }
            if (message instanceof McpSchema.JSONRPCNotification) {
                if (response.getStatus().getCode() != 202
                        && response.getStatus().getCode() != 204) {
                    throw new IOException(
                            "MCP notification must return an empty acceptance response");
                }
                if (response.getEntity() != null
                        && response.getEntity().isAvailable()
                        && response.getEntity().getSize() != 0) {
                    throw new IOException("MCP notification unexpectedly returned an entity");
                }
                return;
            }
            if (!(message instanceof McpSchema.JSONRPCRequest rpc)) {
                throw new IOException(
                        "Server-directed responses are outside the stateless MCP profile");
            }
            readResponse(response, rpc.id());
        } finally {
            if (response != null && response.getEntity() != null) {
                response.getEntity().release();
            }
            requests.remove(request);
        }
    }

    private void readResponse(Response response, Object id) throws IOException {
        if (response.getEntity() == null || response.getEntity().getMediaType() == null) {
            throw new IOException("Missing MCP response entity or media type");
        }
        MediaType mediaType = response.getEntity().getMediaType();
        try (InputStream input =
                new BudgetInputStream(response.getEntity().getStream(), maxMessageBytes)) {
            if (MediaType.APPLICATION_JSON.isCompatible(mediaType)) {
                if (!deliver(
                        McpSchema.deserializeJsonRpcMessage(
                                mapper, McpMessages.utf8(McpMessages.read(input, maxMessageBytes))),
                        id)) {
                    throw new IOException("MCP JSON response must contain the correlated response");
                }
            } else if (MediaType.TEXT_EVENT_STREAM.isCompatible(mediaType)) {
                try (var events =
                        new EventStreamReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                                .newDecoder()
                                                .onMalformedInput(CodingErrorAction.REPORT)))) {
                    org.restlet.data.ServerSentEvent event;
                    while ((event = events.readEvent()) != null) {
                        if (event.getData() != null
                                && !event.getData().isEmpty()
                                && deliver(
                                        McpSchema.deserializeJsonRpcMessage(
                                                mapper, event.getData()),
                                        id)) {
                            return;
                        }
                    }
                    throw new IOException("MCP SSE ended before the correlated response");
                }
            } else {
                throw new IOException("Unsupported MCP response media type: " + mediaType);
            }
        }
    }

    private boolean deliver(McpSchema.JSONRPCMessage message, Object id) throws IOException {
        if (message instanceof McpSchema.JSONRPCResponse rpc && !Objects.equals(id, rpc.id())) {
            throw new IOException("MCP response id does not match its HTTP request");
        }
        if (message instanceof McpSchema.JSONRPCRequest) {
            throw new IOException("Server-initiated MCP requests are not supported");
        }
        incoming.apply(Mono.just(message)).block(timeout);
        return message instanceof McpSchema.JSONRPCResponse;
    }

    @Override
    public <T> T unmarshalFrom(Object data, TypeRef<T> type) {
        return mapper.convertValue(data, type);
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromCallable(
                        () -> {
                            closed = true;
                            requests.forEach(Request::abort);
                            workers.close();
                            return Boolean.TRUE;
                        })
                .then();
    }

    private static final class BudgetInputStream extends FilterInputStream {
        private long remaining;

        private BudgetInputStream(InputStream input, int bytes) {
            super(Objects.requireNonNull(input, "response stream"));
            remaining = bytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1 && --remaining < 0) {
                throw new IOException("MCP response byte budget exceeded");
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int count = super.read(buffer, offset, (int) Math.min(length, remaining + 1));
            if (count > remaining) {
                throw new IOException("MCP response byte budget exceeded");
            }
            if (count > 0) {
                remaining -= count;
            }
            return count;
        }
    }
}
