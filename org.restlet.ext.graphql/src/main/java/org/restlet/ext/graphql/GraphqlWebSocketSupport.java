/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.ext.graphql.internal.GraphqlResultQueue;
import org.restlet.ext.websocket.WebSocketMessage;
import org.restlet.ext.websocket.WebSocketSession;
import org.restlet.ext.websocket.WebSocketUpgrade;

/**
 * Serves the {@code graphql-transport-ws} protocol on WebSocket upgrades of a {@link
 * GraphqlServerAdapter}. Each connection runs on the virtual thread of its session; each operation
 * runs on its own virtual thread.
 */
final class GraphqlWebSocketSupport implements GraphqlUpgradeSupport {

    /** The WebSocket subprotocol. */
    static final String SUBPROTOCOL = "graphql-transport-ws";

    // Close codes of graphql-transport-ws.
    static final int INVALID_MESSAGE = 4400;

    static final int UNAUTHORIZED = 4401;

    static final int INIT_TIMEOUT = 4408;

    static final int SUBSCRIBER_EXISTS = 4409;

    static final int TOO_MANY_INIT = 4429;

    /** The maximum size of a close reason, in UTF-8 bytes. */
    private static final int MAX_CLOSE_REASON_BYTES = 123;

    /** The time given to the open connections to close when stopping. */
    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

    private final GraphqlServerAdapter adapter;

    private final Set<Connection> connections = ConcurrentHashMap.newKeySet();

    private volatile boolean stopped;

