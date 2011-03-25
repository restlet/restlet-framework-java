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

package org.restlet.ext.oauth.provider;

import org.restlet.security.User;

/**
 * Used for storing the OAuth access token in the OAuth security framework.
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthUser extends User {

    private final String accessToken;

    private final String refreshToken;

    private final long expiresIn;

    private String state;

    /**
     * Constructor used for unlimited tokens
     * 
     * @param accessToken
     */
    public OAuthUser(String user, String accessToken) {
        this(user, accessToken, null, 0);
    }

    /**
     * Constructor used for tokens with a exparation time
     * 
     * @param accessToken
     */

    public OAuthUser(String user, String accessToken, String refreshToken,
            long expiresIn) {
        super(user, accessToken);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public boolean isExpireToken() {
        return refreshToken != null;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
