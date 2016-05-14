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

package org.restlet;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Tag;
import org.restlet.data.Warning;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Generic request sent by client connectors. It is then received by server
 * connectors and processed by {@link Restlet}s. This request can also be
 * processed by a chain of Restlets, on both client and server sides. Requests
 * are uniform across all types of connectors, protocols and components.
 * 
 * @see org.restlet.Response
 * @see org.restlet.Uniform
 * @author Jerome Louvel
 */
public class Request extends Message {

    // [ifndef gwt] method
    /**
     * Returns the request associated to the current thread. This is reusing the {@link Response#getCurrent()} method.<br>
     * <br>
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.resource.Resource#getRequest()}.
     * 
     * @return The thread's request.
     */
    public static Request getCurrent() {
        return (Response.getCurrent() == null) ? null : Response.getCurrent()
                .getRequest();
    }

    /**
     * Used when issuing a preflight CORS request to let the origin server knows
     * what headers the client is willing to send in future request to this
     * resource.
     */
    private volatile Set<String> accessControlRequestHeaders;

    /**
     * Used when issuing a preflight CORS request to let the origin server knows
     * what method the client is willing to send in future request to this
     * resource.
     */
    private volatile Method accessControlRequestMethod;

    /** The authentication response sent by a client to an origin server. */
    private volatile ChallengeResponse challengeResponse;

    /** The client-specific information. */
    private volatile ClientInfo clientInfo;

    /** The condition data. */
    private volatile Conditions conditions;

    /** The cookies provided by the client. */
    private volatile Series<Cookie> cookies;

    /** The host reference. */
    private volatile Reference hostRef;

    /** Indicates if the call is loggable. */
    private volatile boolean loggable;

    /** The maximum number of intermediaries. */
    private volatile int maxForwards;

    /** The method. */
    private volatile Method method;

    /** Callback invoked on response reception. */
    private volatile Uniform onResponse;

    /** The original reference. */
    private volatile Reference originalRef;

    /** The protocol. */
    private volatile Protocol protocol;

    // [ifndef gwt] member
    /** The authentication response sent by a client to a proxy. */
    private volatile ChallengeResponse proxyChallengeResponse;

    /** The ranges to return from the target resource's representation. */
    private volatile List<Range> ranges;

    /** The referrer reference. */
    private volatile Reference referrerRef;

    /** The resource reference. */
    private volatile Reference resourceRef;

    /** The application root reference. */
    private volatile Reference rootRef;

