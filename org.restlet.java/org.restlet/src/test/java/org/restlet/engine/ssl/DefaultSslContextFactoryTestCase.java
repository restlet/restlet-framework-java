/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.data.Form;

/** Unit tests for the default algorithms of {@link DefaultSslContextFactory} (#1397). */
class DefaultSslContextFactoryTestCase {

    private static final String TRUST_PROPERTY = "ssl.TrustManagerFactory.algorithm";

    private static final String KEY_PROPERTY = "ssl.KeyManagerFactory.algorithm";

    private static final char[] PASSWORD = "changeit".toCharArray();

    private String savedTrustProperty;

    private String savedKeyProperty;

    @TempDir Path tempDir;

    @BeforeEach
    void clearAlgorithmProperties() {
        savedTrustProperty = System.getProperty(TRUST_PROPERTY);
        savedKeyProperty = System.getProperty(KEY_PROPERTY);
        System.clearProperty(TRUST_PROPERTY);
        System.clearProperty(KEY_PROPERTY);
    }

    @AfterEach
    void restoreAlgorithmProperties() {
        restore(TRUST_PROPERTY, savedTrustProperty);
        restore(KEY_PROPERTY, savedKeyProperty);
    }

    private static void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    // Defaults

    @Test
    void trustManagerAlgorithm_defaultsToTheJvmDefault() {
        assertEquals(
                TrustManagerFactory.getDefaultAlgorithm(),
                new DefaultSslContextFactory().getTrustManagerAlgorithm());
    }

    @Test
    void keyManagerAlgorithm_defaultsToTheJvmDefault() {
        assertEquals(
                KeyManagerFactory.getDefaultAlgorithm(),
                new DefaultSslContextFactory().getKeyManagerAlgorithm());
    }

    @Test
    void algorithms_systemPropertiesOverrideTheJvmDefaults() {
        System.setProperty(TRUST_PROPERTY, "SunX509");
        System.setProperty(KEY_PROPERTY, "NewSunX509");

        DefaultSslContextFactory factory = new DefaultSslContextFactory();

        assertEquals("SunX509", factory.getTrustManagerAlgorithm());
        assertEquals("NewSunX509", factory.getKeyManagerAlgorithm());
    }

    @Test
    void algorithms_settersOverrideTheDefaults() {
        DefaultSslContextFactory factory = new DefaultSslContextFactory();
        factory.setTrustManagerAlgorithm("SunX509");
        factory.setKeyManagerAlgorithm("NewSunX509");

        assertEquals("SunX509", factory.getTrustManagerAlgorithm());
        assertEquals("NewSunX509", factory.getKeyManagerAlgorithm());
    }

    @Test
    void init_withoutAlgorithmParameters_usesTheJvmDefaults() {
        DefaultSslContextFactory factory = new DefaultSslContextFactory();
        factory.setTrustManagerAlgorithm("SunX509");
        factory.setKeyManagerAlgorithm("NewSunX509");

        factory.init(new Form());

        assertEquals(TrustManagerFactory.getDefaultAlgorithm(), factory.getTrustManagerAlgorithm());
        assertEquals(KeyManagerFactory.getDefaultAlgorithm(), factory.getKeyManagerAlgorithm());
    }

    @Test
    void init_withAlgorithmParameters_usesTheParameters() {
        Form parameters = new Form();
        parameters.add("trustManagerAlgorithm", "SunX509");
        parameters.add("keyManagerAlgorithm", "NewSunX509");
        DefaultSslContextFactory factory = new DefaultSslContextFactory();

        factory.init(parameters);

        assertEquals("SunX509", factory.getTrustManagerAlgorithm());
        assertEquals("NewSunX509", factory.getKeyManagerAlgorithm());
    }

    // TLS handshakes: a valid chain served out of order (the symptom of #1397)

    @Test
    void handshake_withChainInOrder_succeedsWithTheDefaultAlgorithms() throws Exception {
        assertNull(handshake(chain("leaf", "intermediate"), null));
    }

    @Test
    void handshake_withChainOutOfOrder_succeedsWithTheDefaultAlgorithms() throws Exception {
        assertNull(handshake(chain("leaf", "root", "intermediate"), null));
    }

    @Test
    void handshake_withChainOutOfOrder_failsWithSunX509() throws Exception {
        // Documents the escape hatch: the old algorithm can still be selected, with its limits.
        assertNotNull(handshake(chain("leaf", "root", "intermediate"), "SunX509"));
    }

