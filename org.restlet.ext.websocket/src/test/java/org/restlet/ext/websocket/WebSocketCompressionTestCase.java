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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

/** Tests permessage-deflate compression and the frame size limit. */
class WebSocketCompressionTestCase {

    private Server server;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    private static final WebSocketHandler ECHO =
            session -> {
                WebSocketMessage message;
                while ((message = session.receive()) != null) {
                    session.send(message);
                }
            };

    private int start(Context context) throws Exception {
        server =
                new Server(
                        context,
                        Protocol.HTTP,
                        0,
                        new WebSocketUpgrader() {
                            @Override
                            protected WebSocketHandler accept(Request request, Response response) {
                                return ECHO;
                            }
                        });
        server.start();
        return server.getActualPort();
    }

    /** Sends a raw handshake and returns the response head lines. */
    private static List<String> handshake(int port, String extensions) throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            String key = Base64.getEncoder().encodeToString(new byte[16]);
            OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET / HTTP/1.1\r\nHost: localhost\r\nUpgrade: websocket\r\n"
                                    + "Connection: Upgrade\r\nSec-WebSocket-Version: 13\r\n"
                                    + "Sec-WebSocket-Key: "
                                    + key
                                    + "\r\n"
                                    + (extensions == null
                                            ? ""
                                            : "Sec-WebSocket-Extensions: " + extensions + "\r\n")
                                    + "\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(), StandardCharsets.US_ASCII));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                lines.add(line.toLowerCase());
            }
            return lines;
        }
    }

    @Test
    void serverAcceptsPermessageDeflate() throws Exception {
        int port = start(new Context());

        List<String> head = handshake(port, "permessage-deflate; client_max_window_bits");

        assertTrue(head.get(0).startsWith("http/1.1 101"), head.toString());
        assertTrue(
                head.stream()
                        .anyMatch(
                                h ->
                                        h.startsWith("sec-websocket-extensions:")
                                                && h.contains("permessage-deflate")),
                head.toString());
    }

    @Test
    void serverIgnoresCompressionWhenDisabledOrNotOffered() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.compression", "false");
        int port = start(context);

        List<String> disabled = handshake(port, "permessage-deflate");
        assertTrue(disabled.get(0).startsWith("http/1.1 101"), disabled.toString());
        assertTrue(
                disabled.stream().noneMatch(h -> h.startsWith("sec-websocket-extensions:")),
                disabled.toString());

        List<String> notOffered = handshake(port, null);
        assertTrue(
                notOffered.stream().noneMatch(h -> h.startsWith("sec-websocket-extensions:")),
                notOffered.toString());
    }

    @Test
    void compressedMessagesRoundTrip() throws Exception {
        int port = start(new Context());
        String large = "compressible ".repeat(10_000);

        try (WebSocketClient client = WebSocketClient.builder().maxMessageSize(1 << 20).build();
                WebSocketSession session = client.connect("ws://localhost:" + port + "/")) {
            // 130 KB of text: larger than the server default limit (64 KB) once inflated, so the
            // server would close the session if the limit were applied to compressed frames
            // only. Use a smaller payload against the default limit instead.
            String text = large.substring(0, 60_000);
            session.sendText(text);
            assertEquals(text, session.receive().getText());

            byte[] binary = text.getBytes(StandardCharsets.UTF_8);
            session.sendBinary(binary);
            assertEquals(text, session.receive().getText());
        }
    }

    @Test
    void inflatedMessagesAreCappedByTheMessageSize() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.maxMessageSize", "1024");
        int port = start(context);

        try (WebSocketClient client = WebSocketClient.create()) {
            WebSocketSession session = client.connect("ws://localhost:" + port + "/");
            // Compresses to far less than 1 KB on the wire, but inflates to 100 KB.
            session.sendText("a".repeat(100_000));
            assertNull(session.receive());
            assertEquals(WebSocketSession.MESSAGE_TOO_BIG, session.getCloseCode());
        }
    }

    @Test
    void framesLargerThanTheFrameSizeCloseTheSession() throws Exception {
        Context context = new Context();
        context.getParameters().add("websocket.compression", "false");
        context.getParameters().add("websocket.maxFrameSize", "16");
        int port = start(context);

        try (WebSocketClient client = WebSocketClient.builder().compression(false).build()) {
            WebSocketSession session = client.connect("ws://localhost:" + port + "/");
            session.sendText("small");
            assertEquals("small", session.receive().getText());

            session.sendText("x".repeat(64));
            assertNull(session.receive());
            assertEquals(WebSocketSession.MESSAGE_TOO_BIG, session.getCloseCode());
        }
    }

    @Test
    void clientFrameSizeLimitAppliesToServerMessages() throws Exception {
        int port = start(new Context());

        try (WebSocketClient client =
                WebSocketClient.builder().compression(false).maxFrameSize(16).build()) {
            WebSocketSession session = client.connect("ws://localhost:" + port + "/");
            session.sendText("y".repeat(64));
            assertNull(session.receive());
            assertEquals(WebSocketSession.MESSAGE_TOO_BIG, session.getCloseCode());
        }
    }
}
