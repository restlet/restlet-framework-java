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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.netty.handler.codec.quic.Quic;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.application.Encoder;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.EncoderService;

/**
 * Tests the experimental HTTP/3 support of the connectors. Skipped when the native QUIC library of
 * Netty is not available on the build platform.
 */
class Http3TestCase {

    private static final String PASSWORD = "testtest";

    @TempDir Path tempDir;

    private Path keystore;

    private Server server;

    private Client client;

    /** The protocol versions seen by the server. */
    private final Set<String> versions = ConcurrentHashMap.newKeySet();

    /** The client UDP ports seen by the server, one per QUIC connection. */
    private final Set<Integer> clientPorts = ConcurrentHashMap.newKeySet();

    @BeforeAll
    static void requireQuic() {
        assumeTrue(Quic.isAvailable(), "The native QUIC library is not available");
    }

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpsServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));

        keystore = tempDir.resolve("dummy.p12");
        try (InputStream in = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, REPLACE_EXISTING);
        }
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

    private final Restlet echo =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    versions.add(
                            String.valueOf(
                                    request.getAttributes()
                                            .get(HeaderConstants.ATTRIBUTE_VERSION)));
                    clientPorts.add(request.getClientInfo().getPort());
                    String body =
                            request.isEntityAvailable() ? " " + request.getEntityAsText() : "";
                    response.setEntity(
                            request.getMethod() + " " + request.getResourceRef().getPath() + body,
                            MediaType.TEXT_PLAIN);
                }
            };

    private void startServer(String protocols, Restlet restlet, String... parameters)
            throws Exception {
        Context context = new Context();
        context.getParameters().add("http.transport.protocols", protocols);
        context.getParameters().add("keystorePath", keystore.toString());
        context.getParameters().add("keystorePassword", PASSWORD);
        context.getParameters().add("keyStoreType", "PKCS12");
        context.getParameters().add("keyPassword", PASSWORD);
        for (int i = 0; i < parameters.length; i += 2) {
            context.getParameters().add(parameters[i], parameters[i + 1]);
        }
        server = new Server(context, Protocol.HTTPS, 0, restlet);
        server.start();
    }

    private void startClient(String mode, String... parameters) throws Exception {
        Context context = new Context();
        context.getParameters().add("httpClientTransportMode", mode);
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", PASSWORD);
        context.getParameters().add("trustStoreType", "PKCS12");
        context.getParameters().add("connectTimeout", "5000");
        context.getParameters().add("idleTimeout", "10000");
        for (int i = 0; i < parameters.length; i += 2) {
            context.getParameters().add(parameters[i], parameters[i + 1]);
        }
        client = new Client(context, Protocol.HTTPS);
        client.start();
    }

    private String uri(String path) {
        return "https://localhost:" + server.getActualPort() + path;
    }

    @Test
    void getAndPutOverHttp3() throws Exception {
        startServer("HTTP1_1,HTTP3", echo);
        startClient("HTTP3");

        Response get = client.handle(new Request(Method.GET, uri("/a")));
        assertEquals(Status.SUCCESS_OK, get.getStatus());
        assertEquals("GET /a", get.getEntityAsText());
        assertEquals(Protocol.HTTPS, get.getProtocol());
        assertEquals("3.0", get.getProtocol().getVersion());

        Response put =
                client.handle(new Request(Method.PUT, uri("/b"), new StringRepresentation("x")));
        assertEquals(Status.SUCCESS_OK, put.getStatus());
        assertEquals("PUT /b x", put.getEntityAsText());

        assertEquals(Set.of("HTTP/3.0"), versions);
    }

    @Test
    void tcpResponsesAdvertiseHttp3() throws Exception {
        startServer("HTTP2,HTTP1_1,HTTP3", echo);
        startClient("DYNAMIC");

        Response response = client.handle(new Request(Method.GET, uri("/")));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "h3=\":" + server.getActualPort() + "\"; ma=86400",
                response.getHeaders().getFirstValue("Alt-Svc", true));
        assertEquals(Set.of("HTTP/2.0"), versions);
    }

    @Test
    void concurrentCallsShareOneQuicConnection() throws Exception {
        startServer("HTTP1_1,HTTP3", echo);
        startClient("HTTP3");

        List<Future<String>> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 30; i++) {
                final String path = "/" + i;
                results.add(
                        executor.submit(
                                () ->
                                        client.handle(new Request(Method.GET, uri(path)))
                                                .getEntityAsText()));
            }
            for (int i = 0; i < 30; i++) {
                assertEquals("GET /" + i, results.get(i).get(30, TimeUnit.SECONDS));
            }
        }
        assertEquals(1, clientPorts.size(), "Calls should share one connection: " + clientPorts);
    }

    @Test
    void largeBodiesOverHttp3() throws Exception {
        final long size = 8L * 1024 * 1024;
        startServer(
                "HTTP1_1,HTTP3",
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        try (InputStream in = request.getEntity().getStream()) {
                            long received = in.transferTo(OutputStream.nullOutputStream());
                            response.setEntity(pattern(size));
                            response.getHeaders().add("X-Received", Long.toString(received));
                        } catch (IOException e) {
                            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                        }
                    }
                });
        startClient("HTTP3");

        Response response = client.handle(new Request(Method.POST, uri("/"), pattern(size)));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(Long.toString(size), response.getHeaders().getFirstValue("X-Received", true));
        try (InputStream in = response.getEntity().getStream()) {
            long total = 0;
            byte[] buffer = new byte[16 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                for (int i = 0; i < n; i++) {
                    assertEquals((byte) ((total + i) % 251), buffer[i]);
                }
                total += n;
            }
            assertEquals(size, total);
        }
    }

    @Test
    void compressedResponseIsDecoded() throws Exception {
        final String text = "compress me ".repeat(1000);
        Restlet plain =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(text, MediaType.TEXT_PLAIN);
                    }
                };
        // Encodes the responses, as an application with its encoder service enabled would.
        Encoder encoder = new Encoder(new Context(), false, true, new EncoderService(true));
        encoder.setNext(plain);
        startServer("HTTP1_1,HTTP3", encoder);
        startClient("HTTP3");

        Request request = new Request(Method.GET, uri("/"));
        request.getClientInfo().getAcceptedEncodings().add(new Preference<>(Encoding.GZIP));
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(text, response.getEntityAsText());
    }

    @Test
    void clientCertificateOverHttp3() throws Exception {
        startServer(
                "HTTP1_1,HTTP3",
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        X509Certificate certificate =
                                (X509Certificate)
                                        request.getClientInfo().getCertificates().getFirst();
                        response.setEntity(
                                certificate.getSubjectX500Principal().getName(),
                                MediaType.TEXT_PLAIN);
                    }
                },
                "truststorePath",
                keystore.toString(),
                "truststorePassword",
                PASSWORD,
                "trustStoreType",
                "PKCS12",
                "needClientAuthentication",
                "true");
        startClient(
                "HTTP3",
                "keystorePath",
                keystore.toString(),
                "keystorePassword",
                PASSWORD,
                "keyStoreType",
                "PKCS12",
                "keyPassword",
                PASSWORD);

        Response response = client.handle(new Request(Method.GET, uri("/")));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(response.getEntityAsText().contains("CN=localhost"));
    }

    @Test
    void untrustedServerIsRejected() throws Exception {
        startServer("HTTP1_1,HTTP3", echo);
        // No trust store: the JDK default trust store doesn't trust the test certificate.
        Context context = new Context();
        context.getParameters().add("httpClientTransportMode", "HTTP3");
        context.getParameters().add("connectTimeout", "5000");
        client = new Client(context, Protocol.HTTPS);
        client.start();

        Response response = client.handle(new Request(Method.GET, uri("/")));
        assertTrue(response.getStatus().isConnectorError(), response.getStatus().toString());
    }

    @Test
    void http3AloneIsRejected() {
        Context context = new Context();
        context.getParameters().add("http.transport.protocols", "HTTP3");
        context.getParameters().add("keystorePath", keystore.toString());
        context.getParameters().add("keystorePassword", PASSWORD);
        context.getParameters().add("keyStoreType", "PKCS12");
        context.getParameters().add("keyPassword", PASSWORD);
        Server alone = new Server(context, Protocol.HTTPS, 0, echo);
        assertThrows(IllegalArgumentException.class, alone::start);
    }

    /** A repeatable representation of the given size with a known byte pattern. */
    private static Representation pattern(final long size) {
        return new OutputRepresentation(MediaType.APPLICATION_OCTET_STREAM, size) {
            @Override
            public void write(OutputStream out) throws IOException {
                byte[] chunk = new byte[16 * 1024];
                long written = 0;
                while (written < size) {
                    int n = (int) Math.min(chunk.length, size - written);
                    for (int i = 0; i < n; i++) {
                        chunk[i] = (byte) ((written + i) % 251);
                    }
                    out.write(chunk, 0, n);
                    written += n;
                }
            }
        };
    }
}
