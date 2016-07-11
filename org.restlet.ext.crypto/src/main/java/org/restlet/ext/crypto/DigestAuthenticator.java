/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Digest;
import org.restlet.data.Reference;
import org.restlet.ext.crypto.internal.CryptoUtils;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Verifier;

/**
 * Authenticator supporting the digest challenge authentication schemes. By
 * default, it only knows about the {@link ChallengeScheme#HTTP_DIGEST} scheme.
 * 
 * @see DigestVerifier
 * @see DigestAuthenticator
 * @author Jerome Louvel
 */
public class DigestAuthenticator extends ChallengeAuthenticator {

    /** Default lifespan for generated nonces (5 minutes). */
    private static final long DEFAULT_MAX_SERVER_NONCE_AGE = 5 * 60 * 1000L;

    /** The URI references that define the protection domains. */
    private volatile List<Reference> domainRefs;

    /** Lifespan of nonce in milliseconds */
    private volatile long maxServerNonceAge;

    /** The secret key known only to server. */
    private volatile String serverKey;

    /**
     * Constructor. Sets the challenge scheme to
     * {@link ChallengeScheme#HTTP_DIGEST} and the nonce lifespan to 5 minutes
     * by default.
     * 
     * @param context
     *            The context.
     * @param optional
     *            Indicates if the authentication success is optional.
     * @param realm
     *            The authentication realm.
     * @param domainRefs
     *            The URI references that define the protection domains.
     * @param serverKey
     *            The secret key known only to server.
     */
    public DigestAuthenticator(Context context, boolean optional, String realm,
            List<Reference> domainRefs, String serverKey) {
        super(context, optional, ChallengeScheme.HTTP_DIGEST, realm);
        this.domainRefs = domainRefs;
        this.maxServerNonceAge = DEFAULT_MAX_SERVER_NONCE_AGE;
        this.serverKey = serverKey;
        setVerifier(new org.restlet.ext.crypto.internal.HttpDigestVerifier(
                this, null, null));
    }

    /**
     * Constructor. By default, it set the "optional" property to 'false' and
     * the "domainUris" property to a single '/' URI.
     * 
     * @param context
     *            The context.
     * @param realm
     *            The authentication realm.
     * @param serverKey
     *            secret key known only to server
     */
    public DigestAuthenticator(Context context, String realm, String serverKey) {
        this(context, false, realm, null, serverKey);
    }

    @Override
    protected ChallengeRequest createChallengeRequest(boolean stale) {
        ChallengeRequest result = super.createChallengeRequest(stale);
        result.setDomainRefs(getDomainRefs());
        result.setStale(stale);
        result.setServerNonce(generateServerNonce());
        return result;
    }

    /**
     * Generates a server nonce.
     * 
     * @return A new server nonce.
     */
    public String generateServerNonce() {
        return CryptoUtils.makeNonce(getServerKey());
    }

    /**
     * Returns the base URI references that collectively define the protected
     * domains for the digest authentication. By default it return a list with a
     * single "/" URI reference.
     * 
     * @return The base URI references.
     */
    public List<Reference> getDomainRefs() {
        // Lazy initialization with double-check.
        List<Reference> r = this.domainRefs;
        if (r == null) {
            synchronized (this) {
                r = this.domainRefs;
                if (r == null) {
                    this.domainRefs = r = new CopyOnWriteArrayList<Reference>();
                    this.domainRefs.add(new Reference("/"));
                }
            }
        }
        return r;
    }

    /**
     * Return the hashed secret. By default, it knows how to hash HTTP DIGEST
     * secrets, specified as A1 in section 3.2.2.2 of RFC2617, or null if the
     * identifier has no corresponding secret.
     * 
     * @param identifier
     *            The user identifier to hash.
     * @param secret
     *            The user secret.
     * @return A hash of the user name, realm, and password.
     */
    public String getHashedSecret(String identifier, char[] secret) {
        if (ChallengeScheme.HTTP_DIGEST.equals(getScheme())) {
            return DigestUtils.toHttpDigest(identifier, secret, getRealm());
        }

        return null;
    }

    /**
     * Returns the number of milliseconds between each mandatory nonce refresh.
     * 
     * @return The server nonce lifespan.
     */
    public long getMaxServerNonceAge() {
        return this.maxServerNonceAge;
    }

    /**
     * Returns the secret key known only by server.
     * 
     * @return The server secret key.
     */
    public String getServerKey() {
        return this.serverKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DigestVerifier<LocalVerifier> getVerifier() {
        return (DigestVerifier<LocalVerifier>) super.getVerifier();
    }

    /**
     * Sets the URI references that define the protection domains for the digest
     * authentication.
     * 
     * @param domainRefs
     *            The base URI references.
     */
    public void setDomainRefs(List<Reference> domainRefs) {
        this.domainRefs = domainRefs;
    }

    /**
     * Sets the number of milliseconds between each mandatory nonce refresh.
     * 
     * @param maxServerNonceAge
     *            The nonce lifespan in milliseconds.
     */
    public void setMaxServerNonceAge(long maxServerNonceAge) {
        this.maxServerNonceAge = maxServerNonceAge;
    }

    /**
     * Sets the secret key known only by server.
     * 
     * @param serverKey
     *            The server secret key.
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    /**
     * Set the internal verifier. In general you shouldn't replace it and
     * instead use the {@link #setWrappedVerifier(LocalVerifier)} method.
     * 
     * @param verifier
     *            The internal verifier.
     */
    @Override
    public void setVerifier(Verifier verifier) {
        if (ChallengeScheme.HTTP_DIGEST.equals(getScheme())) {
            if (!(verifier instanceof DigestVerifier)) {
                throw new IllegalArgumentException(
                        "Only subclasses on HttpDigestVerifier are allowed. You might want to set the \"wrappedVerifier\" property instead.");
            }

            super.setVerifier(verifier);
        } else {
            if (!(verifier instanceof DigestVerifier<?>)) {
                throw new IllegalArgumentException(
                        "Only subclasses on DigestVerifier are allowed. You might want to set the \"wrappedVerifier\" property instead.");
            }

            super.setVerifier(verifier);
        }
    }

    /**
     * Sets the digest algorithm of secrets returned by the wrapped verifier.
     * The secrets from the wrapped verifier are the ones used by the verifier
     * to compare those sent by clients when attempting to authenticate.
     * 
     * @param wrappedAlgorithm
     *            The digest algorithm of secrets returned by the wrapped
     *            verifier.
     * @see Digest
     */
    public void setWrappedAlgorithm(String wrappedAlgorithm) {
        getVerifier().setWrappedAlgorithm(wrappedAlgorithm);
    }

    /**
     * Sets the secret verifier that will be wrapped by real verifier supporting
     * all the HTTP DIGEST verifications (nonce, domain URIs, etc.).
     * 
     * @param localVerifier
     *            The local verifier to wrap.
     */
    public void setWrappedVerifier(LocalVerifier localVerifier) {
        getVerifier().setWrappedVerifier(localVerifier);
    }

}
