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
                cw.appendQuotedChallengeParameter("nextnonce",
                        info.getNextServerNonce());
                firstParameter = false;
            }

            if (info.getQuality() != null && info.getQuality().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendChallengeParameter("qop", info.getQuality());
                firstParameter = false;

                if (info.getNonceCount() > 0) {
                    cw.appendChallengeParameter("nc",
                            formatNonceCount(info.getNonceCount()));
                }
            }

            if (info.getResponseDigest() != null
                    && info.getResponseDigest().length() > 0) {
                cw.setFirstChallengeParameter(firstParameter);
                cw.appendQuotedChallengeParameter("rspauth",
                        info.getResponseDigest());
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
        StringBuilder result = new StringBuilder(
                Integer.toHexString(nonceCount));
        while (result.length() < 8) {
            result.insert(0, '0');
        }

        return result.toString();
    }

    /**
     * Formats a challenge request as a HTTP header value. The header is {@link HeaderConstants#HEADER_WWW_AUTHENTICATE}
     * . The default
     * implementation relies on
     * {@link AuthenticatorHelper#formatRequest(ChallengeWriter, ChallengeRequest, Response, Series)} to append all
     * parameters from {@link ChallengeRequest#getParameters()}.
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
            Response response, Series<Header> httpHeaders) {
        String result = null;

        if (challenge == null) {
            Context.getCurrentLogger().warning(
                    "No challenge response to format.");
        } else if (challenge.getScheme() == null) {
            Context.getCurrentLogger().warning(
                    "A challenge response must have a scheme defined.");
        } else if (challenge.getScheme().getTechnicalName() == null) {
            Context.getCurrentLogger().warning(
                    "A challenge scheme must have a technical name defined.");
        } else {
            ChallengeWriter cw = new ChallengeWriter();
            cw.append(challenge.getScheme().getTechnicalName()).appendSpace();
            int cwInitialLength = cw.getBuffer().length();

            if (challenge.getRawValue() != null) {
                cw.append(challenge.getRawValue());
            } else {
                AuthenticatorHelper helper = Engine.getInstance().findHelper(
                        challenge.getScheme(), false, true);

                if (helper != null) {
                    try {
                        helper.formatRequest(cw, challenge, response,
                                httpHeaders);
                    } catch (Exception e) {
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

            result = (cw.getBuffer().length() > cwInitialLength) ? cw
                    .toString() : null;
        }

        return result;
    }

    /**
     * Formats a challenge response as a HTTP header value. The header is {@link HeaderConstants#HEADER_AUTHORIZATION}.
     * The default implementation
     * relies on {@link AuthenticatorHelper#formatResponse(ChallengeWriter, ChallengeResponse, Request, Series)} unless
     * some custom credentials are provided via
     * 
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     * @return The {@link HeaderConstants#HEADER_AUTHORIZATION} header value.
     * @throws IOException
     * @link ChallengeResponse#getCredentials()}.
     */
    public static String formatResponse(ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
        String result = null;

        if (challenge == null) {
            Context.getCurrentLogger().warning(
                    "No challenge response to format.");
        } else if (challenge.getScheme() == null) {
            Context.getCurrentLogger().warning(
                    "A challenge response must have a scheme defined.");
        } else if (challenge.getScheme().getTechnicalName() == null) {
            Context.getCurrentLogger().warning(
                    "A challenge scheme must have a technical name defined.");
        } else {
            ChallengeWriter cw = new ChallengeWriter();
            cw.append(challenge.getScheme().getTechnicalName()).appendSpace();
            int cwInitialLength = cw.getBuffer().length();

            if (challenge.getRawValue() != null) {
                cw.append(challenge.getRawValue());
            } else {
                AuthenticatorHelper helper = Engine.getInstance().findHelper(
                        challenge.getScheme(), true, false);

                if (helper != null) {
                    try {
                        helper.formatResponse(cw, challenge, request,
                                httpHeaders);
                    } catch (Exception e) {
                        Context.getCurrentLogger().log(
                                Level.WARNING,
                                "Unable to format the challenge response: "
                                        + challenge, e);
                    }
                } else {
                    Context.getCurrentLogger().warning(
                            "Challenge scheme " + challenge.getScheme()
                                    + " not supported by the Restlet engine.");
                }
            }

            result = (cw.getBuffer().length() > cwInitialLength) ? cw
                    .toString() : null;
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
        HeaderReader<Parameter> hr = new HeaderReader<Parameter>(header);

        try {
            String nextNonce = null;
            String qop = null;
            String responseAuth = null;
            String cnonce = null;
            int nonceCount = 0;
            Parameter param = hr.readParameter();

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
                        param = hr.readParameter();
                    } else {
                        param = null;
                    }
                } catch (Exception e) {
                    Context.getCurrentLogger()
                            .log(Level.WARNING,
                                    "Unable to parse the authentication info header parameter",
                                    e);
                }
            }

            result = new AuthenticationInfo(nextNonce, nonceCount, cnonce, qop,
                    responseAuth);
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
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
            String header, Series<Header> httpHeaders) {
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
            String header, Series<Header> httpHeaders) {
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
     * Updates a {@link ChallengeResponse} object according to given request and
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
     * Optionally updates the request with a challenge response before sending
     * it. This is sometimes useful for authentication schemes that aren't based
     * on the Authorization header but instead on URI query parameters or other
     * headers. By default it returns the resource URI reference unchanged.
     * 
     * @param resourceRef
     *            The resource URI reference to update.
     * @param challengeResponse
     *            The challenge response provided.
     * @param request
     *            The request to update.
     * @return The original URI reference if unchanged or a new one if updated.
     */
    public static Reference updateReference(Reference resourceRef,
            ChallengeResponse challengeResponse, Request request) {
        if (challengeResponse != null && challengeResponse.getRawValue() == null) {
            AuthenticatorHelper helper = Engine.getInstance().findHelper(
                    challengeResponse.getScheme(), true, false);

            if (helper != null) {
                resourceRef = helper.updateReference(resourceRef,
                        challengeResponse, request);
            } else {
                Context.getCurrentLogger().warning(
                        "Challenge scheme " + challengeResponse.getScheme()
                                + " not supported by the Restlet engine.");
            }
        }

        return resourceRef;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AuthenticatorUtils() {
    }

}
