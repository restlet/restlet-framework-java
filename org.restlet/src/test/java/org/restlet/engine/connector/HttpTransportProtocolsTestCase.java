/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.util.Series;

/** Test the support of HTTP2 and HTTP3 transport protocols. */
class HttpTransportProtocolsTestCase {

    protected static final String KEYSTORE_FILE_NAME = "dummy.p12";
    protected static final String KEYSTORE_PASSWORD = "testtest";
    protected static final String KEYSTORE_TYPE = "PKCS12";
    protected static File testKeystoreFile;

    @BeforeAll
    public static void setup() throws IOException {
        Path keystorePath =
                Files.createTempFile("HttpTransportProtocolsTestCase", KEYSTORE_FILE_NAME);
        final InputStream resourceAsStream =
                SslBaseConnectorsTestCase.class.getResourceAsStream(KEYSTORE_FILE_NAME);
        assert resourceAsStream != null;
        Files.copy(resourceAsStream, keystorePath, REPLACE_EXISTING);
        testKeystoreFile = keystorePath.toFile();
    }

    @AfterAll
    protected static void tearDown() {
        testKeystoreFile.delete();
    }

    @Nested
    class HttpServerTestCase extends HttpTransportProtocolTest {
        @BeforeAll
        public static void setup() {
            Engine.clearThreadLocalVariables();
            Engine nre = Engine.register(false);
            nre.getRegisteredServers()
                    .add(0, new org.restlet.engine.connector.HttpServerHelper(null));
            nre.getRegisteredClients().add(0, new HttpClientHelper(null));
        }

        @BeforeAll
        public static void tearDown() {
            Engine.clearThreadLocalVariables();
        }

        @Override
        Server newServer(String httpTransportProtocolOption) {
            final Context context = new Context();
            Series<Parameter> parameters = context.getParameters();

            parameters.add("threadPool.minThreads", "1");
            parameters.add("threadPool.maxThreads", "10");
            parameters.add("shutdown.gracefully", "false");
            parameters.add("http.transport.protocol", httpTransportProtocolOption);

            return new Server(context, Protocol.HTTP, null, 0, HELLO_WORLD_RESTLET);
        }

        @Override
        List<String> expectedProtocols() {
            return List.of("HTTP1_1", "HTTP2");
        }

