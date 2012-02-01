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

package org.restlet.test.ext.oauth.app;

import java.util.HashMap;
import java.util.Map;
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
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

public class OAuthMultipleUserTestApplication extends Application {
	public static final String TEST_USER = "dummyUser";
	public static final String TEST_PASS = "dummyPassword";
	
    private long timeout = 0; // unlimited
    private String protocol = null;
    private int port;

    public OAuthMultipleUserTestApplication(long timeout) {
        this(timeout, "http", 8080);
    }
    
    public OAuthMultipleUserTestApplication(long timeout, String protocol, int port) {
        this.timeout = timeout;
        this.protocol = protocol;
        this.port = port;
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
        
        Client client = clientStore.createClient("client1234", "secret1234",
                protocol + "://localhost:" + port + "/");
        
        //Create 10 users:
        Map <String, String> verifierUsers = new HashMap <String, String> ();
        for(int i = 1; i < 10; i++){
            //Bootstrap for password flow test...
            AuthenticatedUser user = client.createUser("user"+i);
            user.setPassword("pass"+i);
            verifierUsers.put("user"+i, "pass"+i);
        }
        
        attribs.put(ClientStore.class.getCanonicalName(), clientStore);

        Router router = new Router(ctx);

        // Oauth 2 resources
        ChallengeAuthenticator au = new ChallengeAuthenticator(getContext(),
                ChallengeScheme.HTTP_BASIC, "OAuth Test Server");
        au.setVerifier(new MultipleVerifier(verifierUsers));
        au.setNext(AuthorizationServerResource.class);
        router.attach("/authorize", au);
        router.attach("/access_token", AccessTokenServerResource.class);
        router.attach("/validate", ValidationServerResource.class);
        router.attach("/auth_page", AuthPageServerResource.class);

        return router;
    }

}
