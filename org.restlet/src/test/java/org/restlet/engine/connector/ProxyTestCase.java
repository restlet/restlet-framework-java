/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

/**
 * Tests the HTTP proxy support of the client connector: absolute-form requests for HTTP, and
 * CONNECT tunnels for HTTPS.
 */
class ProxyTestCase {

    @TempDir Path tempDir;

    private TestProxy proxy;

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpsServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        proxy = new TestProxy();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
        proxy.close();
        Engine.clearThreadLocalVariables();
    }

    private static final Restlet HELLO =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    response.setEntity(
                            "hello " + request.getResourceRef().getPath(), MediaType.TEXT_PLAIN);
                }
            };

    @Test
    void forwardsHttpRequestsWithAbsoluteUris() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", Integer.toString(proxy.port()));
        client = new Client(context, Protocol.HTTP);
        client.start();

        String uri = "http://localhost:" + server.getActualPort() + "/a?b=c";
        for (int i = 0; i < 2; i++) {
            Response response = client.handle(new Request(Method.GET, uri));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("hello /a", response.getEntityAsText());
        }

        // The connection to the proxy is reused: the proxy relays it to the origin after the first
        // request head, which it receives in absolute form.
        assertEquals("GET " + uri + " HTTP/1.1", proxy.requestLines.getFirst());
    }

    @Test
    void tunnelsHttpsRequestsWithConnect() throws Exception {
        Path keystore = tempDir.resolve("dummy.p12");
        try (InputStream in = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, REPLACE_EXISTING);
        }

        Context serverContext = new Context();
        serverContext.getParameters().add("keystorePath", keystore.toString());
        serverContext.getParameters().add("keystorePassword", "testtest");
        serverContext.getParameters().add("keyStoreType", "PKCS12");
        serverContext.getParameters().add("keyPassword", "testtest");
        server = new Server(serverContext, Protocol.HTTPS, 0, HELLO);
        server.start();

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", Integer.toString(proxy.port()));
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", "testtest");
        context.getParameters().add("trustStoreType", "PKCS12");
        client = new Client(context, Protocol.HTTPS);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET,
                                "https://localhost:" + server.getActualPort() + "/secure"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("hello /secure", response.getEntityAsText());

        assertEquals(1, proxy.requestLines.size());
        assertEquals(
                "CONNECT localhost:" + server.getActualPort() + " HTTP/1.1",
                proxy.requestLines.getFirst());
    }

    @Test
    void reportsUnreachableProxy() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", Integer.toString(closedPort));
        context.getParameters().add("connectTimeout", "2000");
        client = new Client(context, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));
        assertTrue(response.getStatus().isConnectorError(), response.getStatus().toString());
    }

    /**
     * Minimal HTTP proxy: forwards absolute-form requests (one connection to the origin per client
     * connection, bytes relayed both ways) and opens CONNECT tunnels.
     */
    private static final class TestProxy implements AutoCloseable {

        final List<String> requestLines = new CopyOnWriteArrayList<>();

        private final ServerSocket serverSocket;

        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        TestProxy() throws IOException {
            serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            executor.execute(this::acceptLoop);
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    executor.execute(() -> handle(socket));
                }
            } catch (IOException _) {
                // closed
            }
        }

        private void handle(Socket client) {
            try (client) {
                InputStream in = client.getInputStream();
                String head = readHead(in);
                String requestLine = head.substring(0, head.indexOf("\r\n"));
                requestLines.add(requestLine);
                String[] parts = requestLine.split(" ");

                if ("CONNECT".equals(parts[0])) {
                    String[] hostPort = parts[1].split(":");
                    try (Socket origin = new Socket(hostPort[0], Integer.parseInt(hostPort[1]))) {
                        client.getOutputStream()
                                .write(
                                        "HTTP/1.1 200 Connection established\r\n\r\n"
                                                .getBytes(StandardCharsets.US_ASCII));
                        client.getOutputStream().flush();
                        relay(client, in, origin);
                    }
                } else {
                    URI uri = URI.create(parts[1]);
                    try (Socket origin = new Socket(uri.getHost(), uri.getPort())) {
                        // Rewrite the request line in origin form, keep the rest as is.
                        String path =
                                uri.getRawPath()
                                        + (uri.getRawQuery() == null
                                                ? ""
                                                : "?" + uri.getRawQuery());
                        String rewritten =
                                parts[0]
                                        + " "
                                        + path
                                        + " "
                                        + parts[2]
                                        + head.substring(head.indexOf("\r\n"));
                        origin.getOutputStream()
                                .write(rewritten.getBytes(StandardCharsets.ISO_8859_1));
                        origin.getOutputStream().flush();
                        relay(client, in, origin);
                    }
                }
            } catch (IOException _) {
                // connection closed
            }
        }

        /** Relays bytes both ways until one side closes. */
        private void relay(Socket client, InputStream clientIn, Socket origin) throws IOException {
            Thread upstream =
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        try {
                                            clientIn.transferTo(origin.getOutputStream());
                                        } catch (IOException _) {
                                            // closed
                                        } finally {
                                            try {
                                                origin.shutdownOutput();
                                            } catch (IOException _) {
                                                // ignore
                                            }
                                        }
                                    });
            try {
                origin.getInputStream().transferTo(client.getOutputStream());
            } finally {
                client.shutdownOutput();
                try {
                    upstream.join(5000);
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /** Reads the request head, up to and including the empty line. */
        private static String readHead(InputStream in) throws IOException {
            ByteArrayOutputStream head = new ByteArrayOutputStream();
            int matched = 0;
            int c;
            while (matched < 4 && (c = in.read()) != -1) {
                head.write(c);
                matched =
                        (c == '\r' && (matched == 0 || matched == 2))
                                        || (c == '\n' && (matched == 1 || matched == 3))
                                ? matched + 1
                                : (c == '\r' ? 1 : 0);
            }
            return head.toString(StandardCharsets.ISO_8859_1);
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            executor.shutdownNow();
        }
    }
}
