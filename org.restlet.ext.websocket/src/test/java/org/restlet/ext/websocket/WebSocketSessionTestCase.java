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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.engine.connector.HttpsServerHelper;

/** Tests TLS, keep-alive pings, handler failures and client settings. */
class WebSocketSessionTestCase {

    private static final String KEYSTORE_PASSWORD = "testtest";

    private Server server;

    private Path keystore;

    @BeforeEach
    void setUp() throws IOException {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpsServerHelper(null));
        keystore = Files.createTempFile("websocket", ".p12");
        try (InputStream in = getClass().getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Files.deleteIfExists(keystore);
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    private static WebSocketUpgrader upgrader(WebSocketHandler handler) {
        return new WebSocketUpgrader() {
            @Override
            protected WebSocketHandler accept(Request request, Response response) {
                return handler;
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

    private int start(Protocol protocol, Context context, WebSocketHandler handler)
            throws Exception {
        server = new Server(context, protocol, 0, upgrader(handler));
        server.start();
        return server.getActualPort();
    }

    private SSLContext trustingContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keystore)) {
            trustStore.load(in, KEYSTORE_PASSWORD.toCharArray());
        }
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context;
    }

    @Test
    void echoesOverTls() throws Exception {
        Context context = new Context();
        context.getParameters().add("keyStorePath", keystore.toString());
        context.getParameters().add("keyStorePassword", KEYSTORE_PASSWORD);
        context.getParameters().add("keyPassword", KEYSTORE_PASSWORD);
        context.getParameters().add("keyStoreType", "PKCS12");
        int port = start(Protocol.HTTPS, context, ECHO);

        try (WebSocketClient client =
                        WebSocketClient.builder()
                                .sslContext(trustingContext())
                                .hostnameVerification(true)
                                .maxMessageSize(1024)
                                .build();
                WebSocketSession session = client.connect("wss://localhost:" + port + "/")) {
            session.sendText("secure");
            assertEquals("secure", session.receive().getText());
        }
    }

    @Test
    void idleSessionsArePingedAndKeptAliveByPongs() throws Exception {
        Context context = new Context();
        context.getParameters().add("connector.idleTimeout", "100");
        int port = start(Protocol.HTTP, context, ECHO);

        try (WebSocketClient client =
                        WebSocketClient.builder().idleTimeout(Duration.ofMillis(100)).build();
                WebSocketSession session = client.connect("ws://localhost:" + port + "/")) {
            // Several idle periods: both sides ping, and the pongs keep the session open.
            Thread.sleep(600);
            assertTrue(session.isOpen());
            session.sendText("still here");
            assertEquals("still here", session.receive().getText());
        }
    }

    @Test
    void handlerFailureClosesWithInternalError() throws Exception {
        int port =
                start(
                        Protocol.HTTP,
                        new Context(),
                        session -> {
                            throw new IllegalStateException("boom");
                        });

        try (WebSocketClient client = WebSocketClient.create()) {
            WebSocketSession session = client.connect("ws://localhost:" + port + "/");
            assertNull(session.receive());
            assertEquals(WebSocketSession.INTERNAL_ERROR, session.getCloseCode());
            assertNull(session.getSubprotocol());
        }
    }

    @Test
    void connectionFailuresAreReported() throws Exception {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }
        try (WebSocketClient client =
                WebSocketClient.builder().connectTimeout(Duration.ofSeconds(2)).build()) {
            assertThrows(
                    ConnectException.class,
                    () -> client.connect("ws://localhost:" + freePort + "/"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> client.connect("http://localhost:" + freePort + "/"));
            assertThrows(IllegalArgumentException.class, () -> client.connect("ws:///nohost"));
        }
    }

    @Test
    void builderValidatesSettings() {
        WebSocketClient.Builder builder = WebSocketClient.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.maxMessageSize(0));
        assertThrows(
                IllegalArgumentException.class, () -> builder.idleTimeout(Duration.ofSeconds(-1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.connectTimeout(Duration.ofSeconds(-1)));
    }

    @Test
    void messagesHaveValueSemantics() {
        assertEquals(WebSocketMessage.text("a"), WebSocketMessage.text("a"));
        assertEquals(WebSocketMessage.text("a").hashCode(), WebSocketMessage.text("a").hashCode());
        assertNotEquals(WebSocketMessage.text("a"), WebSocketMessage.binary(new byte[] {'a'}));
        assertNotEquals(WebSocketMessage.text("a"), "a");
        assertEquals("text[1 chars]", WebSocketMessage.text("a").toString());
    }

    @Test
    void invalidServerParametersAreRejected() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.maxMessageSize", "0");
        int port = start(Protocol.HTTP, context, ECHO);

        try (WebSocketClient client = WebSocketClient.create()) {
            assertThrows(IOException.class, () -> client.connect("ws://localhost:" + port + "/"));
        }
    }
}
