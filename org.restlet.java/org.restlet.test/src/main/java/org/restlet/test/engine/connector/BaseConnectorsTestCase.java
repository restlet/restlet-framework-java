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
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.test.RestletTestCase;

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

    private final boolean enabledClientInternal = true;

    private final boolean enabledClientJetty = true;

    private final boolean enabledServerInternal = true;

    private final boolean enabledServerJetty = true;

    protected abstract void call(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    // Helper methods
    protected void runTest(ConnectorHelper<Server> server,
            ConnectorHelper<Client> client) throws Exception {
        // Engine.setLogLevel(Level.FINE);
        Engine nre = Engine.register(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        nre.registerDefaultAuthentications();
        nre.registerDefaultConverters();

        String host = start();
        String uri = getCallUri(host);
        try {
            call(uri);
        } finally {
            stop();
        }
    }

    protected String getCallUri(String host) {
        return host + "/test";
    }

    protected String start() throws Exception {
        this.component = new Component();
        Server server = this.component.getServers().add(Protocol.HTTP, 0);
        // server.getContext().getParameters().add("tracing", "true");
        Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "http://localhost:" + server.getEphemeralPort();
    }

    protected void stop() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
        this.component = null;
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

    @Test
    public void testInternalAndInternal() throws Exception {
        if (this.enabledServerInternal && this.enabledClientInternal) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    @Test
    public void testInternalAndJetty() throws Exception {
        if (this.enabledServerInternal && this.enabledClientJetty) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }

    @Test
    public void testJettyAndInternal() throws Exception {
        if (this.enabledServerJetty && this.enabledClientInternal) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    @Test
    public void testJettyAndJetty() throws Exception {
        if (this.enabledServerJetty && this.enabledClientJetty) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }
}
