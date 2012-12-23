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

package org.restlet.ext.oauth.internal.memory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.Client;

/**
 * A POJO representing a OAuth client_id. Each client can have collected a
 * number of authenticated users to allow working on their behalf.
 * 
 * Note that authenticated users will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class ClientImpl implements Client {

    private final String clientId;

    private final String clientSecret;

    private final String redirectUri;

    private final Set<AuthenticatedUser> users;

    private final String applicationName;

    // TODO maybe also check the codes and tokens / client level

    public ClientImpl(String clientId, String redirectUri) {
        this(clientId, null, redirectUri, null);
        // this.clientId = clientId;
        // this.redirectUri = redirectUri;
    }

    public ClientImpl(String clientId, String clientSecret, String redirectUri) {
        this(clientId, clientSecret, redirectUri, null);
        // this.clientSecret = clientSecret;
    }

    public ClientImpl(String clientId, String clientSecret, String redirectUri,
            String name) {
        // this(clientId, redirectUri);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.applicationName = name;
        this.users = Collections
                .synchronizedSet(new HashSet<AuthenticatedUser>());
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
