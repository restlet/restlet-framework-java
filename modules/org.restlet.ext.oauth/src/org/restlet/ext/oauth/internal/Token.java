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
 * Abstract Token that must be extended by all token implementations
 * 
 * @author Kristoffer Gronowski
 * 
 * @see UnlimitedToken
 * @see ExpireToken
 */
public abstract class Token {

    /**
     * Value indicating that the Token should not expire
     */
    public static final long UNLIMITED = 0;

    /**
     * 
     * @return the actual token to be used for OAuth invocations.
     */
    public abstract String getToken();

    /**
     * 
     * @return the user that is the owner of this token
     */
    public abstract AuthenticatedUser getUser();

    /**
     * Generic package method since the Token can be revoked and re-issued or
     * just persisted and re-instantiated.
     * 
     * 
     * @param token
     */
    abstract void setToken(String token);
}
