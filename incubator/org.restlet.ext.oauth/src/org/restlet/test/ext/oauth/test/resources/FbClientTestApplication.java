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

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.example.ext.oauth.FacebookProxy;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.internal.OAuthUtils;
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

    /*
    public String getToken() {
        if (local != null) {
            return local.getAccessToken();
        }
        return null;
    }
    */
    public OAuthParameters getOauthParameters() {
        OAuthParameters params = new OAuthParameters("118328624855019",
                "26e1ccb99f6135ebbf901b04508cba09",
                "https://graph.facebook.com/oauth/",
                OAuthUtils.scopesToRole("offline_access,publish_stream"));
        return params;
    }
}
