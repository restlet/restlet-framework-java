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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.routing.Router;
import org.restlet.security.Role;
import org.restlet.util.Series;


public class OAuthProtectedTestApplication extends Application {
    
    private final String protocol;
    private final int port;
    private final Client client;
    
    public OAuthProtectedTestApplication(){
        this("http", 8080, null);
    }
    
    public OAuthProtectedTestApplication(String protocol, int port, Series <Parameter> params){
        this.port = port;
        this.protocol = protocol;
        if(params != null){
            client = new Client(protocol);
            client.setContext(new Context());
            client.getContext().getParameters().addAll(params);   
        }
        else
            client = null;
    }
    
	@Override
	public synchronized Restlet createInboundRoot() {
		Context ctx = getContext();
		Router router = new Router(ctx);
		
		OAuthAuthorizer auth = new OAuthAuthorizer(
				protocol+"://localhost:"
				+port+"/oauth/validate", false, client);
		auth.setNext(DummyResource.class);
		router.attach("/protected",auth);
		
		OAuthAuthorizer auth2 = new OAuthAuthorizer(
				protocol+"://localhost:"+port+"/oauth/validate",
				false, client);
		List <Role> roles = new ArrayList <Role> ();
		roles.add(new Role("foo", null));
		roles.add(new Role("bar", null));
		auth2.setAuthorizedRoles(roles);
		auth2.setNext(ScopedDummyResource.class);
		router.attach("/scoped",auth2);
		
		return router;
	}

}
