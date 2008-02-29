/*
 * Copyright 2005-2008 Noelios Consulting.
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

import com.noelios.restlet.ConnectorHelper;
import com.noelios.restlet.Engine;
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

    private boolean enableApacheClient = true;

    private boolean enableGrizzlyServer = true;

    private boolean enableInternalClient = true;

    private boolean enableInternalServer = true;

    private boolean enableJdkNetClient = true;

    private boolean enableJettyServer = true;

    private boolean enableSimpleServer = true;

    protected abstract void call(String uri) throws Exception;

    protected abstract Application createApplication(Component component);

    // Helper methods
    private void runTest(ConnectorHelper server, ConnectorHelper client)
            throws Exception {
        Engine nre = new Engine(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        org.restlet.util.Engine.setInstance(nre);

        String uri = start();
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
        component = new Component();
        Server server = component.getServers().add(Protocol.HTTP, 0);
        Application application = createApplication(component);

        component.getDefaultHost().attach(application);
        component.start();

        return "http://localhost:" + server.getEphemeralPort() + "/test";
    }

    private void stop() throws Exception {
        if (component != null && component.isStarted()) {
            component.stop();
        }
    }

    public void testGrizzlyAndApache() throws Exception {
        if (enableGrizzlyServer && enableApacheClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testGrizzlyAndInternal() throws Exception {
        if (enableGrizzlyServer && enableInternalClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testGrizzlyAndJdkNet() throws Exception {
        if (enableGrizzlyServer && enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testInternalAndApache() throws Exception {
        if (enableInternalServer && enableApacheClient) {
            runTest(new StreamServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testInternalAndInternal() throws Exception {
        if (enableInternalServer && enableInternalClient) {
            runTest(new StreamServerHelper(null), new StreamClientHelper(null));
        }
    }

    public void testInternalAndJdkNet() throws Exception {
        if (enableInternalServer && enableJdkNetClient) {
            runTest(new StreamServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testJettyAndApache() throws Exception {
        if (enableJettyServer && enableApacheClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testJettyAndInternal() throws Exception {
        if (enableJettyServer && enableInternalClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testJettyAndJdkNet() throws Exception {
        if (enableJettyServer && enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSimpleAndApache() throws Exception {
        if (enableSimpleServer && enableApacheClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSimpleAndInternal() throws Exception {
        if (enableSimpleServer && enableInternalClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new StreamClientHelper(null));
        }
    }

    public void testSimpleAndJdkNet() throws Exception {
        if (enableSimpleServer && enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
