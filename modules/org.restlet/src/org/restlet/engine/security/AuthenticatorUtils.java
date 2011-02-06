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

package org.restlet.engine.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Digest;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.ChallengeRequestReader;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.ParameterReader;
import org.restlet.security.Guard;
import org.restlet.util.Series;

/**
 * Authentication utilities.
 * 
 * @author Jerome Louvel
 * @author Ray Waldin (ray@waldin.net)
 */
@SuppressWarnings("deprecation")
public class AuthenticatorUtils {

    /**
     * Indicates if any of the objects is null.
     * 
     * @param objects
     *            The objects to test.
     * @return True if any of the objects is null.
     */
    public static boolean anyNull(Object... objects) {
        for (final Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates if the request is properly authenticated. By default, this
     * delegates credentials checking to checkSecret().
     * 
     * @param request
     *            The request to authenticate.
     * @param guard
     *            The associated guard to callback.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see Guard#checkSecret(Request, String, char[])
     * @deprecated See new org.restlet.security package.
     */
    @Deprecated
    public static int authenticate(Request request, Guard guard) {
        int result = Guard.AUTHENTICATION_MISSING;

        if (guard.getScheme() != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            final ChallengeResponse cr = request.getChallengeResponse();

            if (cr != null) {
                if (guard.getScheme().equals(cr.getScheme())) {
                    final AuthenticatorHelper helper = Engine.getInstance()
                            .findHelper(cr.getScheme(), false, true);

                    if (helper != null) {
                        result = helper.authenticate(cr, request, guard);
                    } else {
                        throw new IllegalArgumentException("Challenge scheme "
                                + guard.getScheme()
                                + " not supported by the Restlet engine.");
                    }
                } else {
                    // The challenge schemes are incompatible, we need to
                    // challenge the client
                }
            } else {
                // No challenge response found, we need to challenge the client
            }
        }

        if (request.getChallengeResponse() != null) {
            // Update the challenge response accordingly
            request.getChallengeResponse().setAuthenticated(
                    result == Guard.AUTHENTICATION_VALID);
        }

        // Update the client info accordingly
        request.getClientInfo().setAuthenticated(
                result == Guard.AUTHENTICATION_VALID);

        return result;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *            The response to update.
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     * @param guard
     *            The associated guard to callback.
     * @deprecated See new org.restlet.security package.
     */
    @Deprecated
    public static void challenge(Response response, boolean stale, Guard guard) {
        final AuthenticatorHelper helper = Engine.getInstance().findHelper(
                guard.getScheme(), false, true);

        if (helper != null) {
            helper.challenge(response, stale, guard);
        } else {
            throw new IllegalArgumentException("Challenge scheme "
                    + guard.getScheme()
                    + " not supported by the Restlet engine.");
        }
    }

    /**
     * Formats an authentication information as a HTTP header value. The header
     * is {@link HeaderConstants#HEADER_AUTHENTICATION_INFO}.
     * 
     * @param info
     *            The authentication information to format.
     * @return The {@link HeaderConstants#HEADER_AUTHENTICATION_INFO} header
     *         value.
     */
    public static String formatAuthenticationInfo(AuthenticationInfo info) {
        ChallengeWriter cw = new ChallengeWriter();
        boolean firstParameter = true;

        if (info != null) {
            if (info.getNextServerNonce() != null
                    && info.getNextServerNonce().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendQuotedChallengeParameter("nextnonce", info
                        .getNextServerNonce());
                firstParameter = false;
            }

            if (info.getQuality() != null && info.getQuality().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendChallengeParameter("qop", info.getQuality());
                firstParameter = false;

                if (info.getNonceCount() > 0) {
                    cw.appendChallengeParameter("nc", formatNonceCount(info
                            .getNonceCount()));
                }
            }

            if (info.getResponseDigest() != null
                    && info.getResponseDigest().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendQuotedChallengeParameter("rspauth", info
                        .getResponseDigest());
                firstParameter = false;
            }

            if (info.getClientNonce() != null
                    && info.getClientNonce().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendChallengeParameter("cnonce", info.getClientNonce());
                firstParameter = false;
            }
        }

        return cw.toString();
    }

    /**
     * Formats a given nonce count as a HTTP header value. The header is
     * {@link HeaderConstants#HEADER_AUTHENTICATION_INFO}.
     * 
     * @param nonceCount
     *            The given nonce count.
     * @return The formatted value of the given nonce count.
     */
    public static String formatNonceCount(int nonceCount) {
        StringBuilder result = new StringBuilder(Integer
                .toHexString(nonceCount));
        while (result.length() < 8) {
            result.insert(0, '0');
        }

        return result.toString();
    }

    /**
     * Formats a challenge request as a HTTP header value. The header is
     * {@link HeaderConstants#HEADER_WWW_AUTHENTICATE}.
     * 
     * @param challenge
     *            The challenge request to format.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current response HTTP headers.
     * @return The {@link HeaderConstants#HEADER_WWW_AUTHENTICATE} header value.
     */
    public static String formatRequest(ChallengeRequest challenge,
            Response response, Series<Parameter> httpHeaders) {
        String result = null;

        if (challenge != null) {
            AuthenticatorHelper helper = Engine.getInstance().findHelper(
                    challenge.getScheme(), false, true);

            if (helper != null) {
                try {
                    result = helper.formatRequest(challenge, response,
                            httpHeaders);
                } catch (IOException e) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Unable to format the challenge request: "
                                    + challenge, e);
                }
            } else {
                result = "?";
                Context.getCurrentLogger().warning(
                        "Challenge scheme " + challenge.getScheme()
                                + " not supported by the Restlet engine.");
            }
        }

        return result;
    }

    /**
     * Formats a challenge response as a HTTP header value. The header is
     * {@link HeaderConstants#HEADER_AUTHORIZATION}.
     * 
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     * @return The {@link HeaderConstants#HEADER_AUTHORIZATION} header value.
     * @throws IOException
     */
    public static String formatResponse(ChallengeResponse challenge,
            Request request, Series<Parameter> httpHeaders) {
        String result = null;
        AuthenticatorHelper helper = Engine.getInstance().findHelper(
                challenge.getScheme(), true, false);

        if (helper != null) {
            result = helper.formatResponse(challenge, request, httpHeaders);
        } else {
            result = "?";
            Context.getCurrentLogger().warning(
                    "Challenge scheme " + challenge.getScheme()
                            + " not supported by the Restlet engine.");
        }

        return result;
    }

    /**
     * Parses the "Authentication-Info" header.
     * 
     * @param header
     *            The header value to parse.
     * @return The equivalent {@link AuthenticationInfo} instance.
     * @throws IOException
     */
    public static AuthenticationInfo parseAuthenticationInfo(String header) {
        AuthenticationInfo result = null;
        ParameterReader hr = new ParameterReader(header);

        try {
            String nextNonce = null;
            String qop = null;
            String responseAuth = null;
            String cnonce = null;
            int nonceCount = 0;
            Parameter param = hr.readValue();

            while (param != null) {
                try {
                    if ("nextnonce".equals(param.getName())) {
                        nextNonce = param.getValue();
                    } else if ("qop".equals(param.getName())) {
                        qop = param.getValue();
                    } else if ("rspauth".equals(param.getName())) {
                        responseAuth = param.getValue();
                    } else if ("cnonce".equals(param.getName())) {
                        cnonce = param.getValue();
                    } else if ("nc".equals(param.getName())) {
                        nonceCount = Integer.parseInt(param.getValue(), 16);
                    }

                    if (hr.skipValueSeparator()) {
                        param = hr.readValue();
                    } else {
                        param = null;
                    }
                } catch (Exception e) {
                    Context
                            .getCurrentLogger()
                            .log(
                                    Level.WARNING,
                                    "Unable to parse the authentication info header parameter",
                                    e);
                }
            }

            result = new AuthenticationInfo(nextNonce, nonceCount, cnonce, qop,
                    responseAuth);
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the authentication info header: "
                                    + header, e);
        }

        return result;
    }

