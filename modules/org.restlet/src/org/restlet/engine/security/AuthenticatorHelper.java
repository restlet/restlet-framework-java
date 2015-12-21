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
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.Reference;
import org.restlet.engine.Helper;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
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
     * Formats a challenge request as raw credentials.
     * 
     * @param cw
     *            The header writer to update.
     * @param challenge
     *            The challenge request to format.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void formatRequest(ChallengeWriter cw, ChallengeRequest challenge,
            Response response, Series<Header> httpHeaders) throws IOException {
    }

    /**
     * Formats a challenge response as raw credentials.
     * 
     * @param cw
     *            The header writer to update.
     * @param challenge
     *            The challenge response to format.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
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
     * {@link HeaderConstants#HEADER_WWW_AUTHENTICATE}.
     * 
     * @param challenge
     *            The challenge request to update.
     * @param response
     *            The parent response.
     * @param httpHeaders
     *            The current response HTTP headers.
     */
    public void parseRequest(ChallengeRequest challenge, Response response,
            Series<Header> httpHeaders) {
    }

    /**
     * Parses an authorization header into a challenge response. The header is
     * {@link HeaderConstants#HEADER_AUTHORIZATION}.
     * 
     * @param challenge
     *            The challenge response to update.
     * @param request
     *            The parent request.
     * @param httpHeaders
     *            The current request HTTP headers.
     */
    public void parseResponse(ChallengeResponse challenge, Request request,
            Series<Header> httpHeaders) {
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
    public Reference updateReference(Reference resourceRef,
            ChallengeResponse challengeResponse, Request request) {
        return resourceRef;
    }

}
