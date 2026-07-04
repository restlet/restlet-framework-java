/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/** Unit tests for {@link CertificateAuthenticator}. */
class CertificateAuthenticatorTestCase {

    private static X509Certificate selfSignedCertificate;

    @BeforeAll
    static void generateSelfSignedCertificate() throws Exception {
        File keystoreFile = File.createTempFile("CertificateAuthenticatorTestCase", ".p12");
        keystoreFile.deleteOnExit();
        keystoreFile.delete();

        ProcessBuilder pb =
                new ProcessBuilder(
                        "keytool",
                        "-genkeypair",
                        "-alias",
                        "test",
                        "-keyalg",
                        "RSA",
                        "-keysize",
                        "2048",
                        "-validity",
                        "1",
                        "-storetype",
                        "PKCS12",
                        "-keystore",
                        keystoreFile.getAbsolutePath(),
                        "-storepass",
                        "changeit",
                        "-dname",
                        "CN=test-user, OU=Restlet, O=Restlet, L=Paris, ST=IDF, C=FR");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getInputStream().readAllBytes();
        int exit = process.waitFor();
        org.junit.jupiter.api.Assumptions.assumeTrue(exit == 0, "keytool must be available");

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(keystoreFile)) {
            keyStore.load(in, "changeit".toCharArray());
        }
        selfSignedCertificate = (X509Certificate) keyStore.getCertificate("test");
        keystoreFile.delete();
    }

    private Request requestWithCertificates(List<Certificate> certificates) {
        Request request = new Request();
        request.getClientInfo().setCertificates(certificates);
        return request;
    }

    @Test
    void authenticate_withoutCertificates_setsUnauthorizedStatus() {
        CertificateAuthenticator authenticator = new CertificateAuthenticator(new Context());
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void authenticate_withNonX509Certificate_setsUnauthorizedStatus() {
        CertificateAuthenticator authenticator = new CertificateAuthenticator(new Context());
        Certificate nonX509 =
                new Certificate("non-x509") {
                    @Override
                    public byte[] getEncoded() {
                        return new byte[0];
                    }

                    @Override
                    public void verify(java.security.PublicKey key) {}

                    @Override
                    public void verify(java.security.PublicKey key, String sigProvider) {}

                    @Override
                    public String toString() {
                        return "non-x509";
                    }

                    @Override
                    public java.security.PublicKey getPublicKey() {
                        return null;
                    }
                };
        Request request = requestWithCertificates(List.of(nonX509));
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void authenticate_withX509Certificate_setsUserAndPrincipal() {
        org.junit.jupiter.api.Assumptions.assumeTrue(selfSignedCertificate != null);

        CertificateAuthenticator authenticator = new CertificateAuthenticator(new Context());
        Request request = requestWithCertificates(List.of(selfSignedCertificate));
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertTrue(result);
        assertTrue(request.getClientInfo().getUser().getIdentifier().contains("test-user"));
        assertEquals(1, request.getClientInfo().getPrincipals().size());
    }
}
