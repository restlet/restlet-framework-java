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
package org.restlet.ext.oauth.internal.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.AbstractClientManager;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;

/**
 * MongoDB implementation of ClientManager interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoClientManager extends AbstractClientManager {

    private DBCollection clients;
    
    public MongoClientManager(DB db) {
        clients = db.getCollection("clients");
    }
    
    @Override
    protected Client createClient(String clientId, char[] clientSecret, ClientType clientType, String[] redirectURIs, Map properties) {
        BasicDBObject client = new BasicDBObject("_id", clientId);
        if (clientSecret != null) {
            client.put(MongoClient.CLIENT_SECRET, String.copyValueOf(clientSecret));
        }
        switch (clientType) {
        case PUBLIC:
            client.put(MongoClient.CLIENT_TYPE, "public");
            break;
        case CONFIDENTIAL:
            client.put(MongoClient.CLIENT_TYPE, "confidential");
            break;
        }
        if (redirectURIs != null && redirectURIs.length > 0) {
            client.put(MongoClient.REDIRECT_URIS, Arrays.asList(redirectURIs));
        }

        Object[] supportedFlows = (Object[]) properties.remove(Client.PROPERTY_SUPPORTED_FLOWS);

        List responseTypes = new ArrayList();
        List grantTypes = new ArrayList();
        for (Object flow : supportedFlows) {
            if (flow instanceof ResponseType) {
                responseTypes.add(flow.toString());
            } else if (flow instanceof GrantType) {
                grantTypes.add(flow.toString());
            }
        }
        
        client.put(MongoClient.ALLOWED_RESPONSE_TYPES, responseTypes);
        client.put(MongoClient.ALLOWED_GRANT_TYPES, grantTypes);
        client.put(MongoClient.PROPERTIES, properties);
        
        clients.insert(client);
        
        return new MongoClient(client);
    }

    public void deleteClient(String id) {
        clients.findAndRemove(new BasicDBObject("_id", id));
    }

    public Client findById(String id) {
        DBObject client = clients.findOne(new BasicDBObject("_id", id));
        if (client == null) {
            return null;
        }
        return new MongoClient(client);
    }
}
