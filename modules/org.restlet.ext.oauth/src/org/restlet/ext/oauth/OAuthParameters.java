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

import java.util.List;

import org.restlet.data.Reference;
import org.restlet.security.Role;

/**
 * Container for OAuth2 Parameters. It contains the following information
 * <ul>
 * <li>baseRef - defaults to "http://localhost:8080/oauth/"</a>
 * <li>authorizePath - defaults to "authorize"
 * <li>
 * <li>accessTokenPath - defaults to "access_token"</li>
 * <li>scope - string with space delimited scopes</li>
 * <li>clientId</li>
 * <li>clientSecret</li>
 * </ul>
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthParameters {
    private volatile String accessTokenPath = "access_token";

    private volatile String authorizePath = "authorize";

    private final Reference baseRef;

    /** The client identifier. */
    private final String clientId;

    /** The client password. */
    // TODO should be an array of chars. char[]
    private final String clientSecret;

    private volatile String owner = null;

    private volatile List<Role> roles;

    /**
     * Constructor.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client password.
     */
    public OAuthParameters(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseRef = new Reference("http://localhost:8080/oauth/");
    }

    /**
     * Constructor.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client password.
     * @param baseRef
     *            The base reference.
     */
    public OAuthParameters(String clientId, String clientSecret, String baseRef) {
        // this(clientId, clientSecret);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseRef = new Reference(baseRef);
    }

    /**
     * Constructor.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client password.
     * @param baseRef
     *            The base reference.
     * @param roles
     *            The list of user roles.
     */
    public OAuthParameters(String clientId, String clientSecret,
            String baseRef, List<Role> roles) {
        this(clientId, clientSecret, baseRef);
        this.roles = roles;
    }

    /**
     * Returns the access token path.
     * 
     * @return The access token path.
     */
    public String getAccessTokenPath() {
        return accessTokenPath;
    }

    /**
     * Returns the authorize path.
     * 
     * @return The authorize path.
     */
    public String getAuthorizePath() {
        return authorizePath;
    }

    /**
     * Returns the base reference.
     * 
     * @return The base reference.
     */
    public Reference getBaseRef() {
        return baseRef;
    }

    /**
     * Returns the client identifier.
     * 
     * @return The client identifier.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the client secret.
     * 
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Returns the owner.
     * 
     * @return The owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the list of roles.
     * 
     * @return The list of roles.
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the access token path.
     * 
     * @param accessTokenPath
     *            The access token path.
     */
    public void setAccessTokenPath(String accessTokenPath) {
        this.accessTokenPath = accessTokenPath;
    }

    /**
     * Sets the authorize path.
     * 
     * @param authorizePath
     *            The authorize path.
     */
    public void setAuthorizePath(String authorizePath) {
        this.authorizePath = authorizePath;
    }

    /**
     * Sets the owner.
     * 
     * @param owner
     *            The owner.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Sets the list of roles.
     * 
     * @param roles
     *            The list of roles.
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
