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

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Helper;
import org.restlet.security.Guard;
import org.restlet.util.Series;

/**
 * Base class for authentication helpers.
 * 
 * @author Jerome Louvel
 */
public abstract class AuthenticatorHelper extends Helper {

    /** The supported challenge scheme. */
    private volatile ChallengeScheme challengeScheme;

    /** Indicates if client side authentication is supported. */
    private volatile boolean clientSide;

    /** Indicates if server side authentication is supported. */
    private volatile boolean serverSide;

    /**
     * Constructor.
     * 
     * @param challengeScheme
     *            The supported challenge scheme.
     * @param clientSide
     *            Indicates if client side authentication is supported.
     * @param serverSide
     *            Indicates if server side authentication is supported.
     */
    public AuthenticatorHelper(ChallengeScheme challengeScheme,
            boolean clientSide, boolean serverSide) {
        this.challengeScheme = challengeScheme;
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }

    /**
     * Indicates if the call is properly authenticated. You are guaranteed that
     * the request has a challenge response with a scheme matching the one
     * supported by the plugin.
     * 
     * @param cr
     *            The challenge response in the request.
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
    public int authenticate(ChallengeResponse cr, Request request, Guard guard) {
        int result = Guard.AUTHENTICATION_MISSING;

        // The challenge schemes are compatible
        final String identifier = cr.getIdentifier();
        final char[] secret = cr.getSecret();

        // Check the credentials
        if ((identifier != null) && (secret != null)) {
            result = guard.checkSecret(request, identifier, secret) ? Guard.AUTHENTICATION_VALID
                    : Guard.AUTHENTICATION_INVALID;
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
    public void challenge(Response response, boolean stale, Guard guard) {
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.setChallengeRequest(new ChallengeRequest(guard.getScheme(),
                guard.getRealm()));
    }

    /**
     * Formats a challenge request as a HTTP header value.
     * 
     * @param request
     *            The challenge request to format.
     * @return The authenticate header value.
     */
    public String format(ChallengeRequest request) {
        final StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme().getTechnicalName());

        if (request.getRealm() != null) {
            sb.append(" realm=\"").append(request.getRealm()).append('"');
        }

        formatParameters(sb, request.getParameters(), request);
        return sb.toString();
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
    public String format(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        final StringBuilder sb = new StringBuilder();
        sb.append(challenge.getScheme().getTechnicalName()).append(' ');

        if (challenge.getCredentials() != null) {
            sb.append(challenge.getCredentials());
        } else {
            formatCredentials(sb, challenge, request, httpHeaders);
        }

        return sb.toString();
    }

    /**
     * Formats a challenge response as raw credentials.
     * 
     * @param sb
     *            The String builder to update.
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public abstract void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders);

    /**
     * Formats the parameters of a challenge request, to be appended to the
     * scheme technical name and realm.
     * 
     * @param sb
     *            The string builder to update.
     * @param parameters
     *            The parameters to format.
     * @param request
     *            The challenger request.
     */
    public void formatParameters(StringBuilder sb,
            Series<Parameter> parameters, ChallengeRequest request) {
    }

    /**
     * Returns the supported challenge scheme.
     * 
     * @return The supported challenge scheme.
     */
    public ChallengeScheme getChallengeScheme() {
        return this.challengeScheme;
    }

    /**
     * Returns the context's logger.
     * 
     * @return The context's logger.
     */
    public Logger getLogger() {
        return Context.getCurrentLogger();
    }

    /**
     * Indicates if client side authentication is supported.
     * 
     * @return True if client side authentication is supported.
     */
    public boolean isClientSide() {
        return this.clientSide;
    }

    /**
     * Indicates if server side authentication is supported.
     * 
     * @return True if server side authentication is supported.
     */
    public boolean isServerSide() {
        return this.serverSide;
    }

    /**
     * Parses an authenticate header into a challenge request.
     * 
     * @param header
     *            The HTTP header value to parse.
     */
    public void parseRequest(ChallengeRequest cr, String header) {
    }

    /**
     * Parses an authorization header into a challenge response.
     * 
     * @param request
     *            The request.
     */
    public void parseResponse(ChallengeResponse cr, Request request) {
    }

    /**
     * Sets the supported challenge scheme.
     * 
     * @param challengeScheme
     *            The supported challenge scheme.
     */
    public void setChallengeScheme(ChallengeScheme challengeScheme) {
        this.challengeScheme = challengeScheme;
    }

    /**
     * Indicates if client side authentication is supported.
     * 
     * @param clientSide
     *            True if client side authentication is supported.
     */
    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }

    /**
     * Indicates if server side authentication is supported.
     * 
     * @param serverSide
     *            True if server side authentication is supported.
     */
    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
    }

}
