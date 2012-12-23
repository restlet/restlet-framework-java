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
package org.restlet.example.ext.oauth.server;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.Client;

/**
 * MongoDB implementation of Client interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoClient implements Client {

    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String APPLICATION_NAME = "application_name";
    private DBCollection authUsers;
    private DBObject client;

    public MongoClient(DBObject client) {
        this.client = client;
        authUsers = OAuth2Sample.getDefaultDB().getCollection("authenticated_users");
    }
    
    @Override
    public String getClientId() {
        return MongoUtil.toString(client.get("_id"));
    }

    @Override
    public String getClientSecret() {
        return MongoUtil.toString(client.get(CLIENT_SECRET));
    }

    @Override
    public String getRedirectUri() {
        return MongoUtil.toString(client.get(REDIRECT_URI));
    }

    @Override
    public String getApplicationName() {
        return MongoUtil.toString(client.get(APPLICATION_NAME));
    }

    @Override
    public boolean containsUser(String userId) {
        return findUser(userId) != null;
    }

    @Override
    public AuthenticatedUser createUser(String userId) {
        AuthenticatedUser authUser = findUser(userId);
        if (authUser != null) {
            return authUser;
        }
        DBObject id = new BasicDBObject("client_id", getClientId()).append("user_id", userId);
        BasicDBObject authUserObj = new BasicDBObject("_id", id);
        authUsers.insert(authUserObj);
        return new MongoAuthenticatedUser(this, authUserObj);
    }

    @Override
    public AuthenticatedUser findUser(String userId) {
        DBObject authUser = authUsers.findOne(new BasicDBObject()
                .append("_id.client_id", getClientId())
                .append("_id.user_id", userId));
        if (authUser != null) {
            return new MongoAuthenticatedUser(this, authUser);
            
        }
        return null;
    }

    @Override
    public void revokeUser(String userId) {
        authUsers.remove(new BasicDBObject()
                .append("_id.client_id", getClientId())
                .append("_id.user_id", userId));
    }
    
    @Override
    public String toString() {
        return "Application = " + getApplicationName() + " CB = " + getRedirectUri();
    }
}
