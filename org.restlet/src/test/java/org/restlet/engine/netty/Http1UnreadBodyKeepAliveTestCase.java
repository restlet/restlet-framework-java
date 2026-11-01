/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpServerHelper;

/**
 * Checks that an HTTP/1.1 response sent without reading the request body either keeps the
 * connection usable for the next request, or announces that it closes it.
 */
class Http1UnreadBodyKeepAliveTestCase {

    private Server server;

    /** Counted down when the server Restlet handles a request. */
    private volatile CountDownLatch handled;

    @BeforeEach
    void startServer() throws Exception {
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        handled = new CountDownLatch(1);
        server =
                new Server(
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                String path = request.getResourceRef().getPath();
                                if ("/echo".equals(path)) {
                                    // Streams the request body back from the response entity.
                                    response.setEntity(request.getEntity());
                                } else if (Method.POST.equals(request.getMethod())) {
                                    // Rejects the request without reading its body.
                                    response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                                    response.setEntity("forbidden", MediaType.TEXT_PLAIN);
                                } else {
                                    response.setEntity("ok", MediaType.TEXT_PLAIN);
                                }
                                handled.countDown();
                            }
                        });
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
    }

    @Test
    void discardsAReceivedUnreadBodyAndKeepsTheConnection() throws Exception {
        try (Socket socket = connect()) {
            send(socket, post("/reject", "Content-Length: 5") + "hello");
            HttpMessage rejected = HttpMessage.read(socket.getInputStream());
            assertEquals(403, rejected.status);
            assertNull(rejected.headers.get("connection"));

            send(socket, get("/next"));
            HttpMessage next = HttpMessage.read(socket.getInputStream());
            assertEquals(200, next.status);
            assertEquals("ok", next.body);
        }
    }

    @Test
    void discardsASmallUnreadBodyStillInTransitAndKeepsTheConnection() throws Exception {
        try (Socket socket = connect()) {
            send(socket, post("/reject", "Content-Length: 10") + "hello");
            HttpMessage rejected = HttpMessage.read(socket.getInputStream());
            assertEquals(403, rejected.status);
            assertNull(rejected.headers.get("connection"));

            // The rest of the body arrives after the response, followed by the next request.
            send(socket, "world" + get("/next"));
            HttpMessage next = HttpMessage.read(socket.getInputStream());
            assertEquals(200, next.status);
            assertEquals("ok", next.body);
        }
    }

    @Test
    void announcesCloseForALargeUnreadBodyStillInTransit() throws Exception {
        try (Socket socket = connect()) {
            send(
                    socket,
                    post("/reject", "Content-Length: " + (NettyServerCall.MAX_DISCARDED_BYTES + 6))
                            + "hello");
            HttpMessage rejected = HttpMessage.read(socket.getInputStream());
            assertEquals(403, rejected.status);
            assertEquals("close", rejected.headers.get("connection"));
            assertClosed(socket);
        }
    }

    @Test
    void announcesCloseForAnUnreadChunkedBodyStillInTransit() throws Exception {
        try (Socket socket = connect()) {
            send(socket, post("/reject", "Transfer-Encoding: chunked") + "5\r\nhello\r\n");
            HttpMessage rejected = HttpMessage.read(socket.getInputStream());
            assertEquals(403, rejected.status);
            assertEquals("close", rejected.headers.get("connection"));
            assertClosed(socket);
        }
    }

    @Test
    void discardsAReceivedUnreadChunkedBodyAndKeepsTheConnection() throws Exception {
        try (Socket socket = connect()) {
            send(socket, post("/reject", "Transfer-Encoding: chunked") + "5\r\nhello\r\n0\r\n\r\n");
            HttpMessage rejected = HttpMessage.read(socket.getInputStream());
            assertEquals(403, rejected.status);

            if (rejected.headers.get("connection") == null) {
                send(socket, get("/next"));
                HttpMessage next = HttpMessage.read(socket.getInputStream());
                assertEquals(200, next.status);
            } else {
                // The last chunk may arrive after the response head was written.
                assertEquals("close", rejected.headers.get("connection"));
                assertClosed(socket);
            }
        }
    }

    @Test
    void keepsTheConnectionOfAStreamingEchoReadingTheBodyAfterTheHead() throws Exception {
        try (Socket socket = connect()) {
            send(socket, post("/echo", "Content-Length: 10") + "hello");
            assertTrue(handled.await(5, TimeUnit.SECONDS));
            Thread.sleep(100);
            send(socket, "world");
            HttpMessage echoed = HttpMessage.read(socket.getInputStream());
            assertEquals(200, echoed.status);
            assertEquals("helloworld", echoed.body);
            assertNull(echoed.headers.get("connection"));

            send(socket, get("/next"));
            HttpMessage next = HttpMessage.read(socket.getInputStream());
            assertEquals(200, next.status);
            assertEquals("ok", next.body);
        }
    }

    @Test
    void discardedStreamReleasesLaterContentWithoutPausingReads() {
        var channel = new EmbeddedChannel();
        try {
            var body = new ChannelInputStream(channel, 0);
            var buffered = Unpooled.buffer(3).writeBytes(new byte[] {1, 2, 3});
            assertTrue(body.offer(buffered));
            assertFalse(body.isFullyReceived());
            body.discard();
            assertEquals(0, buffered.refCnt());

            var late = Unpooled.buffer(2).writeBytes(new byte[] {4, 5});
            assertTrue(body.offer(late), "a discarded stream keeps reading the content");
            assertEquals(0, late.refCnt());
            assertEquals(5, body.getReceivedBytes());
            body.end();
            assertTrue(body.isFullyReceived());

            var closed = new ChannelInputStream(channel, 0);
            closed.close();
            var ignored = Unpooled.buffer(1).writeByte(1);
            assertFalse(closed.offer(ignored), "a closed stream stops reading the content");
            assertEquals(0, ignored.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private Socket connect() throws IOException {
        Socket socket = new Socket("localhost", server.getActualPort());
        socket.setSoTimeout(5000);
        return socket;
    }

    private static String post(String path, String framing) {
        return "POST "
                + path
                + " HTTP/1.1\r\nHost: localhost\r\nContent-Type: text/plain\r\n"
                + framing
                + "\r\n\r\n";
    }

    private static String get(String path) {
        return "GET " + path + " HTTP/1.1\r\nHost: localhost\r\n\r\n";
    }

    private static void send(Socket socket, String data) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(data.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    /** Checks that the server closes the connection, without sending anything more. */
    private static void assertClosed(Socket socket) throws IOException {
        try {
            assertEquals(-1, socket.getInputStream().read());
        } catch (SocketTimeoutException e) {
            throw new AssertionError("The connection was not closed", e);
        } catch (IOException _) {
            // A connection reset also closes the connection.
        }
    }

    /** A minimal HTTP/1.1 response parser. */
    private record HttpMessage(int status, Map<String, String> headers, String body) {

        static HttpMessage read(InputStream in) throws IOException {
            String statusLine = readLine(in);
            assertNotNull(statusLine, "The connection was closed before a response");
            int status = Integer.parseInt(statusLine.split(" ")[1]);
            Map<String, String> headers = new TreeMap<>();
            String line;
            while (!(line = readLine(in)).isEmpty()) {
                int colon = line.indexOf(':');
                headers.put(
                        line.substring(0, colon).trim().toLowerCase(Locale.ROOT),
                        line.substring(colon + 1).trim().toLowerCase(Locale.ROOT));
            }
            var body = new ByteArrayOutputStream();
            if ("chunked".equals(headers.get("transfer-encoding"))) {
                int size;
                while ((size = Integer.parseInt(readLine(in).trim(), 16)) > 0) {
                    body.write(in.readNBytes(size));
                    readLine(in);
                }
                while (!readLine(in).isEmpty()) {
                    // Skips the trailers.
                }
            } else if (headers.containsKey("content-length")) {
                body.write(in.readNBytes(Integer.parseInt(headers.get("content-length"))));
            }
            return new HttpMessage(
                    status, headers, body.toString(StandardCharsets.US_ASCII.name()));
        }

        private static String readLine(InputStream in) throws IOException {
            var line = new StringBuilder();
            int c;
            while ((c = in.read()) != '\n') {
                if (c == -1) {
                    return line.isEmpty() ? null : line.toString();
                }
                if (c != '\r') {
                    line.append((char) c);
                }
            }
            return line.toString();
        }
    }
}
