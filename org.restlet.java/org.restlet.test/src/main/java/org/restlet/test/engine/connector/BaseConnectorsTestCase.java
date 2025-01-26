/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.adapter.HttpServerHelper;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.engine.connector.ServerHelper;
import org.restlet.test.RestletTestCase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 *
 * @author Kevin Conaway
 * @author Jerome Louvel
 */
public abstract class BaseConnectorsTestCase extends RestletTestCase {

    private Component component;

    protected abstract void doTestUri(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    protected String getCallUri(String host) {
        return host + "/test";
    }

    protected Stream<ConnectorTestCase> listTestCases() {
        return Stream.of(
                new ConnectorTestCase(HttpServer.INTERNAL, HttpClient.INTERNAL),
                new ConnectorTestCase(HttpServer.INTERNAL, HttpClient.JETTY),
                new ConnectorTestCase(HttpServer.JETTY, HttpClient.INTERNAL),
                new ConnectorTestCase(HttpServer.JETTY, HttpClient.JETTY)
        );
    }

    @TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() {
        return listTestCases()
                .map(testCase -> dynamicTest(
                        testCase.getTestLabel(),
                        () -> {
                            runTest(testCase.httpServer, testCase.httpClient);
                            resetEngine();
                        }));
    }

    private void runTest(final HttpServer server, final HttpClient client) throws Exception {
        // Engine.setLogLevel(Level.FINE);
        Engine nre = Engine.register(false);
        nre.getRegisteredServers().add(server.serverHelper);
        nre.getRegisteredClients().add(client.clientHelper);
        nre.registerDefaultAuthentications();
        nre.registerDefaultConverters();

        String host = start();
        String uri = getCallUri(host);
        try {
            doTestUri(uri);
        } finally {
            stop();
        }
    }

    public enum HttpServer {
        INTERNAL(new org.restlet.engine.connector.HttpServerHelper(null)), JETTY(new org.restlet.ext.jetty.HttpServerHelper(null));

        final ServerHelper serverHelper;

        HttpServer(HttpServerHelper serverHelper) {
            this.serverHelper = serverHelper;
        }
    }

    public enum HttpClient {
        INTERNAL(new org.restlet.engine.connector.HttpClientHelper(null)), JETTY(new org.restlet.ext.jetty.HttpClientHelper(null));

        final ClientHelper clientHelper;

        HttpClient(ClientHelper clientHelper) {
            this.clientHelper = clientHelper;
        }
    }

    private String start() throws Exception {
        this.component = new Component();
        Server server = this.component.getServers().add(Protocol.HTTP, 0);
        // server.getContext().getParameters().add("tracing", "true");
        Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "http://localhost:" + server.getEphemeralPort();
    }

    private void stop() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
        this.component = null;
    }

    private void resetEngine() {
        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

}
