/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.oauth.provider.data.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.ext.oauth.provider.data.AuthenticatedUser;
import org.restlet.ext.oauth.provider.data.Token;

/**
 * In memory implementation of the AuthenticatedUser interface.
 * 
 * Note that id and scopes will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class AuthenticatedUserImpl implements AuthenticatedUser {

    private final String id;

    private Map<String, String> grantedScope = new ConcurrentHashMap<String, String>();

    public AuthenticatedUserImpl(String userId) {
        id = userId;
    }

    // Timestamp can be encoded in the code value
    Set<String> codes = Collections.synchronizedSet(new HashSet<String>());

    // TODO could add a mix life time of geenrated code for more security.
    // private static final int maxNoCodes = 10;
    // private static final long maxCodeLifeSec = 3600; //1h

    // Type and Timestamp can be encoded in the token value
    // Map<String,String> tokens = new ConcurrentHashMap<String,String>();
    // //optional value secret
    // TODO could also add number of token refresh or one time token support
    // private static final int maxNoTokens = 100;
    private long maxTokenLifeSec = Token.UNLIMITED;

    public String getId() {
        return id;
    }

    public void addCode(String code) {
        codes.add(code);
    }

    public void removeCode(String code) {
        codes.remove(code);
    }

    public void addScope(String scope, String owner) {
        grantedScope.put(scope, owner);
    }

    public boolean isGrantedScope(String scope, String owner) {
        // TODO implement owner
        return grantedScope.containsKey(scope);
    }

    public void revokeScope(String scope, String owner) {
        // TODO implement owner
        grantedScope.remove(scope);
    }

    public String[] getGrantedScopes() {
        return grantedScope.keySet().toArray(new String[grantedScope.size()]);
    }

    public long getTokenExpire() {
        return maxTokenLifeSec;
    }

    public void setTokenExpire(long deltaTimeSec) {
        maxTokenLifeSec = deltaTimeSec;
    }

    public void revokeScopes() {
        grantedScope.clear();
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