    @Test
    void handshake_withChainInOrder_succeedsWithSunX509() throws Exception {
        assertNull(handshake(chain("leaf", "intermediate"), "SunX509"));
    }

    private Certificate[] chain(String... names) throws Exception {
        Certificate[] result = new Certificate[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = certificate(names[i]);
        }
        return result;
    }

    private Certificate certificate(String name) throws Exception {
        try (InputStream in = resource("test-ca-" + name + ".pem")) {
            return CertificateFactory.getInstance("X.509").generateCertificate(in);
        }
    }

    private PrivateKey leafKey() throws Exception {
        try (InputStream in = resource("test-ca-leaf-key.pem")) {
            String pem =
                    new String(in.readAllBytes(), java.nio.charset.StandardCharsets.US_ASCII)
                            .replaceAll("-----[A-Z ]+-----", "")
                            .replaceAll("\\s", "");
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        }
    }

    private InputStream resource(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream(name);
        if (in == null) {
            throw new IOException("Missing test resource " + name);
        }
        return in;
    }

    /**
     * Performs a TLS handshake over the loopback interface, both sides being built by {@link
     * DefaultSslContextFactory}. The server serves the given chain from a JKS store (a PKCS12 store
     * refuses a chain that is not in order), and the client trusts only the root certificate.
     *
     * @param serverChain The chain served by the server, leaf first.
     * @param clientTrustAlgorithm The trust manager algorithm of the client, or null for the
     *     default.
     * @return Null if the handshake succeeded, the description of the failure otherwise.
     */
    private String handshake(Certificate[] serverChain, String clientTrustAlgorithm)
            throws Exception {
        Path keyStoreFile = tempDir.resolve("server.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server", leafKey(), PASSWORD, serverChain);
        try (OutputStream out = Files.newOutputStream(keyStoreFile)) {
            keyStore.store(out, PASSWORD);
        }

        Path trustStoreFile = tempDir.resolve("trust.p12");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("root", certificate("root"));
        try (OutputStream out = Files.newOutputStream(trustStoreFile)) {
            trustStore.store(out, PASSWORD);
        }

        DefaultSslContextFactory serverFactory = new DefaultSslContextFactory();
        serverFactory.setKeyStorePath(keyStoreFile.toString());
        serverFactory.setKeyStoreType("JKS");
        serverFactory.setKeyStorePassword(PASSWORD);
        serverFactory.setKeyStoreKeyPassword(PASSWORD);

        DefaultSslContextFactory clientFactory = new DefaultSslContextFactory();
        // The client has no key of its own: "NONE" avoids the default ~/.keystore.
        clientFactory.setKeyStorePath("NONE");
        clientFactory.setKeyStorePassword(PASSWORD);
        clientFactory.setKeyStoreKeyPassword(PASSWORD);
        clientFactory.setTrustStorePath(trustStoreFile.toString());
        clientFactory.setTrustStoreType("PKCS12");
        clientFactory.setTrustStorePassword(PASSWORD);
        if (clientTrustAlgorithm != null) {
            clientFactory.setTrustManagerAlgorithm(clientTrustAlgorithm);
        }

        AtomicReference<Throwable> serverFailure = new AtomicReference<>();
        try (SSLServerSocket serverSocket =
                (SSLServerSocket)
                        serverFactory
                                .createSslContext()
                                .getServerSocketFactory()
                                .createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            serverSocket.setSoTimeout(10_000);
            Thread server =
                    new Thread(
                            () -> {
                                try (SSLSocket socket = (SSLSocket) serverSocket.accept()) {
                                    socket.setSoTimeout(10_000);
                                    socket.startHandshake();
                                    socket.getOutputStream().write(1);
                                } catch (Exception e) {
                                    serverFailure.set(e);
                                }
                            });
            server.start();

            try (SSLSocket client =
                    (SSLSocket)
                            clientFactory
                                    .createSslContext()
                                    .getSocketFactory()
                                    .createSocket(
                                            InetAddress.getLoopbackAddress(),
                                            serverSocket.getLocalPort())) {
                client.setSoTimeout(10_000);
                client.startHandshake();
                return null;
            } catch (IOException e) {
                Throwable root = e;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                return root.getClass().getSimpleName() + ": " + root.getMessage();
            } finally {
                server.join(10_000);
            }
        }
    }
}
