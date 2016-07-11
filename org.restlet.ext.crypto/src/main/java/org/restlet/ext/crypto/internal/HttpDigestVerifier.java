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

package org.restlet.ext.crypto.internal;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Digest;
import org.restlet.data.Reference;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.security.LocalVerifier;
import org.restlet.security.User;

/**
 * Verifier for the HTTP DIGEST authentication scheme. Note that the "A1" hash
 * specified in RFC 2617 is available via the
 * {@link #getWrappedSecretDigest(String)} method.
 * 
 * @author Jerome Louvel
 */
public class HttpDigestVerifier extends
        org.restlet.ext.crypto.DigestVerifier<LocalVerifier> {

    /** The associated digest authenticator. */
    private DigestAuthenticator digestAuthenticator;

    /**
     * Constructor.
     * 
     * @param digestAuthenticator
     *            The associated digest authenticator.
     * @param wrappedAlgorithm
     *            The digest algorithm of secrets provided by the wrapped
     *            verifier.
     * @param wrappedVerifier
     *            The wrapped secret verifier.
     */
    public HttpDigestVerifier(DigestAuthenticator digestAuthenticator,
            LocalVerifier wrappedVerifier, String wrappedAlgorithm) {
        super(Digest.ALGORITHM_HTTP_DIGEST, wrappedVerifier, wrappedAlgorithm);
        this.digestAuthenticator = digestAuthenticator;
    }

    /**
     * If the algorithm is {@link Digest#ALGORITHM_HTTP_DIGEST}, then is
     * retrieves the realm for {@link #getDigestAuthenticator()} to compute the
     * digest, otherwise, it keeps the default behavior.
     */
    @Override
    protected char[] digest(String identifier, char[] secret, String algorithm) {
        if (Digest.ALGORITHM_HTTP_DIGEST.equals(algorithm)) {
            String result = DigestUtils.toHttpDigest(identifier, secret,
                    getDigestAuthenticator().getRealm());
            if (result != null) {
                return result.toCharArray();
            }

            return null;
        }

        return super.digest(identifier, secret, algorithm);
    }

    /**
     * Returns the associated digest authenticator.
     * 
     * @return The associated digest authenticator.
     */
    public DigestAuthenticator getDigestAuthenticator() {
        return digestAuthenticator;
    }

    /**
     * Sets the associated digest authenticator.
     * 
     * @param digestAuthenticator
     *            The associated digest authenticator.
     */
    public void setDigestAuthenticator(DigestAuthenticator digestAuthenticator) {
        this.digestAuthenticator = digestAuthenticator;
    }

    @Override
    public int verify(Request request, Response response) {
        int result = RESULT_VALID;
        ChallengeResponse cr = request.getChallengeResponse();

        if (cr == null) {
            result = RESULT_MISSING;
        } else {
            String nonce = cr.getServerNonce();
            String uri = (cr.getDigestRef() == null) ? null : cr.getDigestRef().toString();
            String qop = cr.getQuality();
            int nc = cr.getServerNounceCount();
            String cnonce = cr.getClientNonce();
            String username = getIdentifier(request, response);
            String cresponse = null;
            char[] secret = getSecret(request, response);

            if (secret != null) {
                cresponse = new String(secret);
            } else {
                result = RESULT_INVALID;
            }

            try {
                if (!HttpDigestHelper.isNonceValid(nonce,
                        getDigestAuthenticator().getServerKey(),
                        getDigestAuthenticator().getMaxServerNonceAge())) {
                    // Nonce expired, send challenge request with stale=true
                    result = RESULT_STALE;
                }
            } catch (Exception ce) {
                // Invalid nonce, probably doesn't match serverKey
                result = RESULT_INVALID;
            }

            if (result == RESULT_VALID) {
                if (AuthenticatorUtils.anyNull(nonce, uri)) {
                    result = RESULT_MISSING;
                } else {
                    Reference resourceRef = request.getResourceRef();
                    String requestUri = resourceRef.getPath();

                    if ((resourceRef.getQuery() != null)
                            && (uri.indexOf('?') > -1)) {
                        // IE neglects to include the query string, so
                        // the workaround is to leave it off
                        // unless both the calculated URI and the
                        // specified URI contain a query string
                        requestUri += "?" + resourceRef.getQuery();
                    }

                    if (uri.equals(requestUri)) {
                        char[] a1 = getWrappedSecretDigest(username);
                        if (a1 != null) {
                            String a2 = DigestUtils.toMd5(request.getMethod()
                                    .toString() + ":" + requestUri);
                            StringBuilder expectedResponse = new StringBuilder()
                                    .append(a1).append(':').append(nonce);
                            if (!AuthenticatorUtils.anyNull(qop, cnonce, nc)) {
                                expectedResponse
                                        .append(':')
                                        .append(AuthenticatorUtils
                                                .formatNonceCount(nc))
                                        .append(':').append(cnonce).append(':')
                                        .append(qop);
                            }
                            expectedResponse.append(':').append(a2);

                            if (!DigestUtils.toMd5(expectedResponse.toString())
                                    .equals(cresponse)) {
                                result = RESULT_INVALID;
                            }
                        } else {
                            // The HA1 is null
                            result = RESULT_INVALID;
                        }
                    } else {
                        // The request URI doesn't match
                        result = RESULT_INVALID;
                    }
                }
            }

            if (result == RESULT_VALID) {
                request.getClientInfo().setUser(new User(username));
            }
        }

        return result;
    }

}
