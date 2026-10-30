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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Tests the failures of the client connector after the response head was returned to a synchronous
 * caller (issue #1601).
 */
class ClientLateFailureTestCase {

    private ServerSocket serverSocket;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private Client client;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.stop();
        serverSocket.close();
        executor.shutdownNow();
        Engine.clearThreadLocalVariables();
    }

    @Test
    void keepsTheStatusWhenTheClientStopsBeforeTheEndOfTheResponse() throws Exception {
        CountDownLatch clientStopped = new CountDownLatch(1);
        executor.execute(
                () -> {
                    try (Socket socket = serverSocket.accept()) {
                        readRequestHead(socket.getInputStream());
                        OutputStream out = socket.getOutputStream();
                        write(
                                out,
                                "HTTP/1.1 403 Forbidden\r\n"
                                        + "Content-Type: text/plain\r\n"
                                        + "Transfer-Encoding: chunked\r\n\r\n"
                                        + "9\r\nForbidden\r\n");
                        // The end of the response, sent once the client is stopped.
                        clientStopped.await(5, TimeUnit.SECONDS);
                        write(out, "0\r\n\r\n");
                    } catch (IOException | InterruptedException _) {
                        // Connection closed by the client.
                    }
                });

        Response response = client.handle(new Request(Method.GET, uri("/")));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        client.stop();
        clientStopped.countDown();

        // The failure reaches the response entity stream...
        IOException failure =
                assertThrows(IOException.class, response.getEntity().getStream()::readAllBytes);

        // ...but the status returned to the caller is kept.
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500);
        while (System.nanoTime() < deadline) {
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
            Thread.sleep(10);
        }
        assertEquals("Client stopped", failure.getMessage());
    }

    @Test
    void completesResponsesWithAnEmptyBodyWithTheirHead() throws Exception {
        AtomicInteger connections = new AtomicInteger();
        executor.execute(
                () -> {
                    try (Socket socket = serverSocket.accept()) {
                        connections.incrementAndGet();
                        for (int i = 0; i < 2; i++) {
                            readRequestHead(socket.getInputStream());
                            write(
                                    socket.getOutputStream(),
                                    "HTTP/1.1 403 Forbidden\r\nContent-Length: 0\r\n\r\n");
                        }
                        socket.getInputStream().read();
                    } catch (IOException _) {
                        // Connection closed by the client.
                    }
                });

        for (int i = 0; i < 2; i++) {
            Response response = client.handle(new Request(Method.GET, uri("/" + i)));
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
            assertTrue(
                    (response.getEntity() == null) || response.getEntity().isEmpty(),
                    String.valueOf(response.getEntity()));
        }

        // The connection is reused for the second request.
        assertEquals(1, connections.get());
    }

    private String uri(String path) {
        return "http://localhost:" + serverSocket.getLocalPort() + path;
    }

    private static void readRequestHead(InputStream in) throws IOException {
        int matched = 0;
        while (matched < 4) {
            int b = in.read();
            if (b < 0) {
                throw new IOException("Connection closed");
            }
            matched = (b == "\r\n\r\n".charAt(matched)) ? matched + 1 : ((b == '\r') ? 1 : 0);
        }
    }

    private static void write(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }
}
