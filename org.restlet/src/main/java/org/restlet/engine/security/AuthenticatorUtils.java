/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.header.ChallengeRequestReader;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderReader;
import org.restlet.util.Series;

/**
 * Authentication utilities.
 *
 * @author Jerome Louvel
 * @author Ray Waldin (ray@waldin.net)
 */
public class AuthenticatorUtils {

    /**
     * Indicates if any of the objects is null.
     *
     * @param objects The objects to test.
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
     * Formats authentication information as an HTTP header value. The header is {@link
     * HeaderConstants#HEADER_AUTHENTICATION_INFO}.
     *
     * @param info The authentication information to format.
     * @return The {@link HeaderConstants#HEADER_AUTHENTICATION_INFO} header value.
     */
    public static String formatAuthenticationInfo(AuthenticationInfo info) {
        ChallengeWriter cw = new ChallengeWriter();
        boolean firstParameter = true;

        if (info == null) {
            return cw.toString();
        }

        if (info.getNextServerNonce() != null && !info.getNextServerNonce().isEmpty()) {
            cw.setFirstChallengeParameter(firstParameter);
            cw.appendQuotedChallengeParameter("nextnonce", info.getNextServerNonce());
            firstParameter = false;
        }

        if (info.getQuality() != null && !info.getQuality().isEmpty()) {
            cw.setFirstChallengeParameter(firstParameter);
            cw.appendChallengeParameter("qop", info.getQuality());
            firstParameter = false;

            if (info.getNonceCount() > 0) {
                cw.appendChallengeParameter("nc", formatNonceCount(info.getNonceCount()));
            }
        }

        if (info.getResponseDigest() != null && !info.getResponseDigest().isEmpty()) {
            cw.setFirstChallengeParameter(firstParameter);
            cw.appendQuotedChallengeParameter("rspauth", info.getResponseDigest());
            firstParameter = false;
        }

        if (info.getClientNonce() != null && !info.getClientNonce().isEmpty()) {
            cw.setFirstChallengeParameter(firstParameter);
            cw.appendChallengeParameter("cnonce", info.getClientNonce());
        }

        return cw.toString();
    }

    /**
     * Formats a given nonce count as an HTTP header value. The header is {@link
     * HeaderConstants#HEADER_AUTHENTICATION_INFO}.
     *
     * @param nonceCount The given nonce count.
     * @return The formatted value of the given nonce count.
     */
    public static String formatNonceCount(int nonceCount) {
        StringBuilder result = new StringBuilder(Integer.toHexString(nonceCount));
        while (result.length() < 8) {
            result.insert(0, '0');
        }

        return result.toString();
    }

    /**
     * Formats a challenge request as an HTTP header value. The header is {@link
     * HeaderConstants#HEADER_WWW_AUTHENTICATE} . The default implementation relies on {@link
     * AuthenticatorHelper#formatRequest(ChallengeWriter, ChallengeRequest, Response, Series)} to
     * append all parameters from {@link ChallengeRequest#getParameters()}.
     *
     * @param challenge The challenge request to format.
     * @param response The parent response.
     * @param httpHeaders The current response HTTP headers.
     * @return The {@link HeaderConstants#HEADER_WWW_AUTHENTICATE} header value.
     */
    public static String formatRequest(
            ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
        String result = null;

        if (challenge == null) {
            Context.getCurrentLogger().warning("No challenge response to format.");
        } else if (challenge.getScheme() == null) {
            Context.getCurrentLogger().warning("A challenge response must have a scheme defined.");
        } else if (challenge.getScheme().getTechnicalName() == null) {
            Context.getCurrentLogger()
                    .warning("A challenge scheme must have a technical name defined.");
        } else {
            ChallengeWriter cw = new ChallengeWriter();
            cw.append(challenge.getScheme().getTechnicalName()).appendSpace();
            int cwInitialLength = cw.getBuffer().length();

            if (challenge.getRawValue() != null) {
                cw.append(challenge.getRawValue());
            } else {
                AuthenticatorHelper helper = findHelper(challenge.getScheme(), false, true);

                if (helper != null) {
                    helper.formatRequest(cw, challenge, response, httpHeaders);
                }
            }

            result = (cw.getBuffer().length() > cwInitialLength) ? cw.toString() : null;
        }

        return result;
    }

    /**
     * Formats a challenge response as an HTTP header value. The header is {@link
     * HeaderConstants#HEADER_AUTHORIZATION}. The default implementation relies on {@link
     * AuthenticatorHelper#formatResponse(ChallengeWriter, ChallengeResponse, Request, Series)}
     * unless some custom credentials are provided via
     *
     * @param challenge The challenge response to format.
     * @param request The parent request.
     * @param httpHeaders The current request HTTP headers.
     * @return The {@link HeaderConstants#HEADER_AUTHORIZATION} header value.
     * @link ChallengeResponse#getCredentials()}.
     */
    public static String formatResponse(
            ChallengeResponse challenge, Request request, Series<Header> httpHeaders) {
        String result = null;

        if (challenge == null) {
            Context.getCurrentLogger().warning("No challenge response to format.");
        } else if (challenge.getScheme() == null) {
            Context.getCurrentLogger().warning("A challenge response must have a scheme defined.");
        } else if (challenge.getScheme().getTechnicalName() == null) {
            Context.getCurrentLogger()
                    .warning("A challenge scheme must have a technical name defined.");
        } else {
            ChallengeWriter cw = new ChallengeWriter();
            cw.append(challenge.getScheme().getTechnicalName()).appendSpace();
            int cwInitialLength = cw.getBuffer().length();

            if (challenge.getRawValue() != null) {
                cw.append(challenge.getRawValue());
            } else {
                AuthenticatorHelper helper = findHelper(challenge.getScheme(), true, false);

                if (helper != null) {
                    helper.formatResponse(cw, challenge, request, httpHeaders);
                }
            }

            result = (cw.getBuffer().length() > cwInitialLength) ? cw.toString() : null;
        }

        return result;
    }