    GraphqlWebSocketSupport(GraphqlServerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean handle(Request request, Response response) {
        if (!WebSocketUpgrade.isRequested(request)) {
            return false;
        }
        if (!WebSocketUpgrade.getRequestedSubprotocols(request).contains(SUBPROTOCOL)) {
            response.setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The " + SUBPROTOCOL + " subprotocol is required");
            return true;
        }
        WebSocketUpgrade.accept(
                response, session -> new Connection(request, session).run(), SUBPROTOCOL);
        return true;
    }

    @Override
    public void stop() {
        // Set before the snapshot, so that connections registered later close themselves.
        stopped = true;
        // Closed concurrently, as each close waits for the peer's close handshake.
        List<Thread> closing = new ArrayList<>();
        for (Connection connection : List.copyOf(connections)) {
            closing.add(
                    Thread.ofVirtual()
                            .name("restlet-graphql-ws-close")
                            .start(connection::closeGoingAway));
        }
        long deadline = System.nanoTime() + CLOSE_TIMEOUT.toNanos();
        try {
            for (Thread thread : closing) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0 || !thread.join(Duration.ofNanos(remaining))) {
                    break;
                }
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /** Truncates a close reason to the size allowed in a close frame. */
    static String closeReason(String reason) {
        if (reason == null
                || reason.getBytes(StandardCharsets.UTF_8).length <= MAX_CLOSE_REASON_BYTES) {
            return reason;
        }
        StringBuilder result = new StringBuilder();
        int bytes = 0;
        for (int i = 0; i < reason.length(); ) {
            int codePoint = reason.codePointAt(i);
            int size =
                    new String(Character.toChars(codePoint))
                            .getBytes(StandardCharsets.UTF_8)
                            .length;
            if (bytes + size > MAX_CLOSE_REASON_BYTES) {
                break;
            }
            result.appendCodePoint(codePoint);
            bytes += size;
            i += Character.charCount(codePoint);
        }
        return result.toString();
    }

    private static String message(String type, String id, Object payload) {
        Map<String, Object> message = new LinkedHashMap<>();
        if (id != null) {
            message.put("id", id);
        }
        message.put("type", type);
        if (payload != null) {
            message.put("payload", payload);
        }
        return GraphqlJson.writeString(message);
    }

    private static List<Map<String, Object>> errors(List<GraphQLError> errors) {
        return errors.stream().map(GraphQLError::toSpecification).toList();
    }

    private static List<Map<String, Object>> error(String message) {
        return errors(List.of(GraphqlErrorBuilder.newError().message("%s", message).build()));
    }

    /** A WebSocket connection. */
    private final class Connection {

        private final Request request;

        private final WebSocketSession session;

        private final Map<String, Operation> operations = new ConcurrentHashMap<>();

        private final Object sendLock = new Object();

        private final Object initLock = new Object();

        private boolean initTimedOut;

        private volatile boolean initialized;

        private volatile Map<String, Object> initPayload;

        Connection(Request request, WebSocketSession session) {
            this.request = request;
            this.session = session;
        }

        void run() throws IOException {
            connections.add(this);
            if (stopped) {
                // Accepted while the endpoint was stopping, after its connections were closed.
                connections.remove(this);
                closeGoingAway();
                return;
            }
            Thread timeout =
                    Thread.ofVirtual().name("restlet-graphql-ws-init").start(this::awaitInit);
            try {
                WebSocketMessage received;
                while ((received = session.receive()) != null) {
                    if (!received.isText()) {
                        close(INVALID_MESSAGE, "Binary messages are not supported");
                        return;
                    }
                    if (!dispatch(GraphqlJson.asObject(parse(received.getText())))) {
                        return;
                    }
                }
            } finally {
                timeout.interrupt();
                for (Operation operation : operations.values()) {
                    operation.cancel();
                }
                connections.remove(this);
            }
        }

        private Object parse(String text) {
            try {
                return GraphqlJson.parse(text);
            } catch (GraphqlJson.InvalidJsonException _) {
                return null;
            }
        }

        private void awaitInit() {
            try {
                Thread.sleep(adapter.getConnectionInitTimeout());
            } catch (InterruptedException _) {
                // Interrupted once the connection ends.
                Thread.currentThread().interrupt();
                return;
            }
            synchronized (initLock) {
                if (initialized) {
                    return;
                }
                initTimedOut = true;
            }
            close(INIT_TIMEOUT, "Connection initialisation timeout");
        }

        /** Handles a message. Returns false once the connection is closed. */
        private boolean dispatch(Map<String, Object> message) {
            Object type = message == null ? null : message.get("type");
            if (!(type instanceof String)) {
                return close(INVALID_MESSAGE, "Invalid message");
            }
            switch ((String) type) {
                case "connection_init" -> {
                    if (initialized) {
                        return close(TOO_MANY_INIT, "Too many initialisation requests");
                    }
                    Object payload = message.get("payload");
                    if (payload != null && GraphqlJson.asObject(payload) == null) {
                        return close(INVALID_MESSAGE, "Invalid connection_init payload");
                    }
                    synchronized (initLock) {
                        if (initTimedOut) {
                            return false;
                        }
                        initPayload = GraphqlJson.asObject(payload);
                        initialized = true;
                    }
                    return send(message("connection_ack", null, null));
                }
                case "ping" -> {
                    if (invalidObject(message, "payload")) {
                        return close(INVALID_MESSAGE, "Invalid ping payload");
                    }
                    return send(message("pong", null, null));
                }
                case "pong" -> {
                    if (invalidObject(message, "payload")) {
                        return close(INVALID_MESSAGE, "Invalid pong payload");
                    }
                    return true;
                }
                case "subscribe" -> {
                    return subscribe(message);
                }
                case "complete" -> {
                    if (!(message.get("id") instanceof String id)) {
                        return close(INVALID_MESSAGE, "Invalid complete message");
                    }
                    Operation operation = operations.remove(id);
                    if (operation != null) {
                        operation.cancel();
                    }
                    return true;
                }
                default -> {
                    return close(INVALID_MESSAGE, "Unknown message type '" + type + "'");
                }
            }
        }

        private boolean subscribe(Map<String, Object> message) {
            if (!initialized) {
                return close(UNAUTHORIZED, "Unauthorized");
            }
            Map<String, Object> payload = GraphqlJson.asObject(message.get("payload"));
            if (!(message.get("id") instanceof String id)
                    || payload == null
                    || !(payload.get("query") instanceof String query)
                    || (payload.get("operationName") != null
                            && !(payload.get("operationName") instanceof String))
                    || invalidObject(payload, "variables")
                    || invalidObject(payload, "extensions")) {
                return close(INVALID_MESSAGE, "Invalid subscribe message");
            }

            ExecutionInput.Builder input =
                    ExecutionInput.newExecutionInput()
                            .query(query)
                            .operationName((String) payload.get("operationName"));
            Map<String, Object> variables = GraphqlJson.asObject(payload.get("variables"));
            if (variables != null) {
                input.variables(variables);
            }
            Map<String, Object> extensions = GraphqlJson.asObject(payload.get("extensions"));
            if (extensions != null) {
                input.extensions(extensions);
            }

            Operation operation = new Operation(this, id, input);
            if (operations.putIfAbsent(id, operation) != null) {
                return close(SUBSCRIBER_EXISTS, "Subscriber for " + id + " already exists");
            }
            Thread.ofVirtual().name("restlet-graphql-ws").start(operation);
            return true;
        }

        private boolean invalidObject(Map<String, Object> payload, String name) {
            Object value = payload.get(name);
            return value != null && GraphqlJson.asObject(value) == null;
        }

        /** Sends a message. Returns false if the session is closed. */
        boolean send(String text) {
            synchronized (sendLock) {
                try {
                    session.sendText(text);
                    return true;
                } catch (IOException _) {
                    return false;
                }
            }
        }

        /**
         * Sends a message of an operation, unless the operation is no longer registered under its
         * ID, so that a reused ID never receives the messages of the operation it replaces. A
         * terminal message unregisters the operation first, so the ID can be reused as soon as it
         * is received.
         *
         * @return False if the operation is retired or the session is closed.
         */
        boolean send(Operation operation, String text, boolean terminal) {
            synchronized (sendLock) {
                boolean registered =
                        terminal
                                ? operations.remove(operation.id, operation)
                                : operations.get(operation.id) == operation;
                return registered && send(text);
            }
        }

        void closeGoingAway() {
            close(WebSocketSession.GOING_AWAY, "The GraphQL endpoint is stopping");
        }

        /** Closes the connection. Returns false, as a convenience for the dispatch loop. */
        boolean close(int code, String reason) {
            try {
                session.close(code, closeReason(reason));
            } catch (IOException e) {
                adapter.getLogger().log(Level.FINE, "Unable to close a GraphQL WebSocket", e);
            }
            return false;
        }
    }

    /** An operation of a connection. */
    private final class Operation implements Runnable {

        private final Connection connection;

        private final String id;

        private final ExecutionInput.Builder input;

        private volatile GraphqlResultQueue queue;

        private volatile boolean cancelled;

        Operation(Connection connection, String id, ExecutionInput.Builder input) {
            this.connection = connection;
            this.id = id;
            this.input = input;
        }

        void cancel() {
            cancelled = true;
            GraphqlResultQueue current = queue;
            if (current != null) {
                current.cancelByClient();
            }
        }

        @Override
        public void run() {
            AtomicInteger active = adapter.getActiveSubscriptionCounter();
            boolean reserved = false;
            try {
                // A complete message or a closed connection can come before this thread starts.
                if (cancelled) {
                    return;
                }
                ExecutionInput executionInput =
                        adapter.prepare(connection.request, input, connection.initPayload);
                if (adapter.isSubscription(executionInput)) {
                    // Reserved before execution, which sets up the subscription.
                    if (active.incrementAndGet() > adapter.getMaxSubscriptions()) {
                        active.decrementAndGet();
                        send(message("error", id, error("Too many active subscriptions")), true);
                        return;
                    }
                    reserved = true;
                }
                if (cancelled) {
                    return;
                }
                ExecutionResult result = adapter.execute(executionInput);
                if (cancelled) {
                    return;
                }
                if (!result.isDataPresent()) {
                    send(message("error", id, errors(result.getErrors())), true);
                } else if (result.getData() instanceof Publisher<?> publisher) {
                    @SuppressWarnings("unchecked")
                    Publisher<ExecutionResult> results = (Publisher<ExecutionResult>) publisher;
                    stream(results);
                } else if (send(message("next", id, result.toSpecification()), false)) {
                    send(message("complete", id, null), true);
                }
            } catch (RuntimeException e) {
                adapter.getLogger().log(Level.WARNING, "GraphQL operation failed", e);
                send(message("error", id, error("The operation failed")), true);
            } finally {
                if (reserved) {
                    active.decrementAndGet();
                }
                connection.operations.remove(id, this);
            }
        }

        private boolean send(String text, boolean terminal) {
            return connection.send(this, text, terminal);
        }

        private void stream(Publisher<ExecutionResult> results) {
            GraphqlResultQueue current = new GraphqlResultQueue(adapter.getMaxQueuedBytes());
            queue = current;
            try {
                if (cancelled) {
                    return;
                }
                results.subscribe(current);
                while (true) {
                    GraphqlResultQueue.Signal signal = current.take();
                    if (signal instanceof GraphqlResultQueue.Next next) {
                        if (!send(
                                "{\"id\":"
                                        + GraphqlJson.writeString(id)
                                        + ",\"type\":\"next\",\"payload\":"
                                        + next.json()
                                        + "}",
                                false)) {
                            return;
                        }
                        current.request();
                    } else if (signal instanceof GraphqlResultQueue.Failure failure) {
                        if (failure.cause() != null) {
                            adapter.getLogger()
                                    .log(
                                            Level.WARNING,
                                            "GraphQL subscription failed",
                                            failure.cause());
                        }
                        send(message("error", id, error(failure.message())), true);
                        return;
                    } else if (signal instanceof GraphqlResultQueue.Completed) {
                        send(message("complete", id, null), true);
                        return;
                    } else {
                        return;
                    }
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            } finally {
                current.cancel();
            }
        }
    }
}
