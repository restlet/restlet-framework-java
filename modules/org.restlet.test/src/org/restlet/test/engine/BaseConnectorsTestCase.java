/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.test.RestletTestCase;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 * 
 * @author Kevin Conaway
 */
public abstract class BaseConnectorsTestCase extends RestletTestCase {

    private Component component;

    private final boolean enabledClientApache = true;

    private final boolean enabledClientInternal = true;

    private final boolean enabledClientJdkNet = true;

    private final boolean enabledServerInternal = true;

    private final boolean enabledServerJetty = false;

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

        String uri = start();
        try {
            call(uri);
        } finally {
            stop();
        }
    }

    protected String start() throws Exception {
        this.component = new Component();
        Server server = this.component.getServers().add(Protocol.HTTP, 0);
        // server.getContext().getParameters().add("tracing", "true");
        Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "http://localhost:" + server.getEphemeralPort() + "/test";
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
            runTest(new HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testInternalAndInternal() throws Exception {
        if (this.enabledServerInternal && this.enabledClientInternal) {
            runTest(new HttpServerHelper(null), new HttpClientHelper(null));
        }
    }

    public void testInternalAndJdkNet() throws Exception {
        if (this.enabledServerInternal && this.enabledClientJdkNet) {
            runTest(new HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
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
                    new HttpClientHelper(null));
        }
    }

    public void testJettyAndJdkNet() throws Exception {
        if (this.enabledServerJetty && this.enabledClientJdkNet) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
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
                    new HttpClientHelper(null));
        }
    }

    public void testSimpleAndJdkNet() throws Exception {
        if (this.enabledServerSimple && this.enabledClientJdkNet) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
