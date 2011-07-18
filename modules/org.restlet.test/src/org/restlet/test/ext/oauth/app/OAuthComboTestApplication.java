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

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.ext.oauth.ValidationServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.util.Series;

/**
 * Test for a protected resource embedded with an authorization server
 * 
 * 
 * @author Kristoffer Gronowski
 *
 */

public class OAuthComboTestApplication extends OAuthTestApplication {
    
    protected Client client;

    public OAuthComboTestApplication(long timeout) {
        this("http", 8080, timeout, null);
    }

    public OAuthComboTestApplication(String protocol, int port, long timeout,
            Series <Parameter> requestParams){
        super(timeout, protocol, port);
        if(requestParams != null){
            Protocol p = new Protocol(protocol);
            Client c = new Client(p);
            c.setContext(new Context());
            c.getContext().getParameters().addAll(requestParams);
        }
        
    }

    @Override
    public synchronized Restlet createInboundRoot() {
        //Set context param to only allow local token validation.
        getContext().getAttributes().put(ValidationServerResource.LOCAL_ACCESS_ONLY, "true");
        Restlet r = super.createInboundRoot();
        Router router = (Router)r;

        Authorizer auth = new OAuthAuthorizer("/validate", true, client);
        auth.setNext(DummyResource.class);
        router.attach("/protected",auth);

        return router;
    }

}
