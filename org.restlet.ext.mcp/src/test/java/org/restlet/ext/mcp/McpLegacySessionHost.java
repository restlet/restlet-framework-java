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
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransport;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpTransportException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.ServerSentEvent;
import org.restlet.data.Status;
import org.restlet.representation.EventStreamRepresentation;
import org.restlet.representation.EventStreamRepresentation.EventSink;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A legacy, sessionful MCP server for tests: the MCP SDK's {@link
 * McpStreamableServerTransportProvider} hosted on one {@link Restlet} handling POST, GET and DELETE
 * on the MCP endpoint, with core SSE. A request is answered on an SSE stream that the SDK session
 * writes to and closes; a listening stream stays open until the session replaces or releases it, or
 * its client goes away. A request without a session, {@code initialize} excepted, gets a bare
 * {@code 400}, as such servers answer {@code server/discover}. A sessionless host returns no
 * session id, and serves requests without one from its only session.
 */
final class McpLegacySessionHost extends Restlet implements McpStreamableServerTransportProvider {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();

    final Map<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();
    final Map<String, List<String>> replies = new ConcurrentHashMap<>();

    /** The methods posted, in order, whether served or not. */
    final List<String> methods = new CopyOnWriteArrayList<>();

    /** The protocol version header of each request posted, in order. */
    final List<String> versions = new CopyOnWriteArrayList<>();

    /** The client capabilities each {@code initialize} declared, in order. */
    final List<Object> capabilities = new CopyOnWriteArrayList<>();

    final AtomicInteger listening = new AtomicInteger();
    final AtomicInteger opened = new AtomicInteger();
    final AtomicInteger deleted = new AtomicInteger();
    private final Duration heartbeat;
    private final Duration timeout;
    private volatile McpStreamableServerSession.Factory factory;
    private volatile boolean closing;

    /** Whether no session id is returned, the only session serving requests without one. */
    volatile boolean sessionless;

    McpLegacySessionHost(Duration heartbeat, Duration timeout) {
        this.heartbeat = heartbeat;
        this.timeout = timeout;
    }

    /**
     * Forgets a session without telling its client, as an expiring server does.
     *
     * @param id The session id.
     */
    void expire(String id) {
        McpStreamableServerSession session = sessions.remove(id);
        if (session != null) {
            session.closeGracefully().onErrorComplete().block(timeout);
        }
    }

