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

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.routing.Router;

public class OAuthMultipleUserProtectedTestApplication extends Application {

    private String protocol;
    private int oauthServerPort;
    
    
    public OAuthMultipleUserProtectedTestApplication(){
        this.protocol = "http";
        this.oauthServerPort = 8081;
    }
    
    public OAuthMultipleUserProtectedTestApplication(String protocol,
            int oauthServerPort){
        this.protocol = protocol;
        this.oauthServerPort = oauthServerPort;
    }
    @Override
    public synchronized Restlet createInboundRoot() {
        Context ctx = getContext();
        Router router = new Router(ctx);


        OAuthAuthorizer auth2 = new OAuthAuthorizer(
                    protocol+"://localhost:"
                +oauthServerPort+
                "/oauth/validate",
                protocol+"://localhost:"+
                oauthServerPort+"/oauth/authorize"
        );
        //System.out.println("attaching resource");
        auth2.setNext(ScopedDummyResource.class);
        router.attach("/scoped/{oauth-user}", auth2);
        return router;
    }

}
