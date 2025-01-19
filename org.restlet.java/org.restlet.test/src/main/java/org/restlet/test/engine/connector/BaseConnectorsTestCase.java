/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 * 
 * @author Kevin Conaway
 * @author Jerome Louvel
 */
@SuppressWarnings("unused")
public abstract class BaseConnectorsTestCase extends RestletTestCase {

    private Component component;

    protected abstract void doTestUri(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    protected String getCallUri(String host) {
        return host + "/test";
    }

    @ParameterizedTest(name = "{0} server and {1} client")
    @MethodSource("listTestCases")
    public void runTest(final HttpServer server, final HttpClient client) throws Exception {
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

    private static Stream<Arguments> listTestCases() {
        return Stream.of(
                Arguments.of(HttpServer.INTERNAL, HttpClient.INTERNAL),
                Arguments.of(HttpServer.INTERNAL, HttpClient.JETTY),
                Arguments.of(HttpServer.JETTY, HttpClient.INTERNAL),
                Arguments.of(HttpServer.JETTY, HttpClient.JETTY)
        );
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

    @AfterEach
    protected void resetEngine() {
        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

    public class TestCase {
        final HttpServer httpServer;
        final HttpClient httpClient;

        public TestCase(HttpServer httpServer, HttpClient httpClient) {
            this.httpServer = httpServer;
            this.httpClient = httpClient;
        }
    }

}
