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

package org.restlet.ext.oauth.provider.data.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.restlet.ext.oauth.provider.data.AuthenticatedUser;
import org.restlet.ext.oauth.provider.data.Client;

/**
 * A POJO representing a OAuth client_id. Each client can have collected a
 * number of authnticated users to allow working on their behalf.
 * 
 * Note that authenticated users will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class ClientImpl extends Client {
    private final String clientId;

    private String clientSecret;

    private String redirectUri;

    private String applicationName;

    private Set<AuthenticatedUser> users = Collections
            .synchronizedSet(new HashSet<AuthenticatedUser>());

    // TODO maybe also check the codes and tokens / client level

    public ClientImpl(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public ClientImpl(String clientId, String clientSecret, String redirectUri) {
        this(clientId, redirectUri);
        this.clientSecret = clientSecret;
    }

    public ClientImpl(String clientId, String clientSecret, String redirectUri,
            String name) {
        this(clientId, redirectUri);
        this.clientSecret = clientSecret;
        applicationName = name;
    }

    public boolean containsUser(String userId) {
        return users.contains(new AuthenticatedUserImpl(userId, this));
    }

    public AuthenticatedUser createUser(String id) {
        AuthenticatedUser user = new AuthenticatedUserImpl(id, this);
        users.add(user);
        return user;
    }

    public AuthenticatedUser findUser(String id) {
        if (id != null && id.length() > 0) {
            // TODO slow but works
            for (AuthenticatedUser user : users) {
                if (user.getId().equals(id))
                    return user;
            }
        }
        return null;
    }

    public void revokeUser(String id) {
        users.remove(new AuthenticatedUserImpl(id, this));
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String toString() {
        if (applicationName != null && applicationName.length() > 0) {
            return "Application = " + applicationName + " CB = " + redirectUri;
        }
        return "ClientId = " + clientId + " CB = " + redirectUri;
    }
}
