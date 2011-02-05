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

package org.restlet.data;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.Series;

/**
 * Authentication response sent by client to an origin server. This is typically
 * following a {@link ChallengeRequest} sent by the origin server to the client.<br>
 * <br>
 * Sometimes, it might be faster to preemptively issue a challenge response if
 * the client knows for sure that the target resource will require
 * authentication.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the
 * "Authorization" header.
 * 
 * @author Jerome Louvel
 */
public final class ChallengeResponse extends ChallengeMessage {

    /** The client nonce value. */
    private volatile String clientNonce;

    /**
     * The {@link Request#getResourceRef()} value duplicated here in case a
     * proxy changed it.
     */
    private volatile Reference digestRef;

    /** The user identifier, such as a login name or an access key. */
    private volatile String identifier;

    /** The chosen quality of protection. */
    private volatile String quality;

    /** The user secret, such as a password or a secret key. */
    private volatile char[] secret;

    /** The server nonce count. */
    private volatile int serverNounceCount;

    // [ifndef gwt] method
    /**
     * Constructor. It leverages the latest server response and challenge
     * request in order to compute the credentials.
     * 
     * @param challengeRequest
     *            The challenge request sent by the origin server.
     * @param response
     *            The latest server response.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param baseSecret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeRequest challengeRequest,
            final Response response, final String identifier, char[] baseSecret) {
        this(challengeRequest, response, identifier, baseSecret,
                Digest.ALGORITHM_NONE);
    }

    // [ifndef gwt] method
    /**
     * Constructor. It leverages the latest server response and challenge
     * request in order to compute the credentials.
     * 
     * @param challengeRequest
     *            The challenge request sent by the origin server.
     * @param response
     *            The latest server response.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param baseSecret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeRequest challengeRequest,
            final Response response, final String identifier, String baseSecret) {
        this(challengeRequest, response, identifier, baseSecret.toCharArray(),
                Digest.ALGORITHM_NONE);
    }

    // [ifndef gwt] method
    /**
     * Constructor. It leverages the latest server response and challenge
     * request in order to compute the credentials.
     * 
     * @param challengeRequest
     *            The challenge request sent by the origin server.
     * @param response
     *            The latest server response.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param baseSecret
     *            The base secret used to compute the secret.
     * @param baseSecretAlgorithm
     *            The digest algorithm of the base secret (see {@link Digest}
     *            class).
     */
    public ChallengeResponse(final ChallengeRequest challengeRequest,
            final Response response, final String identifier,
            char[] baseSecret, String baseSecretAlgorithm) {
        super(challengeRequest.getScheme());
        this.identifier = identifier;
        org.restlet.engine.security.AuthenticatorUtils.update(this,
                response.getRequest(), response, identifier, baseSecret,
                baseSecretAlgorithm);
    }

    /**
     * Constructor with no credentials.
     * 
     * @param scheme
     *            The challenge scheme.
     */
    public ChallengeResponse(ChallengeScheme scheme) {
        super(scheme);
        this.identifier = null;
        this.secret = null;
        this.clientNonce = null;
        this.digestRef = null;
        this.quality = null;
        this.serverNounceCount = 1;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, char[] secret) {
        super(scheme);
        this.identifier = identifier;
        this.secret = secret;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param parameters
     *            The additional scheme parameters.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, Series<Parameter> parameters) {
        super(scheme, parameters);
        this.identifier = identifier;
        this.secret = null;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public ChallengeResponse(final ChallengeScheme scheme,
            final String identifier, String secret) {
        super(scheme);
        this.identifier = identifier;
        this.secret = (secret != null) ? secret.toCharArray() : null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a challenge request or is null don't evaluate
            // further
            if (obj instanceof ChallengeResponse) {
                final ChallengeResponse that = (ChallengeResponse) obj;

                if (getRawValue() != null) {
                    result = getRawValue().equals(that.getRawValue());
                } else {
                    result = (that.getRawValue() == null);
                }

                if (result) {
                    if (getIdentifier() != null) {
                        result = getIdentifier().equals(that.getIdentifier());
                    } else {
                        result = (that.getIdentifier() == null);
                    }

                    if (result) {
                        if (getScheme() != null) {
                            result = getScheme().equals(that.getScheme());
                        } else {
                            result = (that.getScheme() == null);
                        }

                        if (result) {
                            if ((getSecret() == null)
                                    || (that.getSecret() == null)) {
                                // check if both are null
                                result = (getSecret() == that.getSecret());
                            } else {
                                if (getSecret().length == that.getSecret().length) {
                                    boolean equals = true;
                                    for (int i = 0; (i < getSecret().length)
                                            && equals; i++) {
                                        equals = (getSecret()[i] == that
                                                .getSecret()[i]);
                                    }
                                    result = equals;
                                }
                            }
                        }
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
     * Returns the {@link Request#getResourceRef()} value duplicated here in
     * case a proxy changed it.
     * 
     * @return The digest URI reference.
     */
    public Reference getDigestRef() {
        return digestRef;
    }

    /**
     * Returns the user identifier, such as a login name or an access key.
     * 
     * @return The user identifier, such as a login name or an access key.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    // [ifndef gwt] method
    /**
     * Gets the principal associated to the identifier property.
     * 
     * @return The principal associated to the identifier property.
     */
    public java.security.Principal getPrincipal() {
        return new java.security.Principal() {
            public String getName() {
                return getIdentifier();
            };
        };
    }

    /**
     * Returns the chosen quality of protection.
     * 
     * @return The chosen quality of protection.
     */
    public String getQuality() {
        return quality;
    }

    /**
     * Returns the user secret, such as a password or a secret key.
     * 
     * It is not recommended to use {@link String#String(char[])} for security
     * reasons.
     * 
     * @return The user secret, such as a password or a secret key.
     */
    public char[] getSecret() {
        return this.secret;
    }

    /**
     * Returns the server nonce count.
     * 
     * @return The server nonce count.
     */
    public int getServerNounceCount() {
        return serverNounceCount;
    }

    // [ifndef gwt] method
    /**
     * Returns the server nonce count as an hexadecimal string of eight
     * characters.
     * 
     * @return The server nonce count as an hexadecimal string.
     */
    public String getServerNounceCountAsHex() {
        return org.restlet.engine.security.AuthenticatorUtils
                .formatNonceCount(getServerNounceCount());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        // Note that the secret is simply discarded from hash code calculation
        // because we don't want it to be materialized as a string
        return SystemUtils
                .hashCode(getScheme(), getIdentifier(), getRawValue());
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
     * Sets the digest URI reference.
     * 
     * @param digestRef
     *            The digest URI reference.
     */
    public void setDigestRef(Reference digestRef) {
        this.digestRef = digestRef;
    }

    /**
     * Sets the user identifier, such as a login name or an access key.
     * 
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the chosen quality of protection.
     * 
     * @param quality
     *            The chosen quality of protection.
     */
    public void setQuality(String quality) {
        this.quality = quality;
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setSecret(char[] secret) {
        this.secret = secret;
    }

    /**
     * Sets the user secret, such as a password or a secret key.
     * 
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setSecret(String secret) {
        this.secret = (secret == null) ? null : secret.toCharArray();
    }

    /**
     * Sets the server nonce count.
     * 
     * @param serverNounceCount
     *            The server nonce count.
     */
    public void setServerNounceCount(int serverNounceCount) {
        this.serverNounceCount = serverNounceCount;
    }
}
