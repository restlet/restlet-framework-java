/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.ClientHelper;
import com.noelios.restlet.Engine;
import com.noelios.restlet.ServerHelper;
import com.noelios.restlet.http.StreamClientHelper;
import com.noelios.restlet.http.StreamServerHelper;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations.
 * 
 * @author Kevin Conaway
 */
public abstract class BaseConnectorsTestCase extends TestCase {

    private Component component;

    private final boolean enableApacheClient = true;

    private final boolean enableGrizzlyServer = true;

    private final boolean enableInternalClient = true;

    private final boolean enableInternalServer = true;

    private final boolean enableJdkNetClient = true;

    private final boolean enableJettyServer = true;

    private final boolean enableSimpleServer = true;

    protected abstract void call(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    // Helper methods
    private void runTest(ServerHelper server, ClientHelper client)
            throws Exception {
        final Engine nre = new Engine(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        org.restlet.util.Engine.setInstance(nre);

        final String uri = start();
        try {
            call(uri);
        } finally {
            stop();
        }
    }

    @Override
    public void setUp() {
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

    public void testGrizzlyAndApache() throws Exception {
        if (this.enableGrizzlyServer && this.enableApacheClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testGrizzlyAndInternal() throws Exception {
        if (this.enableGrizzlyServer && this.enableInternalClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testGrizzlyAndJdkNet() throws Exception {
        if (this.enableGrizzlyServer && this.enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testInternalAndApache() throws Exception {
        if (this.enableInternalServer && this.enableApacheClient) {
            runTest(new StreamServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testInternalAndInternal() throws Exception {
        if (this.enableInternalServer && this.enableInternalClient) {
            runTest(new StreamServerHelper(null), new StreamClientHelper(null));
        }
    }

    public void testInternalAndJdkNet() throws Exception {
        if (this.enableInternalServer && this.enableJdkNetClient) {
            runTest(new StreamServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testJettyAndApache() throws Exception {
        if (this.enableJettyServer && this.enableApacheClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testJettyAndInternal() throws Exception {
        if (this.enableJettyServer && this.enableInternalClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testJettyAndJdkNet() throws Exception {
        if (this.enableJettyServer && this.enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSimpleAndApache() throws Exception {
        if (this.enableSimpleServer && this.enableApacheClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSimpleAndInternal() throws Exception {
        if (this.enableSimpleServer && this.enableInternalClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testSimpleAndJdkNet() throws Exception {
        if (this.enableSimpleServer && this.enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
