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

import java.util.Collection;
import java.util.Collections;

import org.restlet.data.ChallengeScheme;

/**
 * Authenticator supporting the HTTP Digest scheme.
 * 
 * @author Jerome Louvel
 */
public class HttpDigestAuthenticator extends ChallengeAuthenticator {

    /** Default lifespan for generated nonces (5 minutes). */
    public static final long DEFAULT_NONCE_LIFESPAN_MILLIS = 5 * 60 * 1000L;

    /** The URIs that define the HTTP DIGEST authentication protection domains. */
    private volatile Collection<String> domainUris = Collections.singleton("/");

    /** Lifespan of nonce in milliseconds */
    private volatile long nonceLifespan = DEFAULT_NONCE_LIFESPAN_MILLIS;

    /** The authentication realm. */
    private volatile String realm;

    /**
     * The secret key known only to server (use for HTTP DIGEST authentication).
     */
    private volatile String serverKey = "serverKey";

    /**
     * Alternate Constructor for HTTP DIGEST authentication scheme.
     * 
     * @param realm
     *            authentication realm
     * @param baseUris
     *            protection domain as a collection of base URIs
     * @param serverKey
     *            secret key known only to server
     */
    public HttpDigestAuthenticator(String realm, Collection<String> baseUris,
            String serverKey) {
        super(ChallengeScheme.HTTP_DIGEST);
        this.realm = realm;
        this.domainUris = baseUris;
        this.serverKey = serverKey;
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
     * Returns the secret key known only by server. This is used by the HTTP
     * DIGEST authentication scheme.
     * 
     * @return The server secret key.
     */
    public String getServerKey() {
        return this.serverKey;
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
