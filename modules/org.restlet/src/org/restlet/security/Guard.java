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

package org.restlet.security;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.routing.Filter;
import org.restlet.util.Resolver;

/**
 * Filter guarding the access to an attached Restlet. More concretely, it guards
 * from unauthenticated and unauthorized requests, providing facilities to check
 * credentials such as passwords. It is also a relatively generic class which
 * can work with several challenge schemes such as HTTP Basic and HTTP Digest.
 * <p>
 * Here are the processing steps of a Guard when a request reaches it:
 * <ol>
 * <li>It first attempts to authenticate it, i.e. to make sure that the
 * challenge scheme used is supported and that the credentials given by the
 * client (such as a login and password) are valid. The actual implementation of
 * the authentication is delegated to the matching authentication helper. The
 * result of this authentication can be:
 * <ol>
 * <li>Valid: the authentication credentials are valid, the right scheme was
 * used and the credentials could be verified by calling back the checkSecret()
 * method on Guard. Here are the next steps:
 * <ol>
 * <li>The authorize() method is called and if authorization is given the
 * accept() method is invoked, which delegates to the attached Restlet or
 * Resource by default. Otherwise, the forbid method is called, which sets the
 * response status to {@link Status#CLIENT_ERROR_FORBIDDEN} (403).</li>
 * </ol>
 * </li>
 * <li>Missing: no credentials could be found, the challenge() method is invoked
 * which delegates to the matching authenticator helper.</li>
 * <li>Invalid: bad credentials were given such as a wrong password or
 * unsupported scheme was used. If the "rechallenge" property is true, the
 * challenge() method is invoked otherwise, the forbid() method is invoked.</li>
 * <li>Stale: the credentials expired and must be renew. Therefore, the
 * challenge() method is invoked.</li>
 * </ol>
 * </ol>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 * @deprecated Use the {@link ChallengeAuthenticator} class instead.
 */
@Deprecated
public class Guard extends Filter {

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

    /** The URIs that define the HTTP DIGEST authentication protection domains. */
    private volatile Collection<String> domainUris = Collections.singleton("/");

    /** Lifespan of nonce in milliseconds */
    private volatile long nonceLifespan = DEFAULT_NONCE_LIFESPAN_MILLIS;

    /** The authentication realm. */
    private volatile String realm;

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallengeEnabled;

    /** The authentication challenge scheme. */
    private volatile ChallengeScheme scheme;

    /** The secret resolver. */
    private volatile Resolver<char[]> secretResolver;

    /** Map of secrets (login/password combinations). */
    private final ConcurrentMap<String, char[]> secrets;

    /**
     * The secret key known only to server (use for HTTP DIGEST authentication).
     */
    private volatile String serverKey = "serverKey";

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param scheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     * @throws IllegalArgumentException
     *             if the scheme is null
     */
    public Guard(Context context, ChallengeScheme scheme, String realm)
            throws IllegalArgumentException {
        super(context);
        if (scheme == null) {
            throw new IllegalArgumentException(
                    "Please specify an authentication scheme. Use the 'None' challenge if no authentication is required.");
        }

        this.rechallengeEnabled = true;
        this.secretResolver = new Resolver<char[]>() {
            @Override
            public char[] resolve(String identifier) {
                return getSecrets().get(identifier);
            }
        };

        this.secrets = new ConcurrentHashMap<String, char[]>();

        this.scheme = scheme;
        this.realm = realm;
    }

    /**
     * Alternate Constructor for HTTP DIGEST authentication scheme.
     * 
     * @param context
     *            context
     * @param realm
     *            authentication realm
     * @param baseUris
     *            protection domain as a collection of base URIs
     * @param serverKey
     *            secret key known only to server
     */
    public Guard(Context context, String realm, Collection<String> baseUris,
            String serverKey) {
        this(context, ChallengeScheme.HTTP_DIGEST, realm);
        this.domainUris = baseUris;
        this.serverKey = serverKey;
    }

    /**
     * Accepts the call. By default, it is invoked if the request is
     * authenticated and authorized. The default behavior is to ask to the
     * attached Restlet to handle the call.
     * 
     * @param request
     *            The request to accept.
     * @param response
     *            The response to accept.
     */
    public void accept(Request request, Response response) {
        // Invoke the attached Restlet
        super.doHandle(request, response);
    }

    /**
     * Indicates if the call is properly authenticated. By default, this
     * delegates credentials checking to checkSecret(). Note that the
     * {@link ChallengeResponse#setAuthenticated(boolean)} and
     * {@link ClientInfo#setAuthenticated(boolean)} methods are always called
     * after authentication.
     * 
     * @param request
     *            The request to authenticate.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see #checkSecret(Request, String, char[])
     */
    public int authenticate(Request request) {
        // Delegate processing to the Engine
        return AuthenticatorUtils.authenticate(request, this);
    }

