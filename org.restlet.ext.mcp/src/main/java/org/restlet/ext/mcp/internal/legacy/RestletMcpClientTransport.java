/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal.legacy;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpTransportSessionNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ServerSentEvent;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.internal.McpHttpStatusException;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.EventStreamReader;
import reactor.core.publisher.Mono;

/**
 * The SDK client's transport to a legacy MCP server, on a borrowed, started Restlet HTTP connector.
 * It never owns an HTTP engine or retries/reconnects a POST. The borrowed connector must disable
 * redirects. It offers the revisions given, newest first, and sends the one negotiated.
 *
 * <p>Sessions are opt-in, since 2026-07-28 removed them. By default a session response fails. With
 * sessions on, the session a server opens with {@code initialize} is kept, sent with each request,
 * and deleted when the transport closes; once expired, the SDK client initializes again. A session
 * id the caller owns is sent with each request instead, never replaced and never deleted, and its
 * expiry fails the request. Either way, no listening {@code GET} stream is opened, and requests the
 * server sends on a response stream are answered as not found, with or without a session.
 * Notifications and replies, which open no stream, are sent on workers of their own: they never
 * wait for a call slot that the exchange they follow still holds.
 */
public final class RestletMcpClientTransport implements McpClientTransport {
    private static final Logger LOGGER =
            Logger.getLogger(RestletMcpClientTransport.class.getName());

    private final Client client;
    private final String target;
    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final McpWorkers workers;
    private final McpWorkers replies;
    private final Set<Request> requests = ConcurrentHashMap.newKeySet();
    private final int maxMessageBytes;
    private final Duration timeout;
    private final Consumer<Request> customizer;
    private final List<String> versions;
    private final boolean sessions;
    private final String owned;
    private volatile Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
            incoming;
    private volatile Consumer<Throwable> exceptionHandler = error -> {};
    private volatile String session;
    private volatile String negotiated;
    private volatile boolean closed;

    /**
     * Creates the transport of a legacy client.
     *
     * @param client The borrowed, started connector, not following redirects.
     * @param target The endpoint URI.
     * @param maxMessageBytes The largest message.
     * @param maxConcurrentCalls The most exchanges at once.
     * @param timeout The request timeout.
     * @param customizer Customizes each request.
     * @param versions The revisions offered, newest first.
     * @param sessions Whether a session the server opens is kept, sent and finally deleted.
     * @param owned A session id the caller owns, sent with each request, or null.
     */
    public RestletMcpClientTransport(
            Client client,
            String target,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration timeout,
            Consumer<Request> customizer,
            List<String> versions,
            boolean sessions,
            String owned) {
        this.client = Objects.requireNonNull(client, "client");
        Reference reference = new Reference(target);
        if (!"http".equals(reference.getScheme()) && !"https".equals(reference.getScheme())) {
            throw new IllegalArgumentException("MCP HTTP target must use http or https");
        }
        if (maxMessageBytes <= 0 || maxMessageBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid MCP message byte limit");
        }
        if (versions.isEmpty()) {
            throw new IllegalArgumentException("At least one MCP revision required");
        }
        this.target = target;
        this.maxMessageBytes = maxMessageBytes;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.customizer = Objects.requireNonNull(customizer, "customizer");
        this.versions = List.copyOf(versions);
        this.sessions = sessions;
        this.owned = owned;
        workers = new McpWorkers(maxConcurrentCalls, timeout);
        replies = new McpWorkers(maxConcurrentCalls, timeout);
    }

