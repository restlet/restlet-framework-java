package org.restlet.test.ext.oauth.test.resources;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.routing.Router;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

public class OauthClientTestApplication extends Application {
    private OAuthProxy local;

    private OAuthParameters params;

    @Override
    public synchronized Restlet createInboundRoot() {

        Context ctx = getContext();
        Router router = new Router(ctx);

        params = new OAuthParameters("1234567890", "1234567890",
                AuthorizationServerTest.prot + "://localhost:"
                        + AuthorizationServerTest.serverPort + "/oauth/",
                "foo bar");

        local = new OAuthProxy(params, getContext(), true); // Use basic
        local.setNext(DummyResource.class);
        router.attach("/webclient", local);

        router.attach("/unprotected", DummyResource.class);

        return router;
    }

    public String getToken() {
        if (local != null) {
            return local.getAccessToken();
        }
        return null;
    }

    public OAuthUser getUser() {
        if (local != null) {
            return local.getAuthUser();
        }
        return null;
    }

    public OAuthParameters getOauthParameters() {
        return params;
    }
}
