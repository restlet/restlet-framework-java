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

package org.restlet.ext.oauth.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.Token;

/**
 * In memory implementation of the AuthenticatedUser interface.
 * 
 * Note that id and scopes will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class AuthenticatedUserImpl extends AuthenticatedUser {
	
	private Client client;

    private final String id;

    private Map<String, String> grantedScope = new ConcurrentHashMap<String, String>();
    
    private String code;
    
    private Token token;
    
    private String password; // optional for oauth password flow

    public AuthenticatedUserImpl(String userId, Client client) {
        id = userId;
        this.client = client;
    }

    // Timestamp can be encoded in the code value
    // TODO could also add number of token refresh or one time token support
    // private static final int maxNoTokens = 100;
    private long maxTokenLifeSec = Token.UNLIMITED;

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getCode() {
    	return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void clearCode() {
        code = null;
    }

    @Override
    public void addScope(String scope, String owner) {
        grantedScope.put(scope, owner);
    }

    @Override
    public boolean isGrantedScope(String scope, String owner) {
        // TODO implement owner
        return grantedScope.containsKey(scope);
    }

    @Override
    public void revokeScope(String scope, String owner) {
        // TODO implement owner
        grantedScope.remove(scope);
    }

    @Override
    public String[] getGrantedScopes() {
        return grantedScope.keySet().toArray(new String[grantedScope.size()]);
    }

    @Override
    public long getTokenExpire() {
        return maxTokenLifeSec;
    }

    @Override
    public void setTokenExpire(long deltaTimeSec) {
        maxTokenLifeSec = deltaTimeSec;
    }

    @Override
    public void revokeScopes() {
        grantedScope.clear();
    }
    
    @Override
    public Token getToken() {
    	return token;
    }
    
    @Override
    public void setToken(Token token) {
    	this.token = token;
    }
    
    @Override
    public String getPassword() {
    	return password;
    }
    
    @Override
    public void setPassword(String password) {
    	this.password = password;
    }
    
    @Override
    public Client getClient() {
    	return client;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(id);
        } else if (obj instanceof AuthenticatedUserImpl) {
            AuthenticatedUserImpl aui = (AuthenticatedUserImpl) obj;
            return id.equals(aui.id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
