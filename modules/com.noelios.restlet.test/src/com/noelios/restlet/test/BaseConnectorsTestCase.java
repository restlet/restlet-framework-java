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

    private static int port;

    static {
        port = 1137;
        if (System.getProperties().containsKey("restlet.test.port")) {
            port = Integer.parseInt(System.getProperty("restlet.test.port"));
        }
    }

    private Component component;

    private boolean incPorts;

    protected String uri;

    @Override
    public void setUp() {
        incPorts = false;
    }

    public void testDefaultAndDefault() throws Exception {
        runTest(new StreamServerHelper(null), new StreamClientHelper(null));
    }

    public void testDefaultAndHttpClient() throws Exception {
        runTest(new StreamServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testDefaultAndJdkNet() throws Exception {
        runTest(new StreamServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    public void testGrizzlyAndDefault() throws Exception {
        runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                new StreamClientHelper(null));
    }

    public void testGrizzlyAndHttpClient() throws Exception {
        runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testGrizzlyAndJdkNet() throws Exception {
        runTest(new com.noelios.restlet.ext.grizzly.HttpServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    public void testJettyAndDefault() throws Exception {
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new StreamClientHelper(null));
    }

    public void testJettyAndHttpClient() throws Exception {
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testJettyAndJdkNet() throws Exception {
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    public void testSimpleAndDefault() throws Exception {
        // Simple also does not shutdown cleanly, we need to increment
        // run on a different port each time.
        incPorts = true;
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new StreamClientHelper(null));
    }

    public void testSimpleAndHttpClient() throws Exception {
        incPorts = true;
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testSimpleAndJdkNet() throws Exception {
        incPorts = true;
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    // Helper methods
    private void runTest(ConnectorHelper server, ConnectorHelper client)
            throws Exception {
        Engine nre = new Engine(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        org.restlet.util.Engine.setInstance(nre);

        start();
        try {
            call();
        } finally {
            stop();
        }
    }

    private void start() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        uri = "http://localhost:" + (incPorts ? port++ : port) + "/test";

        Application application = createApplication(component);

        component.getDefaultHost().attach(application);
        component.start();
    }

    private void stop() throws Exception {
        if (component != null && component.isStarted()) {
            component.stop();
        }
    }

    protected abstract Application createApplication(Component component);

    protected abstract void call() throws Exception;
}
