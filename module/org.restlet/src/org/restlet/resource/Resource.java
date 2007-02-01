/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Dimension;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Series;
import org.restlet.util.Template;

/**
 * Intended conceptual target of a hypertext reference. "Any information that
 * can be named can be a resource: a document or image, a temporal service (e.g.
 * "today's weather in Los Angeles"), a collection of other resources, a
 * non-virtual object (e.g. a person), and so on. In other words, any concept
 * that might be the target of an author's hypertext reference must fit within
 * the definition of a resource. The only thing that is required to be static
 * for a resource is the semantics of the mapping, since the semantics is what
 * distinguishes one resource from another." Roy T. Fielding<br>
 * <br>
 * Another definition adapted from the URI standard (RFC 3986): a resource is
 * the conceptual mapping to a representation (also known as entity) or set of
 * representations, not necessarily the representation which corresponds to that
 * mapping at any particular instance in time. Thus, a resource can remain
 * constant even when its content (the representations to which it currently
 * corresponds) changes over time, provided that the conceptual mapping is not
 * changed in the process. In addition, a resource is always identified by a
 * URI.<br>
 * <br>
 * Typically created by Finders, Resource instances are the final handlers of
 * calls received by server connectors. Unlike the other handlers in the
 * processing chain, a Resource is genarally not shared between calls and
 * doesn't have to be thread-safe. This is the point where the RESTful view of
 * your Web application can be integrated with your domain objects. Those domain
 * objects can be implemented using any technology, relational databases, object
 * databases, transactional components like EJB, etc. You just have to extend
 * this class to override the REST methods you want to support like post(),
 * put() or delete(). The common GET method is supported by the modifiable
 * "variants" list property and the {@link #getRepresentation(Variant)} method.
 * This allows an easy and cheap declaration of the available variants in the
 * constructor for example, then the on-demand creation of costly
 * representations via the {@link #getRepresentation(Variant)} method.<br>
 * <br>
 * At a lower level, you have a handle*(Request,Response) method for each REST
 * method that is supported by the Resource, where the '*' is replaced by the
 * method name. The Finder handler for example, will be able to dynamically
 * dispatch a call to the appropriate handle*() method. Most common REST methods
 * like GET, POST, PUT and DELETE have default implementations that pre-handle
 * calls to do content negotiation for example, based on the higher-level
 * methods that we discussed previously. For example if you want to support a
 * MOVE method, just add an handleMove(Request,Response) method and it will be
 * detected automatically by a Finder handler.<br>
 * <br>
 * Finally, you need to declare which REST methods are allowed by your Resource
 * by overiding the matching allow*() method. By default, allowGet() returns
 * true, but all other allow*() methods will return false. Therefore, if you
 * want to support the DELETE method, just override allowDelete() and return
 * true. Again, a previous Finder handler will be able to detect this method and
 * know whether or not your Resource should be invoked. It is also used by the
 * handleOptions() method to return the list of allowed methods.
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1">Source
 *      dissertation</a>
 * @see <a href="http://www.restlet.org/tutorial#part12">Tutorial: Reaching
 *      target Resources</a>
 * @see org.restlet.resource.Representation
 * @see org.restlet.Finder
 * @author Jerome Louvel (contact@noelios.com)
 * @author Thierry Boileau (thboileau@gmail.com)
 */
public class Resource {
    /** The parent context. */
    private Context context;

    /** The logger to use. */
    private Logger logger;

    /** Indicates if the best content is automatically negotiated. */
    private boolean negotiateContent;

    /** The handled request. */
    private Request request;

    /** The returned response. */
    private Response response;

    /** The modifiable list of variants. */
    private List<Variant> variants;

    /**
     * Default constructor. Note that the init() method must be invoked right
     * after the creation of the resource.
     */
    public Resource() {
    }

