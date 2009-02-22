/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Engine;
import org.restlet.util.Series;

/**
 * Authentication utilities.
 * 
 * @author Jerome Louvel
 * @author Ray Waldin (ray@waldin.net)
 */
public class AuthenticatorUtils {

    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this respect.
     * Pattern is immutable and thread-safe so reuse one static instance.
     */
    private static final Pattern PATTERN_RFC_2617 = Pattern
            .compile("([^=]+)=\"?([^\",]+)(?:\"\\s*)?,?\\s*");

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
     * delegates credential checking to checkSecret().
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
                    final AuthenticatorHelper helper = Engine
                            .getInstance().findHelper(cr.getScheme(), false,
                                    true);

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
        final AuthenticatorHelper helper = Engine.getInstance()
                .findHelper(guard.getScheme(), false, true);

        if (helper != null) {
            helper.challenge(response, stale, guard);
        } else {
            throw new IllegalArgumentException("Challenge scheme "
                    + guard.getScheme()
                    + " not supported by the Restlet engine.");
        }
    }

    /**
     * Formats a challenge request as a HTTP header value.
     * 
     * @param request
     *            The challenge request to format.
     * @return The authenticate header value.
     */
    public static String format(ChallengeRequest request) {
        String result = null;

        if (request != null) {
            final AuthenticatorHelper helper = Engine.getInstance()
                    .findHelper(request.getScheme(), false, true);

            if (helper != null) {
                result = helper.format(request);
            } else {
                result = "?";
                Context.getCurrentLogger().warning(
                        "Challenge scheme " + request.getScheme()
                                + " not supported by the Restlet engine.");
            }
        }

        return result;
    }

    /**
     * Formats a challenge response as raw credentials.
     * 
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     * @return The authorization header value.
     */
    public static String format(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        String result = null;
        final AuthenticatorHelper helper = Engine.getInstance()
                .findHelper(challenge.getScheme(), true, false);

        if (helper != null) {
            result = helper.format(challenge, request, httpHeaders);
        } else {
            result = "?";
            Context.getCurrentLogger().warning(
                    "Challenge scheme " + challenge.getScheme()
                            + " not supported by the Restlet engine.");
        }

        return result;
    }

    /**
     * Parses an authenticate header into a challenge request.
     * 
     * @param header
     *            The HTTP header value to parse.
     * @return The parsed challenge request.
     */
    public static ChallengeRequest parseAuthenticateHeader(String header) {
        ChallengeRequest result = null;

        if (header != null) {
            final int space = header.indexOf(' ');

            if (space != -1) {
                final String scheme = header.substring(0, space);
                result = new ChallengeRequest(new ChallengeScheme("HTTP_"
                        + scheme, scheme), null);

                // Parse the parameters to extract the realm
                final String rest = header.substring(space + 1);
                parseParameters(rest, result.getParameters());
                result.setRealm(result.getParameters().getFirstValue("realm"));
            } else {
                final String scheme = header.substring(0);
                result = new ChallengeRequest(new ChallengeScheme("HTTP_"
                        + scheme, scheme), null);
            }
        }

        // Give a chance to the authentication helper to do further parsing
        final AuthenticatorHelper helper = Engine.getInstance()
                .findHelper(result.getScheme(), true, false);

        if (helper != null) {
            helper.parseRequest(result, header);
        } else {
            Context.getCurrentLogger().warning(
                    "Challenge scheme " + result.getScheme()
                            + " not supported by the Restlet engine.");
        }

        return result;
    }

    /**
     * Parses an authorization header into a challenge response.
     * 
     * @param request
     *            The request.
     * @param header
     *            The header value to parse.
     * @return The parsed challenge response.
     */
    public static ChallengeResponse parseAuthorizationHeader(Request request,
            String header) {
        ChallengeResponse result = null;

        if (header != null) {
            final int space = header.indexOf(' ');

            if (space != -1) {
                final String scheme = header.substring(0, space);
                final String credentials = header.substring(space + 1);
                result = new ChallengeResponse(new ChallengeScheme("HTTP_"
                        + scheme, scheme), credentials);

                // Give a chance to the authentication helper to do further
                // parsing
                final AuthenticatorHelper helper = Engine
                        .getInstance().findHelper(result.getScheme(), true,
                                false);

                if (helper != null) {
                    helper.parseResponse(result, request);
                } else {
                    Context.getCurrentLogger().warning(
                            "Challenge scheme " + result.getScheme()
                                    + " not supported by the Restlet engine.");
                }
            }
        }

        return result;
    }

    /**
     * Parsed the parameters of a credientials string and updates the series of
     * parameters.
     * 
     * @param paramString
     *            The parameters string to parse.
     * @param parameters
     *            The series to update.
     */
    public static void parseParameters(String paramString,
            Series<Parameter> parameters) {
        final Matcher matcher = PATTERN_RFC_2617.matcher(paramString);

        while (matcher.find() && (matcher.groupCount() == 2)) {
            parameters.add(matcher.group(1), matcher.group(2));
        }
    }

}
