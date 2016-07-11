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

package org.restlet.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.util.Series;

/**
 * Client-side resource. Acts like a proxy of a target resource.<br>
 * This class changes the semantics of the {@link Resource#getRequest()} and
 * {@link Resource#getResponse()} methods. Since a clientResource may receive
 * severals responses for a single request (in case of interim response), the
 * {@link #getResponse()} method returns the last received response object. The
 * Request object returned by the {@link #getRequest()} is actually a prototype
 * which is cloned (except the representation) just before the {@link #handle()}
 * method is called.<br>
 * Users must be aware that by most representations can only be read or written
 * once. Some others, such as {@link StringRepresentation} stored the entity in
 * memory which can be read several times but has the drawback to consume
 * memory.<br>
 * Concurrency note: instances of the class are not designed to be shared among
 * several threads. If thread-safety is necessary, consider using the
 * lower-level {@link Client} class instead.
 * 
 * @author Jerome Louvel
 */
public class ClientResource extends Resource {

    // [ifndef gwt] method
    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls. It basically creates a new instance of
     * {@link ClientResource} and invokes the {@link #wrap(Class)} method.
     * 
     * @param <T>
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Context context, Reference reference,
            Class<? extends T> resourceInterface) {
        ClientResource clientResource = new ClientResource(context, reference);
        return clientResource.wrap(resourceInterface);
    }

    // [ifndef gwt] method
    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls. It basically creates a new instance of
     * {@link ClientResource} and invokes the {@link #wrap(Class)} method.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Reference reference,
            Class<? extends T> resourceInterface) {
        return create(null, reference, resourceInterface);
    }

