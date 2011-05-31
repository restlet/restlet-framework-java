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

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.representation.Representation;
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

    private volatile String state;

    /**
     * Constructor used for unlimited tokens
     * 
     * @param accessToken
     */
    public OAuthUser(String user, String accessToken) {
        this(user, accessToken, null, 0);
    }

    /**
     * Constructor used for tokens with a expiration time
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
    
    //TODO: This should eventually be removed...
    /**
     * Retrieve the access token from the user if and only if the user is of
     * type OAuthUser
     * 
     * @param user
     * @return access token
     * @see org.restlet.ext.oauth.OAuthUser
     */
    @Deprecated
    public static String getToken(User user) {
        String token = null;
        if (user != null) {
            if (user instanceof OAuthUser) {
                OAuthUser ou = (OAuthUser) user;
                token = ou.getAccessToken();
            } else { // Token is stored in secret field
                token = new String(user.getSecret());
            }
        }
        return token;
    }
    
    /**
     * Convert successful JSON token body responses to OAuthUser.
     * 
     * @param body
     *            Representation containing a successful JSON body element.
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     */

    public static OAuthUser createJson(Representation body) {
        Logger log = Context.getCurrentLogger();
        try {
            // Debug test for tracing back error
            JSONObject answer = new JSONObject(body.getText());
            
            log.info("Got answer on JSON = " + answer.toString());

            String accessToken = null;
            if (answer.has(OAuthServerResource.ACCESS_TOKEN)) {
                accessToken = answer
                        .getString(OAuthServerResource.ACCESS_TOKEN);
                log.info("AccessToken = " + accessToken);
            }

            String refreshToken = null;
            if (answer.has(OAuthServerResource.REFRESH_TOKEN)) {
                refreshToken = answer
                        .getString(OAuthServerResource.REFRESH_TOKEN);
                log.info("RefreshToken = " + refreshToken);
            }

            long expiresIn = 0;
            if (answer.has(OAuthServerResource.EXPIRES_IN)) {
                expiresIn = answer.getLong(OAuthServerResource.EXPIRES_IN);
                log.info("ExpiresIn = " + expiresIn);
            }

            // Store away the user
            return new OAuthUser(null, accessToken, refreshToken, expiresIn);

        } catch (JSONException e) {
            log.log(Level.WARNING, "Error parsing JSON", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error creating representation JSON", e);
        }
        return null;
    }
}
