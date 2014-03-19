/**
 * Copyright 2005-2014 Restlet S.A.S.
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth.internal;

/**
 * Abstract Token that must be extended by all token implementations
 * 
 * @author Kristoffer Gronowski
 * @author Shotaro Uchida
 */
public interface Token {

    /**
     * The access token issued by the authorization server. (5.1.
     * 'access_token')
     * 
     * @return the actual token to be used for OAuth invocations.
     */
    public String getAccessToken();

    /**
     * The type of the token.
     * 
     * @return
     */
    public String getTokenType();

    /**
     * The lifetime in seconds of the access token.
     * 
     * @return
     */
    public int getExpirePeriod();

    /**
     * The refresh token. (5.1. 'refresh_token')
     * 
     * @return null if refresh token was not issued.
     */
    public String getRefreshToken();

    /**
     * The actual granted scope. Must not be null.
     * 
     * @return
     */
    public String[] getScope();

}
