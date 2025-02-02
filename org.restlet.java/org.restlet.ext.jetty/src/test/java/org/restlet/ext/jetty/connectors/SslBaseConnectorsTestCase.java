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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.restlet.*;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.Series;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

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

    protected static File testKeystoreFile;

    protected void configureSslClientParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
    }

    protected void configureSslServerParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("keystorePath", testKeystoreFile.getPath());
        parameters.add("keystorePassword", "testtest");
        parameters.add("keyPassword", "testtest");
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
        // parameters.add("tracing", "true");
    }

    @Override
    protected String getCallUri(final int port) {
        return "https://localhost:" + port + "/test";
    }

    @BeforeAll
    public static void globalSetUp() throws IOException {
        testKeystoreFile = Files.createTempFile("sslBaseConnectorsTest", "dummy.jks").toFile();
        testKeystoreFile.delete();

        InputStream resourceAsStream = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.jks");
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
    protected Stream<ConnectorTestCase> listTestCases() {
        return Stream.of(
                new ConnectorTestCase(HttpServer.INTERNAL_HTTPS, HttpClient.JETTY),
                // new ConnectorTestCase(HttpServer.JETTY_HTTPS, HttpClient.INTERNAL), // restore while taking care of #1444
                // new ConnectorTestCase(HttpServer.JETTY_HTTPS, HttpClient.JETTY), // restore while taking care of #1444
                new ConnectorTestCase(HttpServer.INTERNAL_HTTPS, HttpClient.INTERNAL)
        );
    }

    @Override
    protected Server configureServer(final Component component) {
        final Server server = component.getServers().add(Protocol.HTTPS, 0);
        configureSslServerParameters(server.getContext());
        // server.getContext().getParameters().add("tracing", "true");
        return server;
    }

    @AfterAll
    protected static void tearDown() {
        testKeystoreFile.delete();

        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

}
