/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.util.Series;

/**
 * Generic response sent by server connectors. It is then received by client
 * connectors. Responses are uniform across all types of connectors, protocols
 * and components.
 * 
 * @see org.restlet.Request
 * @see org.restlet.Uniform
 * @author Jerome Louvel
 */
public class Response extends Message {
    // [ifndef gwt] member
    private static final ThreadLocal<Response> CURRENT = new ThreadLocal<Response>();

    // [ifndef gwt] method
    /**
     * Returns the response associated to the current thread.
     * 
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.resource.Resource#getResponse()}.
     * 
     * This variable is stored internally as a thread local variable and updated
     * each time a call is handled by a Restlet via the
     * {@link Restlet#handle(org.restlet.Request, org.restlet.Response)} method.
     * 
     * @return The current context.
     */
    public static Response getCurrent() {
        return CURRENT.get();
    }

    // [ifndef gwt] method
    /**
     * Sets the response associated with the current thread.
     * 
     * @param response
     *            The thread's response.
     */
    public static void setCurrent(Response response) {
        CURRENT.set(response);
    }

    /**
     * Estimated amount of time since a response was generated or revalidated by
     * the origin server.
     */
    private volatile int age;

    /** The set of methods allowed on the requested resource. */
    private volatile Set<Method> allowedMethods;

    /**
     * The authentication information sent by an origin server to a client in
     * the case of a successful authentication attempt.
     */
    private volatile AuthenticationInfo authenticationInfo;

    /** Indicates if the response should be automatically committed. */
    private volatile boolean autoCommitting;

    /** The authentication requests sent by an origin server to a client. */
    private volatile List<ChallengeRequest> challengeRequests;

    /** Indicates if the response has been committed. */
    private volatile boolean committed;

    /** The cookie settings provided by the server. */
    private volatile Series<CookieSetting> cookieSettings;

    /** The set of dimensions on which the response entity may vary. */
    private volatile Set<Dimension> dimensions;

    /** The reference used for redirections or creations. */
    private volatile Reference locationRef;

    /** The authentication requests sent by a proxy to a client. */
    private volatile List<ChallengeRequest> proxyChallengeRequests;

    /** The associated request. */
    private volatile Request request;

