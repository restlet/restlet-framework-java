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
import org.restlet.data.Protocol;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.routing.Router;
import org.restlet.util.Series;

public class OAuthClientTestApplication extends Application {
    private OAuthProxy local;

    private OAuthParameters params;
    private String protocol;
    private int port;
    //protected OAuthUser user;
    private Client client;
    
    public OAuthClientTestApplication(){
        this("http", 8080, null);
    }
    
    public OAuthClientTestApplication(String protocol, int port, Series <Parameter> params){
        this.protocol = protocol;
        this.port = port;
        Protocol p = Protocol.valueOf(protocol);
        if(params != null){
            this.client = new Client(p);
            this.client.setContext(new Context());
            this.client.getContext().getParameters().addAll(params);
        }
    }

    @Override
    public synchronized Restlet createInboundRoot() {

        Context ctx = getContext();
        Router router = new Router(ctx);

        params = new OAuthParameters("1234567890", "1234567890",
                protocol + "://localhost:"
                        + port + "/oauth/",
                Scopes.toRoles("foo bar"));

        local = new OAuthProxy(params, getContext(), true, client); // Use basic
        local.setNext(DummyResource.class);
        router.attach("/webclient", local);

        router.attach("/unprotected", DummyResource.class);

        return router;
    }

    
    public String getToken() {
        OAuthUser u = getUser();
        if(u != null) return u.getAccessToken();
        /*if (user != null) {
            return user.getAccessToken();
        }*/
        return null;
    }

    public OAuthUser getUser() {
        OAuthUser u = (OAuthUser) getContext().getAttributes().get("testuser");
        return u;
        /*if (user != null) {
            return user;
        }*/
        //return null;
    }
    
    public void clearUser(){
        getContext().getAttributes().remove("testuser");
        //user = null;
    }
    
    
    public OAuthParameters getOauthParameters() {
        return params;
    }
}
