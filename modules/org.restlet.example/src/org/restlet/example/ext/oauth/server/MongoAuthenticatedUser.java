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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.security.Role;

/**
 * MongoDB implementation of AuthenticatedUser interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoAuthenticatedUser implements AuthenticatedUser {

    private MongoClient client;
    private DBCollection authUsers;
    private DBObject authUser;
    
    public MongoAuthenticatedUser(MongoClient client, DBObject user) {
        this.client = client;
        this.authUser = user;
        authUsers = OAuth2Sample.getDefaultDB().getCollection("authenticated_users");
    }

    @Override
    public boolean persist() {
        authUsers.save(authUser);
        return true;
    }

    @Override
    public Client getClient() {
        return client;
    }
    
    @SuppressWarnings("unchecked")
	private List<Object> getScopes() {
        Object scopes = authUser.get("scopes");
        if (scopes == null) {
            scopes = new BasicDBList();
            authUser.put("scopes", scopes);
        }
        return (List<Object>) scopes;
    }

    @Override
    public void addRole(Role r, String owner) {
        getScopes().add(r.getName());
    }

    @Override
    public List<Role> getGrantedRoles() {
        ArrayList<Role> roles = new ArrayList<Role>();
        for (Object scope : getScopes()) {
            roles.add(new Role(scope.toString()));
        }
        return roles;
    }
    
    @Override
    public boolean isGrantedRole(Role role, String owner) {
        return getScopes().contains(role.getName());
    }
    
    @Override
    public void revokeRole(Role role, String owner) {
        getScopes().remove(role.getName());
    }

    @Override
    public void revokeRoles() {
        getScopes().clear();
    }

    @Override
    public String getId() {
        DBObject id = (DBObject) authUser.get("_id");
        return MongoUtil.toString(id.get("user_id"));
    }

    @Override
    public char[] getPassword() {
        DBCollection users = OAuth2Sample.getDefaultDB().getCollection("users");
        DBObject user = users.findOne(new BasicDBObject("_id", getId()));
        if (user != null) {
            return user.get("password").toString().toCharArray();
        }
        return null;
    }
    
    @Override
    public void setPassword(char[] password) {
        DBCollection users = OAuth2Sample.getDefaultDB().getCollection("users");
        users.update(
                new BasicDBObject("_id", getId()),
                new BasicDBObject("$set",
                    new BasicDBObject("password", new String(password))));
    }

    @Override
    public long getTokenExpire() {
        Object tokenExpire = authUser.get("token_expire");
        if (tokenExpire == null) {
            return 0;
        }
        return ((Number) tokenExpire).longValue();
    }

    @Override
    public void setTokenExpire(long deltaTimeSec) {
        authUser.put("token_expure", deltaTimeSec);
    }

    @Override
    public String getCode() {
        return MongoUtil.toString(authUser.get("code"));
    }
    
    @Override
    public void setCode(String code) {
        authUser.put("code", code);
    }

    @Override
    public void clearCode() {
        authUser.removeField("code");
    }
    
    @Override
    public Token getToken() {
        // Actually, we'll never use this method.
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void setToken(Token token) {
        // Actually, we'll never use this method.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