    /**
     * Indicates how long the service is expected to be unavailable to the
     * requesting client.
     */
    private volatile Date retryAfter;

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
        this.age = 0;
        this.allowedMethods = null;
        this.autoCommitting = true;
        this.challengeRequests = null;
        this.cookieSettings = null;
        this.committed = false;
        this.dimensions = null;
        this.locationRef = null;
        this.proxyChallengeRequests = null;
        this.request = request;
        this.retryAfter = null;
        this.serverInfo = null;
        this.status = Status.SUCCESS_OK;
    }

    /**
     * Ask the connector to abort the related network connection, for example
     * immediately closing the socket.
     */
    public void abort() {
        getRequest().abort();
    }

    /**
     * Asks the server connector to immediately commit the given response,
     * making it ready to be sent back to the client. Note that all server
     * connectors don't necessarily support this feature.
     */
    public void commit() {
        getRequest().commit(this);
    }

    /**
     * Returns the estimated amount of time since a response was generated or
     * revalidated by the origin server. Origin servers should leave the 0
     * default value. Only caches are expected to set this property.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "Age"
     * header.
     * 
     * @return The response age.
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the modifiable set of methods allowed on the requested resource.
     * This property only has to be updated when a status
     * CLIENT_ERROR_METHOD_NOT_ALLOWED is set. Creates a new instance if no one
     * has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Allow" header.
     * 
     * @return The set of allowed methods.
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
     * Returns information sent by an origin server related to an successful
     * authentication attempt. If none is available, null is returned.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Authentication-Info" header.
     * 
     * @return The authentication information provided by the server.
     */
    public AuthenticationInfo getAuthenticationInfo() {
        return this.authenticationInfo;
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client. If none is available, an empty list is returned.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "WWW-Authenticate" header.
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
     * Creates a new instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Set-Cookie" and "Set-Cookie2" headers.
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
                    // [ifndef gwt] instruction
                    this.cookieSettings = c = new Series<CookieSetting>(
                            CookieSetting.class);
                    // [ifdef gwt] instruction uncomment
                    // this.cookieSettings = c = new
                    // org.restlet.engine.util.CookieSettingSeries();
                }
            }
        }
        return c;
    }

    /**
     * Returns the modifiable set of selecting dimensions on which the response
     * entity may vary. If some server-side content negotiation is done, this
     * set should be properly updated, other it can be left empty. Creates a new
     * instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Vary" header.
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
     * Returns the location reference. This is the reference that the client
     * should follow for redirections or resource creations.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Location" header.
     * 
     * @return The redirection reference.
     */
    public Reference getLocationRef() {
        return this.locationRef;
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client. If none is available, an empty list is returned.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Proxy-Authenticate" header.
     * 
     * @return The list of authentication requests.
     */
    public List<ChallengeRequest> getProxyChallengeRequests() {
        // Lazy initialization with double-check.
        List<ChallengeRequest> cr = this.proxyChallengeRequests;
        if (cr == null) {
            synchronized (this) {
                cr = this.proxyChallengeRequests;
                if (cr == null) {
                    this.proxyChallengeRequests = cr = new CopyOnWriteArrayList<ChallengeRequest>();
                }
            }
        }
        return cr;
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
     * Indicates how long the service is expected to be unavailable to the
     * requesting client. Default value is null.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Retry-After" header.
     * 
     * @return Date after with a retry attempt could occur.
     */
    public Date getRetryAfter() {
        return retryAfter;
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
     * Indicates if the response should be automatically committed. When
     * processing a request on the server-side, setting this property to 'false'
     * let you ask to the server connector to wait before sending the response
     * back to the client when the initial calling thread returns. This will let
     * you do further updates to the response and manually calling
     * {@link #commit()} later on, using another thread.
     * 
     * @return True if the response should be automatically committed.
     */
    public boolean isAutoCommitting() {
        return autoCommitting;
    }

    /**
     * Indicates if the response has already been committed.
     * 
     * @return True if the response has already been committed.
     */
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public boolean isConfidential() {
        return getRequest().isConfidential();
    }

    /**
     * Indicates if the response is final or provisional. It relies on the
     * {@link Status#isInformational()} method.
     * 
     * @return True if the response is final.
     */
    public boolean isFinal() {
        return !getStatus().isInformational();
    }

    /**
     * Indicates if the response is provisional or final. It relies on the
     * {@link Status#isInformational()} method.
     * 
     * @return True if the response is provisional.
     */
    public boolean isProvisional() {
        return getStatus().isInformational();
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
     * Sets the estimated amount of time since a response was generated or
     * revalidated by the origin server. Origin servers should leave the 0
     * default value. Only caches are expected to set this property.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "Age"
     * header.
     * 
     * @param age
     *            The response age.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Sets the set of methods allowed on the requested resource. The set
     * instance set must be thread-safe (use {@link CopyOnWriteArraySet} for
     * example.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Allow" header.
     * 
     * @param allowedMethods
     *            The set of methods allowed on the requested resource.
     */
    public void setAllowedMethods(Set<Method> allowedMethods) {
        synchronized (getAllowedMethods()) {
            if (allowedMethods != this.allowedMethods) {
                this.allowedMethods.clear();

                if (allowedMethods != null) {
                    this.allowedMethods.addAll(allowedMethods);
                }
            }
        }
    }

    /**
     * Sets the authentication information sent by an origin server to a client
     * after a successful authentication attempt.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Authentication-Info" header.
     * 
     * @param authenticationInfo
     *            The data returned by the server in response to successful
     *            authentication.
     */
    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }

    /**
     * Indicates if the response should be automatically committed.
     * 
     * @param autoCommitting
     *            True if the response should be automatically committed
     */
    public void setAutoCommitting(boolean autoCommitting) {
        this.autoCommitting = autoCommitting;
    }

    /**
     * Sets the list of authentication requests sent by an origin server to a
     * client. Note that when used with HTTP connectors, this property maps to
     * the "WWW-Authenticate" header. This method clears the current list and
     * adds all entries in the parameter list.
     * 
     * @param challengeRequests
     *            A list of authentication requests sent by an origin server to
     *            a client.
     */
    public void setChallengeRequests(List<ChallengeRequest> challengeRequests) {
        synchronized (getChallengeRequests()) {
            if (challengeRequests != getChallengeRequests()) {
                getChallengeRequests().clear();

                if (challengeRequests != null) {
                    getChallengeRequests().addAll(challengeRequests);
                }
            }
        }
    }

    /**
     * Indicates if the response has already been committed.
     * 
     * @param committed
     *            True if the response has already been committed.
     */
    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    /**
     * Sets the modifiable series of cookie settings provided by the server.
     * Note that when used with HTTP connectors, this property maps to the
     * "Set-Cookie" and "Set-Cookie2" headers. This method clears the current
     * series and adds all entries in the parameter series.
     * 
     * @param cookieSettings
     *            A series of cookie settings provided by the server.
     */
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        synchronized (getCookieSettings()) {
            if (cookieSettings != getCookieSettings()) {
                getCookieSettings().clear();

                if (cookieSettings != null) {
                    getCookieSettings().addAll(cookieSettings);
                }
            }
        }
    }

    /**
     * Sets the set of dimensions on which the response entity may vary. Note
     * that when used with HTTP connectors, this property maps to the "Vary"
     * header. This method clears the current set and adds all entries in the
     * parameter set.
     * 
     * @param dimensions
     *            The set of dimensions on which the response entity may vary.
     */
    public void setDimensions(Set<Dimension> dimensions) {
        synchronized (getDimensions()) {
            if (dimensions != getDimensions()) {
                getDimensions().clear();

                if (dimensions != null) {
                    getDimensions().addAll(dimensions);
                }
            }
        }
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations. Note that when used with HTTP connectors, this
     * property maps to the "Location" header.
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
     * {@link Reference#getBaseRef()}.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Location" header.
     * 
     * @param locationUri
     *            The URI to set.
     * @see #setLocationRef(Reference)
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
     * Sets the modifiable list of authentication requests sent by a proxy to a
     * client. The list instance set must be thread-safe (use
     * {@link CopyOnWriteArrayList} for example. Note that when used with HTTP
     * connectors, this property maps to the "Proxy-Authenticate" header. This
     * method clears the current list and adds all entries in the parameter
     * list.
     * 
     * @param proxyChallengeRequests
     *            A list of authentication requests sent by a proxy to a client.
     */
    public void setProxyChallengeRequests(
            List<ChallengeRequest> proxyChallengeRequests) {
        synchronized (getProxyChallengeRequests()) {
            if (proxyChallengeRequests != getProxyChallengeRequests()) {
                getProxyChallengeRequests().clear();

                if (proxyChallengeRequests != null) {
                    getProxyChallengeRequests().addAll(proxyChallengeRequests);
                }
            }
        }
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
     * Indicates how long the service is expected to be unavailable to the
     * requesting client. Default value is null.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Retry-After" header.
     * 
     * @param retryAfter
     *            Date after with a retry attempt could occur.
     */
    public void setRetryAfter(Date retryAfter) {
        this.retryAfter = retryAfter;
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
     *            The status to set (code and reason phrase).
     * @param description
     *            The longer status description.
     */
    public void setStatus(Status status, String description) {
        setStatus(new Status(status, description));
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

    /**
     * Displays a synthesis of the response like an HTTP status line.
     * 
     * @return A synthesis of the response like an HTTP status line.
     */
    public String toString() {
        return ((getRequest() == null) ? "?" : getRequest().getProtocol())
                + " - " + getStatus();
    }
}