    /**
     * Indicates if the request is authorized to pass through the Guard. This
     * method is only called if the call was successfully authenticated. It
     * always returns true by default. If specific checks are required, they
     * could be added by overriding this method.
     * 
     * @param request
     *            The request to authorize.
     * @return True if the request is authorized.
     */
    public boolean authorize(Request request) {
        // Authorize everything by default
        return true;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *            The response to update.
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     */
    public void challenge(Response response, boolean stale) {
        // Delegate processing to the Engine
        AuthenticatorUtils.challenge(response, stale, this);
    }

    /**
     * Indicates if the secret is valid for the given identifier. By default,
     * this returns true given the correct login/password couple as verified via
     * the findSecret() method.
     * 
     * @param request
     *            The Request
     * @param identifier
     *            the identifier
     * @param secret
     *            the identifier's secret
     * @return true if the secret is valid for the given identifier
     */
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        boolean result = false;
        final char[] secret2 = findSecret(identifier);

        if ((secret == null) || (secret2 == null)) {
            // check if both are null
            result = (secret == secret2);
        } else {
            if (secret.length == secret2.length) {
                boolean equals = true;
                for (int i = 0; (i < secret.length) && equals; i++) {
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
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    @Override
    public int doHandle(Request request, Response response) {
        final boolean loggable = getLogger().isLoggable(Level.FINE);

        switch (authenticate(request)) {
        case AUTHENTICATION_VALID:
            // Valid credentials provided
            ChallengeResponse challengeResponse = request
                    .getChallengeResponse();
            if (loggable) {
                if (challengeResponse != null) {
                    getLogger().fine(
                            "Authentication succeeded. Valid credentials provided for identifier: "
                                    + request.getChallengeResponse()
                                            .getIdentifier() + ".");
                } else {
                    getLogger()
                            .fine("Authentication succeeded. Valid credentials provided.");
                }
            }

            if (authorize(request)) {
                if (loggable) {
                    if (challengeResponse != null) {
                        getLogger().fine(
                                "Request authorized for identifier: "
                                        + request.getChallengeResponse()
                                                .getIdentifier() + ".");
                    } else {
                        getLogger().fine("Request authorized.");
                    }
                }

                accept(request, response);
            } else {
                if (loggable) {
                    if (challengeResponse != null) {
                        getLogger().fine(
                                "Request not authorized for identifier: "
                                        + request.getChallengeResponse()
                                                .getIdentifier() + ".");
                    } else {
                        getLogger().fine("Request not authorized.");
                    }
                }

                forbid(response);
            }
            break;
        case AUTHENTICATION_MISSING:
            // No credentials provided
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. No credentials provided.");
            }

            challenge(response, false);
            break;
        case AUTHENTICATION_INVALID:
            // Invalid credentials provided
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. Invalid credentials provided.");
            }

            if (isRechallengeEnabled()) {
                challenge(response, false);
            } else {
                forbid(response);
            }
            break;
        case AUTHENTICATION_STALE:
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. Stale credentials provided.");
            }

            challenge(response, true);
            break;
        }

        return CONTINUE;
    }

    /**
     * Finds the secret associated to a given identifier. By default it looks up
     * into the secrets map, but this behavior can be overriden by setting a
     * custom secret resolver using the {@link #setSecretResolver(Resolver)}
     * method.
     * 
     * @param identifier
     *            The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    public char[] findSecret(String identifier) {
        return getSecretResolver().resolve(identifier);
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overridden to change the default behavior, for example to display
     * an error page. By default, if authentication is required, the challenge
     * method is invoked, otherwise the call status is set to
     * CLIENT_ERROR_FORBIDDEN.
     * 
     * @param response
     *            The reject response.
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
     * Returns the secret resolver.
     * 
     * @return The secret resolver.
     */
    public Resolver<char[]> getSecretResolver() {
        return this.secretResolver;
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
     *            The URIs of protection domains.
     */
    public void setDomainUris(Collection<String> domainUris) {
        this.domainUris = domainUris;
    }

    /**
     * Sets the number of milliseconds between each mandatory nonce refresh.
     * 
     * @param lifespan
     *            The nonce lifespan in ms.
     */
    public void setNonceLifespan(long lifespan) {
        this.nonceLifespan = lifespan;
    }

    /**
     * Sets the authentication realm.
     * 
     * @param realm
     *            The authentication realm.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received.
     * 
     * @param rechallengeEnabled
     *            True if invalid credentials result in a new challenge.
     * @see #isRechallengeEnabled()
     */
    public void setRechallengeEnabled(boolean rechallengeEnabled) {
        this.rechallengeEnabled = rechallengeEnabled;
    }

    /**
     * Sets the authentication challenge scheme.
     * 
     * @param scheme
     *            The authentication challenge scheme.
     */
    public void setScheme(ChallengeScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the secret resolver.
     * 
     * @param secretResolver
     *            The secret resolver.
     */
    public void setSecretResolver(Resolver<char[]> secretResolver) {
        this.secretResolver = secretResolver;
    }

    /**
     * Sets the secret key known only by server. This is used by the HTTP DIGEST
     * authentication scheme.
     * 
     * @param serverKey
     *            The server secret key.
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }
}