    // [ifndef gwt] method
    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls. It basically creates a new instance of
     * {@link ClientResource} and invokes the {@link #wrap(Class)} method.
     * 
     * @param <T>
     * @param uri
     *            The target URI.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(String uri, Class<? extends T> resourceInterface) {
        return create(null, new Reference(uri), resourceInterface);
    }

    // [ifndef gwt] member
    /** Indicates if redirections should be automatically followed. */
    private volatile boolean followingRedirects;

    /**
     * Indicates if maximum number of redirections that can be automatically
     * followed for a single call.
     */
    private volatile int maxRedirects;

    /** The next Restlet. */
    private volatile Uniform next;

    /** Indicates if the next Restlet has been created. */
    private volatile boolean nextCreated;

    // [ifndef gwt] member
    /**
     * Indicates if transient or unknown size request entities should be
     * buffered before being sent.
     */
    private volatile boolean requestEntityBuffering;

    // [ifndef gwt] member
    /**
     * Indicates if transient or unknown size response entities should be
     * buffered after being received.
     */
    private volatile boolean responseEntityBuffering;

    /** Number of retry attempts before reporting an error. */
    private volatile int retryAttempts;

    /** Delay in milliseconds between two retry attempts. */
    private volatile long retryDelay;

    /** Indicates if idempotent requests should be retried on error. */
    private volatile boolean retryOnError;

    /**
     * Empty constructor.
     */
    protected ClientResource() {
    }

    /**
     * Constructor.
     * 
     * @param resource
     *            The client resource to copy.
     */
    public ClientResource(ClientResource resource) {
        Request request = new Request(resource.getRequest());
        Response response = new Response(request);
        this.next = resource.getNext();
        this.maxRedirects = resource.getMaxRedirects();
        this.retryOnError = resource.isRetryOnError();
        this.retryDelay = resource.getRetryDelay();
        this.retryAttempts = resource.getRetryAttempts();

        // [ifndef gwt]
        this.followingRedirects = resource.isFollowingRedirects();
        this.requestEntityBuffering = resource.isRequestEntityBuffering();
        this.responseEntityBuffering = resource.isResponseEntityBuffering();
        setApplication(resource.getApplication());
        // [enddef]
        init(resource.getContext(), request, response);
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, java.net.URI uri) {
        this(context, Method.GET, uri);
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, java.net.URI uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Method method, Reference reference) {
        this(context, new Request(method, reference), new Response(null));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, String uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Reference reference) {
        this(context, Method.GET, reference);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     */
    public ClientResource(Context context, Request request) {
        this(context, request, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Context context, Request request, Response response) {
        if (context == null) {
            context = Context.getCurrent();
        }

        // Don't remove this line.
        // See other constructor ClientResource(Context, Method, Reference)
        response.setRequest(request);

        this.maxRedirects = 10;
        this.retryOnError = true;
        this.retryDelay = 2000L;
        this.retryAttempts = 2;
        // [ifndef gwt]
        this.followingRedirects = true;
        this.requestEntityBuffering = false;
        this.responseEntityBuffering = false;
        // [enddef]
        init(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, String uri) {
        this(context, Method.GET, uri);
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(java.net.URI uri) {
        this(Context.getCurrent(), null, uri);
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, java.net.URI uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Method method, Reference reference) {
        this(Context.getCurrent(), method, reference);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, String uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public ClientResource(Reference reference) {
        this(Context.getCurrent(), null, reference);
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     */
    public ClientResource(Request request) {
        this(request, new Response(request));
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Request request, Response response) {
        this(Context.getCurrent(), request, response);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(String uri) {
        this(Context.getCurrent(), Method.GET, uri);
    }

    /**
     * Updates the client preferences to accept the given metadata (media types,
     * character sets, etc.) with a 1.0 quality in addition to existing ones.
     * 
     * @param metadata
     *            The metadata to accept.
     * @see ClientInfo#accept(Metadata...)
     */
    public void accept(Metadata... metadata) {
        getClientInfo().accept(metadata);
    }

    /**
     * Updates the client preferences to accept the given metadata (media types,
     * character sets, etc.) with a given quality in addition to existing ones.
     * 
     * @param metadata
     *            The metadata to accept.
     * @param quality
     *            The quality to set.
     * @see ClientInfo#accept(Metadata, float)
     */
    public void accept(Metadata metadata, float quality) {
        getClientInfo().accept(metadata, quality);
    }

    /**
     * Adds a parameter to the query component. The name and value are
     * automatically encoded if necessary.
     * 
     * @param parameter
     *            The parameter to add.
     * @return The updated reference.
     * @see Reference#addQueryParameter(Parameter)
     */
    public Reference addQueryParameter(Parameter parameter) {
        return getReference().addQueryParameter(parameter);
    }

    /**
     * Adds a parameter to the query component. The name and value are
     * automatically encoded if necessary.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The optional parameter value.
     * @return The updated reference.
     * @see Reference#addQueryParameter(String, String)
     */
    public Reference addQueryParameter(String name, String value) {
        return getReference().addQueryParameter(name, value);
    }

    /**
     * Adds several parameters to the query component. The name and value are
     * automatically encoded if necessary.
     * 
     * @param parameters
     *            The parameters to add.
     * @return The updated reference.
     * @see Reference#addQueryParameters(Iterable)
     */
    public Reference addQueryParameters(Iterable<Parameter> parameters) {
        return getReference().addQueryParameters(parameters);
    }

    /**
     * Adds a segment at the end of the path. If the current path doesn't end
     * with a slash character, one is inserted before the new segment value. The
     * value is automatically encoded if necessary.
     * 
     * @param value
     *            The segment value to add.
     * @return The updated reference.
     * @see Reference#addSegment(String)
     */
    public Reference addSegment(String value) {
        return getReference().addSegment(value);
    }

    /**
     * Creates a next Restlet is no one is set. By default, it creates a new
     * {@link Client} based on the protocol of the resource's URI reference.
     * 
     * @return The created next Restlet or null.
     */
    protected Uniform createNext() {
        Uniform result = null;

        // [ifndef gwt]
        // Prefer the outbound root
        result = getApplication().getOutboundRoot();
        // [enddef]

        if ((result == null) && (getContext() != null)) {
            // Try using directly the client dispatcher
            result = getContext().getClientDispatcher();
        }

        if (result == null) {
            // As a final option, try creating a client connector
            // [ifdef gwt] uncomment
            // if (getReference().isRelative()) {
            // getReference().setBaseRef(
            // com.google.gwt.core.client.GWT.getHostPageBaseURL());
            // setReference(getReference().getTargetRef());
            // }
            // [enddef]

            Protocol rProtocol = getProtocol();
            Reference rReference = getReference();
            Protocol protocol = (rProtocol != null) ? rProtocol
                    : (rReference != null) ? rReference.getSchemeProtocol()
                            : null;

            if (protocol != null) {
                // [ifndef gwt]
                org.restlet.engine.util.TemplateDispatcher dispatcher = new org.restlet.engine.util.TemplateDispatcher();
                dispatcher.setContext(getContext());
                dispatcher.setNext(new Client(protocol));
                result = dispatcher;
                // [enddef]
                // [ifdef gwt] uncomment
                // result = new Client(protocol);
                // [enddef]
            }
        }

        return result;
    }

    /**
     * Creates a new request by cloning the one wrapped by this class.
     * 
     * @return The new response.
     * @see #getRequest()
     */
    public Request createRequest() {
        return new Request(getRequest());
    }

    /**
     * Creates a new response for the given request.
     * 
     * @param request
     *            The associated request.
     * @return The new response.
     */
    protected Response createResponse(Request request) {
        return new Response(request);
    }

    /**
     * Deletes the target resource and all its representations. If a success
     * status is not returned, then a resource exception is thrown.
     * 
     * @return The optional response entity.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7">HTTP
     *      DELETE method</a>
     */
    public Representation delete() throws ResourceException {
        return handle(Method.DELETE);
    }

    // [ifndef gwt] method
    /**
     * Deletes the target resource and all its representations. If a success
     * status is not returned, then a resource exception is thrown.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7">HTTP
     *      DELETE method</a>
     */
    public <T> T delete(Class<T> resultClass) throws ResourceException {
        return handle(Method.DELETE, resultClass);
    }

    /**
     * Deletes the target resource and all its representations. If a success
     * status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7">HTTP
     *      DELETE method</a>
     */
    public Representation delete(MediaType mediaType) throws ResourceException {
        return handle(Method.DELETE, mediaType);
    }

    /**
     * By default, it throws a new resource exception.
     * 
     * @param errorStatus
     *            The error status received.
     */
    @Override
    public void doError(Status errorStatus) {
        throw new ResourceException(errorStatus, this);
    }

    /**
     * Releases the resource by stopping any connector automatically created and
     * associated to the "next" property (see {@link #getNext()} method.
     */
    @Override
    protected void doRelease() throws ResourceException {
        if ((getNext() != null) && this.nextCreated) {
            if (getNext() instanceof Restlet) {
                try {
                    ((Restlet) getNext()).stop();
                } catch (Exception e) {
                    throw new ResourceException(e);
                }
            }

            setNext(null);
        }
    }

    /**
     * Attempts to {@link #release()} the resource.
     */
    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences. Note that the client preferences
     * will be automatically adjusted, but only for this request. If you want to
     * change them once for all, you can use the {@link #getClientInfo()}
     * method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get() throws ResourceException {
        return handle(Method.GET);
    }

    // [ifndef gwt] method
    /**
     * Represents the resource in the given object class. Note that the client
     * preferences will be automatically adjusted, but only for this request. If
     * you want to change them once for all, you can use the
     * {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public <T> T get(Class<T> resultClass) throws ResourceException {
        return handle(Method.GET, resultClass);
    }

    /**
     * Represents the resource using a given media type. Note that the client
     * preferences will be automatically adjusted, but only for this request. If
     * you want to change them once for all, you can use the
     * {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get(MediaType mediaType) throws ResourceException {
        return handle(Method.GET, mediaType);
    }

    /**
     * Returns the attribute value by looking up the given name in the response
     * attributes maps. The toString() method is then invoked on the attribute
     * value.
     * 
     * @param name
     *            The attribute name.
     * @return The response attribute value.
     */
    public String getAttribute(String name) {
        Object value = getResponseAttributes().get(name);
        return (value == null) ? null : value.toString();
    }

    /**
     * Returns the child resource defined by its URI relatively to the current
     * resource. The child resource is defined in the sense of hierarchical
     * URIs. If the resource URI is not hierarchical, then an exception is
     * thrown.
     * 
     * @param relativeRef
     *            The URI reference of the child resource relatively to the
     *            current resource seen as the parent resource.
     * @return The child resource.
     * @throws ResourceException
     */
    public ClientResource getChild(Reference relativeRef)
            throws ResourceException {
        ClientResource result = null;

        if ((relativeRef != null) && relativeRef.isRelative()) {
            result = new ClientResource(this);
            result.setReference(new Reference(getReference().getTargetRef(),
                    relativeRef).getTargetRef());
        } else {
            doError(Status.CLIENT_ERROR_BAD_REQUEST, "The child URI is not relative.");
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Wraps the child client resource to proxy calls to the given Java
     * interface into Restlet method calls. The child resource is defined in the
     * sense of hierarchical URIs. If the resource URI is not hierarchical, then
     * an exception is thrown.
     * 
     * @param <T>
     * @param relativeRef
     *            The URI reference of the child resource relatively to the
     *            current resource seen as the parent resource.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public <T> T getChild(Reference relativeRef,
            Class<? extends T> resourceInterface) throws ResourceException {
        T result = null;
        ClientResource childResource = getChild(relativeRef);

        if (childResource != null) {
            result = childResource.wrap(resourceInterface);
        }

        return result;
    }

    /**
     * Returns the child resource defined by its URI relatively to the current
     * resource. The child resource is defined in the sense of hierarchical
     * URIs. If the resource URI is not hierarchical, then an exception is
     * thrown.
     * 
     * @param relativeUri
     *            The URI of the child resource relatively to the current
     *            resource seen as the parent resource.
     * @return The child resource.
     * @throws ResourceException
     */
    public ClientResource getChild(String relativeUri) throws ResourceException {
        return getChild(new Reference(relativeUri));
    }

    // [ifndef gwt] method
    /**
     * Wraps the child client resource to proxy calls to the given Java
     * interface into Restlet method calls. The child resource is defined in the
     * sense of hierarchical URIs. If the resource URI is not hierarchical, then
     * an exception is thrown.
     * 
     * @param <T>
     * @param relativeUri
     *            The URI of the child resource relatively to the current
     *            resource seen as the parent resource.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public <T> T getChild(String relativeUri,
            Class<? extends T> resourceInterface) throws ResourceException {
        return getChild(new Reference(relativeUri), resourceInterface);
    }

    /**
     * Returns the maximum number of redirections that can be automatically
     * followed for a single call. Default value is 10.
     * 
     * @return The maximum number of redirections that can be automatically
     *         followed for a single call.
     */
    public int getMaxRedirects() {
        return maxRedirects;
    }

    /**
     * Returns the next Restlet. By default, it is the client dispatcher if a
     * context is available.
     * 
     * @return The next Restlet or null.
     */
    public Uniform getNext() {
        Uniform result = this.next;

        if (result == null) {
            synchronized (this) {
                if (result == null) {
                    result = createNext();

                    if (result != null) {
                        setNext(result);
                        this.nextCreated = true;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the callback invoked on response reception. If the value is not
     * null, then the associated request will be executed asynchronously.
     * 
     * @return The callback invoked on response reception.
     */
    public Uniform getOnResponse() {
        return getRequest().getOnResponse();
    }

    /**
     * Returns the callback invoked after sending the request.
     * 
     * @return The callback invoked after sending the request.
     */
    public Uniform getOnSent() {
        return getRequest().getOnSent();
    }

    /**
     * Returns the parent resource. The parent resource is defined in the sense
     * of hierarchical URIs. If the resource URI is not hierarchical, then an
     * exception is thrown.
     * 
     * @return The parent resource.
     */
    public ClientResource getParent() throws ResourceException {
        ClientResource result = null;

        if (getReference().isHierarchical()) {
            result = new ClientResource(this);
            result.setReference(getReference().getParentRef());
        } else {
            doError(Status.CLIENT_ERROR_BAD_REQUEST, "The resource URI is not hierarchical.");
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Wraps the parent client resource to proxy calls to the given Java
     * interface into Restlet method calls. The parent resource is defined in
     * the sense of hierarchical URIs. If the resource URI is not hierarchical,
     * then an exception is thrown.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public <T> T getParent(Class<? extends T> resourceInterface)
            throws ResourceException {
        T result = null;

        ClientResource parentResource = getParent();
        if (parentResource != null) {
            result = parentResource.wrap(resourceInterface);
        }

        return result;
    }

    /**
     * Returns the number of retry attempts before reporting an error. Default
     * value is 2.
     * 
     * @return The number of retry attempts before reporting an error.
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Returns the delay in milliseconds between two retry attempts. Default
     * value is 2 seconds.
     * 
     * @return The delay in milliseconds between two retry attempts.
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Handles the call by invoking the next handler. The prototype request is
     * retrieved via {@link #getRequest()} and cloned and the response is set as
     * the latest with {@link #setResponse(Response)}. If necessary the
     * {@link #setNext(Uniform)} method is called as well with a {@link Client}
     * instance matching the request protocol.
     * 
     * @return The optional response entity.
     * @see #getNext()
     */
    @Override
    public Representation handle() {
        Response response = handleOutbound(createRequest());
        return (response == null) ? null : response.getEntity();
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @return The optional response entity.
     */
    protected Representation handle(Method method) {
        return handle(method, (Representation) null);
    }

    // [ifndef gwt] method
    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param method
     *            The request method to use.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     * @throws ResourceException
     */
    protected <T> T handle(Method method, Class<T> resultClass)
            throws ResourceException {
        return handle(method, null, resultClass);
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @param mediaType
     *            The preferred result media type.
     * @return The optional response entity.
     */
    protected Representation handle(Method method, MediaType mediaType) {
        return handle(method, (Representation) null, mediaType);
    }

    // [ifndef gwt] method
    /**
     * Handles an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param method
     *            The request method to use.
     * @param entity
     *            The object entity to post.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     */
    protected <T> T handle(Method method, Object entity, Class<T> resultClass)
            throws ResourceException {
        org.restlet.service.ConverterService cs = getConverterService();
        ClientInfo clientInfo = getClientInfo();

        if (clientInfo.getAcceptedMediaTypes().isEmpty()) {
            cs.updatePreferences(clientInfo.getAcceptedMediaTypes(),
                    resultClass);
        }

        // Prepare the request by cloning the prototype request
        Request request = createRequest();
        request.setMethod(method);
        request.setClientInfo(clientInfo);

        if (entity != null) {
            List<? extends Variant> entityVariants;
            try {
                entityVariants = cs.getVariants(entity.getClass(), null);
                request.setEntity(toRepresentation(
                        entity,
                        getConnegService().getPreferredVariant(entityVariants,
                                request, getMetadataService())));
            } catch (IOException e) {
                throw new ResourceException(e);
            }
        } else {
            request.setEntity(null);
        }

        // Actually handle the call
        Response response = handleOutbound(request);
        Representation responseEntity = handleInbound(response);
        return toObject(responseEntity, resultClass);
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @param entity
     *            The request entity to set.
     * @return The optional response entity.
     */
    protected Representation handle(Method method, Representation entity) {
        return handle(method, entity, getClientInfo());
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @param entity
     *            The request entity to set.
     * @param clientInfo
     *            The client preferences.
     * @return The optional response entity.
     */
    protected Representation handle(Method method, Representation entity,
            ClientInfo clientInfo) {
        // Prepare the request by cloning the prototype request
        Request request = createRequest();
        request.setMethod(method);
        request.setEntity(entity);
        request.setClientInfo(clientInfo);

        // Actually handle the call
        Response response = handleOutbound(request);
        return handleInbound(response);
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @param entity
     *            The request entity to set.
     * @param mediaType
     *            The preferred result media type.
     * @return The optional response entity.
     */
    protected Representation handle(Method method, Representation entity,
            MediaType mediaType) {
        return handle(method, entity, new ClientInfo(mediaType));
    }

    /**
     * Handle the call and follow redirection for safe methods.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            The response to update.
     * @param references
     *            The references that caused a redirection to prevent infinite
     *            loops.
     * @param retryAttempt
     *            The number of remaining attempts.
     * @param next
     *            The next handler handling the call.
     */
    protected void handle(Request request, Response response,
            List<Reference> references, int retryAttempt, Uniform next) {
        if (next != null) {
            // [ifndef gwt]
            // Check if request entity buffering must be done
            if (isRequestEntityBuffering()) {
                request.bufferEntity();
            }
            // [enddef]

            // Actually handle the call
            next.handle(request, response);

            if (isRetryOnError()
                    && response.getStatus().isRecoverableError()
                    && request.getMethod().isIdempotent()
                    && (retryAttempt < getRetryAttempts())
                    && ((request.getEntity() == null) || request.getEntity()
                            .isAvailable())) {
                retry(request, response, references, retryAttempt, next);
            }
            // [ifndef gwt]
            else if (isFollowingRedirects()
                    && response.getStatus().isRedirection()
                    && (response.getLocationRef() != null)) {
                boolean doRedirection = false;

                if (request.getMethod().isSafe()) {
                    doRedirection = true;
                } else {
                    if (Status.REDIRECTION_SEE_OTHER.equals(response
                            .getStatus())) {
                        // The user agent is redirected using the GET method
                        request.setMethod(Method.GET);
                        request.setEntity(null);
                        doRedirection = true;
                    } else if (Status.REDIRECTION_USE_PROXY.equals(response
                            .getStatus())) {
                        doRedirection = true;
                    }
                }

                if (doRedirection) {
                    redirect(request, response, references, retryAttempt, next);
                } else {
                    getLogger().fine(
                            "Unable to redirect the client call after a response"
                                    + response);
                }
            }

            // Check if response entity buffering must be done
            if (isResponseEntityBuffering()) {
                response.bufferEntity();
            }
            // [enddef]
        } else {
            getLogger().log(Level.WARNING,
                    "Request ignored as no next Restlet is available");
        }
    }

    /**
     * Handles the inbound call. Note that only synchronous calls are processed.
     * 
     * @param response
     * @return The response's entity, if any.
     */
    public Representation handleInbound(Response response) {
        if (response == null) {
            return null;
        }

        // Verify that the request was synchronous
        if (response.getRequest().isSynchronous()) {
            if (response.getStatus().isError()) {
                doError(response.getStatus());
                return null;
            }
            return response.getEntity();
        }

        return null;
    }

    /**
     * Handles the outbound call by invoking the next handler.
     * 
     * @param request
     *            The request to handle.
     * @return The response created.
     * @see #getNext()
     */
    public Response handleOutbound(Request request) {
        Response response = createResponse(request);
        Uniform next = getNext();

        if (next != null) {
            // Effectively handle the call
            handle(request, response, null, 0, next);

            // Update the last received response.
            setResponse(response);
        } else {
            getLogger()
                    .warning(
                            "Unable to process the call for a client resource. No next Restlet has been provided.");
        }

        return response;
    }

    /**
     * Indicates if there is a next Restlet.
     * 
     * @return True if there is a next Restlet.
     */
    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences. This method is identical to
     * {@link #get()} but doesn't return the actual content of the
     * representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head() throws ResourceException {
        return handle(Method.HEAD);
    }

    /**
     * Represents the resource using a given media type. This method is
     * identical to {@link #get(MediaType)} but doesn't return the actual
     * content of the representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head(MediaType mediaType) throws ResourceException {
        return handle(Method.HEAD, mediaType);
    }

    // [ifndef gwt] method
    /**
     * Indicates if redirections are followed.
     * 
     * @return True if redirections are followed.
     */
    public boolean isFollowingRedirects() {
        return followingRedirects;
    }

    // [ifndef gwt] method
    /**
     * Indicates if transient or unknown size response entities should be
     * buffered after being received. This is useful to increase the chance of
     * being able to resubmit a failed request due to network error, or to
     * prevent chunked encoding from being used an HTTP connector.
     * 
     * @return True if transient response entities should be buffered after
     *         being received.
     */
    public boolean isRequestEntityBuffering() {
        return requestEntityBuffering;
    }

    // [ifndef gwt] method
    /**
     * Indicates if transient or unknown size response entities should be
     * buffered after being received. This is useful to be able to
     * systematically reuse and process a response entity several times after
     * retrieval.
     * 
     * @return True if transient response entities should be buffered after
     *         being received.
     */
    public boolean isResponseEntityBuffering() {
        return responseEntityBuffering;
    }

    /**
     * Indicates if idempotent requests should be retried on error. Default
     * value is true.
     * 
     * @return True if idempotent requests should be retried on error.
     */
    public boolean isRetryOnError() {
        return retryOnError;
    }

    /**
     * Describes the resource using content negotiation to select the best
     * variant based on the client preferences. If a success status is not
     * returned, then a resource exception is thrown.
     * 
     * @return The best description.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options() throws ResourceException {
        return handle(Method.OPTIONS);
    }

    // [ifndef gwt] method
    /**
     * Describes the resource using a given media type. If a success status is
     * not returned, then a resource exception is thrown.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public <T> T options(Class<T> resultClass) throws ResourceException {
        return handle(Method.OPTIONS, resultClass);
    }

    /**
     * Describes the resource using a given media type. If a success status is
     * not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The matched description or null.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options(MediaType mediaType) throws ResourceException {
        return handle(Method.OPTIONS, mediaType);
    }

    /**
     * Patches a resource with the given object as delta state. Automatically
     * serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity containing the patch.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a href="https://tools.ietf.org/html/rfc5789">HTTP PATCH method</a>
     */
    public Representation patch(Object entity) throws ResourceException {
        try {
            return patch(toRepresentation(entity));
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    // [ifndef gwt] method
    /**
     * Patches a resource with the given object as delta state. Automatically
     * serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity containing the patch.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a href="https://tools.ietf.org/html/rfc5789">HTTP PATCH method</a>
     */
    public <T> T patch(Object entity, Class<T> resultClass)
            throws ResourceException {
        return handle(Method.PATCH, entity, resultClass);
    }

    /**
     * Patches a resource with the given object as delta state. Automatically
     * serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity containing the patch.
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a href="https://tools.ietf.org/html/rfc5789">HTTP PATCH method</a>
     */
    public Representation patch(Object entity, MediaType mediaType)
            throws ResourceException {
        try {
            return handle(Method.PATCH, toRepresentation(entity), mediaType);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * Patches a resource with the given representation as delta state. If a
     * success status is not returned, then a resource exception is thrown.
     * 
     * @param entity
     *            The request entity containing the patch.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a href="https://tools.ietf.org/html/rfc5789">HTTP PATCH method</a>
     */
    public Representation patch(Representation entity) throws ResourceException {
        return handle(Method.PATCH, entity);
    }

    /**
     * Posts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Object entity) throws ResourceException {
        try {
            return post(toRepresentation(entity));
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    // [ifndef gwt] method
    /**
     * Posts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public <T> T post(Object entity, Class<T> resultClass)
            throws ResourceException {
        return handle(Method.POST, entity, resultClass);
    }

    /**
     * Posts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Object entity, MediaType mediaType)
            throws ResourceException {
        try {
            return handle(Method.POST, toRepresentation(entity), mediaType);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * Posts a representation. If a success status is not returned, then a
     * resource exception is thrown.
     * 
     * @param entity
     *            The posted entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Representation entity) throws ResourceException {
        return handle(Method.POST, entity);
    }

    /**
     * Puts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to put.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Object entity) throws ResourceException {
        try {
            return put(toRepresentation(entity));
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    // [ifndef gwt] method
    /**
     * Puts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to put.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public <T> T put(Object entity, Class<T> resultClass)
            throws ResourceException {
        return handle(Method.PUT, entity, resultClass);
    }

    /**
     * Puts an object entity. Automatically serializes the object using the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Object entity, MediaType mediaType)
            throws ResourceException {
        try {
            return handle(Method.PUT, toRepresentation(entity), mediaType);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * Creates or updates a resource with the given representation as new state
     * to be stored. If a success status is not returned, then a resource
     * exception is thrown.
     * 
     * @param entity
     *            The request entity to store.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Representation entity) throws ResourceException {
        return handle(Method.PUT, entity);
    }

    /**
     * Effectively redirects a client call. By default, it checks for infinite
     * loops and unavailable entities, the references list is updated and the
     * {@link #handle(Request, Response, List, int, Uniform)} method invoked.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            The response to update.
     * @param references
     *            The references that caused a redirection to prevent infinite
     *            loops.
     * @param retryAttempt
     *            The number of remaining attempts.
     * @param next
     *            The next handler handling the call.
     */
    protected void redirect(Request request, Response response,
            List<Reference> references, int retryAttempt, Uniform next) {
        Reference newTargetRef = response.getLocationRef();

        if ((references != null) && references.contains(newTargetRef)) {
            getLogger().warning(
                    "Infinite redirection loop detected with URI: "
                            + newTargetRef);
        } else if (request.getEntity() != null && !request.isEntityAvailable()) {
            getLogger()
                    .warning(
                            "Unable to follow the redirection because the request entity isn't available anymore.");
        } else {
            if (references == null) {
                references = new ArrayList<Reference>();
            }

            if (references.size() >= getMaxRedirects()) {
                getLogger()
                        .warning(
                                "Unable to follow the redirection because the request the maximum number of redirections for a single call has been reached.");
            } else {
                // Add to the list of redirection reference
                // to prevent infinite loops
                references.add(request.getResourceRef());
                request.setResourceRef(newTargetRef);
                handle(request, response, references, 0, next);
            }
        }
    }

    /**
     * Effectively retries a failed client call. By default, it sleeps before
     * the retry attempt and increments the number of retries.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            The response to update.
     * @param references
     *            The references that caused a redirection to prevent infinite
     *            loops.
     * @param retryAttempt
     *            The number of remaining attempts.
     * @param next
     *            The next handler handling the call.
     */
    protected void retry(Request request, Response response,
            List<Reference> references, int retryAttempt, Uniform next) {
        getLogger().log(
                Level.INFO,
                "A recoverable error was detected ("
                        + response.getStatus().getCode()
                        + "), attempting again in " + getRetryDelay() + " ms.");

        // Wait before attempting again
        if (getRetryDelay() > 0) {
            // [ifndef gwt]
            try {
                Thread.sleep(getRetryDelay());
            } catch (InterruptedException e) {
                getLogger().log(Level.FINE,
                        "Retry delay sleep was interrupted", e);
                // MITRE, CWE-391 - Unchecked Error Condition
                Thread.currentThread().interrupt();
            }
            // [enddef]
            // [ifdef gwt] uncomment
            // com.google.gwt.user.client.Timer timer = new
            // com.google.gwt.user.client.Timer() {
            // public void run() {}
            // };
            // timer.schedule((int) getRetryDelay());
            // [enddef]
        }

        // Retry the call
        handle(request, response, references, ++retryAttempt, next);
    }

    /**
     * Sets the request attribute value.
     * 
     * @param name
     *            The attribute name.
     * @param value
     *            The attribute to set.
     */
    public void setAttribute(String name, Object value) {
        getRequestAttributes().put(name, value);
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * 
     * @param challengeResponse
     *            The authentication response sent by a client to an origin
     *            server.
     * @see Request#setChallengeResponse(ChallengeResponse)
     */
    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        getRequest().setChallengeResponse(challengeResponse);
    }

    /**
     * Sets the authentication response sent by a client to an origin server
     * given a scheme, identifier and secret.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setChallengeResponse(ChallengeScheme scheme,
            final String identifier, String secret) {
        setChallengeResponse(new ChallengeResponse(scheme, identifier, secret));
    }

    /**
     * Sets the client-specific information.
     * 
     * @param clientInfo
     *            The client-specific information.
     * @see Request#setClientInfo(ClientInfo)
     */
    public void setClientInfo(ClientInfo clientInfo) {
        getRequest().setClientInfo(clientInfo);
    }

    /**
     * Sets the conditions applying to this request.
     * 
     * @param conditions
     *            The conditions applying to this request.
     * @see Request#setConditions(Conditions)
     */
    public void setConditions(Conditions conditions) {
        getRequest().setConditions(conditions);
    }

    /**
     * Sets the cookies provided by the client.
     * 
     * @param cookies
     *            The cookies provided by the client.
     * @see Request#setCookies(Series)
     */
    public void setCookies(Series<Cookie> cookies) {
        getRequest().setCookies(cookies);
    }

    // [ifndef gwt] method
    /**
     * Indicates if transient entities should be buffered after being received
     * or before being sent.
     * 
     * @param entityBuffering
     *            True if transient entities should be buffered.
     * @see ClientResource#setRequestEntityBuffering(boolean)
     * @see #setResponseEntityBuffering(boolean)
     */
    public void setEntityBuffering(boolean entityBuffering) {
        setRequestEntityBuffering(entityBuffering);
        setResponseEntityBuffering(entityBuffering);
    }

    // [ifndef gwt] method
    /**
     * Indicates if redirections are followed.
     * 
     * @param followingRedirects
     *            True if redirections are followed.
     */
    public void setFollowingRedirects(boolean followingRedirects) {
        this.followingRedirects = followingRedirects;
    }

    /**
     * Sets the host reference.
     * 
     * @param hostRef
     *            The host reference.
     * @see Request#setHostRef(Reference)
     */
    public void setHostRef(Reference hostRef) {
        getRequest().setHostRef(hostRef);
    }

    /**
     * Sets the host reference using an URI string.
     * 
     * @param hostUri
     *            The host URI.
     * @see Request#setHostRef(String)
     */
    public void setHostRef(String hostUri) {
        getRequest().setHostRef(hostUri);
    }

    /**
     * Indicates if the call is loggable
     * 
     * @param loggable
     *            True if the call is loggable
     */
    public void setLoggable(boolean loggable) {
        getRequest().setLoggable(loggable);
    }

    /**
     * Sets the maximum number of redirections that can be automatically
     * followed for a single call.
     * 
     * @param maxRedirects
     *            The maximum number of redirections that can be automatically
     *            followed for a single call.
     */
    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *            The method called.
     * @see Request#setMethod(Method)
     */
    public void setMethod(Method method) {
        getRequest().setMethod(method);
    }

    /**
     * Sets the next handler such as a Restlet or a Filter.
     * 
     * In addition, this method will set the context of the next Restlet if it
     * is null by passing a reference to its own context.
     * 
     * @param next
     *            The next handler.
     */
    public void setNext(org.restlet.Uniform next) {
        if (next instanceof Restlet) {
            Restlet nextRestlet = (Restlet) next;

            if (nextRestlet.getContext() == null) {
                nextRestlet.setContext(getContext());
            }
        }

        this.next = next;

        // If true, it must be updated after calling this method
        this.nextCreated = false;
    }

    /**
     * Sets the callback invoked on response reception. If the value is not
     * null, then the associated request will be executed asynchronously.
     * 
     * @param onResponseCallback
     *            The callback invoked on response reception.
     */
    public void setOnResponse(Uniform onResponseCallback) {
        getRequest().setOnResponse(onResponseCallback);
    }

    /**
     * Sets the callback invoked after sending the request.
     * 
     * @param onSentCallback
     *            The callback invoked after sending the request.
     */
    public void setOnSent(Uniform onSentCallback) {
        getRequest().setOnSent(onSentCallback);
    }

    /**
     * Sets the original reference requested by the client.
     * 
     * @param originalRef
     *            The original reference.
     * @see Request#setOriginalRef(Reference)
     */
    public void setOriginalRef(Reference originalRef) {
        getRequest().setOriginalRef(originalRef);
    }

    /**
     * Sets the protocol used or to be used.
     * 
     * @param protocol
     *            The protocol used or to be used.
     */
    public void setProtocol(Protocol protocol) {
        getRequest().setProtocol(protocol);
    }

    // [ifndef gwt] method
    /**
     * Sets the proxy authentication response sent by a client to an origin
     * server.
     * 
     * @param challengeResponse
     *            The proxy authentication response sent by a client to an
     *            origin server.
     * @see Request#setProxyChallengeResponse(ChallengeResponse)
     */
    public void setProxyChallengeResponse(ChallengeResponse challengeResponse) {
        getRequest().setProxyChallengeResponse(challengeResponse);
    }

    // [ifndef gwt] method
    /**
     * Sets the proxy authentication response sent by a client to an origin
     * server given a scheme, identifier and secret.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setProxyChallengeResponse(ChallengeScheme scheme,
            final String identifier, String secret) {
        setProxyChallengeResponse(new ChallengeResponse(scheme, identifier,
                secret));
    }

    /**
     * Sets the ranges to return from the target resource's representation.
     * 
     * @param ranges
     *            The ranges.
     * @see Request#setRanges(List)
     */
    public void setRanges(List<Range> ranges) {
        getRequest().setRanges(ranges);
    }

    /**
     * Sets the resource's reference. If the reference is relative, it will be
     * resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param reference
     *            The resource reference.
     * @see Request#setResourceRef(Reference)
     */
    public void setReference(Reference reference) {
        getRequest().setResourceRef(reference);
    }

    /**
     * Sets the resource's reference using an URI string. Note that the URI can
     * be either absolute or relative to the context's base reference.
     * 
     * @param uri
     *            The resource URI.
     * @see Request#setResourceRef(String)
     */
    public void setReference(String uri) {
        getRequest().setResourceRef(uri);
    }

    /**
     * Sets the referrer reference if available.
     * 
     * @param referrerRef
     *            The referrer reference.
     * @see Request#setReferrerRef(Reference)
     */
    public void setReferrerRef(Reference referrerRef) {
        getRequest().setReferrerRef(referrerRef);
    }

    /**
     * Sets the referrer reference if available using an URI string.
     * 
     * @param referrerUri
     *            The referrer URI.
     * @see Request#setReferrerRef(String)
     */
    public void setReferrerRef(String referrerUri) {
        getRequest().setReferrerRef(referrerUri);
    }

    // [ifndef gwt] method
    /**
     * Indicates if transient or unknown size response entities should be
     * buffered after being received. This is useful to increase the chance of
     * being able to resubmit a failed request due to network error, or to
     * prevent chunked encoding from being used an HTTP connector.
     * 
     * @param requestEntityBuffering
     *            True if transient request entities should be buffered after
     *            being received.
     */
    public void setRequestEntityBuffering(boolean requestEntityBuffering) {
        this.requestEntityBuffering = requestEntityBuffering;
    }

    // [ifndef gwt] method
    /**
     * Indicates if transient or unknown size response entities should be
     * buffered after being received. This is useful to be able to
     * systematically reuse and process a response entity several times after
     * retrieval.
     * 
     * @param responseEntityBuffering
     *            True if transient response entities should be buffered after
     *            being received.
     */
    public void setResponseEntityBuffering(boolean responseEntityBuffering) {
        this.responseEntityBuffering = responseEntityBuffering;
    }

    /**
     * Sets the number of retry attempts before reporting an error.
     * 
     * @param retryAttempts
     *            The number of retry attempts before reporting an error.
     */
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * Sets the delay in milliseconds between two retry attempts. The default
     * value is two seconds.
     * 
     * @param retryDelay
     *            The delay in milliseconds between two retry attempts.
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Indicates if idempotent requests should be retried on error.
     * 
     * @param retryOnError
     *            True if idempotent requests should be retried on error.
     */
    public void setRetryOnError(boolean retryOnError) {
        this.retryOnError = retryOnError;
    }

    // [ifndef gwt] method
    /**
     * Wraps the client resource to proxy calls to the given Java interface into
     * Restlet method calls. Use the {@link org.restlet.engine.Engine}
     * classloader in order to generate the proxy.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public <T> T wrap(Class<? extends T> resourceInterface) {
        return wrap(resourceInterface, org.restlet.engine.Engine.getInstance()
                .getClassLoader());
    }

    // [ifndef gwt] method
    /**
     * Wraps the client resource to proxy calls to the given Java interface into
     * Restlet method calls.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @param classLoader
     *            The classloader used to instantiate the dynamic proxy.
     * @return The proxy instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<? extends T> resourceInterface,
            ClassLoader classLoader) {
        T result = null;

        // Create the client resource proxy
        java.lang.reflect.InvocationHandler h = new org.restlet.engine.resource.ClientInvocationHandler<T>(
                this, resourceInterface);

        // Instantiate our dynamic proxy
        result = (T) java.lang.reflect.Proxy.newProxyInstance(classLoader,
                new Class<?>[] { ClientProxy.class, resourceInterface }, h);

        return result;
    }
}
