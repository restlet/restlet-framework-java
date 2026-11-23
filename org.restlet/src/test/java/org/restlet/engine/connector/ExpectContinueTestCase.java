/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Tests, on the wire, the "Expect: 100-continue" handshake of the HTTP server connector (#363): the
 * server sends the interim "100 Continue" response before the client sends the body, then the final
 * response.
 */
class ExpectContinueTestCase {

    private static final int TIMEOUT_MS = 10_000;

    private static final String HEAD =
            "POST / HTTP/1.1\r\nHost: localhost\r\nContent-Type: text/plain\r\nContent-Length: 5\r\n"
                    + "Connection: close\r\n";

    private Server server;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));

        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                try {
                                    String body = request.getEntity().getText();
                                    response.setEntity("echo:" + body, MediaType.TEXT_PLAIN);
                                } catch (IOException e) {
                                    response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                                }
                            }
                        });
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
    }

    /** Reads up to the end of the next response head (the blank line), without waiting for EOF. */
    private static String readHead(InputStream in) throws IOException {
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        int n;
        while ((n = in.read()) != -1) {
            head.write(n);
            String text = head.toString(StandardCharsets.ISO_8859_1);
            if (text.endsWith("\r\n\r\n")) {
                break;
            }
        }
        return head.toString(StandardCharsets.ISO_8859_1);
    }

    /** Reads until the server closes the connection ("Connection: close"). */
    private static String readToEnd(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
    }

    private static void send(Socket socket, String text) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(text.getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
    }

    private Socket connect() throws IOException {
        Socket socket = new Socket("localhost", server.getEphemeralPort());
        socket.setSoTimeout(TIMEOUT_MS);
        return socket;
    }

    @Test
    void expectContinue_isAnsweredByAnInterimResponseBeforeTheBodyIsSent() throws Exception {
        try (Socket socket = connect()) {
            // The headers only: the client waits for the server's go-ahead before sending the body
            send(socket, HEAD + "Expect: 100-continue\r\n\r\n");

            String interim = readHead(socket.getInputStream());
            assertTrue(interim.startsWith("HTTP/1.1 100 Continue"), interim);

            send(socket, "hello");
            String finalResponse = readToEnd(socket.getInputStream());
            assertTrue(finalResponse.startsWith("HTTP/1.1 200"), finalResponse);
            assertTrue(finalResponse.endsWith("echo:hello"), finalResponse);
        }
    }

    @Test
    void expectContinue_withTheBodySentImmediately_isStillHandled() throws Exception {
        try (Socket socket = connect()) {
            // A client that does not wait for the go-ahead: the interim response is optional
            send(socket, HEAD + "Expect: 100-continue\r\n\r\nhello");

            String response = readToEnd(socket.getInputStream());
            String finalResponse =
                    response.startsWith("HTTP/1.1 100")
                            ? response.substring(response.indexOf("\r\n\r\n") + 4)
                            : response;
            assertTrue(finalResponse.startsWith("HTTP/1.1 200"), response);
            assertTrue(finalResponse.endsWith("echo:hello"), response);
        }
    }

    @Test
    void withoutExpect_noInterimResponseIsSent() throws Exception {
        try (Socket socket = connect()) {
            send(socket, HEAD + "\r\nhello");

            String response = readToEnd(socket.getInputStream());
            assertTrue(response.startsWith("HTTP/1.1 200"), response);
            assertFalse(response.contains("100 Continue"), response);
            assertEquals("echo:hello", response.substring(response.indexOf("\r\n\r\n") + 4));
        }
    }
}
