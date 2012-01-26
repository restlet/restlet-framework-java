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
    
    /**
     * Converts successful JSON token body responses to OAuthUser.
     * 
     * @param body
     *            Representation containing a successful JSON body element.
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     */
    public static OAuthUser createJson(Representation body){
        return createJson(null, body);
    }
    
    /**
     * Converts successful JSON token body responses to OAuthUser.
     * 
     * @param toCopy
     *          Copy user data from. Can be null
     * @param body
     *            Representation containing a successful JSON body element.
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     */
    public static OAuthUser createJson(User toCopy, Representation body) {
        Logger log = Context.getCurrentLogger();
        try {
            // Debug test for tracing back error
            JSONObject answer = new JSONObject(body.getText());

            log.fine("Got answer on JSON = " + answer.toString());

            String accessToken = null;
            if (answer.has(OAuthServerResource.ACCESS_TOKEN)) {
                accessToken = answer
                        .getString(OAuthServerResource.ACCESS_TOKEN);
                log.fine("AccessToken = " + accessToken);
            }

            String refreshToken = null;
            if (answer.has(OAuthServerResource.REFRESH_TOKEN)) {
                refreshToken = answer
                        .getString(OAuthServerResource.REFRESH_TOKEN);
                log.fine("RefreshToken = " + refreshToken);
            }

            long expiresIn = 0;
            if (answer.has(OAuthServerResource.EXPIRES_IN)) {
                expiresIn = answer.getLong(OAuthServerResource.EXPIRES_IN);
                log.fine("ExpiresIn = " + expiresIn);
            }

            // Store away the user
            return new OAuthUser(toCopy, accessToken, refreshToken,
                        expiresIn);
            
        } catch (JSONException e) {
            log.log(Level.WARNING, "Error parsing JSON", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error creating representation JSON", e);
        }
        return null;
    }

    /** The access token. */
    private final String accessToken;

    /** The validity delay of the authentication. */
    private final long expiresIn;

    /** The refresh token. */
    private final String refreshToken;

    /** The current state. */
    private volatile String state;

    /**
     * Constructor used for unlimited tokens.
     * 
     * @param user
     *            The user identifier.
     * @param accessToken
     *            The access token.
     */
    public OAuthUser(String user, String accessToken) {
        this(user, accessToken, null, 0);
    }

    /**
     * Constructor used for tokens with a expiration time.
     * 
     * @param user
     *            The user identifier.
     * @param accessToken
     *            The access token.
     * @param refreshToken
     *            The refresh token.
     * @param expiresIn
     *            The expiration time.
     */
    public OAuthUser(String user, String accessToken, String refreshToken,
            long expiresIn) {
        super(user, accessToken);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    /**
     * Constructor used for unlimited tokens.
     * 
     * @param user
     *            The User object, all fields will be copied (email, lastName,
     *            firstName, identifier).
     * @param accessToken
     *            The access token.
     */
    public OAuthUser(User user, String accessToken) {
        this(user, accessToken, null, 0);
    }

    /**
     * Constructor used for tokens with a expiration time.
     * 
     * @param user
     *            The User object, all fields will be copied (email, lastName,
     *            firstName, identifier).
     * @param accessToken
     *            The access token.
     * @param refreshToken
     *            The refresh token.
     * @param expiresIn
     *            The expiration time.
     */
    public OAuthUser(User user, String accessToken, String refreshToken,
            long expiresIn) {
        this(user != null ? user.getIdentifier() : null, accessToken, refreshToken, expiresIn);
        if(user != null){
            setEmail(user.getEmail());
            setFirstName(user.getFirstName());
            setLastName(user.getLastName());
        }
    }

    /**
     * Returns the access token.
     * 
     * @return The access token.
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * The expiration delay.
     * 
     * @return The expiration delay.
     */
    public long getExpiresIn() {
        return this.expiresIn;
    }

    /**
     * Returns the refresh token.
     * 
     * @return The refresh token.
     */
    public String getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * Returns the current state.
     * 
     * @return The current state.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Indicates if the current user has a refresh token, or not.
     * 
     * @return True if there is a refresh token.
     */
    public boolean isExpireToken() {
        return this.refreshToken != null;
    }

    /**
     * Sets the current state.
     * 
     * @param state
     *            The current state.
     */
    public void setState(String state) {
        this.state = state;
    }
}