    @Override
    public synchronized Mono<Void> connect(
            Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
        // With sessions, the SDK client connects again when it initializes after an expired
        // session: the new handler then receives what the server sends.
        if (closed || (incoming != null && !sessions)) {
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
    public void setExceptionHandler(Consumer<Throwable> handler) {
        exceptionHandler = Objects.requireNonNull(handler, "handler");
    }

    /** Oldest first: the SDK client offers the last one in {@code initialize}. */
    @Override
    public List<String> protocolVersions() {
        return versions.reversed();
    }

    /**
     * The session id sent with each request: the one the caller owns, else the one the server
     * opened, if any.
     *
     * @return The session id, or null.
     */
    public String getSessionId() {
        return owned != null ? owned : session;
    }

    /**
     * The revision the last {@code initialize} handshake negotiated, which a handshake after an
     * expired session may change.
     *
     * @return The revision, or null before a handshake completes.
     */
    public String getNegotiatedVersion() {
        return negotiated;
    }

    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        var active = new AtomicReference<Request>();
        // A reply to a server request, or a notification such as notifications/initialized, may be
        // sent while the exchange it follows still holds its call slot: it takes a reply slot.
        McpWorkers admission = message instanceof McpSchema.JSONRPCRequest ? workers : replies;
        return admission.submit(
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
        boolean initializing =
                message instanceof McpSchema.JSONRPCRequest rpc
                        && McpSchema.METHOD_INITIALIZE.equals(rpc.method());
        if (initializing) {
            // A new handshake, after an expired session too, starts without one.
            session = null;
            negotiated = null;
        }
        String sent = getSessionId();
        byte[] bytes = McpMessages.encode(mapper, message, maxMessageBytes);
        Request request =
                new Request(
                        Method.POST,
                        target,
                        new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        customizer.accept(request);
        String version = negotiated;
        request.getHeaders()
                .set(McpProtocol.VERSION_HEADER, version == null ? versions.getFirst() : version);
        if (sent != null) {
            request.getHeaders().set(McpProtocol.SESSION_HEADER, sent);
        }
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
            if (sent != null && response.getStatus().getCode() == 404) {
                if (sent.equals(owned)) {
                    throw new IOException("MCP session " + sent + " expired");
                }
                expired(sent);
            }
            if (!response.getStatus().isSuccess()) {
                throw new McpHttpStatusException(
                        "MCP HTTP failure: " + response.getStatus(),
                        response.getStatus().getCode(),
                        response.getStatus().getThrowable());
            }
            String opened = response.getHeaders().getFirstValue(McpProtocol.SESSION_HEADER, true);
            if (opened != null && owned == null) {
                if (!sessions) {
                    throw new IOException(
                            "MCP session responses need legacy sessions to be turned on");
                }
                if (initializing) {
                    session = opened;
                }
            }
            if (message instanceof McpSchema.JSONRPCNotification
                    || message instanceof McpSchema.JSONRPCResponse) {
                accepted(response);
                return;
            }
            readResponse(response, ((McpSchema.JSONRPCRequest) message).id(), initializing);
        } finally {
            if (response != null && response.getEntity() != null) {
                response.getEntity().release();
            }
            requests.remove(request);
        }
    }

    /** A notification, or an answer to a server request, gets an empty acceptance. */
    private static void accepted(Response response) throws IOException {
        if (response.getStatus().getCode() != 202 && response.getStatus().getCode() != 204) {
            throw new IOException("MCP notification must return an empty acceptance response");
        }
        if (response.getEntity() != null
                && response.getEntity().isAvailable()
                && response.getEntity().getSize() != 0) {
            throw new IOException("MCP notification unexpectedly returned an entity");
        }
    }

    /**
     * Fails a request whose session expired. If that session is still the current one, it is
     * forgotten and the SDK client told, which initializes again; a delayed answer for a session
     * already replaced leaves the replacement alone.
     */
    private void expired(String sent) throws IOException {
        boolean current;
        synchronized (this) {
            current = sent.equals(session);
            if (current) {
                session = null;
                negotiated = null;
            }
        }
        var expired = new McpTransportSessionNotFoundException(sent);
        if (current) {
            exceptionHandler.accept(expired);
        }
        throw new IOException("MCP session " + sent + " expired", expired);
    }

    private void readResponse(Response response, Object id, boolean initializing)
            throws IOException {
        if (response.getEntity() == null || response.getEntity().getMediaType() == null) {
            throw new IOException("Missing MCP response entity or media type");
        }
        MediaType mediaType = response.getEntity().getMediaType();
        try (InputStream input =
                Objects.requireNonNull(response.getEntity().getStream(), "response stream")) {
            if (MediaType.APPLICATION_JSON.isCompatible(mediaType)) {
                if (!deliver(
                        McpSchema.deserializeJsonRpcMessage(
                                mapper, McpMessages.utf8(McpMessages.read(input, maxMessageBytes))),
                        id,
                        initializing)) {
                    throw new IOException("MCP JSON response must contain the correlated response");
                }
            } else if (MediaType.TEXT_EVENT_STREAM.isCompatible(mediaType)) {
                readEvents(input, id, initializing);
            } else {
                throw new IOException("Unsupported MCP response media type: " + mediaType);
            }
        }
    }

    private void readEvents(InputStream input, Object id, boolean initializing) throws IOException {
        Reader decoded =
                new InputStreamReader(
                        input,
                        StandardCharsets.UTF_8
                                .newDecoder()
                                .onMalformedInput(CodingErrorAction.REPORT));
        try (var events = new EventStreamReader(new LineBudgetReader(decoded, maxMessageBytes))) {
            events.setMaxEventSize(maxMessageBytes);
            ServerSentEvent event;
            while ((event = events.readEvent()) != null) {
                if (event.getData() != null
                        && !event.getData().isEmpty()
                        && deliver(
                                McpSchema.deserializeJsonRpcMessage(mapper, event.getData()),
                                id,
                                initializing)) {
                    return;
                }
            }
            throw new IOException("MCP SSE ended before the correlated response");
        }
    }

    private boolean deliver(McpSchema.JSONRPCMessage message, Object id, boolean initializing)
            throws IOException {
        if (message instanceof McpSchema.JSONRPCResponse rpc && !Objects.equals(id, rpc.id())) {
            throw new IOException("MCP response id does not match its HTTP request");
        }
        if (message instanceof McpSchema.JSONRPCRequest request) {
            // Refused on its own exchange, without holding up the stream: the legacy edge declares
            // no capability, and answers no sampling, roots or elicitation.
            sendMessage(
                            new McpSchema.JSONRPCResponse(
                                    McpSchema.JSONRPC_VERSION,
                                    request.id(),
                                    null,
                                    new McpSchema.JSONRPCResponse.JSONRPCError(
                                            McpSchema.ErrorCodes.METHOD_NOT_FOUND,
                                            "Method not found: " + request.method(),
                                            null)))
                    .subscribe(
                            null,
                            error ->
                                    LOGGER.log(
                                            Level.FINE, "MCP server request not refused", error));
            return false;
        }
        if (initializing
                && message instanceof McpSchema.JSONRPCResponse rpc
                && rpc.result() instanceof java.util.Map<?, ?> result
                && result.get("protocolVersion") instanceof String version
                && versions.contains(version)) {
            negotiated = version;
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
                            try {
                                workers.close();
                                deleteSession();
                            } finally {
                                replies.close();
                            }
                            return Boolean.TRUE;
                        })
                .then();
    }

    /**
     * Ends the session on the server, if one is open; a server may refuse, which is fine. The
     * exchange runs on its own thread, aborted at the request timeout, so that a stalled server
     * never holds up the close.
     */
    private void deleteSession() {
        String open = session;
        session = null;
        if (open == null) {
            return;
        }
        Request request = new Request(Method.DELETE, target);
        customizer.accept(request);
        request.getHeaders().set(McpProtocol.SESSION_HEADER, open);
        String version = negotiated;
        if (version != null) {
            request.getHeaders().set(McpProtocol.VERSION_HEADER, version);
        }
        Thread exchange =
                Thread.ofVirtual()
                        .name("mcp-session-delete")
                        .start(
                                () -> {
                                    Response response = null;
                                    try {
                                        response = client.handle(request);
                                    } catch (RuntimeException e) {
                                        LOGGER.log(Level.FINE, "MCP session not deleted", e);
                                    } finally {
                                        if (response != null && response.getEntity() != null) {
                                            response.getEntity().release();
                                        }
                                    }
                                });
        try {
            if (!exchange.join(timeout)) {
                LOGGER.log(Level.FINE, "MCP session deletion timed out");
                request.abort();
                exchange.interrupt();
            }
        } catch (InterruptedException e) {
            request.abort();
            exchange.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Hands the event parser at most one line per read and charges the UTF-8 bytes of each line it
     * takes. The parser only asks for a line until the correlated response is complete, so earlier
     * comments count against the budget, while bytes decoded ahead of a complete response don't.
     */
    private static final class LineBudgetReader extends Reader {
        private final Reader input;
        private final char[] buffer = new char[8192];
        private int position;
        private int limit;
        private long remaining;

        private LineBudgetReader(Reader input, int bytes) {
            this.input = input;
            remaining = bytes;
        }

        @Override
        public int read(char[] target, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            if (position == limit) {
                position = 0;
                limit = Math.max(0, input.read(buffer, 0, buffer.length));
                if (limit == 0) {
                    return -1;
                }
            }
            int count = 0;
            char c = 0;
            while (count < length && position < limit && c != '\n' && c != '\r') {
                c = buffer[position++];
                remaining -= utf8Length(c);
                if (remaining < 0) {
                    throw new IOException("MCP response byte budget exceeded");
                }
                target[offset + count++] = c;
            }
            return count;
        }

        /** Surrogates count 2 bytes each, the 4 bytes of their supplementary character. */
        private static int utf8Length(char c) {
            if (c < 0x80) {
                return 1;
            }
            return c < 0x800 || Character.isSurrogate(c) ? 2 : 3;
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}
