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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
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
 * Tests TLS client authentication: the server requires or requests a client certificate, and the
 * certificate presented by the client is exposed in the request's client info.
 */
class SslClientAuthenticationTestCase {

    private static final String PASSWORD = "testtest";

    @TempDir Path tempDir;

    private Path keystore;

    private Server server;

    private Client client;

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

    /** Answers with the subject of the client certificate, or "none". */
    private static final Restlet WHO_AM_I =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    List<Certificate> certificates = request.getClientInfo().getCertificates();
                    String subject =
                            (certificates == null || certificates.isEmpty())
                                    ? "none"
                                    : ((X509Certificate) certificates.getFirst())
                                            .getSubjectX500Principal()
                                            .getName();
                    response.setEntity(subject, MediaType.TEXT_PLAIN);
                }
            };

    private void startServer(String clientAuthParameter) throws Exception {
        Context context = new Context();
        context.getParameters().add("keystorePath", keystore.toString());
        context.getParameters().add("keystorePassword", PASSWORD);
        context.getParameters().add("keyStoreType", "PKCS12");
        context.getParameters().add("keyPassword", PASSWORD);
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", PASSWORD);
        context.getParameters().add("trustStoreType", "PKCS12");
        context.getParameters().add(clientAuthParameter, "true");
        server = new Server(context, Protocol.HTTPS, 0, WHO_AM_I);
        server.start();
    }

    private Response call(boolean withCertificate) throws Exception {
        Context context = new Context();
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", PASSWORD);
        context.getParameters().add("trustStoreType", "PKCS12");
        if (withCertificate) {
            context.getParameters().add("keystorePath", keystore.toString());
            context.getParameters().add("keystorePassword", PASSWORD);
            context.getParameters().add("keyStoreType", "PKCS12");
            context.getParameters().add("keyPassword", PASSWORD);
        }
        context.getParameters().add("connectTimeout", "5000");
        context.getParameters().add("idleTimeout", "5000");
        client = new Client(context, Protocol.HTTPS);
        client.start();
        return client.handle(
                new Request(Method.GET, "https://localhost:" + server.getActualPort()));
    }

    @Test
    void requiredCertificateIsExposedToTheApplication() throws Exception {
        startServer("needClientAuthentication");
        Response response = call(true);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(
                response.getEntityAsText().contains("CN=localhost"),
                "Unexpected subject: " + response.getEntityAsText());
    }

    @Test
    void requiredCertificateMissingFailsTheHandshake() throws Exception {
        startServer("needClientAuthentication");
        Response response = call(false);
        assertTrue(response.getStatus().isConnectorError(), response.getStatus().toString());
    }

    @Test
    void optionalCertificateIsExposedWhenPresented() throws Exception {
        startServer("wantClientAuthentication");
        Response response = call(true);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(response.getEntityAsText().contains("CN=localhost"));
    }

    @Test
    void optionalCertificateMayBeOmitted() throws Exception {
        startServer("wantClientAuthentication");
        Response response = call(false);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("none", response.getEntityAsText());
    }
}
