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

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.routing.Router;
import org.restlet.util.Series;

public class OAuthMultipleUserProtectedTestApplication extends Application {

    private String protocol;
    private int oauthServerPort;
    private Client c;
    
    public OAuthMultipleUserProtectedTestApplication(){
        this("http", 8081, null);
    }
    
    public OAuthMultipleUserProtectedTestApplication(String protocol,
            int oauthServerPort, Series <Parameter> params){
        this.protocol = protocol;
        this.oauthServerPort = oauthServerPort;
        if(params != null){
            this.c = new Client(protocol);
            this.c.setContext(new Context());
            this.c.getContext().getParameters().addAll(params);
        }
    }
    @Override
    public synchronized Restlet createInboundRoot() {
        Context ctx = getContext();
        Router router = new Router(ctx);


        OAuthAuthorizer auth2 = new OAuthAuthorizer(
                    protocol+"://localhost:"+oauthServerPort+"/oauth/validate", false, c);
        //System.out.println("attaching resource");
        auth2.setNext(ScopedDummyResource.class);
        router.attach("/scoped/{oauth-user}", auth2);
        return router;
    }

}
