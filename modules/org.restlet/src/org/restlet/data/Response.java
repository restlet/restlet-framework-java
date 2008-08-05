/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Restlet;
import org.restlet.util.Series;

/**
 * Generic response sent by server connectors. It is then received by client
 * connectors. Responses are uniform across all types of connectors, protocols
 * and components.
 * 
 * @see org.restlet.data.Request
 * @see org.restlet.Uniform
 * @author Jerome Louvel
 */
public class Response extends Message {
    /**
     * Private cookie setting series.
     * 
     * @author Jerome Louvel
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
         *            The delegate list.
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
            if (delegate != null) {
                return new CookieSettingSeries(delegate);
            }

            return new CookieSettingSeries();
        }
    }

    private static final ThreadLocal<Response> CURRENT = new ThreadLocal<Response>();

    /**
     * Returns the response associated to the current thread.
     * 
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.resource.Resource#getResponse()}.
     * 
     * This variable is stored internally as a thread local variable and updated
     * each time a call is handled by a Restlet via the
     * {@link Restlet#handle(org.restlet.data.Request, org.restlet.data.Response)}
     * method.
     * 
     * @return The current context.
     */
    public static Response getCurrent() {
        return CURRENT.get();
    }

    /**
     * Sets the response associated with the current thread.
     * 
     * @param response
     *            The thread's response.
     */
    public static void setCurrent(Response response) {
        CURRENT.set(response);
    }

    /** The set of methods allowed on the requested resource. */
    private volatile Set<Method> allowedMethods;

    /** The authentication requests sent by an origin server to a client. */
    private volatile List<ChallengeRequest> challengeRequests;

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
     *            The request associated to this response.
     */
    public Response(Request request) {
        this.allowedMethods = null;
        this.challengeRequests = null;
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
        // Lazy initialization with double-check.
        Set<Method> a = this.allowedMethods;
        if (a == null) {
            synchronized (this) {
                a = this.allowedMethods;
                if (a == null) {
                    this.allowedMethods = a = new CopyOnWriteArraySet<Method>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the authentication request sent by an origin server to a client.
     * 
     * @return The authentication request sent by an origin server to a client.
     * @deprecated Use the {@link #getChallengeRequests()} method instead.
     */
    @Deprecated
    public ChallengeRequest getChallengeRequest() {
        final List<ChallengeRequest> requests = this.challengeRequests;
        if ((requests != null) && (requests.size() > 0)) {
            return requests.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client. If none is found, an empty list is returned.
     * 
     * @return The list of authentication requests.
     */
    public List<ChallengeRequest> getChallengeRequests() {
        // Lazy initialization with double-check.
        List<ChallengeRequest> cr = this.challengeRequests;
        if (cr == null) {
            synchronized (this) {
                cr = this.challengeRequests;
                if (cr == null) {
                    this.challengeRequests = cr = new CopyOnWriteArrayList<ChallengeRequest>();
                }
            }
        }
        return cr;
    }

    /**
     * Returns the modifiable series of cookie settings provided by the server.
     * Creates a new instance if no one has been set.
     * 
     * @return The cookie settings provided by the server.
     */
    public Series<CookieSetting> getCookieSettings() {
        // Lazy initialization with double-check.
        Series<CookieSetting> c = this.cookieSettings;
        if (c == null) {
            synchronized (this) {
                c = this.cookieSettings;
                if (c == null) {
                    this.cookieSettings = c = new CookieSettingSeries();
                }
            }
        }
        return c;
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
        if (this.dimensions == null) {
            this.dimensions = new CopyOnWriteArraySet<Dimension>();
        }
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
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     * @deprecated Use getLocationRef() instead.
     */
    @Deprecated
    public Reference getRedirectRef() {
        return getLocationRef();
    }

    /**
     * Returns the associated request
     * 
     * @return The associated request
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * Returns the server-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The server-specific information.
     */
    public ServerInfo getServerInfo() {
        // Lazy initialization with double-check.
        ServerInfo s = this.serverInfo;
        if (s == null) {
            synchronized (this) {
                s = this.serverInfo;
                if (s == null) {
                    this.serverInfo = s = new ServerInfo();
                }
            }
        }
        return s;
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
     *            The target URI reference.
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
     *            The target URI.
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
     *            The target reference.
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
     *            The target URI.
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
     *            The target reference.
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
     *            The target URI.
     */
    public void redirectTemporary(String targetUri) {
        setLocationRef(targetUri);
        setStatus(Status.REDIRECTION_TEMPORARY);
    }

    /**
     * Sets the set of methods allowed on the requested resource. The set
     * instance set must be thread-safe (use {@link CopyOnWriteArraySet} for
     * example.
     * 
     * @param allowedMethods
     *            The set of methods allowed on the requested resource.
     */
    public void setAllowedMethods(Set<Method> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Sets the authentication request sent by an origin server to a client.
     * 
     * @param request
     *            The authentication request sent by an origin server to a
     *            client.
     */
    public void setChallengeRequest(ChallengeRequest request) {
        final List<ChallengeRequest> requests = new CopyOnWriteArrayList<ChallengeRequest>();
        requests.add(request);
        setChallengeRequests(requests);
    }

    /**
     * Sets the list of authentication requests sent by an origin server to a
     * client. The list instance set must be thread-safe (use
     * {@link CopyOnWriteArrayList} for example.
     * 
     * @param requests
     *            The list of authentication requests sent by an origin server
     *            to a client.
     */
    public void setChallengeRequests(List<ChallengeRequest> requests) {
        this.challengeRequests = requests;
    }

    /**
     * Sets the cookie settings provided by the server.
     * 
     * @param cookieSettings
     *            The cookie settings provided by the server.
     */
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        this.cookieSettings = cookieSettings;
    }

    /**
     * Sets the set of dimensions on which the response entity may vary. The set
     * instance set must be thread-safe (use {@link CopyOnWriteArraySet} for
     * example.
     * 
     * @param dimensions
     *            The set of dimensions on which the response entity may vary.
     */
    public void setDimensions(Set<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationRef
     *            The reference to set.
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
     *            The URI to set.
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
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationRef
     *            The reference to set.
     * @deprecated Use the setLocationRef() method instead.
     */
    @Deprecated
    public void setRedirectRef(Reference locationRef) {
        setLocationRef(locationRef);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationUri
     *            The URI to set.
     * @deprecated Use the setLocationRef() method instead.
     */
    @Deprecated
    public void setRedirectRef(String locationUri) {
        setLocationRef(locationUri);
    }

    /**
     * Sets the associated request.
     * 
     * @param request
     *            The associated request
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the server-specific information.
     * 
     * @param serverInfo
     *            The server-specific information.
     */
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param message
     *            The status message.
     */
    public void setStatus(Status status, String message) {
        setStatus(new Status(status, message));
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param throwable
     *            The related error or exception.
     */
    public void setStatus(Status status, Throwable throwable) {
        setStatus(new Status(status, throwable));
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param throwable
     *            The related error or exception.
     * @param message
     *            The status message.
     */
    public void setStatus(Status status, Throwable throwable, String message) {
        setStatus(new Status(status, throwable, message));
    }

}
