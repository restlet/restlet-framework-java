/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.ext.websocket.WebSocketClient;
import org.restlet.ext.websocket.WebSocketMessage;
import org.restlet.ext.websocket.WebSocketSession;

/**
 * Sends the subscriptions of a {@link GraphqlClientAdapter} with {@code graphql-transport-ws}, all
 * multiplexed over one WebSocket connection opened on the first demand. The {@link WebSocketClient}
 * shares the event loop of the adapter's borrowed HTTP client.
 *
 * <p>The protocol has no flow control: results received without demand are buffered, up to the
 * adapter's maximum response size per subscription, beyond which the subscription fails.
 */
final class GraphqlWebSocketTransport implements GraphqlSubscriptionTransport {

    private static final Duration ACK_TIMEOUT = Duration.ofSeconds(10);

    private final GraphqlClientAdapter adapter;

    private final WebSocketClient webSocketClient;

    private final AtomicLong ids = new AtomicLong();

    private final Map<String, Operation> operations = new ConcurrentHashMap<>();

    private Connection connection;

    private boolean stopped;

    GraphqlWebSocketTransport(GraphqlClientAdapter adapter) throws IOException {
        this.adapter = adapter;
        this.webSocketClient = new WebSocketClient();
        webSocketClient.setClient(adapter.getClient());
        webSocketClient.setMaxMessageSize(
                (int) Math.min(adapter.getMaxResponseBytes(), Integer.MAX_VALUE));
        webSocketClient.start();
    }

