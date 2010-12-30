/**
 * Copyright 2005-2010 Noelios Technologies.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.Engine;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.local.ClapClientHelper;
import org.restlet.test.RestletTestCase;
import org.restlet.util.Series;

/**
 * Base test case that will call an abstract method for several client/server
 * connectors configurations. (Modified for SSL support.)
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public abstract class SslBaseConnectorsTestCase extends RestletTestCase {

    private Component component;

    private final boolean enableApacheClient = false;

    private final boolean enableInternalClient = true;

    private final boolean enableInternalServer = false;

    private final boolean enableJdkNetClient = false;

    private final boolean enableJettyServer = true;

    private final boolean enableSimpleServer = true;

    private final File testDir = new File(System.getProperty("java.io.tmpdir"),
            "SslBaseConnectorsTestCase");

    protected final File testKeystoreFile = new File(testDir, "dummy.jks");

    protected abstract void call(String uri) throws Exception;

    protected void configureSslClientParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
    }

    protected void configureSslServerParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("keystorePath", testKeystoreFile.getPath());
        parameters.add("keystorePassword", "testtest");
        parameters.add("keyPassword", "testtest");
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
    }

    protected abstract Application createApplication(Component component);

    // Helper methods
    private void runTest(ConnectorHelper<Server> server,
            ConnectorHelper<Client> client) throws Exception {
        Engine engine = new Engine(false);
        engine.getRegisteredClients().add(new ClapClientHelper(null));
        engine.getRegisteredServers().add(server);
        engine.getRegisteredClients().add(client);
        org.restlet.engine.Engine.setInstance(engine);
        String uri = start();

        try {
            call(uri);
        } finally {
            stop();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        try {
            if (!testKeystoreFile.exists()) {
                // Prepare a temporary directory for the tests
                BioUtils.delete(this.testDir, true);
                this.testDir.mkdir();
                // Copy the keystore into the test directory
                Response response = new Client(Protocol.CLAP)
                        .handle(new Request(Method.GET,
                                "clap://class/org/restlet/test/engine/dummy.jks"));

                if (response.getEntity() != null) {
                    OutputStream outputStream = new FileOutputStream(
                            testKeystoreFile);
                    response.getEntity().write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                } else {
                    throw new Exception(
                            "Unable to find the dummy.jks file in the classpath.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String start() throws Exception {
        this.component = new Component();

        final Server server = this.component.getServers()
                .add(Protocol.HTTPS, 0);
        configureSslServerParameters(server.getContext());
        final Application application = createApplication(this.component);

        this.component.getDefaultHost().attach(application);
        this.component.start();

        return "https://localhost:" + server.getEphemeralPort() + "/test";
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
        BioUtils.delete(this.testKeystoreFile);
        BioUtils.delete(this.testDir, true);

        // Restore a clean engine
        org.restlet.engine.Engine.setInstance(new Engine());
    }

    public void testSslInternalAndApache() throws Exception {
        if (this.enableInternalServer && this.enableApacheClient) {
            runTest(new org.restlet.engine.connector.HttpsServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testSslInternalAndInternal() throws Exception {
        if (this.enableInternalServer && this.enableInternalClient) {
            runTest(new org.restlet.engine.connector.HttpsServerHelper(null),
                    new org.restlet.engine.connector.HttpsClientHelper(null));
        }
    }

    public void testSslInternalAndJdkNet() throws Exception {
        if (this.enableInternalServer && this.enableJdkNetClient) {
            runTest(new org.restlet.engine.connector.HttpsServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSslJettyAndApache() throws Exception {
        if (this.enableJettyServer && this.enableApacheClient) {
            runTest(new org.restlet.ext.jetty.HttpsServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testSslJettyAndInternal() throws Exception {
        if (this.enableJettyServer && this.enableInternalClient) {
            runTest(new org.restlet.ext.jetty.HttpsServerHelper(null),
                    new org.restlet.engine.connector.HttpsClientHelper(null));
        }
    }

    public void testSslJettyAndJdkNet() throws Exception {
        if (this.enableJettyServer && this.enableJdkNetClient) {
            runTest(new org.restlet.ext.jetty.HttpsServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSslSimpleAndApache() throws Exception {
        if (this.enableSimpleServer && this.enableApacheClient) {
            runTest(new org.restlet.ext.simple.HttpsServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(null));
        }
    }

    public void testSslSimpleAndInternal() throws Exception {
        if (this.enableSimpleServer && this.enableInternalClient) {
            runTest(new org.restlet.ext.simple.HttpsServerHelper(null),
                    new org.restlet.engine.connector.HttpsClientHelper(null));
        }
    }

    public void testSslSimpleAndJdkNet() throws Exception {
        if (this.enableSimpleServer && this.enableJdkNetClient) {
            runTest(new org.restlet.ext.simple.HttpsServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
