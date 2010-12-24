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

package org.restlet.ext.oauth.provider.data;

/**
 * Token that never expires but that can be revoked/deleted.
 * 
 * @author Kristoffer Gronowski
 */
public class Token {

    public static final long UNLIMITED = 0;

    protected String token; // Since ExpireToken can generate new

    private final AuthenticatedUser user;

    protected Token(String token, AuthenticatedUser user) {
        this.token = token;
        this.user = user;
    }

    /**
     * 
     * @return the actual token to be used for OAuth invocations.
     */
    public String getToken() {
        return token;
    }

    /**
     * 
     * @return the user that is the owner of this token
     */
    public AuthenticatedUser getUser() {
        return user;
    }

    // TODO improve on equals.
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            Token t = (Token) obj;
            return token.equals(t.token);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
