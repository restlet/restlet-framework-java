package org.restlet.test.ext.oauth.test.resources;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.oauth.util.OAuthParameters;
import org.restlet.ext.oauth.webclient.FacebookProxy;
import org.restlet.routing.Router;

public class FbClientTestApplication extends Application {
    private FacebookProxy local;

    @Override
    public synchronized Restlet createInboundRoot() {

        Context ctx = getContext();
        Router router = new Router(ctx);

        local = new FacebookProxy("118328624855019",
                "26e1ccb99f6135ebbf901b04508cba09",
                "offline_access,publish_stream", // Not according to spec should
                                                 // be %20 not ','
                getContext().createChildContext()); // Have to create child
                                                    // context not to mix tokens
        local.setNext(FacebookFeedMe.class);
        router.attach("/me", local);

        router.attach("/unprotected", DummyResource.class);

        return router;
    }

    public String getToken() {
        if (local != null) {
            return local.getAccessToken();
        }
        return null;
    }

    public OAuthParameters getOauthParameters() {
        OAuthParameters params = new OAuthParameters("118328624855019",
                "26e1ccb99f6135ebbf901b04508cba09",
                "https://graph.facebook.com/oauth/",
                "offline_access,publish_stream");
        return params;
    }
}
