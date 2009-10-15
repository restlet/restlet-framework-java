/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.security;

import java.io.IOException;
import java.util.Collection;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.util.Base64;
import org.restlet.security.Guard;
import org.restlet.util.Series;

/**
 * Implements the HTTP DIGEST authentication.
 * 
 * @author Jerome Louvel
 */
@SuppressWarnings("deprecation")
public class HttpDigestHelper extends AuthenticatorHelper {

    /**
     * Add parameters to the given challenge request.
     * 
     * @param challengeRequest
     *            The challenge request to update.
     * @param domainUris
     *            The URIs that define the protection domains.
     * @param serverKey
     *            The secret key known only to server.
     * @param stale
     *            Indicates if the given authentication was stale.
     */
    public static void addParameters(ChallengeRequest challengeRequest,
            Collection<String> domainUris, String serverKey, boolean stale) {
        Series<Parameter> parameters = challengeRequest.getParameters();
        StringBuffer domain = new StringBuffer();

        for (String baseUri : domainUris) {
            domain.append(baseUri).append(' ');
        }

        if (domain.length() > 0) {
            domain.delete(domain.length() - 1, domain.length());
            parameters.add("domain", domain.toString());
        }

        parameters.add("nonce", makeNonce(serverKey));

        if (stale) {
            // indicate stale nonce was found in challenge response
            parameters.add("stale", "true");
        }
    }