    /**
     * Parses an authenticate header into a list of challenge request. The
     * header is {@link HeaderConstants#HEADER_WWW_AUTHENTICATE}.
     * 
     * @param header
     *            The HTTP header value to parse.
     * @param httpHeaders
     *            The current response HTTP headers.
     * @return The list of parsed challenge request.
     */
    public static List<ChallengeRequest> parseRequest(Response response,
            String header, Series<Parameter> httpHeaders) {
        List<ChallengeRequest> result = new ArrayList<ChallengeRequest>();

        if (header != null) {
            result = new ChallengeRequestReader(header).readValues();
            for (ChallengeRequest cr : result) {
                // Give a chance to the authenticator helper to do further
                // parsing
                AuthenticatorHelper helper = Engine.getInstance().findHelper(
                        cr.getScheme(), true, false);

                if (helper != null) {
                    helper.parseRequest(cr, response, httpHeaders);
                } else {
                    Context.getCurrentLogger().warning(
                            "Couldn't find any helper support the "
                                    + cr.getScheme() + " challenge scheme.");
                }
            }
        }

        return result;
    }

    /**
     * Parses an authorization header into a challenge response. The header is
     * {@link HeaderConstants#HEADER_AUTHORIZATION}.
     * 
     * @param request
     *            The parent request.
     * @param header
     *            The authorization header.
     * @param httpHeaders
     *            The current request HTTP headers.
     * @return The parsed challenge response.
     */
    public static ChallengeResponse parseResponse(Request request,
            String header, Series<Parameter> httpHeaders) {
        ChallengeResponse result = null;

        if (header != null) {
            int space = header.indexOf(' ');

            if (space != -1) {
                String scheme = header.substring(0, space);
                String rawValue = header.substring(space + 1);

                result = new ChallengeResponse(new ChallengeScheme("HTTP_"
                        + scheme, scheme));
                result.setRawValue(rawValue);
            }
        }

        if (result != null) {
            // Give a chance to the authenticator helper to do further parsing
            AuthenticatorHelper helper = Engine.getInstance().findHelper(
                    result.getScheme(), true, false);

            if (helper != null) {
                helper.parseResponse(result, request, httpHeaders);
            } else {
                Context.getCurrentLogger().warning(
                        "Couldn't find any helper support the "
                                + result.getScheme() + " challenge scheme.");
            }
        }

        return result;

    }

