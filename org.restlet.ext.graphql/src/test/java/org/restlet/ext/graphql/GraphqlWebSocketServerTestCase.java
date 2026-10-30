/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.graphql.internal.GraphqlJson;

/**
 * The {@code graphql-transport-ws} server of {@link GraphqlServerAdapter}, driven by the JDK
 * WebSocket client.
 */
class GraphqlWebSocketServerTestCase {

    private Component component;

    private GraphqlServerAdapter adapter;

    private HttpClient http;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        adapter.getSubscriptionTransports().add(GraphqlTransport.WEBSOCKET);
        adapter.setConnectionInitTimeout(Duration.ofMillis(300));
        component.getDefaultHost().attach("/graphql", adapter);
        component.start();
        base = "localhost:" + component.getServers().get(0).getActualPort() + "/graphql";
        http = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        http.close();
        component.stop();
    }

    private Peer connect() throws Exception {
        Peer peer = new Peer();
        peer.socket =
                http.newWebSocketBuilder()
                        .subprotocols(GraphqlWebSocketSupport.SUBPROTOCOL)
                        .buildAsync(URI.create("ws://" + base), peer)
                        .get(5, TimeUnit.SECONDS);
        return peer;
    }

    private Peer initialized(Map<String, Object> payload) throws Exception {
        Peer peer = connect();
        peer.send(
                payload == null
                        ? Map.of("type", "connection_init")
                        : Map.of("type", "connection_init", "payload", payload));
        assertEquals(Map.of("type", "connection_ack"), peer.next());
        return peer;
    }

    private static Map<String, Object> subscribe(String id, String query) {
        return Map.of("id", id, "type", "subscribe", "payload", Map.of("query", query));
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "Timed out");
            Thread.sleep(10);
        }
    }

    @Test
    void handshakeAndPing() throws Exception {
        Peer peer = initialized(Map.of("token", "t"));
        assertEquals("graphql-transport-ws", peer.socket.getSubprotocol());
        peer.send(Map.of("type", "ping"));
        assertEquals(Map.of("type", "pong"), peer.next());
        peer.send(Map.of("type", "pong"));
        peer.send(subscribe("1", "{ initToken }"));
        assertEquals(
                Map.of(
                        "id",
                        "1",
                        "type",
                        "next",
                        "payload",
                        Map.of("data", Map.of("initToken", "t"))),
                peer.next());
        assertEquals(Map.of("id", "1", "type", "complete"), peer.next());
        peer.close();
    }

    @Test
    void subscriptionStreamsNextThenComplete() throws Exception {
        Peer peer = initialized(null);
        peer.send(subscribe("s", "subscription { ticks(count: 3) }"));
        for (int i = 1; i <= 3; i++) {
            assertEquals(
                    Map.of(
                            "id",
                            "s",
                            "type",
                            "next",
                            "payload",
                            Map.of("data", Map.of("ticks", i))),
                    peer.next());
        }
        assertEquals(Map.of("id", "s", "type", "complete"), peer.next());
        await(() -> adapter.getActiveSubscriptions() == 0);
        peer.close();
    }

    @Test
    void errorsBeforeExecutionAreErrorMessages() throws Exception {
        Peer peer = initialized(null);
        peer.send(subscribe("e", "subscription { unknown }"));
        Map<String, Object> error = peer.next();
        assertEquals("error", error.get("type"));
        assertEquals("e", error.get("id"));
        assertInstanceOf(List.class, error.get("payload"));
        // No complete after an error: the next message answers a ping.
        peer.send(Map.of("type", "ping"));
        assertEquals(Map.of("type", "pong"), peer.next());
        peer.close();
    }

    @Test
    void severalSubscriptionsPerConnection() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        Peer peer = initialized(null);
        peer.send(subscribe("a", "subscription { idle }"));
        peer.send(subscribe("b", "subscription { idle }"));
        await(() -> adapter.getActiveSubscriptions() == 2);
        peer.send(Map.of("id", "a", "type", "complete"));
        await(() -> adapter.getActiveSubscriptions() == 1);
        assertEquals(cancelled + 1, TickPublisher.CANCELLED.get());

        // The id is free again once completed.
        peer.send(subscribe("a", "{ hello }"));
        assertEquals("next", peer.next().get("type"));
        assertEquals("complete", peer.next().get("type"));

        peer.close();
        await(() -> adapter.getActiveSubscriptions() == 0);
        assertEquals(cancelled + 2, TickPublisher.CANCELLED.get());
    }

    @Test
    void protocolViolationsClose() throws Exception {
        Peer early = connect();
        early.send(subscribe("1", "{ hello }"));
        assertEquals(4401, early.closeCode());

        Peer slow = connect();
        assertEquals(4408, slow.closeCode());

        Peer twice = initialized(null);
        twice.send(Map.of("type", "connection_init"));
        assertEquals(4429, twice.closeCode());

        Peer duplicate = initialized(null);
        duplicate.send(subscribe("d", "subscription { idle }"));
        duplicate.send(subscribe("d", "subscription { idle }"));
        assertEquals(4409, duplicate.closeCode());

        for (String text :
                List.of(
                        "not json",
                        "{\"type\": \"unknown\"}",
                        "{\"id\": \"1\", \"type\": \"subscribe\"}",
                        "{\"type\": \"complete\"}",
                        "{\"type\": \"connection_init\", \"payload\": 1}",
                        "{\"id\": \"1\", \"type\": \"subscribe\", \"payload\": {\"query\": \"{ hello }\", \"variables\": []}}")) {
            Peer invalid = text.contains("connection_init") ? connect() : initialized(null);
            invalid.socket.sendText(text, true).get(5, TimeUnit.SECONDS);
            assertEquals(4400, invalid.closeCode(), text);
        }

        Peer binary = initialized(null);
        binary.socket.sendBinary(java.nio.ByteBuffer.wrap(new byte[] {1}), true).get();
        assertEquals(4400, binary.closeCode());
        await(() -> adapter.getActiveSubscriptions() == 0);
    }

    @Test
    void subprotocolIsRequired() {
        ExecutionException e =
                assertThrows(
                        ExecutionException.class,
                        () ->
                                http.newWebSocketBuilder()
                                        .subprotocols("graphql-ws")
                                        .buildAsync(URI.create("ws://" + base), new Peer())
                                        .get(5, TimeUnit.SECONDS));
        WebSocketHandshakeException handshake =
                assertInstanceOf(WebSocketHandshakeException.class, e.getCause());
        assertEquals(400, handshake.getResponse().statusCode());
    }

    @Test
    void httpStillWorksOnTheSamePath() throws Exception {
        Client client = new Client(Protocol.HTTP);
        client.start();
        try {
            GraphqlClientAdapter graphql = new GraphqlClientAdapter();
            graphql.setClient(client);
            graphql.setEndpoint("http://" + base);
            graphql.start();
            assertEquals(
                    Map.of("hello", "Hello world"),
                    graphql.execute("{ hello }", null, null).getData());
        } finally {
            client.stop();
        }
    }

    @Test
    void stoppingTheAdapterClosesConnections() throws Exception {
        Peer peer = initialized(null);
        peer.send(subscribe("s", "subscription { idle }"));
        await(() -> adapter.getActiveSubscriptions() == 1);
        adapter.stop();
        assertEquals(1001, peer.closeCode());
        await(() -> adapter.getActiveSubscriptions() == 0);
    }

    @Test
    void configurationIsValidated() {
        GraphqlServerAdapter other = new GraphqlServerAdapter();
        assertEquals(
                GraphqlServerAdapter.DEFAULT_CONNECTION_INIT_TIMEOUT,
                other.getConnectionInitTimeout());
        assertThrows(IllegalArgumentException.class, () -> other.setConnectionInitTimeout(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> other.setConnectionInitTimeout(Duration.ZERO));
        assertThrows(
                IllegalStateException.class,
                () -> adapter.setConnectionInitTimeout(Duration.ofSeconds(1)));
    }

    /** A JDK WebSocket listener collecting JSON messages and the close code. */
    private static final class Peer implements WebSocket.Listener {

        final BlockingQueue<Map<String, Object>> messages = new LinkedBlockingQueue<>();

        final CompletableFuture<Integer> closed = new CompletableFuture<>();

        private final StringBuilder text = new StringBuilder();

        WebSocket socket;

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            text.append(data);
            if (last) {
                messages.add(GraphqlJson.asObject(GraphqlJson.parse(text.toString())));
                text.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closed.complete(statusCode);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            closed.complete(-1);
        }

        void send(Map<String, Object> message) throws Exception {
            socket.sendText(GraphqlJson.writeString(message), true).get(5, TimeUnit.SECONDS);
        }

        Map<String, Object> next() throws InterruptedException {
            Map<String, Object> message = messages.poll(5, TimeUnit.SECONDS);
            assertTrue(message != null, "Timed out waiting for a message");
            return message;
        }

        int closeCode() throws Exception {
            return closed.get(5, TimeUnit.SECONDS);
        }

        void close() throws Exception {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "").get(5, TimeUnit.SECONDS);
            assertNull(messages.poll(50, TimeUnit.MILLISECONDS));
        }
    }
}
