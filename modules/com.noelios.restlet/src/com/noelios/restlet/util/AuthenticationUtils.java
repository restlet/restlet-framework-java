/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.CredentialException;

import org.restlet.Guard;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * Authentication utilities.
 * 
 * @author Ray Waldin (ray@waldin.net)
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AuthenticationUtils {

    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this respect.
     * Pattern is immutable and thread-safe so reuse one static instance.
     */
    private static final Pattern directivesPattern = Pattern
            .compile("([^=]+)=\"?([^\",]+)(?:\"\\s*)?,?\\s*");

    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this respect.
     * Pattern is immutable and thread-safe so reuse one static instance.
     */
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    /**
     * Parsed the parameters of a credientials string and updates the series of
     * parameters.
     * 
     * @param credentials
     *                The credentials string to parse.
     * @param parameters
     *                The series to update.
     */
    public static void parseParameters(String credentials,
            Series<Parameter> parameters) {
        Matcher matcher = directivesPattern.matcher(credentials);

        while (matcher.find() && matcher.groupCount() == 2) {
            parameters.add(matcher.group(1), matcher.group(2));
        }
    }

    /**
     * Returns the MD5 digest of target string. Target is decoded to bytes using
     * the named charset. The returned hexidecimal String always contains 32
     * lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *                The string to encode.
     * @param charsetName
     *                The character set.
     * @return The MD5 digest of the target string.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String toMd5(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(
                    target.getBytes(charsetName));
            char[] md5Chars = new char[32];
            int i = 0;
            for (byte b : md5) {
                md5Chars[i++] = HEXDIGITS[(b >> 4) & 0xF];
                md5Chars[i++] = HEXDIGITS[b & 0xF];
            }
            return new String(md5Chars);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "No MD5 algorithm, unable to compute MD5");
        }
    }

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset. The returned hexidecimal String always
     * contains 32 lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *                The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toMd5(String target) {
        try {
            return toMd5(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // unlikely, US-ASCII comes with every JVM
            throw new RuntimeException(
                    "US-ASCII is an unsupported encoding, unable to compute MD5");
        }
    }

    /**
     * generates a nonce as recommended in section 3.2.1 of RFC-2617, but
     * without the ETag field. The format is: <code><pre>
     * Base64.encodeBytes(currentTimeMS + &quot;:&quot;
     *         + md5String(currentTimeMS + &quot;:&quot; + secretKey))
     * </pre></code>
     * 
     * @param secretKey
     *                a secret value known only to the creator of the nonce.
     *                It's inserted into the nonce, and can be used later to
     *                validate the nonce.
     */
    public static String makeNonce(String secretKey) {
        long currentTimeMS = System.currentTimeMillis();
        return Base64.encode((currentTimeMS + ":" + toMd5(currentTimeMS + ":"
                + secretKey)).getBytes(), true);
    }

    /**
     * checks whether the specified nonce is valid with respect to the specified
     * secretKey, and further confirms that the nonce was generated less than
     * lifespanMillis milliseconds ago
     * 
     * @param nonce
     * @param secretKey
     *                the same secret value that was inserted into the nonce
     *                when it was generated
     * @param lifespanMS
     *                nonce lifespace in milliseconds
     * @return true if the nonce was generated less than lifespanMS milliseconds
     *         ago, false otherwise
     * @throws CredentialException
     *                 if the nonce does not match the specified secretKey, or
     *                 if it can't be parsed
     */
    public static boolean isNonceValid(String nonce, String secretKey,
            long lifespanMS) throws CredentialException {
        try {
            String decodedNonce = new String(Base64.decode(nonce));
            long nonceTimeMS = Long.parseLong(decodedNonce.substring(0,
                    decodedNonce.indexOf(':')));
            if (decodedNonce.equals(nonceTimeMS + ":"
                    + toMd5(nonceTimeMS + ":" + secretKey))) {
                // valid wrt secretKey, now check lifespan
                return lifespanMS > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new CredentialException("error parsing nonce: " + e);
        }
        throw new CredentialException("nonce does not match secretKey");
    }

    public static boolean anyNull(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates if the call is properly authenticated. By default, this
     * delegates credential checking to checkSecret().
     * 
     * @param request
     *                The request to authenticate.
     * @param guard
     *                The associated guard to callback.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see #checkSecret(String, char[])
     */
    public static int authenticate(Request request, Guard guard) {
        int result = Guard.AUTHENTICATION_MISSING;

        if (guard.getScheme() != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            ChallengeResponse cr = request.getChallengeResponse();

            if (cr != null) {
                if (guard.getScheme().equals(cr.getScheme())) {
                    if (guard.getScheme().equals(ChallengeScheme.HTTP_BASIC)) {
                        // The challenge schemes are compatible
                        String identifier = request.getChallengeResponse()
                                .getIdentifier();
                        char[] secret = request.getChallengeResponse()
                                .getSecret();

                        // Check the credentials
                        if ((identifier != null) && (secret != null)) {
                            result = guard.checkSecret(request, identifier,
                                    secret) ? Guard.AUTHENTICATION_VALID
                                    : Guard.AUTHENTICATION_INVALID;
                        }
                    } else if (guard.getScheme().equals(
                            ChallengeScheme.HTTP_DIGEST)) {
                        Series<Parameter> credentials = cr.getParameters();
                        String username = credentials.getFirstValue("username");
                        String nonce = credentials.getFirstValue("nonce");
                        String response = credentials.getFirstValue("response");
                        String uri = credentials.getFirstValue("uri");
                        String qop = credentials.getFirstValue("qop");
                        String nc = credentials.getFirstValue("nc");
                        String cnonce = credentials.getFirstValue("cnonce");

                        try {
                            if (!isNonceValid(nonce, guard.getServerKey(),
                                    guard.getNonceLifespan())) {
                                // Nonce expired, send challenge request with
                                // stale=true
                                return Guard.AUTHENTICATION_STALE;
                            }
                        } catch (CredentialException ce) {
                            // Invalid nonce, probably doesn't match serverKey
                            return Guard.AUTHENTICATION_INVALID;
                        }

                        if (!AuthenticationUtils.anyNull(username, nonce,
                                response, uri)) {
                            Reference resourceRef = request.getResourceRef();
                            String requestUri = resourceRef.getPath();
                            if (resourceRef.getQuery() != null
                                    && uri.indexOf('?') > -1) {
                                // IE neglects to include the query string, so
                                // the workaround is to leave it off
                                // unless both the calculated uri and the
                                // specified uri contain a query string
                                requestUri += "?" + resourceRef.getQuery();
                            }
                            if (uri.equals(requestUri)) {
                                String a1 = getHashedSecret(username, guard);
                                String a2 = Engine.getInstance().toMd5(
                                        request.getMethod() + ":" + requestUri);

                                StringBuffer expectedResponse = new StringBuffer(
                                        a1).append(':').append(nonce);
                                if (!AuthenticationUtils.anyNull(qop, cnonce,
                                        nc)) {
                                    expectedResponse.append(':').append(nc)
                                            .append(':').append(cnonce).append(
                                                    ':').append(qop);
                                }
                                expectedResponse.append(':').append(a2);

                                if (response.equals(Engine.getInstance().toMd5(
                                        expectedResponse.toString()))) {
                                    return Guard.AUTHENTICATION_VALID;
                                }
                            }

                            return Guard.AUTHENTICATION_INVALID;
                        }

                        return Guard.AUTHENTICATION_MISSING;
                    }
                } else {
                    // The challenge schemes are incompatible, we need to
                    // challenge the client
                }
            } else {
                // No challenge response found, we need to challenge the client
            }
        }

        return result;
    }

    /**
     * Return the hashed secret.
     * 
     * @param identifier
     *                The user identifier to hash.
     * @param guard
     *                The associated guard to callback.
     * 
     * @return a hash of the username, realm, and password, specified as A1 in
     *         section 3.2.2.2 of RFC2617
     */
    private static String getHashedSecret(String identifier, Guard guard) {
        return Engine.getInstance().toMd5(
                identifier + ":" + guard.getRealm() + ":"
                        + new String(guard.findSecret(identifier)));
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *                The response to update.
     * @param stale
     *                Indicates if the new challenge is due to a stale response.
     * @param guard
     *                The associated guard to callback.
     */
    public static void challenge(Response response, boolean stale, Guard guard) {
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.setChallengeRequest(new ChallengeRequest(guard.getScheme(),
                guard.getRealm()));

        if (guard.getScheme().equals(ChallengeScheme.HTTP_DIGEST)) {
            if (stale) {
                // Stale nonce, repeat auth request with fresh nonce
                response.getAttributes().put("stale", "true");
            }

            Series<Parameter> parameters = response.getChallengeRequest()
                    .getParameters();
            StringBuffer domain = new StringBuffer();

            for (String baseUri : guard.getDomainUris()) {
                domain.append(baseUri).append(' ');
            }

            if (domain.length() > 0) {
                domain.delete(domain.length() - 1, domain.length());
                parameters.add("domain", domain.toString());
            }

            parameters.add("nonce", makeNonce(guard.getServerKey()));

            if (response.getAttributes().containsKey("stale")) {
                // indicate stale nonce was found in challenge response
                parameters.add("stale", "true");
            }
        }
    }

}
