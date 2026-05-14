/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * Verifier for the HTTP DIGEST authentication scheme. Note that the "A1" hash specified in RFC 2617
 * is available via the {@link #getWrappedSecretDigest(String)} method.
 *
 * @author Jerome Louvel
 */
public class HttpDigestVerifier extends org.restlet.ext.crypto.DigestVerifier<LocalVerifier> {

    /** The associated digest authenticator. */
    private DigestAuthenticator digestAuthenticator;

    /**
     * Constructor.
     *
     * @param digestAuthenticator The associated digest authenticator.
     * @param wrappedAlgorithm The digest algorithm of secrets provided by the wrapped verifier.
     * @param wrappedVerifier The wrapped secret verifier.
     */
    public HttpDigestVerifier(
            DigestAuthenticator digestAuthenticator,
            LocalVerifier wrappedVerifier,
            String wrappedAlgorithm) {
        super(Digest.ALGORITHM_HTTP_DIGEST, wrappedVerifier, wrappedAlgorithm);
        this.digestAuthenticator = digestAuthenticator;
    }

    /**
     * If the algorithm is {@link Digest#ALGORITHM_HTTP_DIGEST}, then it retrieves the realm for
     * {@link #getDigestAuthenticator()} to compute the digest, otherwise, it keeps the default
     * behavior.
     */
    @Override
    protected char[] digest(String identifier, char[] secret, String algorithm) {
        if (Digest.ALGORITHM_HTTP_DIGEST.equals(algorithm)) {
            String result =
                    DigestUtils.toHttpDigest(
                            identifier, secret, getDigestAuthenticator().getRealm());
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
     * @param digestAuthenticator The associated digest authenticator.
     */
    public void setDigestAuthenticator(DigestAuthenticator digestAuthenticator) {
        this.digestAuthenticator = digestAuthenticator;
    }

    @Override
    public int verify(Request request, Response response) {
        ChallengeResponse cr = request.getChallengeResponse();
        if (cr == null) {
            return RESULT_MISSING;
        }

        char[] secret = getSecret(request, response);

        if (secret == null) {
            return RESULT_INVALID;
        }

        String serverNonce = cr.getServerNonce();
        int result = verifyServerNonce(serverNonce);
        if (result != RESULT_VALID) {
            return result;
        }

        String uri = (cr.getDigestRef() == null) ? null : cr.getDigestRef().toString();
        if (AuthenticatorUtils.anyNull(serverNonce, uri)) {
            return RESULT_MISSING;
        }
        String requestUri = getRequestUri(request, uri);
        if (uri == null || !uri.equals(requestUri)) { // The request URI doesn't match
            return RESULT_INVALID;
        }

        String username = getIdentifier(request, response);
        char[] a1 = getWrappedSecretDigest(username);
        if (a1 == null) { // The HA1 is null
            return RESULT_INVALID;
        }

        String qop = cr.getQuality();
        int nc = cr.getServerNonceCount();
        String clientNonce = cr.getClientNonce();

        String expectedResponse =
                getExpectedResponse(request, a1, serverNonce, qop, clientNonce, nc, requestUri);

        if (!DigestUtils.toMd5(expectedResponse).equals(new String(secret))) {
            return RESULT_INVALID;
        }

        request.getClientInfo().setUser(new User(username));

        return RESULT_VALID;
    }

    private int verifyServerNonce(final String serverNonce) {
        try {
            if (!HttpDigestHelper.isNonceValid(
                    serverNonce,
                    getDigestAuthenticator().getServerKey(),
                    getDigestAuthenticator().getMaxServerNonceAge())) {
                // Nonce expired, send a challenge request with stale=true
                return RESULT_STALE;
            }
        } catch (Exception ce) {
            // Invalid nonce, probably doesn't match serverKey
            return RESULT_INVALID;
        }
        return RESULT_VALID;
    }

    private static String getExpectedResponse(
            final Request request,
            final char[] a1,
            final String serverNonce,
            final String qop,
            final String clientNonce,
            final int nc,
            final String requestUri) {
        StringBuilder expectedResponse =
                new StringBuilder().append(a1).append(':').append(serverNonce);
        if (!AuthenticatorUtils.anyNull(qop, clientNonce, nc)) {
            expectedResponse
                    .append(':')
                    .append(AuthenticatorUtils.formatNonceCount(nc))
                    .append(':')
                    .append(clientNonce)
                    .append(':')
                    .append(qop);
        }
        String a2 = DigestUtils.toMd5(request.getMethod().toString() + ":" + requestUri);
        expectedResponse.append(':').append(a2);
        return expectedResponse.toString();
    }

    private static String getRequestUri(final Request request, final String uri) {
        Reference resourceRef = request.getResourceRef();
        String requestUri = resourceRef.getPath();

        if ((resourceRef.getQuery() != null) && (uri != null && uri.indexOf('?') > -1)) {
            // IE neglects to include the query string, so
            // the workaround is to leave it off
            // unless both the calculated URI and the
            // specified URI contain a query string
            requestUri += "?" + resourceRef.getQuery();
        }
        return requestUri;
    }
}