    @Override
    public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
        factory = sessionFactory;
    }

    @Override
    public Mono<Void> notifyClients(String method, Object params) {
        return Flux.fromIterable(sessions.values())
                .flatMap(session -> session.sendNotification(method, params).onErrorComplete())
                .then();
    }

    @Override
    public Mono<Void> notifyClient(String sessionId, String method, Object params) {
        return Mono.defer(
                () -> {
                    McpStreamableServerSession session = sessions.get(sessionId);
                    return session == null
                            ? Mono.empty()
                            : session.sendNotification(method, params);
                });
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.defer(
                () -> {
                    closing = true;
                    return Flux.fromIterable(List.copyOf(sessions.values()))
                            .flatMap(session -> session.closeGracefully().onErrorComplete())
                            .then(Mono.fromRunnable(sessions::clear));
                });
    }

    @Override
    public void handle(Request request, Response response) {
        if (closing) {
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
        } else if (Method.POST.equals(request.getMethod())) {
            post(request, response);
        } else if (Method.GET.equals(request.getMethod())) {
            listen(request, response);
        } else if (Method.DELETE.equals(request.getMethod())) {
            delete(request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
    }

    private void post(Request request, Response response) {
        McpSchema.JSONRPCMessage message;
        try {
            message = McpSchema.deserializeJsonRpcMessage(MAPPER, request.getEntity().getText());
        } catch (IOException | IllegalArgumentException _) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        versions.add(
                String.valueOf(
                        request.getHeaders().getFirstValue(McpProtocol.VERSION_HEADER, true)));
        if (message instanceof McpSchema.JSONRPCRequest rpc) {
            methods.add(rpc.method());
        }
        if (message instanceof McpSchema.JSONRPCRequest rpc
                && McpSchema.METHOD_INITIALIZE.equals(rpc.method())) {
            capabilities.add(
                    rpc.params() instanceof Map<?, ?> params ? params.get("capabilities") : null);
            McpStreamableServerSession.McpStreamableServerSessionInit init =
                    factory.startSession(MAPPER.convertValue(rpc.params(), new TypeRef<>() {}));
            sessions.put(init.session().getId(), init.session());
            McpSchema.InitializeResult result = init.initResult().block(timeout);
            if (!sessionless) {
                response.getHeaders().add(McpProtocol.SESSION_HEADER, init.session().getId());
            }
            json(response, McpSchema.JSONRPCResponse.result(rpc.id(), result));
            return;
        }
        McpStreamableServerSession session = session(request, response);
        if (session == null) {
            return;
        }
        if (message instanceof McpSchema.JSONRPCResponse answer) {
            session.accept(answer).block(timeout);
            response.setStatus(Status.SUCCESS_ACCEPTED);
        } else if (message instanceof McpSchema.JSONRPCNotification notification) {
            session.accept(notification).block(timeout);
            response.setStatus(Status.SUCCESS_ACCEPTED);
        } else if (message instanceof McpSchema.JSONRPCRequest rpc) {
            String tool =
                    rpc.params() instanceof Map<?, ?> params
                                    && params.get("name") instanceof String name
                            ? name
                            : rpc.method();
            var kinds = new CopyOnWriteArrayList<String>();
            replies.put(tool, kinds);
            response.setEntity(
                    new EventStreamRepresentation(
                            sink ->
                                    session.responseStream(new SseTransport(sink, kinds))
                                            .handle(rpc)
                                            .block(timeout)));
        }
    }

    private void listen(Request request, Response response) {
        McpStreamableServerSession session = session(request, response);
        if (session == null) {
            return;
        }
        response.setEntity(
                new EventStreamRepresentation(
                        sink -> {
                            var transport = new SseTransport(sink, null);
                            request.getDisconnectSignal().thenAccept(cause -> transport.close());
                            McpStreamableServerSession.McpStreamableServerSessionStream stream =
                                    session.listeningStream(transport);
                            listening.incrementAndGet();
                            opened.incrementAndGet();
                            try {
                                transport.closed.await();
                            } catch (InterruptedException _) {
                                // A failed heartbeat: the client is gone.
                                Thread.currentThread().interrupt();
                            } finally {
                                stream.releaseTransport();
                                listening.decrementAndGet();
                            }
                        },
                        heartbeat));
    }

    private void delete(Request request, Response response) {
        McpStreamableServerSession session = session(request, response);
        if (session != null) {
            session.delete().block(timeout);
            sessions.remove(session.getId());
            deleted.incrementAndGet();
            response.setStatus(Status.SUCCESS_OK);
        }
    }

    private McpStreamableServerSession session(Request request, Response response) {
        String id = request.getHeaders().getFirstValue(McpProtocol.SESSION_HEADER, true);
        if (id == null && sessionless && sessions.size() == 1) {
            return sessions.values().iterator().next();
        }
        if (id == null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        McpStreamableServerSession session = sessions.get(id);
        if (session == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        return session;
    }

    private static void json(Response response, Object message) {
        try {
            response.setEntity(
                    new StringRepresentation(
                            MAPPER.writeValueAsString(message), MediaType.APPLICATION_JSON));
        } catch (IOException _) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    /** An SDK stream's transport: each message is one SSE event on the Restlet event sink. */
    private static final class SseTransport implements McpStreamableServerTransport {
        private final EventSink sink;
        private final List<String> kinds;
        private final CountDownLatch closed = new CountDownLatch(1);

        private SseTransport(EventSink sink, List<String> kinds) {
            this.sink = sink;
            this.kinds = kinds;
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return sendMessage(message, null);
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromRunnable(
                    () -> {
                        if (closed.getCount() == 0) {
                            return;
                        }
                        // Recorded first: once the client has the event, its trace entry exists.
                        if (kinds != null) {
                            kinds.add(kind(message));
                        }
                        try {
                            sink.send(
                                    ServerSentEvent.builder()
                                            .id(messageId)
                                            .event("message")
                                            .data(MAPPER.writeValueAsString(message))
                                            .build());
                        } catch (IOException e) {
                            close();
                            throw new McpTransportException("SSE write failed", e);
                        }
                    });
        }

        private static String kind(McpSchema.JSONRPCMessage message) {
            if (message instanceof McpSchema.JSONRPCRequest) {
                return "request";
            }
            return message instanceof McpSchema.JSONRPCNotification ? "notification" : "response";
        }

        @Override
        public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
            return MAPPER.convertValue(data, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(this::close);
        }

        @Override
        public void close() {
            closed.countDown();
        }
    }
}