    /**
     * Constructor. This constructor will invoke the init() method by default.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public Resource(Context context, Request request, Response response) {
        init(context, request, response);
    }

    /**
     * Indicates if it is allowed to delete the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowDelete() {
        return false;
    }

    /**
     * Indicates if it is allowed to get the variants. The default value is
     * true.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowGet() {
        return true;
    }

    /**
     * Indicates if it is allowed to post to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPost() {
        return false;
    }

    /**
     * Indicates if it is allowed to put to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPut() {
        return false;
    }

    /**
     * Asks the resource to delete itself and all its representations.
     */
    public void delete() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Generates a reference based on a template URI. Note that you can leverage
     * all the variables defined in the Template class as they will be resolved
     * using the resource's request and response properties.
     * 
     * @param uriTemplate
     *            The URI template to use for generation.
     * @return The generated reference.
     */
    public Reference generateRef(String uriTemplate) {
        Template tplt = new Template(getLogger(), uriTemplate);
        return new Reference(tplt.format(getRequest(), getResponse()));
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        if (this.context == null)
            this.context = new Context(getClass().getCanonicalName());
        return this.context;
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        if (this.logger == null)
            this.logger = getContext().getLogger();
        return this.logger;
    }

    /**
     * Returns the preferred representation according to the client preferences
     * specified in the associated request.
     * 
     * @return The preferred representation.
     */
    public Representation getPreferredRepresentation() {
        return getRepresentation(getPreferredVariant());
    }

    /**
     * Returns the preferred variant according to the client preferences
     * specified in the associated request.
     * 
     * @return The preferred variant.
     */
    public Variant getPreferredVariant() {
        Variant result = null;
        List<Variant> variants = getVariants();

        if ((variants != null) && (!variants.isEmpty())) {
            Language language = null;
            // Compute the preferred variant. Get the default language
            // preference from the Application (if any).
            Object app = getContext().getAttributes().get(Application.KEY);

            if (app instanceof Application) {
                language = ((Application) app).getMetadataService()
                        .getDefaultLanguage();
            }
            result = getRequest().getClientInfo().getPreferredVariant(
                    getVariants(), language);

        }

        return result;
    }

    /**
     * Returns a full representation for a given variant previously returned via
     * the getVariants() method. The default implementation directly returns the
     * variant in case the variants are already full representations. In all
     * other cases, you will need to override this method in order to provide
     * your own implementation. <br/><br/>
     * 
     * This method is very useful for content negotiation when it is too costly
     * to initilize all the potential representations. It allows a resource to
     * simply expose the available variants via the getVariants() method and to
     * actually server the one selected via this method.
     * 
     * @param variant
     *            The variant whose full representation must be returned.
     * @return The full representation for the variant.
     * @see #getVariants()
     */
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        if (variant instanceof Representation) {
            result = (Representation) variant;
        }

