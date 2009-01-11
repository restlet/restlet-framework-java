/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.engine.ClientHelper;
import org.restlet.engine.Engine;
import org.restlet.engine.ServerHelper;
import org.restlet.engine.local.ClapClientHelper;
import org.restlet.util.Series;


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

    private final File testDir = new File(System.getProperty("java.io.tmpdir"),
            "SslBaseConnectorsTestCase");

    private final File testKeystoreFile = new File(testDir, "dummy.jks");

    protected abstract void call(String uri) throws Exception;

    protected void configureSslParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("sslContextFactory",
                "org.restlet.engine.util.DefaultSslContextFactory");
        parameters.add("keystorePath", testKeystoreFile.getPath());
        parameters.add("keystorePassword", "testtest");
        parameters.add("keyPassword", "testtest");
    }

    protected abstract Application createApplication(Component component);

    // Helper methods
    private void runTest(ServerHelper server, ClientHelper client)
            throws Exception {

        final Engine nre = new Engine(false);
        nre.getRegisteredClients().add(new ClapClientHelper(null));
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
        // Restore a clean engine
        org.restlet.util.Engine.setInstance(new Engine());

        try {
            if (!testKeystoreFile.exists()) {
                // Prepare a temporary directory for the tests
                this.testDir.delete();
                this.testDir.mkdir();
                // Copy the keystore into the test directory
                Response response = new Client(Protocol.CLAP)
                        .get("clap://class/org/restlet/test/engine/dummy.jks");

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

    private String start() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", testKeystoreFile
                .getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "testtest");

        this.component = new Component();

        final Server server = this.component.getServers()
                .add(Protocol.HTTPS, 0);
        configureSslParameters(server.getContext());
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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        this.testKeystoreFile.delete();
        this.testDir.delete();

        // Restore a clean engine
        org.restlet.util.Engine.setInstance(new Engine());
    }

    public void testSslGrizzlyAndApache() throws Exception {
        if (this.enableGrizzlyServer && this.enableApacheClient) {
            runTest(
                    new org.restlet.ext.grizzly.HttpsServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSslGrizzlyAndJdkNet() throws Exception {
        if (this.enableGrizzlyServer && this.enableJdkNetClient) {
            runTest(
                    new org.restlet.ext.grizzly.HttpsServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }

    public void testSslJettyAndApache() throws Exception {
        if (this.enableJettyServer && this.enableApacheClient) {
            runTest(new org.restlet.ext.jetty.HttpsServerHelper(null),
                    new org.restlet.ext.httpclient.HttpClientHelper(
                            null));
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
                    new org.restlet.ext.httpclient.HttpClientHelper(
                            null));
        }
    }

    public void testSslSimpleAndJdkNet() throws Exception {
        if (this.enableSimpleServer && this.enableJdkNetClient) {
            runTest(new org.restlet.ext.simple.HttpsServerHelper(null),
                    new org.restlet.ext.net.HttpClientHelper(null));
        }
    }
}
