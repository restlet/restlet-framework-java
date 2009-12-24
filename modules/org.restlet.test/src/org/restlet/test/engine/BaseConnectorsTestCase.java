/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.Engine;
import org.restlet.engine.http.connector.HttpClientHelper;
import org.restlet.engine.http.connector.HttpServerHelper;
import org.restlet.test.RestletTestCase;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 * 
 * @author Kevin Conaway
 */
public abstract class BaseConnectorsTestCase extends RestletTestCase {

    private final boolean apacheClientEnabled = true;

    private Component component;

    private final boolean grizzlyServerEnabled = true;

    private final boolean internalClientEnabled = true;

    private final boolean internalServerEnabled = true;

    private final boolean jdkNetClientEnabled = true;

    private final boolean jettyServerEnabled = true;

    private final boolean nettyServerEnabled = true;

    private final boolean simpleServerEnabled = true;

    protected abstract void call(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    // Helper methods
    private void runTest(ConnectorHelper<Server> server,
            ConnectorHelper<Client> client) throws Exception {
        final Engine nre = new Engine(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        nre.registerDefaultAuthentications();
        nre.registerDefaultConverters();
        org.restlet.engine.Engine.setInstance(nre);

        final String uri = start();
        try {
            call(uri);
        } finally {
            stop();
        }
    }

    private String start() throws Exception {
        this.component = new Component();
        final Server server = this.component.getServers().add(Protocol.HTTP, 0);
        final Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "http://localhost:" + server.getEphemeralPort() + "/test";
    }

    private void stop() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Restore a clean engine
        org.restlet.engine.Engine.setInstance(new Engine());
    }

    public void testGrizzlyAndApache() throws Exception {
        if (this.grizzlyServerEnabled && this.apacheClientEnabled) {
            runTest(new org.restlet.ext.grizzly.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testGrizzlyAndInternal() throws Exception {
        if (this.grizzlyServerEnabled && this.internalClientEnabled) {
            runTest(new org.restlet.ext.grizzly.HttpServerHelper(null),
                    new HttpClientHelper(null));
        }
    }

    public void testGrizzlyAndJdkNet() throws Exception {
        if (this.grizzlyServerEnabled && this.jdkNetClientEnabled) {
            runTest(new org.restlet.ext.grizzly.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testInternalAndApache() throws Exception {
        if (this.internalServerEnabled && this.apacheClientEnabled) {
            runTest(new HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testInternalAndInternal() throws Exception {
        if (this.internalServerEnabled && this.internalClientEnabled) {
            runTest(new HttpServerHelper(null), new HttpClientHelper(null));
        }
    }

    public void testInternalAndJdkNet() throws Exception {
        if (this.internalServerEnabled && this.jdkNetClientEnabled) {
            runTest(new HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testJettyAndApache() throws Exception {
        if (this.jettyServerEnabled && this.apacheClientEnabled) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testJettyAndInternal() throws Exception {
        if (this.jettyServerEnabled && this.internalClientEnabled) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new HttpClientHelper(null));
        }
    }

    public void testJettyAndJdkNet() throws Exception {
        if (this.jettyServerEnabled && this.jdkNetClientEnabled) {
            runTest(new org.restlet.ext.jetty.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testNettyAndApache() throws Exception {
        if (this.nettyServerEnabled && this.apacheClientEnabled) {
            runTest(new org.restlet.ext.netty.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testNettyAndInternal() throws Exception {
        if (this.nettyServerEnabled && this.internalClientEnabled) {
            runTest(new org.restlet.ext.netty.HttpServerHelper(null),
                    new HttpClientHelper(null));
        }
    }

    public void testNettyAndJdkNet() throws Exception {
        if (this.nettyServerEnabled && this.jdkNetClientEnabled) {
            runTest(new org.restlet.ext.netty.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSimpleAndApache() throws Exception {
        if (this.simpleServerEnabled && this.apacheClientEnabled) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testSimpleAndInternal() throws Exception {
        if (this.simpleServerEnabled && this.internalClientEnabled) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new HttpClientHelper(null));
        }
    }

    public void testSimpleAndJdkNet() throws Exception {
        if (this.simpleServerEnabled && this.jdkNetClientEnabled) {
            runTest(new org.restlet.ext.simple.HttpServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