        return result;
    }

    /**
     * Returns the request.
     * 
     * @return the request.
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * Returns the response.
     * 
     * @return the response.
     */
    public Response getResponse() {
        return this.response;
    }

    /**
     * Returns the modifiable list of variants. A variant can be a purely
     * descriptive representation, with no actual content that can be served. It
     * can also be a full representation in case a resource has only one variant
     * or if the initialization cost is very low.<br>
     * <br>
     * It is recommended to not override this method and to simply use it at
     * construction time to initialize the list of available variants.
     * Overriding it will force you to reconstruct the list for each call which
     * is expensive.
     * 
     * @return The list of variants.
     * @see #getRepresentation(Variant)
     */
    public List<Variant> getVariants() {
        if (this.variants == null)
            this.variants = new ArrayList<Variant>();
        return this.variants;
    }

    /**
     * Handles a DELETE call invoking the 'delete' method of the target resource
     * (as provided by the 'findTarget' method).
     */
    public void handleDelete() {
        boolean bContinue = true;
        if (getRequest().getConditions().hasSome()) {
            Variant preferredVariant = null;

            if (isNegotiateContent()) {
                preferredVariant = getPreferredVariant();
            } else {
                List<Variant> variants = getVariants();

                if (variants.size() == 1) {
                    preferredVariant = variants.get(0);
                } else {
                    getResponse().setStatus(
                            Status.CLIENT_ERROR_PRECONDITION_FAILED);
                    bContinue = false;
                }
            }
            // The conditions have to be checked even if there is no preferred
            // variant.
            if (bContinue) {
                Status status = getRequest().getConditions().getStatus(
                        getRequest().getMethod(), preferredVariant);

                if (status != null) {
                    getResponse().setStatus(status);
                    bContinue = false;
                }
            }
        }

        if (bContinue) {
            delete();
        }

    }

    /**
     * Handles a GET call by automatically returning the best entity available
     * from the target resource (as provided by the 'findTarget' method). The
     * content negotiation is based on the client's preferences available in the
     * handled call and can be turned off using the "negotiateContent" property.
     * If it is disabled and multiple variants are available for the target
     * resource, then a 300 (Multiple Choices) status will be returned with the
     * list of variants URI if available.
     */
    public void handleGet() {
        // The variant that may need to meet the request conditions
        Variant selectedVariant = null;

        List<Variant> variants = getVariants();
        if ((variants == null) || (variants.isEmpty())) {
            // Resource not found
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else if (isNegotiateContent()) {
            Variant preferredVariant = getPreferredVariant();

            // Set the variant dimensions used for content negotiation
            getResponse().getDimensions().clear();
            getResponse().getDimensions().add(Dimension.CHARACTER_SET);
            getResponse().getDimensions().add(Dimension.ENCODING);
            getResponse().getDimensions().add(Dimension.LANGUAGE);
            getResponse().getDimensions().add(Dimension.MEDIA_TYPE);

            if (preferredVariant == null) {
                // No variant was found matching the client preferences
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);

                // The list of all variants is transmitted to the client
                ReferenceList refs = new ReferenceList(variants.size());
                for (Variant variant : variants) {
                    if (variant.getIdentifier() != null) {
                        refs.add(variant.getIdentifier());
                    }
                }

                getResponse().setEntity(refs.getTextRepresentation());
            } else {
                getResponse().setEntity(getRepresentation(preferredVariant));
                selectedVariant = preferredVariant;
            }
            selectedVariant = getResponse().getEntity();
        } else {
            if (variants.size() == 1) {
                getResponse().setEntity(variants.get(0));
                selectedVariant = getResponse().getEntity();
            } else {
                ReferenceList variantRefs = new ReferenceList();

                for (Variant variant : variants) {
                    if (variant.getIdentifier() != null) {
                        variantRefs.add(variant.getIdentifier());
                    } else {
                        getLogger()
                                .warning(
                                        "A resource with multiple variants should provide and identifier for each variants when content negotiation is turned off");
                    }
                }

                if (variantRefs.size() > 0) {
                    // Return the list of variants
                    getResponse()
                            .setStatus(Status.REDIRECTION_MULTIPLE_CHOICES);
                    getResponse()
                            .setEntity(variantRefs.getTextRepresentation());
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }
        }

        // The given representation (even if null) must meet the request conditions
        // (if any).
        if (getRequest().getConditions().hasSome()) {
            Status status = getRequest().getConditions().getStatus(
                    getRequest().getMethod(), selectedVariant);
            if (status != null) {
                getResponse().setStatus(status);
                getResponse().setEntity(null);
            }
        }
    }

    /**
     * Handles a HEAD call, using a logic similar to the handleGet method.
     */
    public void handleHead() {
        handleGet();
    }

    /**
     * Handles an OPTIONS call introspecting the target resource (as provided by
     * the 'findTarget' method).
     */
    public void handleOptions() {
        // HTTP spec says that OPTIONS should return the list of allowed methods
        updateAllowedMethods();
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    /**
     * Handles a POST call invoking the 'post' method of the target resource (as
     * provided by the 'findTarget' method).
     */
    public void handlePost() {
        if (getRequest().isEntityAvailable()) {
            post(getRequest().getEntity());
        } else {
            getResponse().setStatus(
                    new Status(Status.CLIENT_ERROR_BAD_REQUEST,
                            "Missing request entity"));
        }
    }

    /**
     * Handles a PUT call invoking the 'put' method of the target resource (as
     * provided by the 'findTarget' method).
     */
    public void handlePut() {
        boolean bContinue = true;

        if (getRequest().getConditions().hasSome()) {
            Variant preferredVariant = null;

            if (isNegotiateContent()) {
                preferredVariant = getPreferredVariant();
            } else {
                List<Variant> variants = getVariants();

                if (variants.size() == 1) {
                    preferredVariant = variants.get(0);
                } else {
                    getResponse().setStatus(
                            Status.CLIENT_ERROR_PRECONDITION_FAILED);
                    bContinue = false;
                }
            }
            // The conditions have to be checked even if there is no preferred
            // variant.
            if (bContinue) {
                Status status = getRequest().getConditions().getStatus(
                        getRequest().getMethod(), preferredVariant);
                if (status != null) {
                    getResponse().setStatus(status);
                    bContinue = false;
                }
            }
        }

        if (bContinue) {
            // Check the Content-Range HTTP Header in order to prevent usage of
            // partial PUTs
            Object oHeaders = getRequest().getAttributes().get(
                    "org.restlet.http.headers");
            if (oHeaders != null) {
                Series headers = (Series) oHeaders;
                if (headers.getFirst("Content-Range", true) != null) {
                    getResponse()
                            .setStatus(
                                    new Status(
                                            Status.SERVER_ERROR_NOT_IMPLEMENTED,
                                            "the Content-Range header is not understood"));
                    bContinue = false;
                }
            }
        }

        if (bContinue) {
            if (getRequest().isEntityAvailable()) {
                put(getRequest().getEntity());

                // HTTP spec says that PUT may return the list of allowed
                // methods
                updateAllowedMethods();
            } else {
                getResponse().setStatus(
                        new Status(Status.CLIENT_ERROR_BAD_REQUEST,
                                "Missing request entity"));
            }
        }
    }

    /**
     * Initialize the resource with its context.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public void init(Context context, Request request, Response response) {
        this.context = context;
        this.logger = (context != null) ? context.getLogger() : null;
        this.negotiateContent = true;
        this.request = request;
        this.response = response;
        this.variants = null;
    }

    /**
     * Invokes a method with the given arguments.
     * 
     * @param method
     *            The method to invoke.
     * @param args
     *            The arguments to pass.
     * @return Invocation result.
     */
    private Object invoke(java.lang.reflect.Method method, Object... args) {
        Object result = null;

        if (method != null) {
            try {
                result = method.invoke(this, args);
            } catch (Exception e) {
                getLogger().log(
                        Level.WARNING,
                        "Couldn't invoke the handle method for \"" + method
                                + "\"", e);
            }
        }

        return result;
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
     * Posts a representation to the resource.
     * 
     * @param entity
     *            The posted entity.
     */
    public void post(Representation entity) {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Puts a representation in the resource.
     * 
     * @param entity
     *            A new or updated representation.
     */
    public void put(Representation entity) {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Sets the parent context.
     * 
     * @param context
     *            The parent context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @param negotiateContent
     *            True if the best content is automatically negotiated.
     */
    public void setNegotiateContent(boolean negotiateContent) {
        this.negotiateContent = negotiateContent;
    }

    /**
     * Sets the request to handle.
     * 
     * @param request
     *            The request to handle.
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the response to update.
     * 
     * @param response
     *            The response to update.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Updates the set of allowed methods on the response.
     */
    private void updateAllowedMethods() {
        Set<Method> allowedMethods = getResponse().getAllowedMethods();
        for (java.lang.reflect.Method classMethod : getClass().getMethods()) {
            if (classMethod.getName().startsWith("allow")
                    && (classMethod.getParameterTypes().length == 0)) {
                if ((Boolean) invoke(classMethod)) {
                    Method allowedMethod = Method.valueOf(classMethod.getName()
                            .substring(5));
                    allowedMethods.add(allowedMethod);
                }
            }
        }
    }

}
