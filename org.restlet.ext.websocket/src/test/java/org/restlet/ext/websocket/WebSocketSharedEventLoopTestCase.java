/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.engine.netty.NettyClientHelper;

/** Tests WebSocket clients sharing the I/O threads of a borrowed HTTP client. */
class WebSocketSharedEventLoopTestCase {

    private Server server;

    private Client http;

    private String uri;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new WebSocketUpgrader() {
                            @Override
                            protected WebSocketHandler accept(Request request, Response response) {
                                return session -> {
                                    WebSocketMessage message;
                                    while ((message = session.receive()) != null) {
                                        session.send(message);
                                    }
                                };
                            }
                        });
        server.start();
        uri = "ws://localhost:" + server.getActualPort() + "/";
        http = new Client(new Context(), Protocol.HTTP);
    }

    @AfterEach
    void tearDown() throws Exception {
        http.stop();
        server.stop();
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    private NettyClientHelper helper(Client client) {
        return (NettyClientHelper)
                client.getContext().getAttributes().get("org.restlet.engine.helper");
    }

    @Test
    void stoppingKeepsTheBorrowedEventLoop() throws Exception {
        http.start();
        WebSocketClient client = new WebSocketClient();
        client.setClient(http);
        assertSame(http, client.getClient());

        WebSocketSession session = client.connect(uri);
        session.sendText("shared");
        assertEquals("shared", session.receive().getText());

        client.stop();
        assertNull(session.receive());
        assertFalse(session.isOpen());
        assertFalse(helper(http).getEventLoopGroup().isShuttingDown());

        // The borrowed client still serves new WebSocket clients.
        try (WebSocketClient other = new WebSocketClient()) {
            other.setClient(http);
            WebSocketSession again = other.connect(uri);
            again.sendText("again");
            assertEquals("again", again.receive().getText());
        }
    }

    @Test
    void stoppingTheBorrowedClientClosesTheSessions() throws Exception {
        http.start();
        try (WebSocketClient client = new WebSocketClient()) {
            client.setClient(http);
            WebSocketSession session = client.connect(uri);

            http.stop();
            assertNull(session.receive());
            assertThrows(IOException.class, () -> client.connect(uri));
        }
    }

    @Test
    void acceptsAClientWhoseContextIsSetLater() throws Exception {
        // As done by Component.getClients().add(Protocol.HTTP).
        Client late = new Client(Protocol.HTTP);
        late.setContext(new Context());
        late.start();
        try (WebSocketClient client = new WebSocketClient()) {
            client.setClient(late);
            WebSocketSession session = client.connect(uri);
            session.sendText("late");
            assertEquals("late", session.receive().getText());
        } finally {
            late.stop();
        }
    }

    @Test
    void rejectsUnsuitableClients() throws Exception {
        WebSocketClient client = new WebSocketClient();

        client.setClient(http);
        assertThrows(IllegalStateException.class, client::start);

        client.setClient(new Client(Protocol.HTTP));
        assertThrows(IllegalStateException.class, client::start);

        client.setClient(new Client(new Context(), Protocol.FTP));
        assertThrows(IllegalStateException.class, client::start);

        http.start();
        client.setClient(http);
        client.start();
        assertThrows(IllegalStateException.class, () -> client.setClient(null));
        client.stop();
    }
}
