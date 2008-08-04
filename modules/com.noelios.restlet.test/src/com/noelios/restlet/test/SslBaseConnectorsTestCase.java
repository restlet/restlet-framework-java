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
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.ClientHelper;
import com.noelios.restlet.Engine;
import com.noelios.restlet.ServerHelper;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations. (Modified for SSL support.)
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public abstract class SslBaseConnectorsTestCase extends TestCase {

    private Component component;

    private final boolean enableApacheClient = true;

    private final boolean enableGrizzlyServer = true;

    private final boolean enableJdkNetClient = true;

    private final boolean enableJettyServer = true;

    private final boolean enableSimpleServer = true;

    protected abstract void call(String uri) throws Exception;

    protected abstract void configureSslParameters(Context context);

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
        System.setProperty("javax.net.ssl.keyStorePassword", System
                .getProperty("javax.net.ssl.keyStorePassword", "testtest"));
        System.setProperty("javax.net.ssl.trustStorePassword", System
                .getProperty("javax.net.ssl.trustStorePassword", "testtest"));
        System.setProperty("javax.net.ssl.keyStore", System.getProperty(
                "javax.net.ssl.keyStore", "dummy.jks"));
        System.setProperty("javax.net.ssl.trustStore", System.getProperty(
                "javax.net.ssl.trustStore", "dummy.jks"));

        this.component = new Component();
        configureSslParameters(this.component.getContext());

        final Server server = this.component.getServers()
                .add(Protocol.HTTPS, 0);
        final Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "https://localhost:" + server.getEphemeralPort() + "/test";
    }

    private void stop() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
    }

    public void testSslGrizzlyAndApache() throws Exception {
        if (this.enableGrizzlyServer && this.enableApacheClient) {
            runTest(
                    new com.noelios.restlet.ext.grizzly.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSslGrizzlyAndJdkNet() throws Exception {
        if (this.enableGrizzlyServer && this.enableJdkNetClient) {
            runTest(
                    new com.noelios.restlet.ext.grizzly.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSslJettyAndApache() throws Exception {
        if (this.enableJettyServer && this.enableApacheClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSslJettyAndJdkNet() throws Exception {
        if (this.enableJettyServer && this.enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.jetty.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSslSimpleAndApache() throws Exception {
        if (this.enableSimpleServer && this.enableApacheClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSslSimpleAndJdkNet() throws Exception {
        if (this.enableSimpleServer && this.enableJdkNetClient) {
            runTest(new com.noelios.restlet.ext.simple.HttpsServerHelper(null),
                    new com.noelios.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
