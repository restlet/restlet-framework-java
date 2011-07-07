/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.test.ext.oauth;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.local.ClapClientHelper;
import org.restlet.engine.local.RiapClientHelper;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.oauth.Flow;
import org.restlet.ext.oauth.OAuthHelper;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.oauth.app.OAuthClientTestApplication;
import org.restlet.test.ext.oauth.app.OAuthComboTestApplication;
import org.restlet.test.ext.oauth.app.OAuthProtectedTestApplication;
import org.restlet.test.ext.oauth.app.OAuthTestApplication;
import org.restlet.util.Series;


public class OAuthHttpsTestBase extends RestletTestCase{
    
    protected Component component;
    protected OAuthClientTestApplication client;
    protected static long tokenTimeout = 0;
    
    public static int serverPort = 8080;
    public static final String prot = "https";
    
    
    private final File testDir = new File(System.getProperty("java.io.tmpdir"),
    "TimedTokenSSLTestCase");

    protected final File testKeystoreFile = new File(testDir, "dummy.jks");
    
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Engine engine = Engine.getInstance();
        //engine.getRegisteredClients().add(0, new org.restlet.ext.httpclient.HttpClientHelper(null));
        engine.getRegisteredServers().add(0,new org.restlet.ext.jetty.HttpsServerHelper(null));
        //engine.getRegisteredClients().add(new ClapClientHelper(null));
        //engine.getRegisteredClients().add(new RiapClientHelper(null));
        System.setProperty("javax.net.ssl.trustStore", testKeystoreFile.getPath());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStorePassword", "testtest");
        //create keystore:
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
        Server server;// = new Server( new Context(), Protocol.HTTPS, serverPort);
        
        
        component = new Component();
        server = component.getServers().add(Protocol.HTTPS, serverPort);
        //serverPort = server.getPort();
        configureSslServerParameters(server.getContext());
        component.getClients().add(Protocol.HTTPS);
        component.getClients().add(Protocol.RIAP);
        component.getClients().add(Protocol.CLAP);
        component.getDefaultHost().attach("/oauth",
                new OAuthTestApplication(tokenTimeout, "https", 8080)); // limited token life
        client = new OAuthClientTestApplication("https", 8080);
        component.getDefaultHost().attach("/client", client);
        component.getDefaultHost().attach("/server",
                new OAuthProtectedTestApplication("https", 8080));
        component.getDefaultHost().attach("/combo",
                new OAuthComboTestApplication(prot, serverPort, 0)); // unlimited token life
        

        List<AuthenticatorHelper> authenticators = Engine.getInstance()
                .getRegisteredAuthenticators();
        authenticators.add(new OAuthHelper());

        component.start();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        BioUtils.delete(this.testKeystoreFile);
        BioUtils.delete(this.testDir, true);
        // Restore a clean engine
        org.restlet.engine.Engine.register();
        super.tearDown();
    }

}
