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

package org.restlet.test.ext.oauth.app;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.oauth.AccessTokenServerResource;
import org.restlet.ext.oauth.AuthPageServerResource;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.AuthorizationServerResource;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.ClientStore;
import org.restlet.ext.oauth.ClientStoreFactory;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.ValidationServerResource;
import org.restlet.ext.oauth.internal.MemClientStore;
import org.restlet.resource.Finder;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.test.ext.oauth.AuthorizationServerTestCase;


public class OAuthTestApplication extends Application {
    
    public static final String TEST_USER = "bob";
    public static final String TEST_PASS = "alice";
	
    private long timeout = 0; // unlimited

    public OAuthTestApplication(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public synchronized Restlet createInboundRoot() {

        Context ctx = getContext();

        // Setup the OAuth data backend
        ConcurrentMap<String, Object> attribs = ctx.getAttributes();

        // Setup token timeout
        attribs.put(OAuthServerResource.TOKEN_SERVER_TIME_SEC, timeout);
        attribs.put(OAuthServerResource.TOKEN_SERVER_MAX_TIME_SEC, timeout);

        // Setup a test to check against in-mem auth server
        Object[] params = { new ScheduledThreadPoolExecutor(5) };
        ClientStoreFactory.setClientStoreImpl(MemClientStore.class, params);

        ClientStore<?> clientStore = ClientStoreFactory.getInstance();
        Client client = clientStore.createClient("1234567890", "1234567890",
                AuthorizationServerTestCase.prot + "://localhost:"
                        + AuthorizationServerTestCase.serverPort + "/");
        
        //Bootstrap for password flow test...
        AuthenticatedUser user = client.createUser(TEST_USER);
        user.setPassword(TEST_PASS);

        Router router = new Router(ctx);

        //Set up a simple challenge authenticator:
        //Challenge Authenticator
        ChallengeAuthenticator au = new ChallengeAuthenticator(getContext(),
                ChallengeScheme.HTTP_BASIC, "OAuth Test Server");
        au.setVerifier(new MyVerifier());
        au.setNext(AuthorizationServerResource.class);
        
        // Oauth 2 resources
        router.attach("/authorize", au);
        router.attach("/access_token", new Finder(ctx,
                AccessTokenServerResource.class));
        router.attach("/validate", 
                ValidationServerResource.class);
        router.attach("/auth_page", new Finder(ctx, AuthPageServerResource.class));

        return router;
    }

}
