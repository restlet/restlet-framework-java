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

package org.restlet.test.ext.oauth;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.oauth.HttpOAuthHelper;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.oauth.app.OAuthClientTestApplication;
import org.restlet.test.ext.oauth.app.OAuthComboTestApplication;
import org.restlet.test.ext.oauth.app.OAuthMultipleUserProtectedTestApplication;
import org.restlet.test.ext.oauth.app.OAuthMultipleUserTestApplication;
import org.restlet.test.ext.oauth.app.OAuthProtectedTestApplication;
import org.restlet.test.ext.oauth.app.OAuthTestApplication;
import org.restlet.util.Series;

public abstract class OAuthHttpTestBase extends RestletTestCase{

    public enum ClientConnector {INTERNAL, HTTP_CLIENT, NET};
    public enum ServerConnector {INTERNAL, SIMPLE, JETTY};
    protected Component component, oauthcomp;

    // Use for http test when debugging
    protected int serverPort = 8080;
    protected int oauthServerPort = 8081;
    //public static String prot = "http";
    private final boolean https; 
    public static int tokenTimeout = 0;
    private final boolean multiple;
    private final ClientConnector cc;
    private final ServerConnector sc;
    
    private final File testDir = new File(System.getProperty("java.io.tmpdir"),
    "TimedTokenSSLTestCase");

    protected final File testKeystoreFile = new File(testDir, "dummy.jks");


    protected OAuthClientTestApplication client;
    protected Client reqClient;

    protected OAuthHttpTestBase(){
        this(false, false);
    }

    protected OAuthHttpTestBase(boolean multiple, boolean https){
        this(multiple, https, ClientConnector.INTERNAL, ServerConnector.INTERNAL);
    }

    protected OAuthHttpTestBase(boolean multiple, boolean https, ClientConnector cc, ServerConnector sc){
        //generate ports
        Random r = new Random();
        int portNo = r.nextInt(1000);
        this.serverPort = 9000 + portNo;
        this.oauthServerPort = 9000 + portNo + 1;
        this.multiple = multiple;
        this.https = https;
        this.cc = cc;
        if(this.https && sc == ServerConnector.INTERNAL)
            this.sc = ServerConnector.JETTY;
        else
            this.sc = sc;
        Engine.getAnonymousLogger().info("Oauth TEST:\t"+multiple+"\t"
                +getProt()+"\t"+cc+"\t"+this.sc+"\t"+serverPort+"\t"+oauthServerPort);
    }
    
    protected String getProt(){
        return https ? "https" : "http";
    }
    
    protected Client createClient(){
        Client c;
        if(https){
            c = new Client(getProt());
            c.setContext(this.getSslClientContext());
        }
        else{
            c = new Client(getProt());
        }
        return c;
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        setupConnectors(cc, sc);
        if(https) setupHttps();
        this.reqClient = this.createClient();
        List<AuthenticatorHelper> authenticators = Engine.getInstance()
        .getRegisteredAuthenticators();
        authenticators.add(new HttpOAuthHelper());
        if(multiple)
            setupMultiple();
        else
            setupSingle();
    }
    
