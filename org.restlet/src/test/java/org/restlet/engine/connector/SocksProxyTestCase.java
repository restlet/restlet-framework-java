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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

/** Tests the SOCKS5 proxy support of the client connector, for HTTP and HTTPS requests. */
class SocksProxyTestCase {

    @TempDir Path tempDir;

    private TestSocksProxy proxy;

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpsServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        proxy = new TestSocksProxy();
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

    private static final Restlet ECHO =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    response.setEntity(
                            request.getResourceRef().getScheme()
                                    + " "
                                    + request.getResourceRef().getPath(),
                            MediaType.TEXT_PLAIN);
                }
            };

    @Test
    void connectsHttpRequestsThroughSocksProxy() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, ECHO);
        server.start();

        Context context = socksContext();
        client = new Client(context, Protocol.HTTP);
        client.start();

        for (int i = 0; i < 2; i++) {
            Response response =
                    client.handle(
                            new Request(
                                    Method.GET,
                                    "http://localhost:" + server.getActualPort() + "/a?b=c"));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("http /a", response.getEntityAsText());
        }

        // The connection is reused, and the proxy resolves the host name.
        assertEquals(List.of("localhost:" + server.getActualPort()), proxy.targets);
    }

    @Test
    void connectsHttpsRequestsThroughSocksProxyWithCredentials() throws Exception {
        proxy.requiredCredentials = "scott:tiger";
        Path keystore = keystore();
        startHttpsServer(keystore);

        Context context = socksContext();
        context.getParameters().add("socksProxyUsername", "scott");
        context.getParameters().add("socksProxyPassword", "tiger");
        addTrustStore(context, keystore);
        client = new Client(context, Protocol.HTTPS);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "https://localhost:" + server.getActualPort() + "/s"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("https /s", response.getEntityAsText());
        assertEquals(List.of("localhost:" + server.getActualPort()), proxy.targets);
    }

    @Test
    void reportsRefusedSocksCredentials() throws Exception {
        proxy.requiredCredentials = "scott:tiger";
        server = new Server(new Context(), Protocol.HTTP, 0, ECHO);
        server.start();

        Context context = socksContext();
        context.getParameters().add("socksProxyUsername", "scott");
        context.getParameters().add("socksProxyPassword", "wrong");
        client = new Client(context, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));
        assertTrue(response.getStatus().isConnectorError(), response.getStatus().toString());
        assertTrue(proxy.targets.isEmpty(), proxy.targets.toString());
    }

    @Test
    void bypassesSocksProxyForNonProxyHosts() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, ECHO);
        server.start();

        Context context = socksContext();
        context.getParameters().add("nonProxyHosts", "localhost");
        client = new Client(context, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "http://localhost:" + server.getActualPort() + "/d"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(proxy.targets.isEmpty(), proxy.targets.toString());
    }

    @Test
    void usesSocksProxySystemProperties() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, ECHO);
        server.start();

        String previousHost = System.setProperty("socksProxyHost", "localhost");
        String previousPort = System.setProperty("socksProxyPort", Integer.toString(proxy.port()));
        try {
            client = new Client(new Context(), Protocol.HTTP);
            client.start();
            Response response =
                    client.handle(
                            new Request(Method.GET, "http://localhost:" + server.getActualPort()));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
        } finally {
            restore("socksProxyHost", previousHost);
            restore("socksProxyPort", previousPort);
        }

        assertEquals(List.of("localhost:" + server.getActualPort()), proxy.targets);
    }

    private static void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    private Context socksContext() {
        Context context = new Context();
        context.getParameters().add("socksProxyHost", "localhost");
        context.getParameters().add("socksProxyPort", Integer.toString(proxy.port()));
        return context;
    }

    private Path keystore() throws IOException {
        Path keystore = tempDir.resolve("dummy.p12");
        try (InputStream in = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, REPLACE_EXISTING);
        }
        return keystore;
    }

    private void startHttpsServer(Path keystore) throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().add("keystorePath", keystore.toString());
        serverContext.getParameters().add("keystorePassword", "testtest");
        serverContext.getParameters().add("keyStoreType", "PKCS12");
        serverContext.getParameters().add("keyPassword", "testtest");
        server = new Server(serverContext, Protocol.HTTPS, 0, ECHO);
        server.start();
    }

    private static void addTrustStore(Context context, Path keystore) {
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", "testtest");
        context.getParameters().add("trustStoreType", "PKCS12");
    }

    /**
     * Minimal SOCKS5 proxy (RFC 1928), with optional username/password authentication (RFC 1929),
     * supporting the CONNECT command only.
     */
    private static final class TestSocksProxy implements AutoCloseable {

        /** The "host:port" targets of the CONNECT commands accepted. */
        final List<String> targets = new CopyOnWriteArrayList<>();

        /** The "user:password" credentials required, if any. */
        volatile String requiredCredentials;

        private final ServerSocket serverSocket;

        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        TestSocksProxy() throws IOException {
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
                DataInputStream in = new DataInputStream(client.getInputStream());
                OutputStream out = client.getOutputStream();

                // Method selection.
                in.readUnsignedByte();
                byte[] methods = in.readNBytes(in.readUnsignedByte());
                int method = (requiredCredentials == null) ? 0x00 : 0x02;
                if (!contains(methods, method)) {
                    out.write(new byte[] {0x05, (byte) 0xFF});
                    return;
                }
                out.write(new byte[] {0x05, (byte) method});
                out.flush();

                if (method == 0x02 && !authenticate(in, out)) {
                    return;
                }

                // CONNECT request.
                in.readUnsignedByte();
                int command = in.readUnsignedByte();
                in.readUnsignedByte();
                String host =
                        switch (in.readUnsignedByte()) {
                            case 0x01 ->
                                    InetAddress.getByAddress(in.readNBytes(4)).getHostAddress();
                            case 0x03 ->
                                    new String(
                                            in.readNBytes(in.readUnsignedByte()),
                                            StandardCharsets.US_ASCII);
                            default -> InetAddress.getByAddress(in.readNBytes(16)).getHostAddress();
                        };
                int port = in.readUnsignedShort();
                if (command != 0x01) {
                    out.write(new byte[] {0x05, 0x07, 0x00, 0x01, 0, 0, 0, 0, 0, 0});
                    return;
                }

                try (Socket origin = new Socket(host, port)) {
                    targets.add(host + ":" + port);
                    out.write(new byte[] {0x05, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0});
                    out.flush();
                    relay(client, in, origin);
                }
            } catch (IOException _) {
                // connection closed
            }
        }

        private boolean authenticate(DataInputStream in, OutputStream out) throws IOException {
            in.readUnsignedByte();
            String user = new String(in.readNBytes(in.readUnsignedByte()), StandardCharsets.UTF_8);
            String password =
                    new String(in.readNBytes(in.readUnsignedByte()), StandardCharsets.UTF_8);
            boolean accepted = requiredCredentials.equals(user + ":" + password);
            out.write(new byte[] {0x01, (byte) (accepted ? 0x00 : 0x01)});
            out.flush();
            return accepted;
        }

        private static boolean contains(byte[] values, int value) {
            for (byte b : values) {
                if ((b & 0xFF) == value) {
                    return true;
                }
            }
            return false;
        }

        /** Relays bytes both ways until one side closes. */
        private static void relay(Socket client, InputStream clientIn, Socket origin)
                throws IOException {
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

        @Override
        public void close() throws IOException {
            serverSocket.close();
            executor.shutdownNow();
        }
    }
}
