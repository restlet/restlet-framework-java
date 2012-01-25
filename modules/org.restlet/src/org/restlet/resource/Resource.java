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

package org.restlet.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;
import org.restlet.service.StatusService;
import org.restlet.util.Series;

/**
 * Base resource class exposing the uniform REST interface. Intended conceptual
 * target of a hypertext reference. An uniform resource encapsulates a
 * {@link Context}, a {@link Request} and a {@link Response}, corresponding to a
 * specific target resource.<br>
 * <br>
 * It also defines a precise life cycle. First, the instance is created and the
 * final {@link #init(Context, Request, Response)} method is invoked, with a
 * chance for the developer to do some additional initialization by overriding
 * the {@link #doInit()} method.<br>
 * <br>
 * Then, the abstract {@link #handle()} method can be invoked. For concrete
 * behavior, see the {@link ClientResource} and {@link ServerResource}
 * subclasses. Note that the state of the resource can be changed several times
 * and the {@link #handle()} method called more than once, but always by the
 * same thread.<br>
 * <br>
 * Finally, the final {@link #release()} method can be called to clean-up the
 * resource, with a chance for the developer to do some additional clean-up by
 * overriding the {@link #doRelease()} method.<br>
 * <br>
 * Note also that throwable raised such as {@link Error} and {@link Exception}
 * can be caught in a single point by overriding the {@link #doCatch(Throwable)}
 * method.<br>
 * <br>
 * "The central feature that distinguishes the REST architectural style from
 * other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to
 * the component interface, the overall system architecture is simplified and
 * the visibility of interactions is improved. Implementations are decoupled
 * from the services they provide, which encourages independent evolvability."
 * Roy T. Fielding<br>
 * <br>
 * Concurrency note: contrary to the {@link org.restlet.Uniform} class and its
 * main {@link Restlet} subclass where a single instance can handle several
 * calls concurrently, one instance of {@link Resource} is created for each call
 * handled and accessed by only one thread at a time.
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel
 */
public abstract class Resource {

    // [ifndef gwt] member
    /** The parent application. */
    private volatile org.restlet.Application application;

    /** The parent context. */
    private volatile Context context;

    /** The handled request. */
    private volatile Request request;

    /** The handled response. */
    private volatile Response response;

    /**
     * Invoked when a {@link Throwable} is caught during initialization,
     * handling or releasing. By default, updates the responses's status with
     * the result of
     * {@link org.restlet.service.StatusService#getStatus(Throwable, Resource)}
     * .
     * 
     * @param throwable
     *            The caught error or exception.
     */
    protected void doCatch(Throwable throwable) {
        getLogger().log(Level.INFO, "Exception or error caught in resource",
                throwable);
    }

    /**
     * Invoked when an error response status is received.
     * 
     * @param errorStatus
     *            The error status received.
     */
    protected void doError(Status errorStatus) {
    }

    /**
     * Invoked when an error response status is received.
     * 
     * @param errorStatus
     *            The error status received.
     * @param errorMessage
     *            The custom error message.
     */
    protected final void doError(Status errorStatus, String errorMessage) {
        doError(new Status(errorStatus, errorMessage));
    }

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource. By default it does nothing.
     * 
     * @see #init(Context, Request, Response)
     */
    protected void doInit() throws ResourceException {
    }

    /**
     * Clean-up method that can be overridden in order to release the state of
     * the resource. By default it does nothing.
     * 
     * @see #release()
     */
    protected void doRelease() throws ResourceException {
    }

    /**
     * Returns the set of methods allowed for the current client by the
     * resource. The result can vary based on the client's user agent,
     * authentication and authorization data provided by the client.
     * 
     * @return The set of allowed methods.
     */
    public Set<Method> getAllowedMethods() {
        return getResponse() == null ? null : getResponse().getAllowedMethods();
    }

