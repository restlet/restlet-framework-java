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
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.Helper;
import org.restlet.engine.http.HeaderBuilder;
import org.restlet.engine.http.HttpConstants;
import org.restlet.security.Guard;
import org.restlet.util.Series;

/**
 * Base class for authentication helpers.
 * 
 * @author Jerome Louvel
 */
@SuppressWarnings("deprecation")
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
        response.getChallengeRequests().add(
                new ChallengeRequest(guard.getScheme(), guard.getRealm()));
    }

    /**
     * Formats a challenge request as a HTTP header value. The header is
     * {@link HttpConstants#HEADER_WWW_AUTHENTICATE}. The default implementation
     * relies on
     * {@link #formatParameters(StringBuilder, Series, ChallengeRequest)} to
     * append all parameters from {@link ChallengeRequest#getParameters()}.
     * 
     * @param challenge
     *            The challenge request to format.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current response HTTP headers.
     * @return The {@link HttpConstants#HEADER_WWW_AUTHENTICATE} header value.
     * @throws IOException
     */
    public String formatRequest(ChallengeRequest challenge, Response response,
            Series<Parameter> httpHeaders) throws IOException {
        HeaderBuilder hb = new HeaderBuilder();
        hb.append(challenge.getScheme().getTechnicalName()).appendSpace();

        if (challenge.getRawValue() != null) {
            hb.append(challenge.getRawValue());
        } else {
            formatRawRequest(hb, challenge, response, httpHeaders);
        }

        return hb.toString();
    }

    /**
     * Formats a challenge request as raw credentials.
     * 
     * @param hb
     *            The header builder to update.
     * @param challenge
     *            The challenge request to format.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void formatRawRequest(HeaderBuilder hb, ChallengeRequest challenge,
            Response response, Series<Parameter> httpHeaders)
            throws IOException {
    }

    /**
     * Formats a challenge response as a HTTP header value. The header is
     * {@link HttpConstants#HEADER_AUTHORIZATION}. The default implementation
     * relies on
     * {@link #formatRawResponse(StringBuilder, ChallengeResponse, Request, Series)}
     * unless some custom credentials are provided via
     * {@link ChallengeResponse#getCredentials()}.
     * 
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     * @return The {@link HttpConstants#HEADER_AUTHORIZATION} header value.
     * @throws IOException
     */
    public String formatResponse(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) throws IOException {
        HeaderBuilder hb = new HeaderBuilder();
        hb.append(challenge.getScheme().getTechnicalName()).appendSpace();

        if (challenge.getRawValue() != null) {
            hb.append(challenge.getRawValue());
        } else {
            formatRawResponse(hb, challenge, request, httpHeaders);
        }

        return hb.toString();
    }

    /**
     * Formats a challenge response as raw credentials.
     * 
     * @param hb
     *            The header builder to update.
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void formatRawResponse(HeaderBuilder hb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) throws IOException {
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
     * Parses an authenticate header into a challenge request. The header is
     * {@link HttpConstants#HEADER_WWW_AUTHENTICATE}.
     * 
     * @param challenge
     *            The challenge request to update.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current response HTTP headers.
     */
    public void parseRequest(ChallengeRequest challenge, Response response,
            Series<Parameter> httpHeaders) {
    }

    /**
     * Parses an authorization header into a challenge response. The header is
     * {@link HttpConstants#HEADER_AUTHORIZATION}.
     * 
     * @param challenge
     *            The challenge response to update.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void parseResponse(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
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