    protected void configureSslClientParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
    }
    
    protected Series <Parameter> getSslClientParameters() {
        Series<Parameter> parameters = new Form();
        parameters.add("truststorePath", testKeystoreFile.getPath());
        parameters.add("truststorePassword", "testtest");
        return parameters;
    }
    
    protected Context getSslClientContext(){
        Context c = new Context();
        configureSslClientParameters(c);
        return c;
    }

    protected void configureSslServerParameters(Context context) {
        Series<Parameter> parameters = context.getParameters();
        parameters.add("keyStorePath", testKeystoreFile.getPath());
        parameters.add("keyStorePassword", "testtest");
        parameters.add("keyPassword", "testtest");
        parameters.add("trustStorePath", testKeystoreFile.getPath());
        parameters.add("trustStorePassword", "testtest");
    }
    
    protected void setupHttps(){
        
        //System.setProperty("javax.net.ssl.trustStore", testKeystoreFile.getPath());
        //System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        //System.setProperty("javax.net.ssl.trustStorePassword", "testtest");
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

    protected void setupConnectors(ClientConnector cc, ServerConnector sc){
        Engine engine = Engine.getInstance();
        //engine.get
        switch(cc){
        case INTERNAL:
            //engine.getRegisteredClients().add(0, new org.restlet.ext.ssl.HttpsClientHelper(null));
            //engine.getRegisteredClients().add(0, 
            //        new org.restlet.engine.connector.HttpClientHelper(null));
            break;
        case HTTP_CLIENT:
            engine.getRegisteredClients().add(0, new org.restlet.ext.httpclient.HttpClientHelper(null));
            break;
        case NET:
            engine.getRegisteredClients().add(0, new org.restlet.ext.net.HttpClientHelper(null));
            break;
        }
        //set https
        switch(sc){
        case INTERNAL:
            engine.getRegisteredServers().add(0,
                    new org.restlet.engine.connector.HttpServerHelper(null));
            break; 
        case JETTY:
            engine.getRegisteredServers().add(0, 
                    new org.restlet.ext.jetty.HttpServerHelper(null));
            break;
        case SIMPLE:
            engine.getRegisteredServers().add(0, 
                    new org.restlet.ext.simple.HttpServerHelper(null));
            break;
        }

    }

    protected void setupSingle() throws Exception {
            
        component = new Component();
        if(https){
            Context c = component.getServers().add(Protocol.HTTPS, serverPort).getContext();
            configureSslServerParameters(c);
            client = new OAuthClientTestApplication(getProt(), serverPort, this.getSslClientParameters());
            component.getDefaultHost().attach("/combo",
                    new OAuthComboTestApplication(getProt(), serverPort, 0, this.getSslClientParameters())); // unlimited token life
            component.getDefaultHost().attach("/server",
                    new OAuthProtectedTestApplication(getProt(), serverPort, this.getSslClientParameters()));
        }
        else{
            client = new OAuthClientTestApplication(getProt(), serverPort, null);
            component.getServers().add(Protocol.HTTP, serverPort);
            component.getDefaultHost().attach("/combo",
                    new OAuthComboTestApplication(getProt(), serverPort, 0, null)); // unlimited token life
            component.getDefaultHost().attach("/server",
                    new OAuthProtectedTestApplication(getProt(), serverPort, null));
        }
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.RIAP);
        //component.getClients().add(client)
        component.getDefaultHost().attach("/oauth", 
                new OAuthTestApplication(tokenTimeout, getProt(), serverPort));                                                 
        component.getDefaultHost().attach("/client", client);

        component.start();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        if(oauthcomp != null){
            oauthcomp.stop();
            oauthcomp = null;
        }
        if(https){
            BioUtils.delete(this.testKeystoreFile);
            BioUtils.delete(this.testDir, true);
            org.restlet.engine.Engine.register();
        }
        this.reqClient.stop();
        super.tearDown();
    }

    private void setupMultiple() throws Exception{
        
        
        component = new Component();
        if(https){
            client = new OAuthClientTestApplication(getProt(), serverPort, this.getSslClientParameters());
            Context c = component.getServers().add(Protocol.HTTPS, serverPort).getContext();
            configureSslServerParameters(c);
            component.getClients().add(Protocol.HTTPS);
            component.getDefaultHost().attach("/server",
                    new OAuthMultipleUserProtectedTestApplication(getProt(), oauthServerPort,
                            this.getSslClientParameters()));
        }
        else{
            client = new OAuthClientTestApplication(getProt(), serverPort, null);
            //component.getServers().add
            component.getServers().add(Protocol.HTTP, serverPort);
            component.getDefaultHost().attach("/server",
                    new OAuthMultipleUserProtectedTestApplication(getProt(), oauthServerPort, null));
            //new Server(new Context(), Protocol.HTTP, serverPort);
        }
        // protected resource server
        //Server server = new Server(new Context(), Protocol.HTTP, serverPort);
        //component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.RIAP);

        // oauth server
        oauthcomp = new Component();
        if(https){
            Context c = oauthcomp.getServers().add(Protocol.HTTPS, oauthServerPort).getContext();
            configureSslServerParameters(c);
            oauthcomp.getClients().add(Protocol.HTTPS);
        }
        else{
            oauthcomp.getServers().add(Protocol.HTTP, oauthServerPort); 
        }
        oauthcomp.getClients().add(Protocol.HTTP);
        oauthcomp.getClients().add(Protocol.RIAP);

        oauthcomp.getDefaultHost().attach("/oauth",
                new OAuthMultipleUserTestApplication(0, getProt(), serverPort)); // unlimited

        component.start();
        oauthcomp.start();


    }
}
