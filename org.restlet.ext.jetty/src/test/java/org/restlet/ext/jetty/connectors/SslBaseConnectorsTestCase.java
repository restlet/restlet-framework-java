/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.connectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.restlet.*;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.Series;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations. (Modified for SSL support.)
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot
 * @author Jerome Louvel
 */
@SuppressWarnings("unused")
public abstract class SslBaseConnectorsTestCase extends BaseConnectorsTestCase {

    protected static final String KEYSTORE_FILE_NAME = "dummy.p12";
    protected static final String KEYSTORE_PASSWORD = "testtest";
    protected static final String KEYSTORE_TYPE = "PKCS12";
    protected static File testKeystoreFile;

    @BeforeAll
    public static void globalSetUp() throws IOException {
        testKeystoreFile = Files.createTempFile("sslBaseConnectorsTest", KEYSTORE_FILE_NAME).toFile();
        testKeystoreFile.delete();

        InputStream resourceAsStream = SslBaseConnectorsTestCase.class.getResourceAsStream(KEYSTORE_FILE_NAME);
        if (resourceAsStream != null) {
            OutputStream outputStream = new FileOutputStream(testKeystoreFile);
            IoUtils.copy(resourceAsStream, outputStream);
            outputStream.flush();
            outputStream.close();
        } else {
            throw new RuntimeException("Can't find the key store");
        }

    }

    @Override
    protected List<ConnectorTestCase> listTestCases() {
        return List.of(
                // let's focus on Jetty server extension
                // new ConnectorTestCase(HttpServer.INTERNAL_HTTPS, HttpClient.JETTY),
                // new ConnectorTestCase(HttpServer.INTERNAL_HTTPS, HttpClient.INTERNAL),
                new ConnectorTestCase(HttpServer.JETTY_HTTPS, HttpClient.INTERNAL),
                new ConnectorTestCase(HttpServer.JETTY_HTTPS, HttpClient.JETTY)
        );
    }

    @Override
    protected Server createServer(Component component) {
        return component.getServers().add(Protocol.HTTPS, 0);
    }

    @Override
    protected void configureServer(final Server server) {
        super.configureServer(server);
        configureSslServerParameters(server);
    }

    protected void configureSslClientParameters(final Client client) {
        Series<Parameter> parameters = client.getContext().getParameters();

        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", KEYSTORE_PASSWORD);
        parameters.add("trustStoreType", KEYSTORE_TYPE);
        if (shouldDebug()) {
            parameters.add("tracing", "true");
        }
    }

    protected void configureSslServerParameters(final Server server) {
        Series<Parameter> parameters = server.getContext().getParameters();

        parameters.add("keyStorePath", testKeystoreFile.getPath());
        parameters.add("keyStorePassword", KEYSTORE_PASSWORD);
        parameters.add("keyStoreType", KEYSTORE_TYPE);
        parameters.add("keyPassword", KEYSTORE_PASSWORD);

        parameters.add("trustStorePath", testKeystoreFile.getPath());
        parameters.add("trustStorePassword", KEYSTORE_PASSWORD);
        parameters.add("trustStoreType", KEYSTORE_TYPE);

        if (shouldDebug()) {
            System.setProperty("javax.net.debug", "ssl:handshake");
            parameters.add("tracing", "true");
        }
    }

    @AfterAll
    protected static void tearDown() {
        testKeystoreFile.delete();

        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

}
