/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine.connector;

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

    private final boolean enabledClientApache = true;

    private final boolean enabledClientInternal = true;

    private final boolean enabledClientJetty = false;

    private final boolean enabledClientNio = false;

    private final boolean enabledServerInternal = true;

    private final boolean enabledServerJetty = true;

    private final boolean enabledServerNio = false;

    private final boolean enabledServerSimple = true;

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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Restore a clean engine
        org.restlet.engine.Engine.register();
    }

    public void testInternalAndApache() throws Exception {
        if (this.enabledServerInternal && this.enabledClientApache) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testInternalAndInternal() throws Exception {
        if (this.enabledServerInternal && this.enabledClientInternal) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    public void testInternalAndJetty() throws Exception {
        if (this.enabledServerInternal && this.enabledClientJetty) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }

    public void testInternalAndNio() throws Exception {
        if (this.enabledServerInternal && this.enabledClientNio) {
            runTest(new org.restlet.engine.connector.HttpServerHelper(null),
                    new org.restlet.ext.nio.HttpClientHelper(null));
        }
    }

    public void testJettyAndApache() throws Exception {
        if (this.enabledServerJetty && this.enabledClientApache) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testJettyAndInternal() throws Exception {
        if (this.enabledServerJetty && this.enabledClientInternal) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    public void testJettyAndJetty() throws Exception {
        if (this.enabledServerJetty && this.enabledClientJetty) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }

    public void testJettyAndNio() throws Exception {
        if (this.enabledServerJetty && this.enabledClientNio) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.nio.HttpClientHelper(null));
        }
    }

    public void testNioAndApache() throws Exception {
        if (this.enabledServerNio && this.enabledClientApache) {
            runTest(new org.restlet.ext.nio.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testNioAndInternal() throws Exception {
        if (this.enabledServerNio && this.enabledClientInternal) {
            runTest(new org.restlet.ext.nio.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    public void testNioAndJetty() throws Exception {
        if (this.enabledServerNio && this.enabledClientJetty) {
            runTest(new org.restlet.ext.nio.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }

    public void testNioAndNio() throws Exception {
        if (this.enabledServerNio && this.enabledClientNio) {
            runTest(new org.restlet.ext.nio.HttpServerHelper(null),
                    new org.restlet.ext.nio.HttpClientHelper(null));
        }
    }

    public void testSimpleAndApache() throws Exception {
        if (this.enabledServerSimple && this.enabledClientApache) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testSimpleAndInternal() throws Exception {
        if (this.enabledServerSimple && this.enabledClientInternal) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.engine.connector.HttpClientHelper(null));
        }
    }

    public void testSimpleAndJetty() throws Exception {
        if (this.enabledServerSimple && this.enabledClientJetty) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.jetty.HttpClientHelper(null));
        }
    }

    public void testSimpleAndNio() throws Exception {
        if (this.enabledServerSimple && this.enabledClientNio) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.nio.HttpClientHelper(null));
        }
    }
}
