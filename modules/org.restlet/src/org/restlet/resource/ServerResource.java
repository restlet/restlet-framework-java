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

package org.restlet.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.util.Series;

/**
 * Server-side resource. TODO<br>
 * <br>
 * Concurrency note: contrary to the {@link org.restlet.Uniform} class and its
 * main {@link Restlet} subclass where a single instance can handle several
 * calls concurrently, one instance of {@link ServerResource} is created for
 * each call handled and accessed by only one thread at a time.
 * 
 * @author Jerome Louvel
 */
public class ServerResource extends UniformResource {

    /** Indicates if the best content is automatically negotiated. */
    private boolean negotiateContent;

    /** The modifiable list of variants. */
    private volatile List<Variant> variants;

    /**
     * Initializer block to ensure that the basic properties of the Resource are
     * initialized consistently across constructors.
     */
    {
        this.negotiateContent = true;
        this.variants = null;
    }

    /**
     * Special constructor used by IoC frameworks. Note that the
     * {@link #init(Context, Request, Response)}() method MUST be invoked right
     * after the creation of the handler in order to keep a behavior consistent
     * with the normal {@link #ServerResource(Context, Request, Response)}
     * constructor.
     */
    public ServerResource() {
    }

    /**
     * Normal constructor. This constructor will invoke the
     * {@link #init(Context, Request, Response)} method by default.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public ServerResource(Context context, Request request, Response response) {
        init(context, request, response);
    }

    /**
     * Deletes the resource and all its representations. The default behavior is
     * to set the response status to {@link Status#SERVER_ERROR_INTERNAL}.
     * 
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7">HTTP
     *      DELETE method</a>
     */
    @Override
    public Representation delete() throws ResourceException {
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return null;
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences. By default it calls the
     * {@link #get(Variant)} method with the preferred variant returned by
     * {@link #getPreferredVariant()}.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    @Override
    public Representation get() throws ResourceException {
        return negotiate(Method.GET);
    }

    /**
     * Returns a full representation for a given variant previously returned via
     * the getVariants() method. The default implementation directly returns the
     * variant in case the variants are already full representations. In all
     * other cases, you will need to override this method in order to provide
     * your own implementation. <br>
     * <br>
     * 
     * This method is very useful for content negotiation when it is too costly
     * to initialize all the potential representations. It allows a resource to
     * simply expose the available variants via the getVariants() method and to
     * actually server the one selected via this method.
     * 
     * @param variant
     *            The variant whose full representation must be returned.
     * @return The full representation for the variant.
     * @see #getVariants()
     */
    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant instanceof Representation) {
            result = (Representation) variant;
        }

        return result;
    }

    @Override
    public Set<Method> getAllowedMethods() {
        return getResponse().getAllowedMethods();
    }

    /**
     * Returns the preferred variant according to the client preferences
     * specified in the request.
     * 
     * @return The preferred variant.
     */
    public Variant getPreferredVariant() {
        Variant result = null;
        final List<Variant> variants = getVariants();

        if ((variants != null) && (!variants.isEmpty())) {
            Language language = null;
            // Compute the preferred variant. Get the default language
            // preference from the Application (if any).
            final Application app = Application.getCurrent();

            if (app != null) {
                language = app.getMetadataService().getDefaultLanguage();
            }

            result = getRequest().getClientInfo().getPreferredVariant(variants,
                    language);

        }

        return result;
    }

    /**
     * Returns the modifiable list of variants. Creates a new instance if no one
     * has been set. A variant can be a purely descriptive representation, with
     * no actual content that can be served. It can also be a full
     * representation in case a resource has only one variant or if the
     * initialization cost is very low.<br>
     * <br>
     * Note that the order in which the variants are inserted in the list
     * matters. For example, if the client has no preference defined, or if the
     * acceptable variants have the same quality level for the client, the first
     * acceptable variant in the list will be returned.<br>
     * <br>
     * It is recommended to not override this method and to simply use it at
     * construction time to initialize the list of available variants.
     * Overriding it may reconstruct the list for each call which can be
     * expensive.
     * 
     * @return The list of variants.
     * @see #getRepresentation(Variant)
     */
    public List<Variant> getVariants() {
        // Lazy initialization with double-check.
        List<Variant> v = this.variants;
        if (v == null) {
            synchronized (this) {
                v = this.variants;
                if (v == null) {
                    this.variants = v = new ArrayList<Variant>();
                }
            }
        }
        return v;
    }

    @Override
    public Representation handle() {
        Representation result = null;

        try {
            final Method method = getRequest().getMethod();

            if (method == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                        "No method specified");
            } else {
                if (method.equals(Method.GET)) {
                    result = get();
                } else if (method.equals(Method.HEAD)) {
                    result = head();
                } else if (method.equals(Method.POST)) {
                    result = handlePost();
                } else if (method.equals(Method.PUT)) {
                    result = handlePut();
                } else if (method.equals(Method.DELETE)) {
                    result = handleDelete();
                } else if (method.equals(Method.OPTIONS)) {
                    result = handleOptions();
                } else {
                    setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                }
            }
        } catch (ResourceException re) {
            setStatus(re.getStatus(), re.getCause(), re.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Handles a DELETE call by invoking the {@link #delete()} method. It also
     * automatically support conditional DELETEs.
     * 
     * @throws ResourceException
     */
    private Representation handleDelete() throws ResourceException {
        Representation result = null;

        boolean canDelete = true;
        if (getRequest().getConditions().hasSome()) {
            Variant preferredVariant = null;

            if (isNegotiateContent()) {
                preferredVariant = getPreferredVariant();
            } else {
                final List<Variant> variants = getVariants();

                if (variants.size() == 1) {
                    preferredVariant = variants.get(0);
                } else {
                    setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
                    canDelete = false;
                }
            }

            // The conditions have to be checked
            // even if there is no preferred variant.
            if (canDelete) {
                final Status status = getRequest().getConditions().getStatus(
                        getRequest().getMethod(), get(preferredVariant));

                if (status != null) {
                    setStatus(status);
                    canDelete = false;
                }
            }
        }

        if (canDelete) {
            try {
                result = delete();
            } catch (ResourceException re) {
                setStatus(re.getStatus(), re);
            }
        }

        return result;
    }

    /**
     * Handles an OPTIONS call introspecting the target resource (as provided by
     * the 'findTarget' method). The default implementation is based on the HTTP
     * specification which says that OPTIONS should return the list of allowed
     * methods in the Response headers.
     * 
     * @throws ResourceException
     */
    private Representation handleOptions() throws ResourceException {
        return options();
    }

    /**
     * Handles a POST call by invoking the
     * {@link #acceptRepresentation(Representation)} method. It also logs a
     * trace if there is no entity posted.
     */
    private Representation handlePost() throws ResourceException {
        Representation result = null;

        if (!getRequest().isEntityAvailable()) {
            getLogger()
                    .fine(
                            "POST request received without any entity. Continuing processing.");
        }

        try {
            result = post(getRequest().getEntity());
        } catch (ResourceException re) {
            setStatus(re.getStatus(), re);
        }

        return result;
    }

    /**
     * Handles a PUT call by invoking the
     * {@link #storeRepresentation(Representation)} method. It also handles
     * conditional PUTs and forbids partial PUTs as they are not supported yet.
     * Finally, it prevents PUT with no entity by setting the response status to
     * {@link Status#CLIENT_ERROR_BAD_REQUEST} following the HTTP
     * specifications.
     * 
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    private Representation handlePut() throws ResourceException {
        Representation result = null;
        boolean canPut = true;

        if (getRequest().getConditions().hasSome()) {
            Variant preferredVariant = null;

            if (isNegotiateContent()) {
                preferredVariant = getPreferredVariant();
            } else {
                final List<Variant> variants = getVariants();

                if (variants.size() == 1) {
                    preferredVariant = variants.get(0);
                } else {
                    setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
                    canPut = false;
                }
            }

            // The conditions have to be checked
            // even if there is no preferred variant.
            if (canPut) {
                result = get(preferredVariant);
                final Status status = getRequest().getConditions().getStatus(
                        getRequest().getMethod(), result);
                if (status != null) {
                    setStatus(status);
                    canPut = false;
                }
            }
        }

        if (canPut) {
            // Check the Content-Range HTTP Header
            // in order to prevent usage of partial PUTs
            final Object oHeaders = getRequest().getAttributes().get(
                    "org.restlet.http.headers");
            if (oHeaders != null) {
                final Series<Parameter> headers = (Series<Parameter>) oHeaders;
                if (headers.getFirst("Content-Range", true) != null) {
                    setStatus(new Status(Status.SERVER_ERROR_NOT_IMPLEMENTED,
                            "The Content-Range header is not understood"));
                    canPut = false;
                }
            }
        }

        if (canPut) {
            if (getRequest().isEntityAvailable()) {
                try {
                    result = put(getRequest().getEntity());
                } catch (ResourceException re) {
                    setStatus(re.getStatus(), re);
                }

                // HTTP specification says that PUT may return
                // the list of allowed methods
                getAllowedMethods();
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Missing request entity");
            }
        }

        return result;
    }

    /**
     * Handles the {@link Method#HEAD} uniform method. By default, it just
     * invokes {@link #get()}. The Restlet connector will use the result
     * representation to extract the metadata and not return the actual content
     * to the client.
     */
    @Override
    public Representation head() throws ResourceException {
        return negotiate(Method.HEAD);
    }

    @Override
    public Representation head(Variant variant) throws ResourceException {
        return get(variant);
    }

    /**
     * Indicates if the authenticated subject associated to the current request
     * is in the given role name.
     * 
     * @param roleName
     *            The role name to test.
     * @return True if the authenticated subject is in the given role.
     */
    public boolean isInRole(String roleName) {
        return getClientInfo().isInRole(getApplication().findRole(roleName));
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @return True if the best content is automatically negotiated.
     */
    public boolean isNegotiateContent() {
        return this.negotiateContent;
    }

    /**
     * Handles a GET call by automatically returning the best representation
     * available. The content negotiation is automatically supported based on
     * the client's preferences available in the request. This feature can be
     * turned off using the "negotiateContent" property.<br>
     * <br>
     * If the resource's "available" property is set to false, the method
     * immediately returns with a {@link Status#CLIENT_ERROR_NOT_FOUND} status.<br>
     * <br>
     * The negotiated representation is obtained by calling the
     * {@link #getPreferredVariant()}. If a variant is successfully selected,
     * then the {@link #represent(Variant)} method is called to get the actual
     * representation corresponding to the metadata in the variant.<br>
     * <br>
     * If no variant matching the client preferences is available, the response
     * status is set to {@link Status#CLIENT_ERROR_NOT_ACCEPTABLE} and the list
     * of available representations is returned in the response entity as a
     * textual list of URIs (only if the variants have an identifier properly
     * set).<br>
     * <br>
     * If the content negotiation is turned off and only one variant is defined
     * in the "variants" property, then its representation is returned by
     * calling the {@link #represent(Variant)} method. If several variants are
     * available, then the list of available representations is returned in the
     * response entity as a textual list of URIs (only if the variants have an
     * identifier properly set).<br>
     * <br>
     * If no variant is defined in the "variants" property, the response status
     * is set to {@link Status#CLIENT_ERROR_NOT_FOUND}. <br>
     * If it is disabled and multiple variants are available for the target
     * resource, then a 300 (Multiple Choices) status will be returned with the
     * list of variants URI if available. Conditional GETs are also
     * automatically supported.
     * 
     * @throws ResourceException
     */
    private Representation negotiate(Method method) throws ResourceException {
        Representation result = null;

        // The variant that may need to meet the request conditions
        Representation selectedRepresentation = null;

        final List<Variant> variants = getVariants();
        if ((variants == null) || (variants.isEmpty())) {
            // Resource not found
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            getLogger()
                    .warning(
                            "A resource should normally have at least one variant added by calling getVariants().add() in the constructor. Check your resource \""
                                    + getRequest().getResourceRef() + "\".");
        } else if (isNegotiateContent()) {
            final Variant preferredVariant = getPreferredVariant();

            if (preferredVariant == null) {
                // No variant was found matching the client preferences
                setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);

                // The list of all variants is transmitted to the client
                final ReferenceList refs = new ReferenceList(variants.size());
                for (final Variant variant : variants) {
                    if (variant.getIdentifier() != null) {
                        refs.add(variant.getIdentifier());
                    }
                }

                result = refs.getTextRepresentation();
            } else {
                // Set the variant dimensions used for content negotiation
                getDimensions().clear();
                getDimensions().add(Dimension.CHARACTER_SET);
                getDimensions().add(Dimension.ENCODING);
                getDimensions().add(Dimension.LANGUAGE);
                getDimensions().add(Dimension.MEDIA_TYPE);

                // Set the negotiated representation as response entity
                negotiate(method, preferredVariant);
            }

            selectedRepresentation = result;
        } else {
            if (variants.size() == 1) {
                negotiate(method, variants.get(0));
                selectedRepresentation = result;
            } else {
                final ReferenceList variantRefs = new ReferenceList();

                for (final Variant variant : variants) {
                    if (variant.getIdentifier() != null) {
                        variantRefs.add(variant.getIdentifier());
                    } else {
                        getLogger()
                                .warning(
                                        "A resource with multiple variants should provide an identifier for each variant when content negotiation is turned off");
                    }
                }

                if (variantRefs.size() > 0) {
                    // Return the list of variants
                    setStatus(Status.REDIRECTION_MULTIPLE_CHOICES);
                    result = variantRefs.getTextRepresentation();
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }
        }

        if (selectedRepresentation == null) {
            if ((getStatus() == null)
                    || (getStatus().isSuccess() && !Status.SUCCESS_NO_CONTENT
                            .equals(getStatus()))) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                // Keep the current status as the developer might prefer a
                // special status like 'method not authorized'.
            }
        } else {
            // The given representation (even if null) must meet the request
            // conditions (if any).
            if (getRequest().getConditions().hasSome()) {
                final Status status = getRequest().getConditions().getStatus(
                        getRequest().getMethod(), selectedRepresentation);

                if (status != null) {
                    setStatus(status);
                    result = null;
                }
            }
        }

        return result;
    }

    private Representation negotiate(Method method, Variant variant)
            throws ResourceException {
        Representation result = null;

        if (Method.GET.equals(method)) {
            result = get(variant);
        } else if (Method.HEAD.equals(method)) {
            result = head(variant);
        } else if (Method.OPTIONS.equals(method)) {
            result = options(variant);
        }

        return result;
    }

    /**
     * Indicates the communication options available for this resource. The
     * default implementation is based on the HTTP specification which says that
     * OPTIONS should return the list of allowed methods in the Response
     * headers.
     * 
     * @return
     */
    @Override
    public Representation options() throws ResourceException {
        return negotiate(Method.OPTIONS);
    }

    @Override
    public Representation options(Variant variant) throws ResourceException {
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return null;
    }

    /**
     * Posts a representation to the resource at the target URI reference. The
     * default behavior is to set the response status to
     * {@link Status#SERVER_ERROR_INTERNAL}.
     * 
     * @param entity
     *            The posted entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    @Override
    public Representation post(Representation entity) throws ResourceException {
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return null;
    }

    @Override
    public Representation put(Representation representation)
            throws ResourceException {
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return null;
    }

    /**
     * Sets the set of methods allowed on the requested resource. The set
     * instance set must be thread-safe (use {@link CopyOnWriteArraySet} for
     * example.
     * 
     * @param allowedMethods
     *            The set of methods allowed on the requested resource.
     * @see Response#setAllowedMethods(Set)
     */
    public void setAllowedMethods(Set<Method> allowedMethods) {
        getResponse().setAllowedMethods(allowedMethods);
    }

    /**
     * Sets the authentication request sent by an origin server to a client.
     * 
     * @param request
     *            The authentication request sent by an origin server to a
     *            client.
     * @see Response#setChallengeRequest(ChallengeRequest)
     */
    public void setChallengeRequest(ChallengeRequest request) {
        getResponse().setChallengeRequest(request);
    }

    /**
     * Sets the list of authentication requests sent by an origin server to a
     * client. The list instance set must be thread-safe (use
     * {@link CopyOnWriteArrayList} for example.
     * 
     * @param requests
     *            The list of authentication requests sent by an origin server
     *            to a client.
     * @see Response#setChallengeRequests(List)
     */
    public void setChallengeRequests(List<ChallengeRequest> requests) {
        getResponse().setChallengeRequests(requests);
    }

    /**
     * Sets the cookie settings provided by the server.
     * 
     * @param cookieSettings
     *            The cookie settings provided by the server.
     * @see Response#setCookieSettings(Series)
     */
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        getResponse().setCookieSettings(cookieSettings);
    }

    /**
     * Sets the set of dimensions on which the response entity may vary. The set
     * instance set must be thread-safe (use {@link CopyOnWriteArraySet} for
     * example.
     * 
     * @param dimensions
     *            The set of dimensions on which the response entity may vary.
     * @see Response#setDimensions(Set)
     */
    public void setDimensions(Set<Dimension> dimensions) {
        getResponse().setDimensions(dimensions);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationRef
     *            The reference to set.
     * @see Response#setLocationRef(Reference)
     */
    public void setLocationRef(Reference locationRef) {
        getResponse().setLocationRef(locationRef);
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
     * @see Response#setLocationRef(String)
     */
    public void setLocationRef(String locationUri) {
        getResponse().setLocationRef(locationUri);
    }

    /**
     * Indicates if the returned representation is automatically negotiated.
     * Default value is true.
     * 
     * @param negotiateContent
     *            True if content negotiation is enabled.
     */
    public void setNegotiateContent(boolean negotiateContent) {
        this.negotiateContent = negotiateContent;
    }

    /**
     * Sets the server-specific information.
     * 
     * @param serverInfo
     *            The server-specific information.
     * @see Response#setServerInfo(ServerInfo)
     */
    public void setServerInfo(ServerInfo serverInfo) {
        getResponse().setServerInfo(serverInfo);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @see Response#setStatus(Status)
     */
    public void setStatus(Status status) {
        getResponse().setStatus(status);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param message
     *            The status message.
     * @see Response#setStatus(Status, String)
     */
    public void setStatus(Status status, String message) {
        getResponse().setStatus(status, message);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param throwable
     *            The related error or exception.
     * @see Response#setStatus(Status, Throwable)
     */
    public void setStatus(Status status, Throwable throwable) {
        getResponse().setStatus(status, throwable);
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
     * @see Response#setStatus(Status, Throwable, String)
     */
    public void setStatus(Status status, Throwable throwable, String message) {
        getResponse().setStatus(status, throwable, message);
    }

    /**
     * Invoked when the list of allowed methods needs to be updated. The
     * {@link #getAllowedMethods()} or the {@link #setAllowedMethods(Set)}
     * methods should be used. The default implementation does nothing.
     * 
     */
    public void updateAllowedMethods() {
    }

}
