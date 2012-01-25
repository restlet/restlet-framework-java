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

package org.restlet.ext.oauth.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.Client;
import org.restlet.security.Role;

/**
 * In memory implementation of the AuthenticatedUser interface.
 * 
 * Note that id and scopes will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class AuthenticatedUserImpl extends AuthenticatedUser {

    private final Client client;

    private final String id;

    private final Map<Role, String> grantedRoles;

    private volatile String code;

    private volatile Token token;

    private volatile String password; // optional for oauth password flow

    public AuthenticatedUserImpl(String userId, Client client) {
        id = userId;
        this.client = client;
        this.grantedRoles = new ConcurrentHashMap<Role, String>();
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
    public void addRole(Role role, String owner) {
        grantedRoles.put(role, owner);
    }

    @Override
    public boolean isGrantedRole(Role role, String owner) {
        return grantedRoles.containsKey(role);
    }

    @Override
    public void revokeRole(Role role, String owner) {
        // TODO implement owner
        grantedRoles.remove(role);
    }

    @Override
    public List<Role> getGrantedRoles() {
        return new ArrayList<Role>(grantedRoles.keySet());
        // return null;
        // return grantedRoles.keySet()(new Role[grantedRoles.size()]);
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
    public void revokeRoles() {
        grantedRoles.clear();
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