    /**
     * Parses the "Authentication-Info" header.
     *
     * @param header The header value to parse.
     * @return The equivalent {@link AuthenticationInfo} instance.
     */
    public static AuthenticationInfo parseAuthenticationInfo(final String header) {
        HeaderReader<Parameter> hr = new HeaderReader<>(header);

        Parameter param;
        try {
            param = hr.readParameter();
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () -> "Unable to parse the authentication info header: " + header);
            return null;
        }

        String nextNonce = null;
        String qop = null;
        String responseAuth = null;
        String cnonce = null;
        String nonceCountAsString = null;

        try {
            while (param != null) {
                if ("nextnonce".equals(param.getName())) {
                    nextNonce = param.getValue();
                } else if ("qop".equals(param.getName())) {
                    qop = param.getValue();
                } else if ("rspauth".equals(param.getName())) {
                    responseAuth = param.getValue();
                } else if ("cnonce".equals(param.getName())) {
                    cnonce = param.getValue();
                } else if ("nc".equals(param.getName())) {
                    nonceCountAsString = param.getValue();
                }

                param =
                        hr.skipValueSeparator()
                                ? hr.readParameter() // next parameter
                                : null; // end of header
            }
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the authentication info header parameter",
                            e);
        }

        int nonceCount = getNonceCount(nonceCountAsString);

        return new AuthenticationInfo(nextNonce, nonceCount, cnonce, qop, responseAuth);
    }

    private static int getNonceCount(final String nonceCountAsString) {
        if (nonceCountAsString == null) {
            return 0;
        }
        try {
            return Integer.parseInt(nonceCountAsString, 16);
        } catch (NumberFormatException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () -> "Unable to parse the nonce count value: " + nonceCountAsString);
        }
        return 0;
    }

    /**
     * Parses an WWW-Authenticate header into a list of challenge request. The header is {@link
     * HeaderConstants#HEADER_WWW_AUTHENTICATE}.
     *
     * @param header The HTTP header value to parse.
     * @param httpHeaders The current response HTTP headers.
     * @return The list of parsed challenge request.
     */
    public static List<ChallengeRequest> parseRequest(
            Response response, String header, Series<Header> httpHeaders) {
        List<ChallengeRequest> result = new ArrayList<>();

        if (header != null) {
            result = new ChallengeRequestReader(header).readValues();
            for (ChallengeRequest cr : result) {
                // Give a chance to the authenticator helper to do further parsing
                AuthenticatorHelper helper = findHelper(cr.getScheme(), true, false);

                if (helper != null) {
                    helper.parseRequest(cr, response, httpHeaders);
                }
            }
        }

        return result;
    }

    /**
     * Parses an authorization header into a challenge response. The header is {@link
     * HeaderConstants#HEADER_AUTHORIZATION}.
     *
     * @param request The parent request.
     * @param header The authorization header.
     * @param httpHeaders The current request HTTP headers.
     * @return The parsed challenge response.
     */
    public static ChallengeResponse parseResponse(
            Request request, String header, Series<Header> httpHeaders) {
        ChallengeResponse result = null;

        if (header != null) {
            int space = header.indexOf(' ');

            if (space != -1) {
                String scheme = header.substring(0, space);
                String rawValue = header.substring(space + 1);

                result = new ChallengeResponse(new ChallengeScheme("HTTP_" + scheme, scheme));
                result.setRawValue(rawValue);
            }
        }

        if (result != null) {
            // Give a chance to the authenticator helper to do further parsing
            AuthenticatorHelper helper = findHelper(result.getScheme(), true, false);

            if (helper != null) {
                helper.parseResponse(result, request, httpHeaders);
            }
        }

        return result;
    }

    /**
     * Updates a {@link ChallengeResponse} object according to given request and response.
     *
     * @param challengeResponse The challengeResponse to update.
     * @param request The request.
     * @param response The response.
     */
    public static void update(
            ChallengeResponse challengeResponse, Request request, Response response) {
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

        challengeResponse.setDigestRef(new Reference(request.getResourceRef().getPath()));
    }

    /**
     * Optionally updates the request with a challenge response before sending it. This is sometimes
     * useful for authentication schemes that aren't based on the Authorization header but instead
     * on URI query parameters or other headers. By default, it returns the resource URI reference
     * unchanged.
     *
     * @param resourceRef The resource URI reference to update.
     * @param challengeResponse The challenge response provided.
     * @param request The request to update.
     * @return The original URI reference if unchanged or a new one if updated.
     */
    public static Reference updateReference(
            Reference resourceRef, ChallengeResponse challengeResponse, Request request) {
        if (challengeResponse != null && challengeResponse.getRawValue() == null) {
            AuthenticatorHelper helper = findHelper(challengeResponse.getScheme(), true, false);

            if (helper != null) {
                resourceRef = helper.updateReference(resourceRef, challengeResponse, request);
            }
        }

        return resourceRef;
    }

    private static AuthenticatorHelper findHelper(
            final ChallengeScheme challengeScheme,
            final boolean clientSide,
            final boolean serverSide) {
        final AuthenticatorHelper helper =
                Engine.getInstance().findHelper(challengeScheme, clientSide, serverSide);
        if (helper == null) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Challenge scheme {0} not supported by the Restlet engine.",
                            challengeScheme);
        }
        return helper;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private AuthenticatorUtils() {}
}
