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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

/**
 * Tests the back-pressure of the connectors: a fast producer is slowed down by a slow consumer
 * instead of buffering the whole content in memory, and the content arrives intact.
 */
class BackPressureTestCase {

    /** Large enough to exceed the socket and Netty buffers by far. */
    private static final long SIZE = 64L * 1024 * 1024;

    private static final int CHUNK = 64 * 1024;

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
    }

    /** A large response body written as fast as possible, tracking the bytes produced. */
    private static Representation largeBody(AtomicLong produced) {
        Representation result =
                new OutputRepresentation(MediaType.APPLICATION_OCTET_STREAM, SIZE) {
                    @Override
                    public void write(OutputStream out) throws IOException {
                        byte[] chunk = pattern(CHUNK);
                        for (long sent = 0; sent < SIZE; sent += CHUNK) {
                            out.write(chunk);
                            produced.addAndGet(CHUNK);
                        }
                    }
                };
        return result;
    }

    private static byte[] pattern(int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) i;
        }
        return result;
    }

    private static long drain(InputStream in, int bytesPerRead) throws IOException {
        byte[] buffer = new byte[bytesPerRead];
        long total = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            for (int i = 0; i < n; i++) {
                if (buffer[i] != (byte) ((total + i) % CHUNK)) {
                    throw new IOException("Corrupted content at offset " + (total + i));
                }
            }
            total += n;
        }
        return total;
    }

    @Test
    void serverWriterIsSlowedDownBySlowReader() throws Exception {
        final AtomicLong produced = new AtomicLong();
        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(largeBody(produced));
                            }
                        });
        server.start();

        // A raw socket client that pauses before reading anything.
        try (Socket socket = new Socket("localhost", server.getActualPort())) {
            socket.setReceiveBufferSize(64 * 1024);
            OutputStream out = socket.getOutputStream();
            out.write(
                    "GET / HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();

            Thread.sleep(1500);
            long producedWhileStalled = produced.get();
            assertTrue(
                    producedWhileStalled < SIZE / 4,
                    "The server buffered " + producedWhileStalled + " bytes for a stalled reader");

            InputStream in = socket.getInputStream();
            String head = readHead(in);
            assertTrue(head.startsWith("HTTP/1.1 200"), head);
            assertEquals(SIZE, drain(in, CHUNK));
        }
        assertEquals(SIZE, produced.get());
    }

    @Test
    void clientWriterIsSlowedDownBySlowServerReader() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final AtomicLong received = new AtomicLong();
        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                started.countDown();
                                try (InputStream in = request.getEntity().getStream()) {
                                    Thread.sleep(1500);
                                    received.set(drain(in, CHUNK));
                                    response.setEntity(
                                            Long.toString(received.get()), MediaType.TEXT_PLAIN);
                                } catch (Exception e) {
                                    response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                                }
                            }
                        });
        server.start();

        client = new Client(new Context(), Protocol.HTTP);
        client.start();

        final AtomicLong produced = new AtomicLong();
        Request request =
                new Request(
                        Method.PUT,
                        "http://localhost:" + server.getActualPort(),
                        largeBody(produced));

        final Response[] result = new Response[1];
        Thread sender = Thread.ofVirtual().start(() -> result[0] = client.handle(request));

        assertTrue(started.await(10, TimeUnit.SECONDS));
        Thread.sleep(500);
        long producedWhileStalled = produced.get();
        assertTrue(
                producedWhileStalled < SIZE / 4,
                "The client buffered " + producedWhileStalled + " bytes for a stalled server");

        sender.join(60_000);
        assertEquals(Status.SUCCESS_OK, result[0].getStatus());
        assertEquals(Long.toString(SIZE), result[0].getEntityAsText());
        assertEquals(SIZE, received.get());
    }

    @Test
    void clientReadsLargeBodySlowlyWithoutLoss() throws Exception {
        final AtomicLong produced = new AtomicLong();
        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(largeBody(produced));
                            }
                        });
        server.start();

        client = new Client(new Context(), Protocol.HTTP);
        client.start();
        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        try (InputStream in = response.getEntity().getStream()) {
            Thread.sleep(1500);
            long producedWhileStalled = produced.get();
            assertTrue(
                    producedWhileStalled < SIZE / 4,
                    "The server produced "
                            + producedWhileStalled
                            + " bytes while the client body was not read");
            assertEquals(SIZE, drain(in, 8 * 1024));
        }
    }

    private static String readHead(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (!sb.toString().endsWith("\r\n\r\n")) {
            int c = in.read();
            if (c == -1) {
                throw new IOException("Unexpected end of stream");
            }
            sb.append((char) c);
        }
        return sb.toString();
    }
}