    // [ifndef gwt] method
    /**
     * Returns the parent application. If it wasn't set, it attempts to retrieve
     * the current one via {@link org.restlet.Application#getCurrent()} if it
     * exists, or instantiates a new one as a last resort.
     * 
     * @return The parent application if it exists, or a new one.
     */
    public org.restlet.Application getApplication() {
        org.restlet.Application result = this.application;

        if (result == null) {
            result = org.restlet.Application.getCurrent();

            if (result == null) {
                result = new org.restlet.Application(getContext());
            }

            this.application = result;
        }

        return result;
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client. If none is available, an empty list is returned.
     * 
     * @return The list of authentication requests.
     * @see Response#getChallengeRequests()
     */
    public List<ChallengeRequest> getChallengeRequests() {
        return getResponse() == null ? null : getResponse()
                .getChallengeRequests();
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * 
     * @return The authentication response sent by a client to an origin server.
     * @see Request#getChallengeResponse()
     */
    public ChallengeResponse getChallengeResponse() {
        return getRequest() == null ? null : getRequest()
                .getChallengeResponse();
    }

    /**
     * Returns the client-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The client-specific information.
     * @see Request#getClientInfo()
     */
    public ClientInfo getClientInfo() {
        return getRequest() == null ? null : getRequest().getClientInfo();
    }

    /**
     * Returns the modifiable conditions applying to this request. Creates a new
     * instance if no one has been set.
     * 
     * @return The conditions applying to this call.
     * @see Request#getConditions()
     */
    public Conditions getConditions() {
        return getRequest() == null ? null : getRequest().getConditions();
    }

    /**
     * Returns the current context.
     * 
     * @return The current context.
     */
    public Context getContext() {
        return context;
    }

    // [ifndef gwt] method
    /**
     * Returns the application's content negotiation service or create a new
     * one.
     * 
     * @return The content negotiation service.
     */
    public org.restlet.service.ConnegService getConnegService() {
        org.restlet.service.ConnegService result = null;

        // [ifndef gwt] instruction
        result = getApplication().getConnegService();

        if (result == null) {
            result = new org.restlet.service.ConnegService();
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns the application's converter service or create a new one.
     * 
     * @return The converter service.
     */
    public org.restlet.service.ConverterService getConverterService() {
        org.restlet.service.ConverterService result = null;

        // [ifndef gwt] instruction
        result = getApplication().getConverterService();

        if (result == null) {
            result = new org.restlet.service.ConverterService();
        }

        return result;
    }

    /**
     * Returns the modifiable series of cookies provided by the client. Creates
     * a new instance if no one has been set.
     * 
     * @return The cookies provided by the client.
     * @see Request#getCookies()
     */
    public Series<Cookie> getCookies() {
        return getRequest() == null ? null : getRequest().getCookies();
    }

    /**
     * Returns the modifiable series of cookie settings provided by the server.
     * Creates a new instance if no one has been set.
     * 
     * @return The cookie settings provided by the server.
     * @see Response#getCookieSettings()
     */
    public Series<CookieSetting> getCookieSettings() {
        return getResponse() == null ? null : getResponse().getCookieSettings();
    }

    /**
     * Returns the modifiable set of selecting dimensions on which the response
     * entity may vary. If some server-side content negotiation is done, this
     * set should be properly updated, other it can be left empty. Creates a new
     * instance if no one has been set.
     * 
     * @return The set of dimensions on which the response entity may vary.
     * @see Response#getDimensions()
     */
    public Set<Dimension> getDimensions() {
        return getResponse() == null ? null : getResponse().getDimensions();
    }

    /**
     * Returns the host reference. This may be different from the resourceRef's
     * host, for example for URNs and other URIs that don't contain host
     * information.
     * 
     * @return The host reference.
     * @see Request#getHostRef()
     */
    public Reference getHostRef() {
        return getRequest() == null ? null : getRequest().getHostRef();
    }

    /**
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     * @see Response#getLocationRef()
     */
    public Reference getLocationRef() {
        return getResponse() == null ? null : getResponse().getLocationRef();
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return getContext() != null ? getContext().getLogger() : Context
                .getCurrentLogger();
    }

    /**
     * Returns the resource reference's optional matrix.
     * 
     * @return The resource reference's optional matrix.
     * @see Reference#getMatrixAsForm()
     */
    public Form getMatrix() {
        return getReference() == null ? null : getReference().getMatrixAsForm();
    }

    /**
     * Returns the maximum number of intermediaries.
     * 
     * @return The maximum number of intermediaries.
     */
    public int getMaxForwards() {
        return getRequest() == null ? null : getRequest().getMaxForwards();
    }

    /**
     * Returns the application's metadata service or create a new one.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        MetadataService result = null;

        // [ifndef gwt] instruction
        result = getApplication().getMetadataService();

        if (result == null) {
            result = new MetadataService();
        }

        return result;
    }

    /**
     * Returns the method.
     * 
     * @return The method.
     * @see Request#getMethod()
     */
    public Method getMethod() {
        return getRequest() == null ? null : getRequest().getMethod();
    }

    /**
     * Returns the original reference as requested by the client. Note that this
     * property is not used during request routing.
     * 
     * @return The original reference.
     * @see Request#getOriginalRef()
     */
    public Reference getOriginalRef() {
        return getRequest() == null ? null : getRequest().getOriginalRef();
    }

    /**
     * Returns the protocol by first returning the resourceRef.schemeProtocol
     * property if it is set, or the baseRef.schemeProtocol property otherwise.
     * 
     * @return The protocol or null if not available.
     * @see Request#getProtocol()
     */
    public Protocol getProtocol() {
        return getRequest() == null ? null : getRequest().getProtocol();
    }

    /**
     * Returns the resource reference's optional query. Note that modifications
     * to the returned {@link Form} object aren't reported to the underlying
     * reference.
     * 
     * @return The resource reference's optional query.
     * @see Reference#getQueryAsForm()
     */
    public Form getQuery() {
        return getReference() == null ? null : getReference().getQueryAsForm();
    }

    /**
     * Returns the ranges to return from the target resource's representation.
     * 
     * @return The ranges to return.
     * @see Request#getRanges()
     */
    public List<Range> getRanges() {
        return getRequest() == null ? null : getRequest().getRanges();
    }

    /**
     * Returns the URI reference.
     * 
     * @return The URI reference.
     */
    public Reference getReference() {
        return getRequest() == null ? null : getRequest().getResourceRef();
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    public Reference getReferrerRef() {
        return getRequest() == null ? null : getRequest().getReferrerRef();
    }

    /**
     * Returns the handled request.
     * 
     * @return The handled request.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the request attributes.
     * 
     * @return The request attributes.
     * @see Request#getAttributes()
     */
    public Map<String, Object> getRequestAttributes() {
        return getRequest() == null ? null : getRequest().getAttributes();
    }

    /**
     * Returns the request cache directives.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Cache-Control" header.
     * 
     * @return The cache directives.
     */
    public List<CacheDirective> getRequestCacheDirectives() {
        return getRequest() == null ? null : getRequest().getCacheDirectives();
    }

    /**
     * Returns the request entity representation.
     * 
     * @return The request entity representation.
     */
    public Representation getRequestEntity() {
        return getRequest() == null ? null : getRequest().getEntity();
    }

    /**
     * Returns the handled response.
     * 
     * @return The handled response.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Returns the response attributes.
     * 
     * @return The response attributes.
     * @see Response#getAttributes()
     */
    public Map<String, Object> getResponseAttributes() {
        return getResponse() == null ? null : getResponse().getAttributes();
    }

    /**
     * Returns the response cache directives.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Cache-Control" header.
     * 
     * @return The cache directives.
     */
    public List<CacheDirective> getResponseCacheDirectives() {
        return getResponse() == null ? null : getResponse()
                .getCacheDirectives();
    }

    /**
     * Returns the response entity representation.
     * 
     * @return The response entity representation.
     */
    public Representation getResponseEntity() {
        return getResponse() == null ? null : getResponse().getEntity();
    }

    /**
     * Returns the application root reference.
     * 
     * @return The application root reference.
     * @see Request#getRootRef()
     */
    public Reference getRootRef() {
        return getRequest() == null ? null : getRequest().getRootRef();
    }

    /**
     * Returns the server-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The server-specific information.
     * @see Response#getServerInfo()
     */
    public ServerInfo getServerInfo() {
        return getResponse() == null ? null : getResponse().getServerInfo();
    }

    /**
     * Returns the status.
     * 
     * @return The status.
     * @see Response#getStatus()
     */
    public Status getStatus() {
        return getResponse() == null ? null : getResponse().getStatus();
    }

    /**
     * Returns the application's status service or create a new one.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        StatusService result = null;

        // [ifndef gwt] instruction
        result = getApplication().getStatusService();

        if (result == null) {
            result = new StatusService();
        }

        return result;
    }

    /**
     * Handles the call composed of the current context, request and response.
     * 
     * @return The optional response entity.
     */
    public abstract Representation handle();

    /**
     * Initialization method setting the environment of the current resource
     * instance. It the calls the {@link #doInit()} method that can be
     * overridden.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public void init(Context context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;

        try {
            doInit();
        } catch (Throwable t) {
            doCatch(t);
        }
    }

    /**
     * Indicates if the message was or will be exchanged confidentially, for
     * example via a SSL-secured connection.
     * 
     * @return True if the message is confidential.
     * @see Request#isConfidential()
     */
    public boolean isConfidential() {
        return getRequest() == null ? null : getRequest().isConfidential();
    }

    /**
     * Indicates if the call is loggable
     * 
     * @return True if the call is loggable
     */
    public boolean isLoggable() {
        return getRequest() == null ? null : getRequest().isLoggable();
    }

    /**
     * Releases the resource by calling {@link #doRelease()}.
     */
    public final void release() {
        try {
            doRelease();
        } catch (Throwable t) {
            doCatch(t);
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the parent application.
     * 
     * @param application
     *            The parent application.
     */
    public void setApplication(org.restlet.Application application) {
        this.application = application;
    }

    /**
     * Sets the handled request.
     * 
     * @param request
     *            The handled request.
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the handled response.
     * 
     * @param response
     *            The handled response.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    // [ifndef gwt] method
    /**
     * Converts a representation into a Java object. Leverages the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param source
     *            The source representation to convert.
     * @param target
     *            The target class of the Java object.
     * @return The converted Java object.
     * @throws ResourceException
     */
    public <T> T toObject(Representation source, Class<T> target)
            throws ResourceException {
        T result = null;

        if (source != null) {
            try {
                org.restlet.service.ConverterService cs = getConverterService();
                result = cs.toObject(source, target, this);
            } catch (Exception e) {
                throw new ResourceException(e);
            }
        }

        return result;
    }

    /**
     * Converts an object into a representation based on client preferences.
     * 
     * @param source
     *            The object to convert.
     * @param target
     *            The target representation variant.
     * @return The wrapper representation.
     */
    public Representation toRepresentation(Object source, Variant target) {
        Representation result = null;

        if (source != null) {
            // [ifndef gwt]
            org.restlet.service.ConverterService cs = getConverterService();
            result = cs.toRepresentation(source, target, this);
            // [enddef]
            // [ifdef gwt] uncomment
            // if (source instanceof Representation) {
            // result = (Representation) source;
            // }
            // [enddef]
        }

        return result;
    }

    @Override
    public String toString() {
        return (getRequest() == null ? "" : getRequest().toString())
                + (getResponse() == null ? "" : " => "
                        + getResponse().toString());
    }
}
