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
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.security.HttpBasicHelper;

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
        Engine.getInstance().getRegisteredAuthenticators().add(new HttpBasicHelper());
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

    /** Returns the "Proxy-Authorization" header received by the origin server, if any. */
    private static final Restlet PROXY_AUTHORIZATION_ECHO =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    String authorization =
                            request.getHeaders()
                                    .getFirstValue(
                                            HeaderConstants.HEADER_PROXY_AUTHORIZATION, true);
                    response.setEntity(
                            (authorization == null) ? "none" : authorization, MediaType.TEXT_PLAIN);
                }
            };

    @Test
    void forwardsHttpRequestsWithAbsoluteUris() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = proxyContext();
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
    void forwardsCleartextHttp2RequestsWithHttp1() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = proxyContext();
        context.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(context, Protocol.HTTP);
        client.start();

        String uri = "http://localhost:" + server.getActualPort() + "/h2";
        Response response = client.handle(new Request(Method.GET, uri));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("hello /h2", response.getEntityAsText());
        assertEquals(List.of("GET " + uri + " HTTP/1.1"), proxy.requestLines);
    }

    @Test
    void tunnelsHttpsRequestsWithConnect() throws Exception {
        startHttpsServer(HELLO);
        client = newHttpsClient();

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
    void sendsProxyCredentialsWithConnectOnly() throws Exception {
        proxy.requiredAuthorization =
                "Basic "
                        + Base64.getEncoder()
                                .encodeToString("scott:tiger".getBytes(StandardCharsets.UTF_8));
        startHttpsServer(PROXY_AUTHORIZATION_ECHO);
        client = newHttpsClient();

        for (int i = 0; i < 2; i++) {
            Request request =
                    new Request(Method.GET, "https://localhost:" + server.getActualPort() + "/s");
            request.setProxyChallengeResponse(
                    new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "tiger"));
            Response response = client.handle(request);
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            // The origin server never receives the proxy credentials.
            assertEquals("none", response.getEntityAsText());
        }

        // The tunnel is reused for the second request.
        assertEquals(1, proxy.requestLines.size());
        assertEquals(List.of(proxy.requiredAuthorization), proxy.proxyAuthorizations);
    }

    @Test
    void keepsTunnelsApartPerProxyCredentials() throws Exception {
        startHttpsServer(PROXY_AUTHORIZATION_ECHO);
        client = newHttpsClient();

        List<ChallengeResponse> credentials =
                List.of(
                        new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "tiger"),
                        new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "lion"),
                        rawCredentials("token1"),
                        rawCredentials("token2"),
                        new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "tiger"));
        for (ChallengeResponse challengeResponse : credentials) {
            Request request =
                    new Request(Method.GET, "https://localhost:" + server.getActualPort() + "/s");
            request.setProxyChallengeResponse(challengeResponse);
            Response response = client.handle(request);
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("none", response.getEntityAsText());
        }

        // A tunnel per credentials, the last request reusing the tunnel of the first one.
        assertEquals(
                List.of(
                        "Basic c2NvdHQ6dGlnZXI=",
                        "Basic c2NvdHQ6bGlvbg==",
                        "Bearer token1",
                        "Bearer token2"),
                proxy.proxyAuthorizations);
    }

    @Test
    void sharesConnectionLimitAcrossProxyCredentials() throws Exception {
        startHttpsServer(PROXY_AUTHORIZATION_ECHO);
        client = newHttpsClient("maxConnectionsPerDestination", "1");

        for (String password : List.of("tiger", "lion", "tiger")) {
            Request request =
                    new Request(Method.GET, "https://localhost:" + server.getActualPort() + "/s");
            request.setProxyChallengeResponse(
                    new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", password));
            Response response = client.handle(request);
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("none", response.getEntityAsText());
        }

        // A single connection at a time: the idle tunnel of the other credentials is closed to open
        // a new one, so the first tunnel can't be reused by the third request.
        assertEquals(3, proxy.requestLines.size());
    }

    @Test
    void returnsProxyChallengeWhenTunnelIsRefused() throws Exception {
        proxy.requiredAuthorization = "Basic c2NvdHQ6dGlnZXI=";
        startHttpsServer(HELLO);
        client = newHttpsClient();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "https://localhost:" + server.getActualPort() + "/s"));
        assertEquals(Status.CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED, response.getStatus());
        assertEquals(1, response.getProxyChallengeRequests().size());
        assertEquals(
                ChallengeScheme.HTTP_BASIC,
                response.getProxyChallengeRequests().getFirst().getScheme());
        assertEquals("proxy", response.getProxyChallengeRequests().getFirst().getRealm());
        // The body of the refusal is not returned, nor its entity headers.
        assertNull(response.getHeaders().getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE, true));
        assertNull(
                response.getHeaders().getFirstValue(HeaderConstants.HEADER_CONTENT_LENGTH, true));
    }

    private static ChallengeResponse rawCredentials(String token) {
        ChallengeResponse result =
                new ChallengeResponse(new ChallengeScheme("HTTP_BEARER", "Bearer"));
        result.setRawValue(token);
        return result;
    }

    @Test
    void usesHttpsProxySystemPropertiesForHttpsRequests() throws Exception {
        startHttpsServer(HELLO);
        withSystemProperties(
                Map.of(
                        "https.proxyHost",
                        "localhost",
                        "https.proxyPort",
                        proxyPort(),
                        "http.nonProxyHosts",
                        ""),
                () -> {
                    client = newHttpsClient(false);
                    Response response =
                            client.handle(
                                    new Request(
                                            Method.GET,
                                            "https://localhost:" + server.getActualPort() + "/s"));
                    assertEquals(Status.SUCCESS_OK, response.getStatus());
                });

        assertEquals(
                List.of("CONNECT localhost:" + server.getActualPort() + " HTTP/1.1"),
                proxy.requestLines);
    }

    @Test
    void ignoresHttpProxySystemPropertiesForHttpsRequests() throws Exception {
        startHttpsServer(HELLO);
        withSystemProperties(
                Map.of(
                        "http.proxyHost",
                        "localhost",
                        "http.proxyPort",
                        proxyPort(),
                        "http.nonProxyHosts",
                        ""),
                () -> {
                    client = newHttpsClient(false);
                    Response response =
                            client.handle(
                                    new Request(
                                            Method.GET,
                                            "https://localhost:" + server.getActualPort() + "/s"));
                    assertEquals(Status.SUCCESS_OK, response.getStatus());
                });

        assertTrue(proxy.requestLines.isEmpty(), proxy.requestLines.toString());
    }

    @Test
    void doesNotSendProxyCredentialsToOriginServerWithoutProxy() throws Exception {
        startHttpsServer(PROXY_AUTHORIZATION_ECHO);
        withSystemProperties(
                Map.of(
                        "http.proxyHost",
                        "localhost",
                        "http.proxyPort",
                        proxyPort(),
                        "http.nonProxyHosts",
                        ""),
                () -> {
                    client = newHttpsClient(false);
                    Request request =
                            new Request(
                                    Method.GET,
                                    "https://localhost:" + server.getActualPort() + "/s");
                    request.setProxyChallengeResponse(
                            new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "tiger"));
                    Response response = client.handle(request);
                    assertEquals(Status.SUCCESS_OK, response.getStatus());
                    assertEquals("none", response.getEntityAsText());
                });

        assertTrue(proxy.requestLines.isEmpty(), proxy.requestLines.toString());
    }

    @Test
    void bypassesProxyForNonProxyHosts() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", proxyPort());
        context.getParameters().add("nonProxyHosts", "example.org | LOCAL*");
        client = new Client(context, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "http://localhost:" + server.getActualPort() + "/d"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("hello /d", response.getEntityAsText());
        assertTrue(proxy.requestLines.isEmpty(), proxy.requestLines.toString());
    }

    @Test
    void bypassesProxyForNonProxyHostsSystemProperty() throws Exception {
        startHttpsServer(HELLO);
        withSystemProperties(
                Map.of(
                        "https.proxyHost",
                        "localhost",
                        "https.proxyPort",
                        proxyPort(),
                        "http.nonProxyHosts",
                        "*.example.org|*host"),
                () -> {
                    client = newHttpsClient(false);
                    Response response =
                            client.handle(
                                    new Request(
                                            Method.GET,
                                            "https://localhost:" + server.getActualPort() + "/s"));
                    assertEquals(Status.SUCCESS_OK, response.getStatus());
                });

        assertTrue(proxy.requestLines.isEmpty(), proxy.requestLines.toString());
    }

    @Test
    void bypassesProxyForLoopbackHostsByDefault() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", proxyPort());
        client = new Client(context, Protocol.HTTP);
        client.start();

        for (String host : List.of("localhost", "127.0.0.1")) {
            Response response =
                    client.handle(
                            new Request(
                                    Method.GET,
                                    "http://" + host + ":" + server.getActualPort() + "/l"));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
        }
        assertTrue(proxy.requestLines.isEmpty(), proxy.requestLines.toString());
    }

    @Test
    void usesProxyForOtherHosts() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, HELLO);
        server.start();

        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", proxyPort());
        context.getParameters().add("nonProxyHosts", "*.example.org|local|127.*");
        client = new Client(context, Protocol.HTTP);
        client.start();

        String uri = "http://localhost:" + server.getActualPort() + "/p";
        Response response = client.handle(new Request(Method.GET, uri));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(List.of("GET " + uri + " HTTP/1.1"), proxy.requestLines);
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
        context.getParameters().add("nonProxyHosts", "");
        context.getParameters().add("connectTimeout", "2000");
        client = new Client(context, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));
        assertTrue(response.getStatus().isConnectorError(), response.getStatus().toString());
    }

    private Path keystore() throws IOException {
        Path keystore = tempDir.resolve("dummy.p12");
        try (InputStream in = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, REPLACE_EXISTING);
        }
        return keystore;
    }

    private void startHttpsServer(Restlet next) throws Exception {
        Path keystore = keystore();
        Context serverContext = new Context();
        serverContext.getParameters().add("keystorePath", keystore.toString());
        serverContext.getParameters().add("keystorePassword", "testtest");
        serverContext.getParameters().add("keyStoreType", "PKCS12");
        serverContext.getParameters().add("keyPassword", "testtest");
        server = new Server(serverContext, Protocol.HTTPS, 0, next);
        server.start();
    }

    private Client newHttpsClient(String... parameters) throws Exception {
        return newHttpsClient(true, parameters);
    }

    private Client newHttpsClient(boolean proxyParameters, String... parameters) throws Exception {
        Path keystore = keystore();
        Context context = proxyParameters ? proxyContext() : new Context();
        for (int i = 0; i < parameters.length; i += 2) {
            context.getParameters().add(parameters[i], parameters[i + 1]);
        }
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", "testtest");
        context.getParameters().add("trustStoreType", "PKCS12");
        Client result = new Client(context, Protocol.HTTPS);
        result.start();
        return result;
    }

    /** Returns a context using the test proxy, including for the loopback addresses. */
    private Context proxyContext() {
        Context context = new Context();
        context.getParameters().add("proxyHost", "localhost");
        context.getParameters().add("proxyPort", proxyPort());
        context.getParameters().add("nonProxyHosts", "");
        return context;
    }

    private String proxyPort() {
        return Integer.toString(proxy.port());
    }

    /** Runs an action with system properties set, then restores their previous values. */
    private static void withSystemProperties(Map<String, String> properties, ThrowingAction action)
            throws Exception {
        Map<String, String> previous = new HashMap<>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            previous.put(
                    property.getKey(), System.setProperty(property.getKey(), property.getValue()));
        }
        try {
            action.run();
        } finally {
            for (Map.Entry<String, String> property : previous.entrySet()) {
                if (property.getValue() == null) {
                    System.clearProperty(property.getKey());
                } else {
                    System.setProperty(property.getKey(), property.getValue());
                }
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    /**
     * Minimal HTTP proxy: forwards absolute-form requests (one connection to the origin per client
     * connection, bytes relayed both ways) and opens CONNECT tunnels.
     */
    private static final class TestProxy implements AutoCloseable {

        final List<String> requestLines = new CopyOnWriteArrayList<>();

        /** The "Proxy-Authorization" headers received with CONNECT requests. */
        final List<String> proxyAuthorizations = new CopyOnWriteArrayList<>();

        /** The "Proxy-Authorization" header required to open a tunnel, if any. */
        volatile String requiredAuthorization;

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
                    String authorization = headerValue(head, "Proxy-Authorization");
                    if (authorization != null) {
                        proxyAuthorizations.add(authorization);
                    }
                    if ((requiredAuthorization != null)
                            && !requiredAuthorization.equals(authorization)) {
                        client.getOutputStream()
                                .write(
                                        ("HTTP/1.1 407 Proxy Authentication Required\r\n"
                                                        + "Proxy-Authenticate: Basic realm=\"proxy\"\r\n"
                                                        + "Content-Type: text/plain\r\n"
                                                        + "Content-Length: 6\r\n\r\n"
                                                        + "denied")
                                                .getBytes(StandardCharsets.US_ASCII));
                        client.getOutputStream().flush();
                        return;
                    }
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

        /** Returns the value of a header in a request head, ignoring case, or null. */
        private static String headerValue(String head, String name) {
            for (String line : head.split("\r\n")) {
                int colon = line.indexOf(':');
                if ((colon > 0) && line.substring(0, colon).trim().equalsIgnoreCase(name)) {
                    return line.substring(colon + 1).trim();
                }
            }
            return null;
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
