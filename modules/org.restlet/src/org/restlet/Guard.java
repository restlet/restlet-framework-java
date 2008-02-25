/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Engine;

/**
 * Filter guarding the access to an attached Restlet.
 * 
 * @see <a href="http://www.restlet.org/tutorial#part09">Tutorial: Guarding
 *      access to sensitive resources</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Guard extends Filter {
    /**
     * Implementation of the Principal interface.
     * 
     * @author Stephan Koops
     */
    private class PrincipalImpl implements Principal, Serializable {

        private static final long serialVersionUID = -1842197948591956691L;

        /** The name of the Principal. */
        private String name;

        /**
         * Constructor for deserialization.
         */
        @SuppressWarnings("unused")
        private PrincipalImpl() {
        }

        /**
         * Creates a new Principal with the given name
         * 
         * @param name
         *                The name of the Principal; must not be null.
         */
        public PrincipalImpl(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The name must not be null");
            }

            this.name = name;
        }

        @Override
        public boolean equals(Object another) {
            if (another == this)
                return true;
            if (!(another instanceof Principal))
                return false;
            Principal otherPrinc = (Principal) another;
            return getName().equals(otherPrinc.getName());
        }

        /**
         * Returns the name of this principal.
         * 
         * @return the name of this principal.
         */
        public String getName() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /** Indicates that an authentication response is considered invalid. */
    public static final int AUTHENTICATION_INVALID = -1;

    /** Indicates that an authentication response couldn't be found. */
    public static final int AUTHENTICATION_MISSING = 0;

    /** Indicates that an authentication response is stale. */
    public static final int AUTHENTICATION_STALE = 2;

    /** Indicates that an authentication response is valid. */
    public static final int AUTHENTICATION_VALID = 1;

    /** Default lifespan for generated nonces (5 minutes). */
    public static final long DEFAULT_NONCE_LIFESPAN_MILLIS = 5 * 60 * 1000L;

    /**
     * Name of the request attribute containing the Principal instance.
     * 
     * @see Principal
     */
    private static final String NAME_ATTRIBUTE_PRINCIPAL = "java.security.Principal";

    /**
     * Gets the logged in user.
     * 
     * @param request
     *                The Restlet {@link Request}
     * @return The {@link Principal} of the logged in user.
     * @see #setPrincipal(Request)
     */
    public static Principal getPrincipal(Request request) {
        return (Principal) request.getAttributes()
                .get(NAME_ATTRIBUTE_PRINCIPAL);
    }

    /** The URIs that define the HTTP DIGEST authentication protection domains. */
    private Collection<String> domainUris = Collections.singleton("/");

    /** Lifespan of nonce in milliseconds */
    private long nonceLifespan = DEFAULT_NONCE_LIFESPAN_MILLIS;

    /** The authentication realm. */
    private volatile String realm;

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallengeEnabled;

    /** The authentication challenge scheme. */
    private volatile ChallengeScheme scheme;

    /** Map of secrets (login/password combinations). */
    private final ConcurrentMap<String, char[]> secrets;

    /** The secret key known only to server (use for HTTP DIGEST authentication). */
    private String serverKey = "serverKey";

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param scheme
     *                The authentication scheme to use.
     * @param realm
     *                The authentication realm.
     */
    public Guard(Context context, ChallengeScheme scheme, String realm) {
        super(context);
        this.rechallengeEnabled = true;
        this.secrets = new ConcurrentHashMap<String, char[]>();

        if ((scheme == null)) {
            throw new IllegalArgumentException(
                    "Please specify an authentication scheme. Use the 'None' challenge if no authentication is required.");
        } else {
            this.scheme = scheme;
            this.realm = realm;
        }
    }

    /**
     * Alternate Constructor for HTTP DIGEST authentication scheme.
     * 
     * @param context
     *                context
     * @param realm
     *                authentication realm
     * @param baseUris
     *                protection domain as a collection of base URIs
     * @param serverKey
     *                secret key known only to server
     */
    public Guard(Context context, String realm, Collection<String> baseUris,
            String serverKey) {
        this(context, ChallengeScheme.HTTP_DIGEST, realm);
        this.domainUris = baseUris;
        this.serverKey = serverKey;
    }

    /**
     * Accepts the call. By default, it is invoked if the request is
     * authenticated and authorized. The default behavior is to add a Principal
     * to Request attributes by calling {@link #setPrincipal(Request)} and to
     * ask to the attached Restlet to handle the call.
     * 
     * @param request
     *                The request to accept.
     * @param response
     *                The response to accept.
     */
    public void accept(Request request, Response response) {
        setPrincipal(request);

        // Invoke the attached Restlet
        super.doHandle(request, response);
    }

    /**
     * Indicates if the call is properly authenticated. By default, this
     * delegates credential checking to checkSecret().
     * 
     * @param request
     *                The request to authenticate.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see #checkSecret(String, char[])
     */
    public int authenticate(Request request) {
        // Delegate processing to the Engine
        return Engine.getInstance().authenticate(request, this);
    }

    /**
     * Indicates if the request is authorized to pass through the Guard. This
     * method is only called if the call was sucessfully authenticated. It
     * always returns true by default. If specific checks are required, they
     * could be added by overriding this method.
     * 
     * @param request
     *                The request to authorize.
     * @return True if the request is authorized.
     */
    @SuppressWarnings("unused")
    public boolean authorize(Request request) {
        // Authorize everything by default
        return true;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *                The response to update.
     * @deprecated Use the {@link #challenge(Response, boolean)} method instead.
     */
    @Deprecated
    public void challenge(Response response) {
        challenge(response, false);
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *                The response to update.
     * @param stale
     *                Indicates if the new challenge is due to a stale response.
     */
    public void challenge(Response response, boolean stale) {
        // Delegate processing to the Engine
        Engine.getInstance().challenge(response, stale, this);
    }

    /**
     * Indicates if the secret is valid for the given identifier. By default,
     * this returns true given the correct login/password couple as verified via
     * the findSecret() method.
     * 
     * @param request
     *                The Request
     * @param identifier
     *                the identifier
     * @param secret
     *                the identifier's secret
     * @return true if the secret is valid for the given identifier
     */
    @SuppressWarnings("unused")
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        return checkSecret(identifier, secret);
    }

    /**
     * Indicates if the secret is valid for the given identifier. By default,
     * this returns true given the correct login/password couple as verified via
     * the findSecret() method.
     * 
     * @param identifier
     *                the identifier
     * @param secret
     *                the identifier's secret
     * @return true if the secret is valid for the given identifier
     * @deprecated Use the {@link #checkSecret(Request, String, char[])} method
     *             instead.
     */
    @Deprecated
    protected boolean checkSecret(String identifier, char[] secret) {
        boolean result = false;
        char[] secret2 = findSecret(identifier);
        if (secret == null || secret2 == null) {
            // check if both are null
            result = (secret == secret2);
        } else {
            if (secret.length == secret2.length) {
                boolean equals = true;
                for (int i = 0; i < secret.length && equals; i++) {
                    equals = (secret[i] == secret2[i]);
                }
                result = equals;
            }
        }

        return result;
    }

    /**
     * Handles the call by distributing it to the next Restlet.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The continuation status.
     */
    @Override
    public int doHandle(Request request, Response response) {
        switch (authenticate(request)) {
        case AUTHENTICATION_VALID:
            // Valid credentials provided
            if (authorize(request)) {
                accept(request, response);
            } else {
                forbid(response);
            }
            break;
        case AUTHENTICATION_MISSING:
            // No credentials provided
            challenge(response, false);
            break;
        case AUTHENTICATION_INVALID:
            // Invalid credentials provided
            if (isRechallengeEnabled()) {
                challenge(response, false);
            } else {
                forbid(response);
            }
            break;
        case AUTHENTICATION_STALE:
            challenge(response, true);
            break;
        }

        return CONTINUE;
    }

    /**
     * Finds the secret associated to a given identifier. By default it looks up
     * into the secrets map, but this behavior can be overriden.
     * 
     * @param identifier
     *                The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    public char[] findSecret(String identifier) {
        return getSecrets().get(identifier);
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overriden to change the defaut behavior, for example to display an
     * error page. By default, if authentication is required, the challenge
     * method is invoked, otherwise the call status is set to
     * CLIENT_ERROR_FORBIDDEN.
     * 
     * @param response
     *                The reject response.
     */
    public void forbid(Response response) {
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    /**
     * Returns the base URIs that collectively define the protected domain for
     * HTTP Digest Authentication.
     * 
     * @return The base URIs.
     */
    public Collection<String> getDomainUris() {
        return this.domainUris;
    }

    /**
     * Returns the number of milliseconds between each mandatory nonce refresh.
     * 
     * @return The nonce lifespan.
     */
    public long getNonceLifespan() {
        return this.nonceLifespan;
    }

    /**
     * Returns the authentication realm.
     * 
     * @return The authentication realm.
     */
    public String getRealm() {
        return this.realm;
    }

    /**
     * Returns the authentication challenge scheme.
     * 
     * @return The authentication challenge scheme.
     */
    public ChallengeScheme getScheme() {
        return this.scheme;
    }

    /**
     * Returns the modifiable map of identifiers and secrets.
     * 
     * @return The map of identifiers and secrets.
     */
    public ConcurrentMap<String, char[]> getSecrets() {
        return this.secrets;
    }

    /**
     * Returns the secret key known only by server. This is used by the HTTP
     * DIGEST authentication scheme.
     * 
     * @return The server secret key.
     */
    public String getServerKey() {
        return this.serverKey;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations). If set to
     * false, upon reception of invalid credentials, the Guard will forbid the
     * access ({@link Status#CLIENT_ERROR_FORBIDDEN}).
     * 
     * @return True if invalid credentials result in a new challenge.
     */
    public boolean isRechallengeEnabled() {
        return this.rechallengeEnabled;
    }

    /**
     * Sets the URIs that define the HTTP DIGEST authentication protection
     * domains.
     * 
     * @param domainUris
     *                The URIs of protection domains.
     */
    public void setDomainUris(Collection<String> domainUris) {
        this.domainUris = domainUris;
    }

    /**
     * Sets the number of milliseconds between each mandatory nonce refresh.
     * 
     * @param lifespan
     *                The nonce lifespan in ms.
     */
    public void setNonceLifespan(long lifespan) {
        this.nonceLifespan = lifespan;
    }

    /**
     * Sets a {@link Principal} in in the given request. It actually creates a
     * new instance based on the challenge responses's identifier then adds it
     * to the request's attributes map.
     * 
     * You can later retrieve the Principal using the
     * {@link Guard#getPrincipal(Request)} static method.
     * 
     * @param request
     *                The request to update.
     */
    protected void setPrincipal(Request request) {
        Principal principal = null;
        ChallengeResponse challengeResponse = request.getChallengeResponse();

        if (challengeResponse != null) {
            String credentials = challengeResponse.getIdentifier();

            if (credentials != null) {
                principal = new PrincipalImpl(credentials);
            }
        }

        if (principal != null) {
            request.getAttributes().put(NAME_ATTRIBUTE_PRINCIPAL, principal);
        } else {
            request.getAttributes().remove(NAME_ATTRIBUTE_PRINCIPAL);
        }
    }

    /**
     * Sets the authentication realm.
     * 
     * @param realm
     *                The authentication realm.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received.
     * 
     * @param rechallengeEnabled
     *                True if invalid credentials result in a new challenge.
     * @see #isRechallengeEnabled()
     */
    public void setRechallengeEnabled(boolean rechallengeEnabled) {
        this.rechallengeEnabled = rechallengeEnabled;
    }

    /**
     * Sets the authentication challenge scheme.
     * 
     * @param scheme
     *                The authentication challenge scheme.
     */
    public void setScheme(ChallengeScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the secret key known only by server. This is used by the HTTP DIGEST
     * authentication scheme.
     * 
     * @param serverKey
     *                The server secret key.
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }
}