    /**
     * Return the hashed secret.
     * 
     * @param identifier
     *            The user identifier to hash.
     * @param guard
     *            The associated guard to callback.
     * 
     * @return A hash of the user name, realm, and password, specified as A1 in
     *         section 3.2.2.2 of RFC2617, or null if the identifier has no
     *         corresponding secret.
     */
    @Deprecated
    private static String getHashedSecret(String identifier, Guard guard) {
        char[] secret = guard.getSecretResolver().resolve(identifier);

        if (secret != null) {
            return DigestUtils.toHttpDigest(identifier, secret, guard
                    .getRealm());
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
     *            The nonce value.
     * @param secretKey
     *            The same secret value that was inserted into the nonce when it
     *            was generated
     * @param lifespan
     *            The nonce lifespan in milliseconds.
     * @return True if the nonce was generated less than lifespan milliseconds
     *         ago, false otherwise.
     * @throws Exception
     *             If the nonce does not match the specified secretKey, or if it
     *             can't be parsed
     */
    public static boolean isNonceValid(String nonce, String secretKey,
            long lifespan) throws Exception {
        try {
            String decodedNonce = new String(Base64.decode(nonce));
            long nonceTimeMS = Long.parseLong(decodedNonce.substring(0,
                    decodedNonce.indexOf(':')));

            if (decodedNonce.equals(nonceTimeMS + ":"
                    + DigestUtils.toMd5(nonceTimeMS + ":" + secretKey))) {
                // Valid with regard to the secretKey, now check lifespan
                return lifespan > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new Exception("Error detected parsing nonce: " + e);
        }

        throw new Exception("The nonce does not match secretKey");
    }

    /**
     * Generates a nonce as recommended in section 3.2.1 of RFC-2617, but
     * without the ETag field. The format is: <code><pre>
     * Base64.encodeBytes(currentTimeMS + &quot;:&quot;
     *         + md5String(currentTimeMS + &quot;:&quot; + secretKey))
     * </pre></code>
     * 
     * @param secretKey
     *            a secret value known only to the creator of the nonce. It's
     *            inserted into the nonce, and can be used later to validate the
     *            nonce.
     */
    public static String makeNonce(String secretKey) {
        final long currentTimeMS = System.currentTimeMillis();
        return Base64.encode((currentTimeMS + ":" + DigestUtils
                .toMd5(currentTimeMS + ":" + secretKey)).getBytes(), true);
    }

    /**
     * Constructor.
     */
    public HttpDigestHelper() {
        super(ChallengeScheme.HTTP_DIGEST, true, true);
    }

    @Deprecated
    @Override
    public int authenticate(ChallengeResponse cr, Request request, Guard guard) {
        Series<Parameter> parameters = cr.getParameters();
        String username = cr.getIdentifier();
        String response = new String(cr.getSecret());
        String nonce = parameters.getFirstValue("nonce");
        String uri = parameters.getFirstValue("uri");
        String qop = parameters.getFirstValue("qop");
        String nc = parameters.getFirstValue("nc");
        String cnonce = parameters.getFirstValue("cnonce");

        try {
            if (!isNonceValid(nonce, guard.getServerKey(), guard
                    .getNonceLifespan())) {
                // Nonce expired, send challenge request with
                // stale=true
                return Guard.AUTHENTICATION_STALE;
            }
        } catch (Exception ce) {
            // Invalid nonce, probably doesn't match serverKey
            return Guard.AUTHENTICATION_INVALID;
        }

        if (!AuthenticatorUtils.anyNull(username, nonce, response, uri)) {
            Reference resourceRef = request.getResourceRef();
            String requestUri = resourceRef.getPath();

            if ((resourceRef.getQuery() != null) && (uri.indexOf('?') > -1)) {
                // IE neglects to include the query string, so
                // the workaround is to leave it off
                // unless both the calculated URI and the
                // specified URI contain a query string
                requestUri += "?" + resourceRef.getQuery();
            }

            if (uri.equals(requestUri)) {
                String a1 = getHashedSecret(username, guard);

                if (a1 != null) {
                    String a2 = DigestUtils.toMd5(request.getMethod() + ":"
                            + requestUri);
                    StringBuffer expectedResponse = new StringBuffer(a1)
                            .append(':').append(nonce);

                    if (!AuthenticatorUtils.anyNull(qop, cnonce, nc)) {
                        expectedResponse.append(':').append(nc).append(':')
                                .append(cnonce).append(':').append(qop);
                    }

                    expectedResponse.append(':').append(a2);

                    if (response.equals(DigestUtils.toMd5(expectedResponse
                            .toString()))) {
                        return Guard.AUTHENTICATION_VALID;
                    }
                }
            }

            return Guard.AUTHENTICATION_INVALID;
        }

        return Guard.AUTHENTICATION_MISSING;
    }

    @Deprecated
    @Override
    public void challenge(Response response, boolean stale, Guard guard) {
        super.challenge(response, stale, guard);

        // This is temporary, pending Guard re-factoring. We still assume
        // there is only one challenge scheme, that of the Guard.
        ChallengeRequest mainChallengeRequest = null;

        for (ChallengeRequest challengeRequest : response
                .getChallengeRequests()) {
            if (challengeRequest.getScheme().equals(guard.getScheme())) {
                mainChallengeRequest = challengeRequest;
                break;
            }
        }

        addParameters(mainChallengeRequest, guard.getDomainUris(), guard
                .getServerKey(), stale);
    }

    @Override
    public void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        Series<Parameter> params = challenge.getParameters();

        appendParameter(sb, "username", challenge.getIdentifier());
        appendParameter(sb, "response", new String(challenge.getSecret()));

        for (Parameter param : params) {
            appendParameter(sb, param.getName(), param.getValue());
        }

        // Remove the trailing comma
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    /**
     * Appends a HTTP DIGEST parameter to the credentials string.
     * 
     * @param credentials
     *            The credentials to append.
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @throws IOException
     */
    private static void appendParameter(StringBuilder credentials, String name,
            String value) {
        credentials.append(name).append('=');

        if (name.equals("qop") || name.equals("algorithm") || name.equals("nc")) {
            // These values are left unquoted as per RC2617
            credentials.append(value).append(",");
        } else {
            credentials.append('"').append(value).append('"').append(",");
        }
    }

    @Override
    public void formatParameters(StringBuilder sb,
            Series<Parameter> parameters, ChallengeRequest challenge) {
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
    public void parseResponse(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        Series<Parameter> parameters = challenge.getParameters();
        AuthenticatorUtils.parseParameters(challenge.getCredentials(),
                parameters);

        // Extract the identifier and secret parameters and remove them
        String username = parameters.getFirstValue("username");
        parameters.removeAll("username");
        String response = parameters.getFirstValue("response");
        parameters.removeAll("response");

        if ((username != null) && (response != null)) {
            challenge.setIdentifier(username);
            challenge.setSecret(response);
        } else {
            // Log the blocking
            getLogger().warning(
                    "Invalid credentials given by client with IP: "
                            + ((request != null) ? request.getClientInfo()
                                    .getAddress() : "?"));
        }
    }

}
