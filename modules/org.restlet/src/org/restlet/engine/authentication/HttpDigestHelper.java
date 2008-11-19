/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.authentication;

import javax.security.auth.login.CredentialException;

import org.restlet.Guard;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.SecurityUtils;
import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * Implements the HTTP DIGEST authentication.
 * 
 * @author Jerome Louvel
 */
public class HttpDigestHelper extends AuthenticationHelper {

    /**
     * Return the hashed secret.
     * 
     * @param identifier
     *            The user identifier to hash.
     * @param guard
     *            The associated guard to callback.
     * 
     * @return a hash of the username, realm, and password, specified as A1 in
     *         section 3.2.2.2 of RFC2617, or null if the identifier has no
     *         corresponding secret
     */
    private static String getHashedSecret(String identifier, Guard guard) {
        char[] result = guard.getSecretResolver().resolve(identifier);
        if (result != null) {
            return Engine.getInstance().toMd5(
                    identifier + ":" + guard.getRealm() + ":"
                            + new String(result));
        } else {
            // The given identifier is not known
            return null;
        }
    }

    /**
     * Checks whether the specified nonce is valid with respect to the specified
     * secretKey, and further confirms that the nonce was generated less than
     * lifespanMillis milliseconds ago
     * 
     * @param nonce
     * @param secretKey
     *            the same secret value that was inserted into the nonce when it
     *            was generated
     * @param lifespanMS
     *            nonce lifespace in milliseconds
     * @return true if the nonce was generated less than lifespanMS milliseconds
     *         ago, false otherwise
     * @throws CredentialException
     *             if the nonce does not match the specified secretKey, or if it
     *             can't be parsed
     */
    private static boolean isNonceValid(String nonce, String secretKey,
            long lifespanMS) throws CredentialException {
        try {
            final String decodedNonce = new String(Base64.decode(nonce));
            final long nonceTimeMS = Long.parseLong(decodedNonce.substring(0,
                    decodedNonce.indexOf(':')));
            if (decodedNonce.equals(nonceTimeMS + ":"
                    + SecurityUtils.toMd5(nonceTimeMS + ":" + secretKey))) {
                // valid wrt secretKey, now check lifespan
                return lifespanMS > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new CredentialException("error parsing nonce: " + e);
        }
        throw new CredentialException("nonce does not match secretKey");
    }

    /**
     * Constructor.
     */
    public HttpDigestHelper() {
        super(ChallengeScheme.HTTP_DIGEST, true, true);
    }

    @Override
    public int authenticate(ChallengeResponse cr, Request request, Guard guard) {
        final Series<Parameter> credentials = cr.getParameters();
        final String username = credentials.getFirstValue("username");
        final String nonce = credentials.getFirstValue("nonce");
        final String response = credentials.getFirstValue("response");
        final String uri = credentials.getFirstValue("uri");
        final String qop = credentials.getFirstValue("qop");
        final String nc = credentials.getFirstValue("nc");
        final String cnonce = credentials.getFirstValue("cnonce");

        try {
            if (!isNonceValid(nonce, guard.getServerKey(), guard
                    .getNonceLifespan())) {
                // Nonce expired, send challenge request with
                // stale=true
                return Guard.AUTHENTICATION_STALE;
            }
        } catch (CredentialException ce) {
            // Invalid nonce, probably doesn't match serverKey
            return Guard.AUTHENTICATION_INVALID;
        }

        if (!AuthenticationUtils.anyNull(username, nonce, response, uri)) {
            final Reference resourceRef = request.getResourceRef();
            String requestUri = resourceRef.getPath();
            if ((resourceRef.getQuery() != null) && (uri.indexOf('?') > -1)) {
                // IE neglects to include the query string, so
                // the workaround is to leave it off
                // unless both the calculated uri and the
                // specified uri contain a query string
                requestUri += "?" + resourceRef.getQuery();
            }
            if (uri.equals(requestUri)) {
                final String a1 = getHashedSecret(username, guard);
                if (a1 != null) {
                    final String a2 = Engine.getInstance().toMd5(
                            request.getMethod() + ":" + requestUri);

                    final StringBuffer expectedResponse = new StringBuffer(a1)
                            .append(':').append(nonce);
                    if (!AuthenticationUtils.anyNull(qop, cnonce, nc)) {
                        expectedResponse.append(':').append(nc).append(':')
                                .append(cnonce).append(':').append(qop);
                    }
                    expectedResponse.append(':').append(a2);

                    if (response.equals(Engine.getInstance().toMd5(
                            expectedResponse.toString()))) {
                        return Guard.AUTHENTICATION_VALID;
                    }
                }
            }

            return Guard.AUTHENTICATION_INVALID;
        }

        return Guard.AUTHENTICATION_MISSING;
    }

    @Override
    public void challenge(Response response, boolean stale, Guard guard) {
        super.challenge(response, stale, guard);

        if (stale) {
            // Stale nonce, repeat auth request with fresh nonce
            response.getAttributes().put("stale", "true");
        }

        // This is temporary, pending Guard re-factoring. We still assume
        // there is only one challenge scheme, that of the Guard.
        ChallengeRequest mainChallengeRequest = null;
        for (final ChallengeRequest challengeRequest : response
                .getChallengeRequests()) {
            if (challengeRequest.getScheme().equals(guard.getScheme())) {
                mainChallengeRequest = challengeRequest;
                break;
            }
        }
        final Series<Parameter> parameters = mainChallengeRequest
                .getParameters();
        final StringBuffer domain = new StringBuffer();

        for (final String baseUri : guard.getDomainUris()) {
            domain.append(baseUri).append(' ');
        }

        if (domain.length() > 0) {
            domain.delete(domain.length() - 1, domain.length());
            parameters.add("domain", domain.toString());
        }

        parameters.add("nonce", SecurityUtils.makeNonce(guard.getServerKey()));

        if (response.getAttributes().containsKey("stale")) {
            // indicate stale nonce was found in challenge response
            parameters.add("stale", "true");
        }
    }

    @Override
    public void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        final Series<Parameter> params = challenge.getParameters();

        for (final Parameter param : params) {
            sb.append(param.getName()).append('=');

            if (param.getName().equals("qop")
                    || param.getName().equals("algorithm")
                    || param.getName().equals("nc")) {
                // These values are left unquoted as per RC2617
                sb.append(param.getValue()).append(",");
            } else {
                sb.append('"').append(param.getValue()).append('"').append(",");
            }
        }

        if (!params.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    @Override
    public void formatParameters(StringBuilder sb,
            Series<Parameter> parameters, ChallengeRequest request) {
        sb.append(", domain=\"").append(parameters.getFirstValue("domain"))
                .append('"');
        sb.append(", qop=\"auth\"");
        sb.append(", algorithm=MD5"); // leave this value unquoted as per
        // RFC-2617
        sb.append(", nonce=\"").append(parameters.getFirstValue("nonce"))
                .append('"');

        if (parameters.getFirst("stale") != null) {
            sb.append(", stale=\"true\"");
        }
    }

    @Override
    public void parseResponse(ChallengeResponse cr, Request request) {
        AuthenticationUtils.parseParameters(cr.getCredentials(), cr
                .getParameters());
    }

}