    @Override
    public Publisher<ExecutionResult> subscribe(
            String document, String operationName, Map<String, Object> variables) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", document);
        if (operationName != null) {
            payload.put("operationName", operationName);
        }
        if (variables != null && !variables.isEmpty()) {
            payload.put("variables", variables);
        }
        return subscriber -> {
            Objects.requireNonNull(subscriber, "subscriber");
            subscriber.onSubscribe(new Operation(subscriber, payload));
        };
    }

    @Override
    public void stop() {
        Connection current;
        synchronized (this) {
            stopped = true;
            current = connection;
            connection = null;
        }
        GraphqlTransportException error =
                new GraphqlTransportException("The GraphQL client was stopped", null, null, null);
        for (Operation operation : List.copyOf(operations.values())) {
            operation.fail(error);
        }
        if (current != null) {
            current.close(WebSocketSession.NORMAL_CLOSURE, null);
        }
        webSocketClient.stop();
    }

    /** Returns the open connection, opening one if needed. */
    private synchronized Connection connection() throws GraphqlTransportException {
        if (stopped) {
            throw new GraphqlTransportException("The GraphQL client was stopped", null, null, null);
        }
        if (connection == null || !connection.session.isOpen()) {
            connection = open();
        }
        return connection;
    }

    private Connection open() throws GraphqlTransportException {
        URI uri = URI.create(adapter.getEndpoint().replaceFirst("^http", "ws"));
        // The request customizer adds handshake headers, such as an API key.
        Request handshake = new Request(Method.GET, adapter.getEndpoint());
        if (adapter.getRequestCustomizer() != null) {
            adapter.getRequestCustomizer().accept(handshake);
        }
        Map<String, String> headers = new LinkedHashMap<>();
        for (Header header : handshake.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }

        WebSocketSession session;
        try {
            session =
                    webSocketClient.connect(
                            uri, List.of(GraphqlWebSocketSupport.SUBPROTOCOL), headers);
        } catch (IOException e) {
            throw new GraphqlTransportException(
                    "Unable to open the GraphQL WebSocket", null, null, e);
        }
        Connection opened = new Connection(session);
        if (!GraphqlWebSocketSupport.SUBPROTOCOL.equals(session.getSubprotocol())) {
            opened.close(WebSocketSession.PROTOCOL_ERROR, null);
            throw new GraphqlTransportException(
                    "The server did not accept the graphql-transport-ws subprotocol",
                    null,
                    null,
                    null);
        }
        Thread.ofVirtual().name("restlet-graphql-ws-client").start(opened::read);
        Map<String, Object> init = new LinkedHashMap<>();
        init.put("type", "connection_init");
        if (adapter.getConnectionInitPayload() != null) {
            init.put("payload", adapter.getConnectionInitPayload());
        }
        try {
            opened.send(GraphqlJson.writeString(init));
            opened.acknowledged.get(ACK_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (IOException | ExecutionException | TimeoutException e) {
            opened.close(WebSocketSession.NORMAL_CLOSURE, null);
            throw new GraphqlTransportException(
                    "The GraphQL WebSocket was not acknowledged", null, null, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            opened.close(WebSocketSession.NORMAL_CLOSURE, null);
            throw new GraphqlTransportException(
                    "Interrupted while opening the GraphQL WebSocket", null, null, e);
        }
        return opened;
    }

    /** One WebSocket connection, read by a virtual thread. */
    private final class Connection {

        private final WebSocketSession session;

        private final CompletableFuture<Void> acknowledged = new CompletableFuture<>();

        private final Object sendLock = new Object();

        Connection(WebSocketSession session) {
            this.session = session;
        }

        void send(String text) throws IOException {
            synchronized (sendLock) {
                session.sendText(text);
            }
        }

        void close(int code, String reason) {
            try {
                session.close(code, reason);
            } catch (IOException _) {
                // Closing anyway
            }
        }

        void read() {
            try {
                WebSocketMessage received;
                while ((received = session.receive()) != null) {
                    if (received.isText()) {
                        dispatch(received.getText());
                    }
                }
            } catch (IOException | RuntimeException _) {
                // Reported below as a closed connection
            }
            GraphqlTransportException closed =
                    new GraphqlTransportException(
                            "The GraphQL WebSocket closed: "
                                    + session.getCloseCode()
                                    + (session.getCloseReason() == null
                                            ? ""
                                            : " " + session.getCloseReason()),
                            null,
                            null,
                            null);
            acknowledged.completeExceptionally(closed);
            for (Operation operation : List.copyOf(operations.values())) {
                if (operation.connection == this) {
                    operation.fail(closed);
                }
            }
        }

        private void dispatch(String text) throws IOException {
            Map<String, Object> message = GraphqlJson.asObject(GraphqlJson.parse(text));
            Object type = message == null ? null : message.get("type");
            Operation operation =
                    message != null && message.get("id") instanceof String id
                            ? operations.get(id)
                            : null;
            if ("connection_ack".equals(type)) {
                acknowledged.complete(null);
            } else if ("ping".equals(type)) {
                send("{\"type\":\"pong\"}");
            } else if (operation != null && "next".equals(type)) {
                Map<String, Object> payload = GraphqlJson.asObject(message.get("payload"));
                operation.next(
                        ExecutionResult.fromSpecification(payload == null ? Map.of() : payload),
                        text.getBytes(StandardCharsets.UTF_8).length);
            } else if (operation != null && "error".equals(type)) {
                List<GraphQLError> errors =
                        message.get("payload") instanceof List<?> list
                                ? list.stream()
                                        .map(GraphqlJson::asObject)
                                        .filter(Objects::nonNull)
                                        .map(GraphQLError::fromSpecification)
                                        .toList()
                                : List.of();
                operation.error(ExecutionResult.newExecutionResult().errors(errors).build());
            } else if (operation != null && "complete".equals(type)) {
                operation.complete();
            }
        }
    }

    /** A signal for the delivery thread of an operation. */
    private record Signal(ExecutionResult result, long size, boolean last, Throwable error) {}

    /** One operation, delivered to its subscriber by its own virtual thread. */
    private final class Operation implements Subscription {

        private final Subscriber<? super ExecutionResult> subscriber;

        private final Map<String, Object> payload;

        private final LinkedBlockingQueue<Signal> inbox = new LinkedBlockingQueue<>();

        private final String id = Long.toString(ids.incrementAndGet());

        private volatile Connection connection;

        private long demand;

        private long bufferedBytes;

        private boolean started;

        private boolean invalidDemand;

        private volatile boolean cancelled;

        private volatile boolean terminated;

        private Throwable failure;

        Operation(Subscriber<? super ExecutionResult> subscriber, Map<String, Object> payload) {
            this.subscriber = subscriber;
            this.payload = payload;
        }

        @Override
        public void request(long n) {
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                if (n <= 0) {
                    invalidDemand = true;
                } else {
                    demand = demand + n < 0 ? Long.MAX_VALUE : demand + n;
                }
                notifyAll();
                if (started) {
                    return;
                }
                started = true;
            }
            Thread.ofVirtual().name("restlet-graphql-ws-operation").start(this::run);
        }

        @Override
        public void cancel() {
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                notifyAll();
            }
            inbox.add(new Signal(null, 0, true, null));
            if (operations.remove(id, this) && !terminated) {
                Connection current = connection;
                if (current != null) {
                    try {
                        current.send("{\"id\":\"" + id + "\",\"type\":\"complete\"}");
                    } catch (IOException _) {
                        // The connection is closing
                    }
                }
            }
        }

        void next(ExecutionResult result, long size) throws IOException {
            synchronized (this) {
                bufferedBytes += size;
                if (bufferedBytes <= adapter.getMaxResponseBytes()) {
                    inbox.add(new Signal(result, size, false, null));
                    return;
                }
            }
            // Still registered: tell the server to complete the subscription.
            boolean registered = operations.remove(id, this);
            fail(
                    new GraphqlTransportException(
                            "The subscription exceeded "
                                    + adapter.getMaxResponseBytes()
                                    + " buffered bytes",
                            null,
                            null,
                            null));
            if (registered) {
                connection.send("{\"id\":\"" + id + "\",\"type\":\"complete\"}");
            }
        }

        void error(ExecutionResult result) {
            terminated = true;
            operations.remove(id, this);
            inbox.add(new Signal(result, 0, true, null));
        }

        void complete() {
            terminated = true;
            operations.remove(id, this);
            inbox.add(new Signal(null, 0, true, null));
        }

        /** Fails the operation at once, even ahead of buffered results. */
        void fail(Throwable error) {
            terminated = true;
            operations.remove(id, this);
            synchronized (this) {
                if (failure == null) {
                    failure = error;
                }
                notifyAll();
            }
            inbox.add(new Signal(null, 0, true, error));
        }

        private void run() {
            try {
                Connection current = connection();
                connection = current;
                operations.put(id, this);
                if (cancelled) {
                    operations.remove(id, this);
                    return;
                }
                Map<String, Object> subscribe = new LinkedHashMap<>();
                subscribe.put("id", id);
                subscribe.put("type", "subscribe");
                subscribe.put("payload", payload);
                current.send(GraphqlJson.writeString(subscribe));
                deliver();
            } catch (GraphqlTransportException e) {
                operations.remove(id, this);
                signalError(e);
            } catch (IOException e) {
                operations.remove(id, this);
                signalError(
                        new GraphqlTransportException(
                                "Unable to send the GraphQL subscription", null, null, e));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void deliver() throws InterruptedException {
            while (true) {
                Signal signal = inbox.take();
                synchronized (this) {
                    bufferedBytes -= signal.size();
                }
                if (cancelled) {
                    return;
                }
                if (signal.error() != null) {
                    signalError(signal.error());
                    return;
                }
                if (signal.result() != null) {
                    if (!awaitDemand()) {
                        Throwable error = failure();
                        if (error != null) {
                            signalError(error);
                        }
                        return;
                    }
                    subscriber.onNext(signal.result());
                }
                if (signal.last()) {
                    subscriber.onComplete();
                    return;
                }
            }
        }

        private void signalError(Throwable error) {
            if (!cancelled) {
                cancelled = true;
                subscriber.onError(error);
            }
        }

        private synchronized Throwable failure() {
            return failure;
        }

        /** Waits for demand. Returns false if cancelled or failed meanwhile. */
        private boolean awaitDemand() throws InterruptedException {
            synchronized (this) {
                while (!cancelled && failure == null && demand == 0 && !invalidDemand) {
                    wait();
                }
                if (cancelled || failure != null) {
                    return false;
                }
                if (invalidDemand) {
                    cancelled = true;
                    subscriber.onError(
                            new IllegalArgumentException(
                                    "The requested number of results must be positive"));
                    return false;
                }
                if (demand != Long.MAX_VALUE) {
                    demand--;
                }
                return true;
            }
        }
    }
}
