/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpServerHelper;

/**
 * Checks the status that the HTTP/1.1 server answers to a request it cannot decode: 431 (RFC 6585)
 * for a header field larger than "http.requestHeaderSize" (8192 bytes by default), 414 (RFC 9110)
 * for a request line larger than it, and 400 for the other malformed requests.
 */
class NettyServerRequestLimitsTestCase {

    /** A little more than the default limit of 8192 bytes. */
    private static final int TOO_LARGE = 10_000;

    private Server server;

    @BeforeEach
    void startServer() throws Exception {
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        server =
                new Server(
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity("ok", MediaType.TEXT_PLAIN);
                            }
                        });
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }

        Engine.clearThreadLocalVariables();
    }

    /** Sends a raw request and returns the status line of the response. */
    private String statusLineOf(String rawRequest) throws IOException {
        try (Socket socket = new Socket("localhost", server.getActualPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(rawRequest.getBytes(StandardCharsets.ISO_8859_1));
            socket.getOutputStream().flush();

            InputStream in = socket.getInputStream();
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int b;

            while ((b = in.read()) != -1 && b != '\n') {
                line.write(b);
            }

            return line.toString(StandardCharsets.ISO_8859_1).trim();
        }
    }

    @Test
    void normalRequest_answers200() throws Exception {
        String status =
                statusLineOf("GET / HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
        assertTrue(status.startsWith("HTTP/1.1 200"), status);
    }

    @Test
    void headerFieldLargerThanTheLimit_answers431() throws Exception {
        String status =
                statusLineOf(
                        "GET / HTTP/1.1\r\nHost: localhost\r\nX-Big: "
                                + "x".repeat(TOO_LARGE)
                                + "\r\nConnection: close\r\n\r\n");
        assertTrue(status.startsWith("HTTP/1.1 431"), status);
    }

    @Test
    void requestLineLargerThanTheLimit_answers414() throws Exception {
        String status =
                statusLineOf(
                        "GET /"
                                + "x".repeat(TOO_LARGE)
                                + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
        assertTrue(status.startsWith("HTTP/1.1 414"), status);
    }

    @Test
    void malformedRequest_stillAnswers400() throws Exception {
        String status = statusLineOf("THIS IS NOT AN HTTP REQUEST\r\n\r\n");
        assertTrue(status.startsWith("HTTP/1.1 400"), status);
    }
}
