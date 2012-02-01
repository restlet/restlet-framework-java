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

import org.restlet.ext.oauth.AuthenticatedUser;

/**
 * Token that never expires but that can be revoked/deleted.
 * 
 * @author Kristoffer Gronowski
 */
public class UnlimitedToken extends Token {

    private volatile String token;

    private final AuthenticatedUser user;

    /**
     * 
     * @param token
     *            string representing the OAuth token
     * @param user
     *            the end user being represented
     */
    public UnlimitedToken(String token, AuthenticatedUser user) {
        this.token = token;
        this.user = user;
    }

    /**
     * 
     * @return the actual token to be used for OAuth invocations.
     */
    @Override
    public String getToken() {
        return token;
    }

    /**
     * 
     * @return the user that is the owner of this token
     */
    @Override
    public AuthenticatedUser getUser() {
        return user;
    }

    /**
     * Generic package method since the Token can be revoked and re-issued or
     * just persisted and re-instantiated.
     * 
     * 
     * @param token
     */
    @Override
    void setToken(String token) {
        this.token = token;
    }

    // TODO improve on equals.
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            Token t = (Token) obj;
            return token.equals(t.getToken());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
