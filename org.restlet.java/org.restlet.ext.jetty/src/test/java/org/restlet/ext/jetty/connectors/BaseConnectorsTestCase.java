/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.connectors;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.adapter.HttpServerHelper;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.connector.ServerHelper;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 *
 * @author Kevin Conaway
 * @author Jerome Louvel
 */
public abstract class BaseConnectorsTestCase {
    private Component component;
    private int port;

    /**
     * The method to implement in the tests
     *
     * @param serverPort The port that the server is listening to.
     * @throws Exception
     */
    protected abstract void doTest(final int serverPort) throws Exception;

    protected boolean shouldDebug() {
        return false;
    }

    protected Server createServer(final Component component) {
        return component.getServers().add(Protocol.HTTP, 0);
    }

    protected void configureServer(final Server server) {
        server.getContext().getParameters().add("threadPool.minThreads", "1");
        server.getContext().getParameters().add("threadPool.maxThreads", "10");
        server.getContext().getParameters().add("shutdown.gracefully", "false");

        if (shouldDebug()) {
            server.getContext().getParameters().add("tracing", "true");
        }
    }

    protected abstract Application createApplication();

    protected List<ConnectorTestCase> listTestCases() {
        return List.of(
                // let's focus on Jetty server extension
                // new ConnectorTestCase(HttpServer.INTERNAL_HTTP, HttpClient.INTERNAL),
                // new ConnectorTestCase(HttpServer.INTERNAL_HTTP, HttpClient.JETTY),
                new ConnectorTestCase(HttpServer.JETTY_HTTP, HttpClient.INTERNAL),
                new ConnectorTestCase(HttpServer.JETTY_HTTP, HttpClient.JETTY)
        );
    }

    @TestFactory
    Stream<DynamicTest> testsFactory() {
        return listTestCases().stream()
                .map(testCase -> dynamicTest(
                        testCase.getTestLabel(),
                        () -> runTest(testCase.httpServer, testCase.httpClient)));
    }

    private void runTest(final HttpServer server, final HttpClient client) throws Exception {
        if (shouldDebug()) {
            System.setProperty("org.eclipse.jetty.LEVEL", "TRACE");
            System.setProperty("sun.net.www.protocol.http.HttpURLConnection.LEVEL", "ALL");
        }

        initEngine(server, client);
        start();
        try {
            doTest(port);
        } finally {
            stop();
            resetEngine();
        }
    }

    private void start() throws Exception {
        this.component = new Component();
        final Server server = createServer(this.component);
        configureServer(server);
        Application application = createApplication();

        this.component.getDefaultHost().attach(application);
        this.component.start();
        this.port = server.getEphemeralPort();
    }

    private void stop() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
        this.component = null;
    }

    private void initEngine(HttpServer server, HttpClient client) {
        if (shouldDebug()) {
            Engine.setLogLevel(Level.FINE);
        }

        Engine nre = Engine.register(false);
        nre.getRegisteredServers().add(server.serverHelper);
        nre.getRegisteredClients().add(client.clientHelper);
        nre.registerDefaultAuthentications();
        nre.registerDefaultConverters();
    }

    private void resetEngine() {
        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

    public enum HttpServer {
        INTERNAL_HTTP(new org.restlet.engine.connector.HttpServerHelper(null)),
        INTERNAL_HTTPS(new org.restlet.engine.connector.HttpsServerHelper(null)),
        JETTY_HTTP(new org.restlet.ext.jetty.HttpServerHelper(null)),
        JETTY_HTTPS(new org.restlet.ext.jetty.HttpsServerHelper(null));

        final ServerHelper serverHelper;

        HttpServer(HttpServerHelper serverHelper) {
            this.serverHelper = serverHelper;
        }
    }

    public enum HttpClient {
        INTERNAL(new HttpClientHelper(null)), JETTY(new org.restlet.ext.jetty.HttpClientHelper(null));

        final ClientHelper clientHelper;

        HttpClient(ClientHelper clientHelper) {
            this.clientHelper = clientHelper;
        }
    }

}
