package org.restlet.test.ext.oauth.test.resources;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.oauth.provider.AccessTokenServerResource;
import org.restlet.ext.oauth.provider.AuthPageServerResource;
import org.restlet.ext.oauth.provider.AuthorizationServerResource;
import org.restlet.ext.oauth.provider.OAuthServerResource;
import org.restlet.ext.oauth.provider.ValidationServerResource;
import org.restlet.ext.oauth.provider.data.ClientStore;
import org.restlet.ext.oauth.provider.data.ClientStoreFactory;
import org.restlet.ext.oauth.provider.data.impl.MemClientStore;
import org.restlet.ext.openid.CallbackCacheFilter;
import org.restlet.ext.openid.OpenIdConsumer;
import org.restlet.ext.openid.OpenIdProvider;
import org.restlet.ext.openid.SetCallbackFilter;
import org.restlet.ext.openid.XrdsResource;
import org.restlet.resource.Finder;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.server.ServerManager;

public class OauthTestApplication extends Application {
    private long timeout = 0; // unlimited

    public OauthTestApplication(long timeout) {
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

        ClientStore clientStore = ClientStoreFactory.getInstance();
        // Testcode TODO remove
        clientStore.createClient("1234567890", "1234567890",
                AuthorizationServerTest.prot + "://localhost:"
                        + AuthorizationServerTest.serverPort + "/");

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

        router.attach("/xrds", XrdsResource.class);

        ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(AuthorizationServerTest.prot + "://localhost:"
                + AuthorizationServerTest.serverPort + "/oauth/provider");
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
