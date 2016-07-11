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

package org.restlet.data;

import org.restlet.engine.util.SystemUtils;

/**
 * Preemptive authentication information. Sent by an origin server to a client
 * after a successful digest authentication attempt.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the
 * "Authentication-Info" header.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc2617#section-3.2.3">HTTP
 *      Authentication - The Authentication-Info Header</a>
 * 
 * @author Kelly McLaughlin
 * @author Jerome Louvel
 */
public class AuthenticationInfo {

    /** The next nonce value. */
    private volatile String nextServerNonce;

    /** The nonce-count value. */
    private volatile int nonceCount;

    /** The client nonce. */
    private volatile String clientNonce;

    /** The quality of protection. */
    private volatile String quality;

    /** The optional response digest for mutual authentication. */
    private volatile String responseDigest;

    /**
     * Default constructor.
     * 
     * @param nextNonce
     *            The next nonce value.
     */
    // public AuthenticationInfo(String nextNonce) {
    // this(nextNonce, 0, );
    // }

    /**
     * Constructor.
     * 
     * @param nextNonce
     *            The next nonce value.
     * @param nonceCount
     *            The nonce-count value.
     * @param cnonce
     *            The cnonce value.
     * @param quality
     *            The quality of protection.
     * @param responseDigest
     *            The optional response digest for mutual authentication.
     */
    public AuthenticationInfo(String nextNonce, int nonceCount, String cnonce,
            String quality, String responseDigest) {
        this.nextServerNonce = nextNonce;
        this.nonceCount = nonceCount;
        this.clientNonce = cnonce;
        this.quality = quality;
        this.responseDigest = responseDigest;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a challenge request or is null don't evaluate
            // further
            if (obj instanceof AuthenticationInfo) {
                final AuthenticationInfo that = (AuthenticationInfo) obj;
                if (getNextServerNonce() != null) {
                    result = getNextServerNonce().equals(
                            that.getNextServerNonce());
                } else {
                    result = (that.getNextServerNonce() == null);
                }

                if (result) {
                    result = (getNonceCount() == that.getNonceCount());
                }

                if (result) {
                    if (getClientNonce() != null) {
                        result = getClientNonce().equals(that.getClientNonce());
                    } else {
                        result = (that.getClientNonce() == null);
                    }
                }

                if (result) {
                    if (getQuality() != null) {
                        result = getQuality().equals(that.getQuality());
                    } else {
                        result = (that.getQuality() == null);
                    }
                }

                if (result) {
                    if (getResponseDigest() != null) {
                        result = getResponseDigest().equals(
                                that.getResponseDigest());
                    } else {
                        result = (that.getResponseDigest() == null);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the client nonce.
     * 
     * @return The client nonce.
     */
    public String getClientNonce() {
        return this.clientNonce;
    }

    /**
     * Returns the next server nonce. This is the nonce the server wishes the
     * client to use for a future authentication response
     * 
     * @return The next nonce value.
     */
    public String getNextServerNonce() {
        return this.nextServerNonce;
    }

    /**
     * Returns the nonce-count value.
     * 
     * @return The nonce-count value.
     */
    public int getNonceCount() {
        return this.nonceCount;
    }

    /**
     * Returns the quality of protection. The value can be
     * {@link ChallengeMessage#QUALITY_AUTHENTICATION} for authentication or
     * {@link ChallengeMessage#QUALITY_AUTHENTICATION_INTEGRITY} for
     * authentication with integrity protection.
     * 
     * @return The quality of protection.
     */
    public String getQuality() {
        return this.quality;
    }

    /**
     * Returns the optional response digest for mutual authentication. Note that
     * when used with HTTP connectors, this property maps to the
     * "response-digest" value in the "response-auth" directive of the
     * "Authentication-Info" header.
     * 
     * @return The optional response digest for mutual authentication.
     */
    public String getResponseDigest() {
        return this.responseDigest;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getNextServerNonce(), getNonceCount(),
                getClientNonce(), getQuality(), getResponseDigest());
    }

    /**
     * Sets the client nonce.
     * 
     * @param clientNonce
     *            The client nonce.
     */
    public void setClientNonce(String clientNonce) {
        this.clientNonce = clientNonce;
    }

    /**
     * Sets the next server nonce. This is the nonce the server wishes the
     * client to use for a future authentication response
     * 
     * @param nextNonce
     *            The next nonce.
     */
    public void setNextServerNonce(String nextNonce) {
        this.nextServerNonce = nextNonce;
    }

    /**
     * Sets the nonce-count value.
     * 
     * @param nonceCount
     *            The nonceCount value.
     */
    public void setNonceCount(int nonceCount) {
        this.nonceCount = nonceCount;
    }

    /**
     * Sets the quality of protection. The value can be
     * {@link ChallengeMessage#QUALITY_AUTHENTICATION} for authentication or
     * {@link ChallengeMessage#QUALITY_AUTHENTICATION_INTEGRITY} for
     * authentication with integrity protection.
     * 
     * @param qop
     *            The quality of protection.
     */
    public void setQuality(String qop) {
        this.quality = qop;
    }

    /**
     * Sets the optional response digest for mutual authentication. Note that
     * when used with HTTP connectors, this property maps to the
     * "response-digest" value in the "response-auth" directive of the
     * "Authentication-Info" header.
     * 
     * @param responseDigest
     *            The response digest.
     */
    public void setResponseDigest(String responseDigest) {
        this.responseDigest = responseDigest;
    }
}