    /**
     * Updates a ChallengeResponse object according to given request and
     * response.
     * 
     * @param challengeResponse
     *            The challengeResponse to update.
     * @param request
     *            The request.
     * @param response
     *            The response.
     */
    public static void update(ChallengeResponse challengeResponse,
            Request request, Response response) {
        ChallengeRequest challengeRequest = null;
        for (ChallengeRequest c : response.getChallengeRequests()) {
            if (challengeResponse.getScheme().equals(c.getScheme())) {
                challengeRequest = c;
                break;
            }
        }

        String realm = null;
        String nonce = null;
        if (challengeRequest != null) {
            realm = challengeRequest.getRealm();
            nonce = challengeRequest.getServerNonce();
            challengeResponse.setOpaque(challengeRequest.getOpaque());
        }
        challengeResponse.setRealm(realm);
        challengeResponse.setServerNonce(nonce);

        challengeResponse.setDigestRef(new Reference(request.getResourceRef()
                .getPath()));
    }

    /**
     * Updates a ChallengeResponse object according to given request and
     * response and compute a new secret according to the response sent by the
     * server.
     * 
     * @param challengeResponse
     *            The challengeResponse to update.
     * @param request
     *            The request if available.
     * @param response
     *            The response if available.
     * @param identifier
     *            The identifier.
     * @param baseSecret
     *            The base secret used to compute the secret.
     * @param baseSecretAlgorithm
     *            The digest algorithm of the base secret (@see {@link Digest}
     *            class).
     */
    public static void update(ChallengeResponse challengeResponse,
            Request request, Response response, String identifier,
            char[] baseSecret, String baseSecretAlgorithm) {
        update(challengeResponse, request, response);

        // Compute the new secret.
        final AuthenticatorHelper helper = Engine.getInstance().findHelper(
                challengeResponse.getScheme(), false, true);
        challengeResponse
                .setSecret(helper.formatSecret(challengeResponse, request,
                        response, identifier, baseSecret, baseSecretAlgorithm));
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AuthenticatorUtils() {
    }

}
