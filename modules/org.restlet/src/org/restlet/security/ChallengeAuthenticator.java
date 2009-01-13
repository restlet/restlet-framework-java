/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.authentication.ChallengeAuthenticatorHelper;
import org.restlet.util.Resolver;

/**
 * Authenticator base on a challenge scheme like HTTP Basic or HTTP Digest.
 * 
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator implements Authenticator {

    private final ChallengeScheme scheme;

    private final ChallengeAuthenticatorHelper helper;

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallengeEnabled;

    private volatile Resolver<char[]> secretResolver;

    /** Map of secrets (login/password combinations). */
    private final ConcurrentMap<String, char[]> secrets;

    /**
     * Constructor.
     * 
     * @param challengeScheme
     */
    public ChallengeAuthenticator(ChallengeScheme challengeScheme) {
        this.scheme = challengeScheme;
        this.secrets = new ConcurrentHashMap<String, char[]>();
        this.secretResolver = new Resolver<char[]>() {
            @Override
            public char[] resolve(String identifier) {
                return getSecrets().get(identifier);
            }
        };

        if (this.scheme != null) {
            this.helper = Engine.getInstance().findHelper(challengeScheme,
                    false, true);
        } else {
            this.helper = null;
        }
    }

    /**
     * 
     */
    public int authenticate(Request request) {
        return getHelper().authenticate(request.getChallengeResponse(),
                request, null);
    }

    public void challenge(Response response, boolean stale) {
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
     * Returns the authentication challenge scheme.
     * 
     * @return The authentication challenge scheme.
     */
    public ChallengeScheme getScheme() {
        return scheme;
    }

    /**
     * Returns the private helper.
     * 
     * @return The private helper.
     */
    private ChallengeAuthenticatorHelper getHelper() {
        return helper;
    }

    /**
     * Returns the secret resolver.
     * 
     * @return The secret resolver.
     */
    public Resolver<char[]> getSecretResolver() {
        return secretResolver;
    }

    /**
     * Returns the modifiable map of identifiers and secrets.
     * 
     * @return The map of identifiers and secrets.
     */
    public Map<String, char[]> getSecrets() {
        return secrets;
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
     * Sets the secret resolver.
     * 
     * @param secretResolver
     *            The secret resolver.
     */
    public void setSecretResolver(Resolver<char[]> secretResolver) {
        this.secretResolver = secretResolver;
    }

}
