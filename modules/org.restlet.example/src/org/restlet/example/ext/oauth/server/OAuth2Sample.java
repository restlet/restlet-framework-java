/**
 * Copyright 2005-2013 Restlet S.A.S.
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
package org.restlet.example.ext.oauth.server;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 *
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuth2Sample {

    private static Mongo mongo;
    
    public static Mongo getMongo() {
        return mongo;
    }
    
    public static DB getDefaultDB() {
        return mongo.getDB("oauth2");
    }
    
    public static void main(String[] args) throws Exception {
        // Setup MongoDB
        mongo = new Mongo("localhost", 27017);

        // Setup Restlet
        Component component = new Component();
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);
        component.getClients().add(Protocol.RIAP);
        component.getClients().add(Protocol.CLAP);
        component.getServers().add(Protocol.HTTP, 8080);

        component.getDefaultHost().attach("/sample", new SampleApplication());
        OAuth2ServerApplication app = new OAuth2ServerApplication();
        component.getDefaultHost().attach("/oauth", app);
        component.getInternalRouter().attach("/oauth", app);
        
        component.start();
    }
}
