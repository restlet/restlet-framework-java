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


import java.util.List;


import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.oauth.OAuthHelper;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.oauth.app.OAuthClientTestApplication;
import org.restlet.test.ext.oauth.app.OAuthComboTestApplication;
import org.restlet.test.ext.oauth.app.OAuthProtectedTestApplication;
import org.restlet.test.ext.oauth.app.OAuthTestApplication;

public abstract class OAuthHttpTestBase extends RestletTestCase{
    protected Component component;

    // Use for http test when debugging
    public static int serverPort = 8080;
    public static final String prot = "http";
    public static int tokenTimeout = 0;


    protected OAuthClientTestApplication client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new OAuthClientTestApplication(prot, serverPort);
        
        Server server = new Server(new Context(), Protocol.HTTP, serverPort);
        component = new Component();
        component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.RIAP);
        component.getDefaultHost().attach("/oauth", 
                new OAuthTestApplication(tokenTimeout));                                                 
        component.getDefaultHost().attach("/client", client);
        component.getDefaultHost().attach("/server",
                new OAuthProtectedTestApplication());
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
        super.tearDown();
    }
}
