/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

class WebSocketTestCase {

    private Server server;

    private WebSocketClient client;

    @BeforeEach
    void setUp() throws IOException {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        client = new WebSocketClient();
        client.setConnectTimeout(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
        // Restore the default engine, with its discovered helpers, for the tests that follow.
        Engine.register();
    }

    private static WebSocketUpgrader upgrader(WebSocketHandler handler) {
        return new WebSocketUpgrader() {
            @Override
            protected WebSocketHandler accept(Request request, Response response) {
                return handler;
            }

            @Override
            protected String selectSubprotocol(List<String> requested) {
                return requested.contains("chat") ? "chat" : null;
            }
        };
    }

    private static final WebSocketHandler ECHO =
            session -> {
                WebSocketMessage message;
                while ((message = session.receive()) != null) {
                    session.send(message);
                }
            };

    private String start(Restlet root, Context context) throws Exception {
        server = new Server(context, Protocol.HTTP, 0, root);
        server.start();
        return "ws://localhost:" + server.getActualPort();
    }

    @Test
    void echoesTextAndBinaryMessagesWithSubprotocol() throws Exception {
        String base = start(upgrader(ECHO), new Context());

        try (WebSocketSession session =
                client.connect(URI.create(base + "/echo"), List.of("other", "chat"), Map.of())) {
            assertEquals("chat", session.getSubprotocol());
            session.sendText("héllo");
            WebSocketMessage text = session.receive();
            assertTrue(text.isText());
            assertEquals("héllo", text.getText());

            byte[] bytes = {0, 1, 2, (byte) 255};
            session.sendBinary(bytes);
            WebSocketMessage binary = session.receive();
            assertTrue(binary.isBinary());
            assertArrayEquals(bytes, binary.getData());
        }
    }

    @Test
    void serverHandlerReturningClosesTheSessionNormally() throws Exception {
        String base =
                start(
                        upgrader(
                                session -> {
                                    session.sendText("bye");
                                }),
                        new Context());

        WebSocketSession session = client.connect(base + "/");
        assertEquals("bye", session.receive().getText());
        assertNull(session.receive());
        assertEquals(WebSocketSession.NORMAL_CLOSURE, session.getCloseCode());
        assertFalse(session.isOpen());
    }

    @Test
    void clientCloseIsReportedToTheServer() throws Exception {
        AtomicReference<Integer> serverCode = new AtomicReference<>();
        AtomicReference<String> serverReason = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);
        String base =
                start(
                        upgrader(
                                session -> {
                                    assertNull(session.receive());
                                    serverCode.set(session.getCloseCode());
                                    serverReason.set(session.getCloseReason());
                                    closed.countDown();
                                }),
                        new Context());

        WebSocketSession session = client.connect(base + "/");
        session.close(4000, "done");

        assertTrue(closed.await(5, TimeUnit.SECONDS));
        assertEquals(4000, serverCode.get());
        assertEquals("done", serverReason.get());
        assertEquals(4000, session.getCloseCode());
        assertThrows(IOException.class, () -> session.sendText("late"));
    }

    @Test
    void largerMessagesThanTheLimitCloseTheSession() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.maxMessageSize", "16");
        String base = start(upgrader(ECHO), context);

        WebSocketSession session = client.connect(base + "/");
        session.sendText("x".repeat(64));
        assertNull(session.receive());
        assertEquals(WebSocketSession.MESSAGE_TOO_BIG, session.getCloseCode());
    }

    @Test
    void routingAndAuthenticationApplyToTheUpgrade() throws Exception {
        Application application =
                new Application() {
                    @Override
                    public Restlet createInboundRoot() {
                        Router router = new Router(getContext());
                        router.attach("/echo", upgrader(ECHO));
                        Authenticator guard =
                                new Authenticator(getContext()) {
                                    @Override
                                    protected boolean authenticate(
                                            Request request, Response response) {
                                        boolean ok =
                                                "secret"
                                                        .equals(
                                                                request.getHeaders()
                                                                        .getFirstValue(
                                                                                "X-Token", true));
                                        if (!ok) {
                                            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                                        }
                                        return ok;
                                    }
                                };
                        guard.setNext(router);
                        return guard;
                    }
                };
        Component component = new Component();
        Server componentServer = component.getServers().add(Protocol.HTTP, 0);
        component.getDefaultHost().attach("/app", application);
        component.start();
        String base = "ws://localhost:" + componentServer.getActualPort() + "/app";

        try {
            IOException rejected =
                    assertThrows(IOException.class, () -> client.connect(base + "/echo"));
            assertTrue(rejected.getMessage().contains("401"), rejected.getMessage());

            try (WebSocketSession session =
                    client.connect(
                            URI.create(base + "/echo"), List.of(), Map.of("X-Token", "secret"))) {
                session.sendText("ok");
                assertEquals("ok", session.receive().getText());
            }

            IOException missing =
                    assertThrows(
                            IOException.class,
                            () ->
                                    client.connect(
                                            URI.create(base + "/missing"),
                                            List.of(),
                                            Map.of("X-Token", "secret")));
            assertTrue(missing.getMessage().contains("404"), missing.getMessage());
        } finally {
            component.stop();
        }
    }

    @Test
    void plainRequestsToAWebSocketUpgraderAreRejected() throws Exception {
        String base = start(upgrader(ECHO), new Context());
        Client http = new Client(Protocol.HTTP);
        http.start();
        try {
            Response response =
                    http.handle(new Request(Method.GET, base.replace("ws:", "http:") + "/"));
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
        } finally {
            http.stop();
        }
    }

    @Test
    void enforcesTheOpenSessionLimit() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.maxSessions", "1");
        String base = start(upgrader(ECHO), context);

        WebSocketSession first = client.connect(base + "/");
        IOException rejected = assertThrows(IOException.class, () -> client.connect(base + "/"));
        assertTrue(rejected.getMessage().contains("503"), rejected.getMessage());

        first.close();
        // The permit is released when the first connection is closed.
        WebSocketSession second = null;
        for (int i = 0; i < 50 && second == null; i++) {
            try {
                second = client.connect(base + "/");
            } catch (IOException _) {
                Thread.sleep(50);
            }
        }
        assertTrue(second != null && second.isOpen());
        second.close();
    }

    @Test
    void interoperatesWithTheJdkWebSocketClient() throws Exception {
        String base = start(upgrader(ECHO), new Context());
        CompletableFuture<String> reply = new CompletableFuture<>();
        HttpClient jdk = HttpClient.newHttpClient();
        WebSocket ws =
                jdk.newWebSocketBuilder()
                        .buildAsync(
                                URI.create(base + "/"),
                                new WebSocket.Listener() {
                                    private final StringBuilder text = new StringBuilder();

                                    @Override
                                    public CompletionStage<?> onText(
                                            WebSocket webSocket, CharSequence data, boolean last) {
                                        text.append(data);
                                        if (last) {
                                            reply.complete(text.toString());
                                        }
                                        webSocket.request(1);
                                        return null;
                                    }
                                })
                        .get(5, TimeUnit.SECONDS);
        try {
            ws.sendText("from jdk", true).get(5, TimeUnit.SECONDS);
            assertEquals("from jdk", reply.get(5, TimeUnit.SECONDS));
        } catch (CompletionException e) {
            throw e;
        } finally {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "").get(5, TimeUnit.SECONDS);
            jdk.close();
        }
    }
}
