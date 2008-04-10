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

package org.restlet.data;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.util.Series;

/**
 * Generic response sent by server connectors. It is then received by client
 * connectors. Responses are uniform across all types of connectors, protocols
 * and components.
 * 
 * @see org.restlet.data.Request
 * @see org.restlet.Uniform
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Response extends Message {
    private static final ThreadLocal<Response> CURRENT = new ThreadLocal<Response>();

    /**
     * Returns the response associated to the current thread. This variable is
     * stored internally as a thread local variable and updated when the request
     * is handled by a Component or an Application.
     * 
     * @return The thread's response.
     */
    public static Response getCurrent() {
        return CURRENT.get();
    }

    /**
     * Sets the respone associated with the current thread.
     * 
     * @param response
     *                The thread's response.
     */
    public static void setCurrent(Response response) {
        CURRENT.set(response);
    }

    /**
     * Private cookie setting series.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private static class CookieSettingSeries extends Series<CookieSetting> {
        /**
         * Constructor.
         */
        public CookieSettingSeries() {
            super();
        }

        /**
         * Constructor.
         * 
         * @param delegate
         *                The delegate list.
         */
        public CookieSettingSeries(List<CookieSetting> delegate) {
            super(delegate);
        }

        @Override
        public CookieSetting createEntry(String name, String value) {
            return new CookieSetting(name, value);
        }

        @Override
        public Series<CookieSetting> createSeries(List<CookieSetting> delegate) {
            if (delegate != null)
                return new CookieSettingSeries(delegate);
            else
                return new CookieSettingSeries();
        }
    }

    /** The set of methods allowed on the requested resource. */
    private volatile Set<Method> allowedMethods;

    /** The authentication request sent by an origin server to a client. */
    private volatile ChallengeRequest challengeRequest;

    /** The cookie settings provided by the server. */
    private volatile Series<CookieSetting> cookieSettings;

    /** The set of dimensions on which the response entity may vary. */
    private volatile Set<Dimension> dimensions;

    /** The reference used for redirections or creations. */
    private volatile Reference locationRef;

    /** The associated request. */
    private volatile Request request;

    /** The server-specific information. */
    private volatile ServerInfo serverInfo;

    /** The status. */
    private volatile Status status;

    /**
     * Constructor.
     * 
     * @param request
     *                The request associated to this response.
     */
    public Response(Request request) {
        this.allowedMethods = null;
        this.challengeRequest = null;
        this.cookieSettings = null;
        this.dimensions = null;
        this.locationRef = null;
        this.request = request;
        this.serverInfo = null;
        this.status = Status.SUCCESS_OK;
    }

    /**
     * Returns the modifiable set of methods allowed on the requested resource.
     * This property only has to be updated when a status
     * CLIENT_ERROR_METHOD_NOT_ALLOWED is set. Creates a new instance if no one
     * has been set.
     * 
     * @return The list of allowed methods.
     */
    public Set<Method> getAllowedMethods() {
        if (this.allowedMethods == null)
            this.allowedMethods = new HashSet<Method>();
        return this.allowedMethods;
    }

    /**
     * Returns the authentication request sent by an origin server to a client.
     * 
     * @return The authentication request sent by an origin server to a client.
     */
    public ChallengeRequest getChallengeRequest() {
        return this.challengeRequest;
    }

    /**
     * Returns the modifiable series of cookie settings provided by the server.
     * Creates a new instance if no one has been set.
     * 
     * @return The cookie settings provided by the server.
     */
    public Series<CookieSetting> getCookieSettings() {
        if (this.cookieSettings == null)
            this.cookieSettings = new CookieSettingSeries();
        return this.cookieSettings;
    }

    /**
     * Returns the modifiable set of selecting dimensions on which the response
     * entity may vary. If some server-side content negotiation is done, this
     * set should be properly updated, other it can be left empty. Creates a new
     * instance if no one has been set.
     * 
     * @return The set of dimensions on which the response entity may vary.
     */
    public Set<Dimension> getDimensions() {
        if (this.dimensions == null)
            this.dimensions = EnumSet.noneOf(Dimension.class);
        return this.dimensions;
    }

    /**
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     */
    public Reference getLocationRef() {
        return this.locationRef;
    }

    /**
     * Returns the associated request
     * 
     * @return The associated request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the server-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The server-specific information.
     */
    public ServerInfo getServerInfo() {
        if (this.serverInfo == null)
            this.serverInfo = new ServerInfo();
        return this.serverInfo;
    }

    /**
     * Returns the status.
     * 
     * @return The status.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Permanently redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *                The target URI reference.
     */
    public void redirectPermanent(Reference targetRef) {
        setLocationRef(targetRef);
        setStatus(Status.REDIRECTION_PERMANENT);
    }

    /**
     * Permanently redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.<br>
     * <br>
     * If you pass a relative target URI, it will be resolved with the current
     * base reference of the request's resource reference (see
     * {@link Request#getResourceRef()} and {@link Reference#getBaseRef()}.
     * 
     * @param targetUri
     *                The target URI.
     */
    public void redirectPermanent(String targetUri) {
        setLocationRef(targetUri);
        setStatus(Status.REDIRECTION_PERMANENT);
    }

    /**
     * Redirects the client to a different URI that SHOULD be retrieved using a
     * GET method on that resource. This method exists primarily to allow the
     * output of a POST-activated script to redirect the user agent to a
     * selected resource. The new URI is not a substitute reference for the
     * originally requested resource.
     * 
     * @param targetRef
     *                The target reference.
     */
    public void redirectSeeOther(Reference targetRef) {
        setLocationRef(targetRef);
        setStatus(Status.REDIRECTION_SEE_OTHER);
    }

    /**
     * Redirects the client to a different URI that SHOULD be retrieved using a
     * GET method on that resource. This method exists primarily to allow the
     * output of a POST-activated script to redirect the user agent to a
     * selected resource. The new URI is not a substitute reference for the
     * originally requested resource.<br>
     * <br>
     * If you pass a relative target URI, it will be resolved with the current
     * base reference of the request's resource reference (see
     * {@link Request#getResourceRef()} and {@link Reference#getBaseRef()}.
     * 
     * @param targetUri
     *                The target URI.
     */
    public void redirectSeeOther(String targetUri) {
        setLocationRef(targetUri);
        setStatus(Status.REDIRECTION_SEE_OTHER);
    }

    /**
     * Temporarily redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *                The target reference.
     */
    public void redirectTemporary(Reference targetRef) {
        setLocationRef(targetRef);
        setStatus(Status.REDIRECTION_TEMPORARY);
    }

    /**
     * Temporarily redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.<br>
     * <br>
     * If you pass a relative target URI, it will be resolved with the current
     * base reference of the request's resource reference (see
     * {@link Request#getResourceRef()} and {@link Reference#getBaseRef()}.
     * 
     * @param targetUri
     *                The target URI.
     */
    public void redirectTemporary(String targetUri) {
        setLocationRef(targetUri);
        setStatus(Status.REDIRECTION_TEMPORARY);
    }

    /**
     * Sets the set of methods allowed on the requested resource.
     * 
     * @param allowedMethods
     *                The set of methods allowed on the requested resource.
     */
    public void setAllowedMethods(Set<Method> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Sets the authentication request sent by an origin server to a client.
     * 
     * @param request
     *                The authentication request sent by an origin server to a
     *                client.
     */
    public void setChallengeRequest(ChallengeRequest request) {
        this.challengeRequest = request;
    }

    /**
     * Sets the cookie settings provided by the server.
     * 
     * @param cookieSettings
     *                The cookie settings provided by the server.
     */
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        this.cookieSettings = cookieSettings;
    }

    /**
     * Sets the set of dimensions on which the response entity may vary.
     * 
     * @param dimensions
     *                The set of dimensions on which the response entity may
     *                vary.
     */
    public void setDimensions(Set<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationRef
     *                The reference to set.
     */
    public void setLocationRef(Reference locationRef) {
        this.locationRef = locationRef;
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations. If you pass a relative location URI, it will be
     * resolved with the current base reference of the request's resource
     * reference (see {@link Request#getResourceRef()} and
     * {@link Reference#getBaseRef()}.
     * 
     * @param locationUri
     *                The URI to set.
     */
    public void setLocationRef(String locationUri) {
        Reference baseRef = null;

        if (getRequest().getResourceRef() != null) {
            if (getRequest().getResourceRef().getBaseRef() != null) {
                baseRef = getRequest().getResourceRef().getBaseRef();
            } else {
                baseRef = getRequest().getResourceRef();
            }
        }

        setLocationRef(new Reference(baseRef, locationUri).getTargetRef());
    }

    /**
     * Sets the associated request.
     * 
     * @param request
     *                The associated request
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the server-specific information.
     * 
     * @param serverInfo
     *                The server-specific information.
     */
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *                The status to set.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *                The status to set.
     * @param message
     *                The status message.
     */
    public void setStatus(Status status, String message) {
        setStatus(new Status(status, message));
    }

}