    /**
     * Constructor.
     */
    public Request() {
        this((Method) null, (Reference) null, (Representation) null);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The call's method.
     * @param resourceRef
     *            The resource reference.
     */
    public Request(Method method, Reference resourceRef) {
        this(method, resourceRef, null);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The call's method.
     * @param resourceRef
     *            The resource reference.
     * @param entity
     *            The entity.
     */
    public Request(Method method, Reference resourceRef, Representation entity) {
        super(entity);
        this.accessControlRequestHeaders = null;
        this.accessControlRequestMethod = null;
        this.challengeResponse = null;
        this.clientInfo = null;
        this.conditions = null;
        this.cookies = null;
        this.hostRef = null;
        this.loggable = true;
        this.maxForwards = -1;
        this.method = method;
        this.originalRef = null;
        this.onResponse = null;
        // [ifndef gwt] instruction
        this.proxyChallengeResponse = null;
        this.protocol = null;
        this.ranges = null;
        this.referrerRef = null;
        this.resourceRef = resourceRef;
        this.rootRef = null;
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The call's method.
     * @param resourceUri
     *            The resource URI.
     */
    public Request(Method method, String resourceUri) {
        this(method, new Reference(resourceUri));
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The call's method.
     * @param resourceUri
     *            The resource URI.
     * @param entity
     *            The entity.
     */
    public Request(Method method, String resourceUri, Representation entity) {
        this(method, new Reference(resourceUri), entity);
    }

    /**
     * Copy constructor.
     * 
     * @param request
     *            The request to copy.
     */
    public Request(Request request) {
        this(request.getMethod(), new Reference(request.getResourceRef()),
                request.getEntity());
        challengeResponse = request.getChallengeResponse();

        // Copy client info
        ClientInfo rci = request.getClientInfo();
        clientInfo = new ClientInfo();

        for (Preference<CharacterSet> o : rci.getAcceptedCharacterSets()) {
            clientInfo.getAcceptedCharacterSets().add(o);
        }

        for (Preference<Encoding> o : rci.getAcceptedEncodings()) {
            clientInfo.getAcceptedEncodings().add(o);
        }

        for (Preference<Language> o : rci.getAcceptedLanguages()) {
            clientInfo.getAcceptedLanguages().add(o);
        }

        for (Preference<MediaType> o : rci.getAcceptedMediaTypes()) {
            clientInfo.getAcceptedMediaTypes().add(o);
        }

        clientInfo.setAddress(rci.getAddress());
        clientInfo.setAgent(rci.getAgent());

        for (String o : rci.getForwardedAddresses()) {
            clientInfo.getForwardedAddresses().add(o);
        }

        clientInfo.setFrom(rci.getFrom());
        clientInfo.setPort(rci.getPort());

        // [ifndef gwt]
        clientInfo.setAgentAttributes(rci.getAgentAttributes());
        clientInfo.setAgentProducts(rci.getAgentProducts());
        clientInfo.setAuthenticated(rci.isAuthenticated());

        for (org.restlet.data.Expectation o : rci.getExpectations()) {
            clientInfo.getExpectations().add(o);
        }

        for (java.security.Principal o : rci.getPrincipals()) {
            clientInfo.getPrincipals().add(o);
        }

        for (org.restlet.security.Role o : rci.getRoles()) {
            clientInfo.getRoles().add(o);
        }

        clientInfo.setUser(rci.getUser());
        // [enddef]

        // Copy conditions
        conditions = new Conditions();

        for (Tag o : request.getConditions().getMatch()) {
            conditions.getMatch().add(o);
        }

        conditions.setModifiedSince(request.getConditions().getModifiedSince());

        for (Tag o : request.getConditions().getNoneMatch()) {
            conditions.getNoneMatch().add(o);
        }

        conditions.setRangeDate(request.getConditions().getRangeDate());
        conditions.setRangeTag(request.getConditions().getRangeTag());
        conditions.setUnmodifiedSince(request.getConditions()
                .getUnmodifiedSince());

        for (Cookie o : request.getCookies()) {
            getCookies().add(o);
        }

        this.hostRef = request.getHostRef();
        this.maxForwards = request.getMaxForwards();
        this.originalRef = (request.getOriginalRef() == null) ? null
                : new Reference(request.getOriginalRef());
        this.onResponse = request.getOnResponse();
        // [ifndef gwt] instruction
        this.proxyChallengeResponse = request.getProxyChallengeResponse();
        this.protocol = request.getProtocol();

        for (Range o : request.getRanges()) {
            getRanges().add(o);
        }

        this.referrerRef = (request.getReferrerRef() == null) ? null
                : new Reference(request.getReferrerRef());
        this.rootRef = (request.getRootRef() == null) ? null : request
                .getRootRef();

        for (Entry<String, Object> e : request.getAttributes().entrySet()) {
            getAttributes().put(e.getKey(), e.getValue());
        }

        for (CacheDirective o : request.getCacheDirectives()) {
            getCacheDirectives().add(o);
        }

        this.setOnSent(request.getOnSent());

        for (Warning o : request.getWarnings()) {
            getWarnings().add(o);
        }

        this.setDate(request.getDate());
    }

    /**
     * Ask the connector to attempt to abort the related network connection, for
     * example immediately closing the socket.
     * 
     * @return True if the request was aborted.
     */
    public boolean abort() {
        return false;
    }

    /**
     * Asks the server connector to immediately commit the given response
     * associated to this request, making it ready to be sent back to the
     * client. Note that all server connectors don't necessarily support this
     * feature.
     */
    public void commit(Response response) {
    }

    /**
     * Returns the modifiable set of headers the client is willing to send in
     * future request to this resource. Used when issuing a preflight CORS
     * request to let the origin server knows what headers will be sent later.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Allow-Headers" header.
     * 
     * @return The headers the client is willing to send in future request to
     *         this resource. Useful for CORS support.
     */
    public Set<String> getAccessControlRequestHeaders() {
        // Lazy initialization with double-check.
        Set<String> a = this.accessControlRequestHeaders;
        if (a == null) {
            synchronized (this) {
                a = this.accessControlRequestHeaders;
                if (a == null) {
                    this.accessControlRequestHeaders = a = new CopyOnWriteArraySet<String>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the method the client is willing to use in future request to this
     * resource. Used when issuing a preflight CORS request to let the origin
     * server knows what method will be sent later.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Request-Method" header.
     * 
     * @return The method the client is willing to send in future request to
     *         this resource. Useful for CORS support.
     */
    public Method getAccessControlRequestMethod() {
        return this.accessControlRequestMethod;
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * Note that when used with HTTP connectors, this property maps to the
     * "Authorization" header.
     * 
     * @return The authentication response sent by a client to an origin server.
     */
    public ChallengeResponse getChallengeResponse() {
        return this.challengeResponse;
    }

    /**
     * Returns the client-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The client-specific information.
     */
    public ClientInfo getClientInfo() {
        // Lazy initialization with double-check.
        ClientInfo c = this.clientInfo;
        if (c == null) {
            synchronized (this) {
                c = this.clientInfo;
                if (c == null) {
                    this.clientInfo = c = new ClientInfo();
                }
            }
        }
        return c;
    }

    /**
     * Returns the modifiable conditions applying to this request. Creates a new
     * instance if no one has been set.
     * 
     * @return The conditions applying to this call.
     */
    public Conditions getConditions() {
        // Lazy initialization with double-check.
        Conditions c = this.conditions;
        if (c == null) {
            synchronized (this) {
                c = this.conditions;
                if (c == null) {
                    this.conditions = c = new Conditions();
                }
            }
        }
        return c;
    }

    /**
     * Returns the modifiable series of cookies provided by the client. Creates
     * a new instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Cookie" header.
     * 
     * @return The cookies provided by the client.
     */
    public Series<Cookie> getCookies() {
        // Lazy initialization with double-check.
        Series<Cookie> c = this.cookies;
        if (c == null) {
            synchronized (this) {
                c = this.cookies;
                if (c == null) {
                    // [ifndef gwt] instruction
                    this.cookies = c = new Series<Cookie>(Cookie.class);
                    // [ifdef gwt] instruction uncomment
                    // this.cookies = c = new
                    // org.restlet.engine.util.CookieSeries();
                }
            }
        }
        return c;
    }

    /**
     * Returns the host reference. This may be different from the resourceRef's
     * host, for example for URNs and other URIs that don't contain host
     * information.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Host" header.
     * 
     * @return The host reference.
     */
    public Reference getHostRef() {
        return this.hostRef;
    }

    /**
     * Returns the maximum number of intermediaries.
     * 
     * @return The maximum number of intermediaries.
     */
    public int getMaxForwards() {
        return maxForwards;
    }

    /**
     * Returns the method.
     * 
     * @return The method.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Returns the callback invoked on response reception. If the value is not
     * null, then the associated request will be executed asynchronously.
     * 
     * @return The callback invoked on response reception.
     */
    public Uniform getOnResponse() {
        return onResponse;
    }

    /**
     * Returns the original reference as requested by the client. Note that this
     * property is not used during request routing. See the {@link #getResourceRef()} method for details.
     * 
     * @return The original reference.
     * @see #getResourceRef()
     */
    public Reference getOriginalRef() {
        return this.originalRef;
    }

    /**
     * Returns the protocol used or to be used, if known.
     * 
     * @return The protocol used or to be used.
     */
    public Protocol getProtocol() {
        Protocol result = this.protocol;

        if ((result == null) && (getResourceRef() != null)) {
            // Attempt to guess the protocol to use
            // from the target reference scheme
            result = getResourceRef().getSchemeProtocol();
            // Fallback: look at base reference scheme
            if (result == null) {
                result = (getResourceRef().getBaseRef() != null) ? getResourceRef()
                        .getBaseRef().getSchemeProtocol() : null;
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns the authentication response sent by a client to a proxy. Note
     * that when used with HTTP connectors, this property maps to the
     * "Proxy-Authorization" header.
     * 
     * @return The authentication response sent by a client to a proxy.
     */
    public ChallengeResponse getProxyChallengeResponse() {
        return this.proxyChallengeResponse;
    }

    /**
     * Returns the ranges to return from the target resource's representation.
     * Note that when used with HTTP connectors, this property maps to the
     * "Range" header.
     * 
     * @return The ranges to return.
     */
    public List<Range> getRanges() {
        // Lazy initialization with double-check.
        List<Range> r = this.ranges;
        if (r == null) {
            synchronized (this) {
                r = this.ranges;
                if (r == null) {
                    this.ranges = r = new CopyOnWriteArrayList<Range>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the referrer reference if available. Note that when used with
     * HTTP connectors, this property maps to the "Referer" header.
     * 
     * @return The referrer reference.
     */
    public Reference getReferrerRef() {
        return this.referrerRef;
    }

    /**
     * Returns the reference of the target resource. This reference is
     * especially important during routing, dispatching and resource finding.
     * During such processing, its base reference is constantly updated to
     * reflect the reference of the parent Restlet or resource and the remaining
     * part of the URI that must be routed or analyzed.
     * 
     * If you need to get the URI reference originally requested by the client,
     * then you should use the {@link #getOriginalRef()} method instead. Also,
     * note that beside the update of its base property, the resource reference
     * can be modified during the request processing.
     * 
     * For example, the {@link org.restlet.service.TunnelService} associated to
     * an application can extract some special extensions or query parameters
     * and replace them by semantically equivalent properties on the request
     * object. Therefore, the resource reference can become different from the
     * original reference.
     * 
     * Finally, when sending out requests via a dispatcher such as {@link Context#getClientDispatcher()} or
     * {@link Context#getServerDispatcher()}, if the reference contains URI
     * template variables, those variables are automatically resolved using the
     * request's attributes.
     * 
     * @return The reference of the target resource.
     * @see #getOriginalRef()
     * @see #getHostRef()
     */
    public Reference getResourceRef() {
        return this.resourceRef;
    }

    /**
     * Returns the application root reference.
     * 
     * @return The application root reference.
     */
    public Reference getRootRef() {
        return this.rootRef;
    }

    /**
     * Indicates if the request is asynchronous. The test consist in verifying
     * that the {@link #getOnResponse()} method returns a callback object.
     * 
     * @return True if the request is synchronous.
     */
    public boolean isAsynchronous() {
        return getOnResponse() != null;
    }

    /**
     * Implemented based on the {@link Protocol#isConfidential()} method for the
     * request's protocol returned by {@link #getProtocol()};
     */
    @Override
    public boolean isConfidential() {
        return (getProtocol() == null) ? false : getProtocol().isConfidential();
    }

    /**
     * Indicates if a content is available and can be sent. Several conditions
     * must be met: the method must allow the sending of content, the content
     * must exists and have some available data.
     * 
     * @return True if a content is available and can be sent.
     */
    @Override
    public boolean isEntityAvailable() {
        // The declaration of the "result" variable is a workaround for the GWT
        // platform.
        boolean result = (Method.GET.equals(getMethod())
                || Method.HEAD.equals(getMethod()));
        if (result) {
            return false;
        }

        return super.isEntityAvailable();
    }

    /**
     * Indicates if an associated response is expected.
     * 
     * @return True if an associated response is expected.
     */
    public boolean isExpectingResponse() {
        return (getMethod() == null) ? false : getMethod().isReplying();
    }

    /**
     * Indicates if the call is loggable
     * 
     * @return True if the call is loggable
     */
    public boolean isLoggable() {
        return loggable;
    }

    /**
     * Indicates if the request is synchronous. The test consist in verifying
     * that the {@link #getOnResponse()} method returns null.
     * 
     * @return True if the request is synchronous.
     */
    public boolean isSynchronous() {
        return getOnResponse() == null;
    }

    /**
     * Sets the set of headers the client is willing to use in future request to
     * this resource. Used when issuing a preflight CORS request to let the
     * origin server knows what headers will be sent later.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Request-Method" header.
     * 
     * @param accessControlRequestHeaders
     *            The set of headers the client is willing to send in future
     *            request to this resource. Useful for CORS support.
     */
    public void setAccessControlRequestHeaders(
            Set<String> accessControlRequestHeaders) {
        synchronized (getAccessControlRequestHeaders()) {
            if (accessControlRequestHeaders != this.accessControlRequestHeaders) {
                this.accessControlRequestHeaders.clear();

                if (accessControlRequestHeaders != null) {
                    this.accessControlRequestHeaders
                            .addAll(accessControlRequestHeaders);
                }
            }
        }
    }

    /**
     * Sets the method the client is willing to use in future request to this
     * resource. Used when issuing a preflight CORS request to let the origin
     * server knows what method will be sent later.<br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Access-Control-Request-Method" header.
     * 
     * @param accessControlRequestMethod
     *            The method the client is willing to send in future request to
     *            this resource. Useful for CORS support.
     */
    public void setAccessControlRequestMethod(Method accessControlRequestMethod) {
        this.accessControlRequestMethod = accessControlRequestMethod;
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * Note that when used with HTTP connectors, this property maps to the
     * "Authorization" header.
     * 
     * @param challengeResponse
     *            The authentication response sent by a client to an origin
     *            server.
     */
    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        this.challengeResponse = challengeResponse;
    }

    /**
     * Sets the client-specific information.
     * 
     * @param clientInfo
     *            The client-specific information.
     */
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * Sets the conditions applying to this request.
     * 
     * @param conditions
     *            The conditions applying to this request.
     */
    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    /**
     * Sets the modifiable series of cookies provided by the client. Note that
     * when used with HTTP connectors, this property maps to the "Cookie"
     * header. This method clears the current series and adds all entries in the
     * parameter series.
     * 
     * @param cookies
     *            A series of cookies provided by the client.
     */
    public void setCookies(Series<Cookie> cookies) {
        synchronized (getCookies()) {
            if (cookies != getCookies()) {
                if (getCookies() != null) {
                    getCookies().clear();
                }

                if (cookies != null) {
                    getCookies().addAll(cookies);
                }
            }
        }
    }

    /**
     * Sets the host reference. Note that when used with HTTP connectors, this
     * property maps to the "Host" header.
     * 
     * @param hostRef
     *            The host reference.
     */
    public void setHostRef(Reference hostRef) {
        this.hostRef = hostRef;
    }

    /**
     * Sets the host reference using an URI string. Note that when used with
     * HTTP connectors, this property maps to the "Host" header.
     * 
     * @param hostUri
     *            The host URI.
     */
    public void setHostRef(String hostUri) {
        setHostRef(new Reference(hostUri));
    }

    /**
     * Indicates if the call is loggable
     * 
     * @param loggable
     *            True if the call is loggable
     */
    public void setLoggable(boolean loggable) {
        this.loggable = loggable;
    }

    /**
     * Sets the maximum number of intermediaries.
     * 
     * @param maxForwards
     *            The maximum number of intermediaries.
     */
    public void setMaxForwards(int maxForwards) {
        this.maxForwards = maxForwards;
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *            The method called.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Sets the callback invoked on response reception. If the value is not
     * null, then the associated request will be executed asynchronously.
     * 
     * @param onResponseCallback
     *            The callback invoked on response reception.
     */
    public void setOnResponse(Uniform onResponseCallback) {
        this.onResponse = onResponseCallback;
    }

    /**
     * Sets the original reference requested by the client.
     * 
     * @param originalRef
     *            The original reference.
     * @see #getOriginalRef()
     */
    public void setOriginalRef(Reference originalRef) {
        this.originalRef = originalRef;
    }

    /**
     * Sets the protocol used or to be used.
     * 
     * @param protocol
     *            The protocol used or to be used.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    // [ifndef gwt] method
    /**
     * Sets the authentication response sent by a client to a proxy. Note that
     * when used with HTTP connectors, this property maps to the
     * "Proxy-Authorization" header.
     * 
     * @param challengeResponse
     *            The authentication response sent by a client to a proxy.
     */
    public void setProxyChallengeResponse(ChallengeResponse challengeResponse) {
        this.proxyChallengeResponse = challengeResponse;
    }

    /**
     * Sets the modifiable list of ranges to return from the target resource's
     * representation. Note that when used with HTTP connectors, this property
     * maps to the "Range" header. This method clears the current list and adds
     * all entries in the parameter list.
     * 
     * @param ranges
     *            A list of ranges.
     */
    public void setRanges(List<Range> ranges) {
        synchronized (getRanges()) {
            if (ranges != getRanges()) {
                getRanges().clear();

                if (ranges != null) {
                    getRanges().addAll(ranges);
                }
            }
        }
    }

    /**
     * Sets the referrer reference if available. Note that when used with HTTP
     * connectors, this property maps to the "Referer" header.
     * 
     * @param referrerRef
     *            The referrer reference.
     */
    public void setReferrerRef(Reference referrerRef) {
        this.referrerRef = referrerRef;

        // A referrer reference must not include a fragment.
        if ((this.referrerRef != null)
                && (this.referrerRef.getFragment() != null)) {
            this.referrerRef.setFragment(null);
        }
    }

    /**
     * Sets the referrer reference if available using an URI string. Note that
     * when used with HTTP connectors, this property maps to the "Referer"
     * header.
     * 
     * @param referrerUri
     *            The referrer URI.
     * @see #setReferrerRef(Reference)
     */
    public void setReferrerRef(String referrerUri) {
        setReferrerRef(new Reference(referrerUri));
    }

    /**
     * Sets the target resource reference. If the reference is relative, it will
     * be resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param resourceRef
     *            The resource reference.
     * @see #getResourceRef()
     */
    public void setResourceRef(Reference resourceRef) {
        this.resourceRef = resourceRef;
    }

    /**
     * Sets the target resource reference using an URI string. Note that the URI
     * can be either absolute or relative to the context's base reference.
     * 
     * @param resourceUri
     *            The resource URI.
     * @see #setResourceRef(Reference)
     */
    public void setResourceRef(String resourceUri) {
        if (getResourceRef() != null) {
            // Allow usage of URIs relative to the current base reference
            setResourceRef(new Reference(getResourceRef().getBaseRef(),
                    resourceUri));
        } else {
            setResourceRef(new Reference(resourceUri));
        }
    }

    /**
     * Sets the application root reference.
     * 
     * @param rootRef
     *            The application root reference.
     */
    public void setRootRef(Reference rootRef) {
        this.rootRef = rootRef;
    }

    /**
     * Displays a synthesis of the request like an HTTP request line.
     * 
     * @return A synthesis of the request like an HTTP request line.
     */
    public String toString() {
        return ((getMethod() == null) ? "" : getMethod().toString())
                + " "
                + ((getResourceRef() == null) ? "" : getResourceRef()
                        .toString())
                + " "
                + ((getProtocol() == null) ? ""
                        : (getProtocol().getName() + ((getProtocol()
                                .getVersion() == null) ? "" : "/"
                                + getProtocol().getVersion())));
    }

}