        static Stream<Arguments> validTestCases() {
            return Stream.of(
                    Arguments.of(
                            null,
                            newJdkHttpClient(HttpClient.Version.HTTP_1_1)), // server default to
                    // HTTP1_1
                    Arguments.of(
                            null,
                            newJdkHttpClient(
                                    HttpClient.Version.HTTP_2)), // server default to HTTP1_1
                    Arguments.of("HTTP1_1", newJdkHttpClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of("HTTP1_1", newJdkHttpClient(HttpClient.Version.HTTP_2)),
                    Arguments.of("HTTP2", newJdkHttpClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of("HTTP1_1", newJdkHttpClient(HttpClient.Version.HTTP_2)),
                    Arguments.of(null, newJettyHttpClient("HTTP1_1")), // server default to HTTP1_1
                    Arguments.of(null, newJettyHttpClient("DYNAMIC")), // server default to HTTP1_1
                    Arguments.of("HTTP1_1", newJettyHttpClient("HTTP1_1")),
                    Arguments.of("HTTP1_1", newJettyHttpClient("DYNAMIC")),
                    Arguments.of("HTTP2", newJettyHttpClient("HTTP1_1")),
                    Arguments.of("HTTP2", newJettyHttpClient("HTTP2")),
                    Arguments.of("HTTP2", newJettyHttpClient("DYNAMIC")));
        }

        static Stream<Arguments> invalidTestCases() {
            return Stream.of(
                    Arguments.of(null, newJettyHttpClient("HTTP2")), // server default to HTTP1_1
                    Arguments.of("HTTP1_1", newJettyHttpClient("HTTP2")));
        }
    }

    @Nested
    class HttpsServerTestCase extends HttpTransportProtocolTest {

        @BeforeAll
        public static void setup() {
            Engine.clearThreadLocalVariables();
            Engine nre = Engine.register(false);
            nre.getRegisteredServers()
                    .add(0, new org.restlet.engine.connector.HttpsServerHelper(null));
            nre.getRegisteredClients().add(0, new HttpClientHelper(null));
        }

        @AfterAll
        protected static void tearDown() {
            // Restore a clean engine
            Engine.register();
            Engine.clearThreadLocalVariables();
        }

        @Override
        List<String> expectedProtocols() {
            return List.of("HTTP1_1", "HTTP2", "HTTP3");
        }

        static Stream<Arguments> validTestCases() {
            return Stream.of(
                    Arguments.of(
                            null,
                            newJdkHttpsClient(HttpClient.Version.HTTP_1_1)), // server default to
                    // HTTP1_1
                    Arguments.of(
                            null,
                            newJdkHttpsClient(
                                    HttpClient.Version.HTTP_2)), // server default to HTTP1_1
                    Arguments.of("HTTP1_1", newJdkHttpsClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of("HTTP1_1", newJdkHttpsClient(HttpClient.Version.HTTP_2)),
                    Arguments.of("HTTP2", newJdkHttpsClient(HttpClient.Version.HTTP_2)),
                    Arguments.of("HTTP1_1,HTTP2", newJdkHttpsClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of("HTTP1_1,HTTP2", newJdkHttpsClient(HttpClient.Version.HTTP_2)),
                    Arguments.of("HTTP2,HTTP1_1", newJdkHttpsClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of("HTTP2,HTTP1_1", newJdkHttpsClient(HttpClient.Version.HTTP_2)),
                    Arguments.of(null, newJettyHttpsClient("HTTP1_1")), // server default to HTTP1_1
                    Arguments.of(null, newJettyHttpsClient("DYNAMIC")), // server default to HTTP1_1
                    Arguments.of("HTTP1_1", newJettyHttpsClient("HTTP1_1")),
                    Arguments.of("HTTP1_1", newJettyHttpsClient("DYNAMIC")),
                    Arguments.of("HTTP2", newJettyHttpsClient("HTTP2")),
                    Arguments.of("HTTP2", newJettyHttpsClient("DYNAMIC")),
                    Arguments.of("HTTP1_1,HTTP2", newJettyHttpsClient("HTTP1_1")),
                    Arguments.of("HTTP1_1,HTTP2", newJettyHttpsClient("DYNAMIC")),
                    Arguments.of("HTTP2,HTTP1_1", newJettyHttpsClient("HTTP1_1")),
                    Arguments.of("HTTP2,HTTP1_1", newJettyHttpsClient("HTTP2")),
                    Arguments.of("HTTP2,HTTP1_1", newJettyHttpsClient("DYNAMIC")));
        }

        static Stream<Arguments> invalidTestCases() {
            return Stream.of(
                    Arguments.of("HTTP2", newJdkHttpsClient(HttpClient.Version.HTTP_1_1)),
                    Arguments.of(null, newJettyHttpsClient("HTTP2")), // server default to http1
                    Arguments.of("HTTP1_1", newJettyHttpsClient("HTTP2")),
                    Arguments.of("HTTP2", newJettyHttpsClient("HTTP1_1")),
                    Arguments.of("HTTP1_1,HTTP2", newJettyHttpsClient("HTTP2")));
        }

        @Override
        Server newServer(String httpTransportProtocolOption) {
            final Context context = new Context();
            Series<Parameter> parameters = context.getParameters();

            parameters.add("threadPool.minThreads", "1");
            parameters.add("threadPool.maxThreads", "10");
            parameters.add("shutdown.gracefully", "false");
            parameters.add("http.transport.protocols", httpTransportProtocolOption);

            parameters.add("keystorePath", testKeystoreFile.getPath());
            parameters.add("keystorePassword", KEYSTORE_PASSWORD);
            parameters.add("keyStoreType", KEYSTORE_TYPE);
            parameters.add("keyPassword", KEYSTORE_PASSWORD);

            parameters.add("truststorePath", testKeystoreFile.getPath());
            parameters.add("truststorePassword", KEYSTORE_PASSWORD);
            parameters.add("trustStoreType", KEYSTORE_TYPE);

            return new Server(context, Protocol.HTTPS, null, 0, HELLO_WORLD_RESTLET);
        }
    }

    abstract static class HttpTransportProtocolTest {

        abstract Server newServer(final String httpTransportProtocolOption);

        abstract List<String> expectedProtocols();

        @ParameterizedTest(name = "server: {0} / client: {1}")
        @MethodSource("validTestCases")
        void clientCompliesWithServer(
                final String httpTransportProtocol, final TestHttpClient testHttpClient)
                throws Exception {
            final Server server = newServer(httpTransportProtocol);
            server.start();

            assertEquals("hello, world", testHttpClient.sendRequest(server.getActualPort()));
        }

        @ParameterizedTest(name = "server: {0} / client: {1}")
        @MethodSource("invalidTestCases")
        void clientDoesNotComplyWithServer(
                final String httpTransportProtocol, final TestHttpClient testHttpClient)
                throws Exception {
            final Server server = newServer(httpTransportProtocol);
            server.start();

            final int actualPort = server.getActualPort();
            RuntimeException runtimeException =
                    assertThrows(
                            RuntimeException.class, () -> testHttpClient.sendRequest(actualPort));
            assertEquals("Error while sending request", runtimeException.getMessage());
        }

        @ParameterizedTest(name = "server: {0}")
        @ValueSource(strings = {"", "invalid", "http3"})
        void invalidServerConfiguration(final String httpTransportProtocol) {
            final Server server = newServer(httpTransportProtocol);

            final Exception exception = assertThrows(IllegalArgumentException.class, server::start);
            assertEquals(
                    format(
                            "'%s' is not one of the supported values: %s",
                            httpTransportProtocol, expectedProtocols()),
                    exception.getMessage());
        }
    }

    private static final Restlet HELLO_WORLD_RESTLET =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    response.setEntity("hello, world", MediaType.TEXT_PLAIN);
                }
            };

    interface TestHttpClient {
        String sendRequest(final int port) throws Exception;
    }

    private static TestHttpClient newJdkHttpClient(final HttpClient.Version httpVersion) {
        return new JdkTestHttpClient(Protocol.HTTP, httpVersion);
    }

    private static TestHttpClient newJdkHttpsClient(final HttpClient.Version httpVersion) {
        return new JdkTestHttpClient(Protocol.HTTPS, httpVersion);
    }

    private static TestHttpClient newJettyHttpClient(final String httpClientTransportMode) {
        return new JettyTestHttpClient(Protocol.HTTP, httpClientTransportMode);
    }

    private static TestHttpClient newJettyHttpsClient(final String httpClientTransportMode) {
        return new JettyTestHttpClient(Protocol.HTTPS, httpClientTransportMode);
    }

    static class JdkTestHttpClient implements TestHttpClient {

        final Protocol protocol;
        final HttpClient httpClient;

        private JdkTestHttpClient(final Protocol protocol, final HttpClient.Version httpVersion) {
            this.protocol = protocol;
            if (Protocol.HTTP.equals(protocol)) {
                httpClient = HttpClient.newBuilder().version(httpVersion).build();
            } else {
                try {
                    DefaultSslContextFactory sslContextFactory = new DefaultSslContextFactory();
                    sslContextFactory.setKeyStorePath(testKeystoreFile.getPath());
                    sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
                    sslContextFactory.setKeyStoreKeyPassword(KEYSTORE_PASSWORD);
                    sslContextFactory.setKeyStoreType(KEYSTORE_TYPE);

                    sslContextFactory.setTrustStorePath(testKeystoreFile.getPath());
                    sslContextFactory.setTrustStorePassword(KEYSTORE_PASSWORD);
                    sslContextFactory.setTrustStoreType(KEYSTORE_TYPE);
                    httpClient =
                            HttpClient.newBuilder()
                                    .sslContext(sslContextFactory.createSslContext())
                                    .version(httpVersion)
                                    .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public String sendRequest(int port) {
            final HttpRequest requestGet =
                    HttpRequest.newBuilder()
                            .uri(URI.create(protocol.getSchemeName() + "://localhost:" + port))
                            .build();
            try {
                return httpClient.send(requestGet, HttpResponse.BodyHandlers.ofString()).body();
            } catch (Exception e) {
                throw new RuntimeException("Error while sending request");
            }
        }

        @Override
        public String toString() {
            return "Jdk " + httpClient.version();
        }
    }

    static class JettyTestHttpClient implements TestHttpClient {

        final Protocol protocol;
        final String httpClientTransportMode;

        private JettyTestHttpClient(final Protocol protocol, final String httpClientTransportMode) {
            this.protocol = protocol;
            this.httpClientTransportMode = httpClientTransportMode;
        }

        @Override
        public String sendRequest(int port) {
            final Client client =
                    new Client(
                            newClientContext(),
                            List.of(protocol),
                            HttpClientHelper.class.getCanonicalName());

            final Request request =
                    new Request(Method.GET, protocol.getSchemeName() + "://localhost:" + port);
            final Response response = client.handle(request);

            if (response.getStatus().isError()) {
                throw new RuntimeException("Error while sending request");
            } else {
                return response.getEntityAsText();
            }
        }

        protected Context newClientContext() {
            Context context = new Context();
            context.getParameters().add("httpClientTransportMode", httpClientTransportMode);

            if (Protocol.HTTPS.equals(protocol)) {
                context.getParameters().add("truststorePath", testKeystoreFile.getPath());
                context.getParameters().add("truststorePassword", KEYSTORE_PASSWORD);
                context.getParameters().add("trustStoreType", KEYSTORE_TYPE);
            }
            return context;
        }

        @Override
        public String toString() {
            return "Jetty " + protocol.getSchemeName() + " " + httpClientTransportMode;
        }
    }
}
