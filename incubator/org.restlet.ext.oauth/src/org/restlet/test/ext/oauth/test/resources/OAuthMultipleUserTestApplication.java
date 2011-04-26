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

package org.restlet.test.ext.oauth.test.resources;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
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
import org.restlet.ext.openid.CallbackCacheFilter;
import org.restlet.ext.openid.OpenIdConsumer;
import org.restlet.ext.openid.OpenIdProvider;
import org.restlet.ext.openid.SetCallbackFilter;
import org.restlet.ext.openid.XrdsServerResource;
import org.restlet.resource.Finder;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.server.ServerManager;

public class OAuthMultipleUserTestApplication extends Application {
	public static final String TEST_USER = "dummyUser";
	public static final String TEST_PASS = "dummyPassword";
	
    private long timeout = 0; // unlimited

    public OAuthMultipleUserTestApplication(long timeout) {
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
        
        Client client = clientStore.createClient("client1234", "secret1234",
                AuthorizationServerTest.prot + "://localhost:"
                + AuthorizationServerTest.serverPort + "/");
        
        //Create 10 users:
        for(int i = 1; i < 6; i++){
            //Bootstrap for password flow test...
            AuthenticatedUser user = client.createUser("user"+i);
            user.setPassword("pass"+i);
        }
        
        attribs.put(ClientStore.class.getCanonicalName(), clientStore);

        Router router = new Router(ctx);

        // Oauth 2 resources
        router.attach("/authorize",
                new Finder(ctx, AuthorizationServerResource.class));
        router.attach("/access_token", new Finder(ctx,
                AccessTokenServerResource.class));

        // REST Auth endpoint for the case where an Application validates
        // access_token remotely
        router.attach("/validate", ValidationServerResource.class);

        // Create the identity cache filter
        CallbackCacheFilter authCache = new CallbackCacheFilter(getContext());
        // Used to set the callback cookie
        Filter f = new SetCallbackFilter(authCache);
        f.setNext(LoginPageResource.class);
        router.attach("/login", f);

        // OpenID
        try {
            ConsumerManager consumerManager = new ConsumerManager();
            attribs.put("consumer_manager", consumerManager);
            // attribs.put("openid_return_to",
            // "http://localhost:8080/oauth/openid_login");
        } catch (ConsumerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Set the cache since it is olso the part handeling the callback after
        // auth
        authCache.setNext(new Finder(getContext(), OpenIdConsumer.class));
        router.attach("/openid_login", authCache);

        router.attach("/xrds", XrdsServerResource.class);

        ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(AuthorizationServerTest.prot + "://localhost:"
                + AuthorizationServerTest.serverPort + "/oauth/provider");
        manager.setEnforceRpId(true);
        attribs.put("openid_manager", manager);
        attribs.put("xrds", AuthorizationServerTest.prot + "://localhost:"
                + AuthorizationServerTest.serverPort + "/oauth/xrds");

        Finder finder = new Finder(ctx, OpenIdProvider.class);
        router.attach("/provider", finder);

        // Page to show and consume scope authorization
        // Add a dynamic auth page
        // attribs.put("oauth_auth_page", "authorize.html");
        router.attach("/auth_page", new Finder(ctx, AuthPageServerResource.class));

        return router;
    }

}
