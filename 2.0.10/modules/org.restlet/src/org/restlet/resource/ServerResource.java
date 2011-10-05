/**
 * Copyright 2005-2011 Noelios Technologies.
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ClientInfo;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.Variant;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.service.MetadataService;
import org.restlet.util.Series;

/**
 * Base class for server-side resources. It is a full replacement for the
 * deprecated {@link Resource} class. It acts as a wrapper to a given call,
 * including the incoming {@link Request} and the outgoing {@link Response}. <br>
 * <br>
 * It's life cycle is managed by a {@link Finder} created either explicitly or
 * more likely implicitly when your {@link ServerResource} subclass is attached
 * to a {@link Filter} or a {@link Router} via the {@link Filter#setNext(Class)}
 * or {@link Router#attach(String, Class)} methods for example. After
 * instantiation using the default constructor, the final
 * {@link #init(Context, Request, Response)} method is invoked, setting the
 * context, request and response. You can intercept this by overriding the
 * {@link #doInit()} method. Then, if the response status is still a success,
 * the {@link #handle()} method is invoked to actually handle the call. Finally,
 * the final {@link #release()} method is invoked to do the necessary clean-up,
 * which you can intercept by overriding the {@link #doRelease()} method. During
 * this life cycle, if any exception is caught, then the
 * {@link #doCatch(Throwable)} method is invoked.<br>
 * <br>
 * Note that when an annotated method manually sets the response entity, if this
 * entity is available then it will be preserved and the result of the annotated
 * method ignored.<br>
 * <br>
 * In addition, there are two ways to declare representation variants, one is
 * based on the {@link #getVariants()} method and another one on the annotated
 * methods. Both approaches can't however be used at the same time for now.<br>
 * <br>
 * Concurrency note: contrary to the {@link org.restlet.Uniform} class and its
 * main {@link Restlet} subclass where a single instance can handle several
 * calls concurrently, one instance of {@link ServerResource} is created for
 * each call handled and accessed by only one thread at a time.
 * 
 * @author Jerome Louvel
 */
public abstract class ServerResource extends UniformResource {

    /** Indicates if annotations are supported. */
    private volatile boolean annotated;

    /** Indicates if conditional handling is enabled. */
    private volatile boolean conditional;

    /** Indicates if the identified resource exists. */
    private volatile boolean existing;

    /** Indicates if content negotiation of response entities is enabled. */
    private volatile boolean negotiated;

    /** Modifiable list of variants. */
    private volatile List<Variant> variants;


    /**
     * Initializer block to ensure that the basic properties are initialized
     * consistently across constructors.
     */
    {
        this.annotated = true;
        this.conditional = true;
        this.existing = true;
        this.negotiated = true;
        this.variants = null;
    }
    /**
     * Default constructor. Note that the
     * {@link #init(Context, Request, Response)}() method will be invoked right
     * after the creation of the resource.
     */
    public ServerResource() {
    }

    /**
     * Ask the connector to abort the related network connection, for example
     * immediately closing the socket.
     */
    public void abort() {
        getResponse().abort();
    }

    /**
     * Asks the response to immediately commit making it ready to be sent back
     * to the client. Note that all server connectors don't necessarily support
     * this feature.
     */
    public void commit() {
        getResponse().commit();
    }

    /**
     * Deletes the resource and all its representations. This method is only
     * invoked if content negotiation has been disabled as indicated by the
     * {@link #isNegotiated()}, otherwise the {@link #delete(Variant)} method is
     * invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @return The optional response entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7"
     *      >HTTP DELETE method</a>
     */
    protected Representation delete() throws ResourceException {
        Representation result = null;
        AnnotationInfo annotationInfo = getAnnotation(Method.DELETE);

        if (annotationInfo != null) {
            result = doHandle(annotationInfo, null);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Deletes the resource and all its representations. A variant parameter is
     * passed to indicate which representation should be returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the {@link #delete()}
     * method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @param variant
     *            The variant of the response entity.
     * @return The optional response entity.
     * @throws ResourceException
     * @see #get(Variant)
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7"
     *      >HTTP DELETE method</a>
     */
    protected Representation delete(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant instanceof VariantInfo) {
            result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                    variant);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Describes the available variants to help client-side content negotiation.
     * Return null by default.
     * 
     * @return The description of available variants.
     */
    protected Representation describeVariants() {
        Representation result = null;

        // The list of all variants is transmitted to the client
        // final ReferenceList refs = new ReferenceList(variants.size());
        // for (final Variant variant : variants) {
        // if (variant.getIdentifier() != null) {
        // refs.add(variant.getIdentifier());
        // }
        // }
        //
        // result = refs.getTextRepresentation();
        return result;
    }

    /**
     * Handles a call by first verifying the optional request conditions and
     * continue the processing if possible. Note that in order to evaluate those
     * conditions, {@link #getInfo()} or {@link #getInfo(Variant)} methods might
     * be invoked.
     * 
     * @return The response entity.
     * @throws ResourceException
     */
    protected Representation doConditionalHandle() throws ResourceException {
        Representation result = null;

        if (getConditions().hasSome()) {
            RepresentationInfo resultInfo = null;

            if (existing) {
                if (isNegotiated()) {
                    resultInfo = doGetInfo(getPreferredVariant(getVariants(Method.GET)));
                } else {
                    resultInfo = doGetInfo();
                }

                if (resultInfo == null) {
                    if ((getStatus() == null)
                            || (getStatus().isSuccess() && !Status.SUCCESS_NO_CONTENT
                                    .equals(getStatus()))) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        // Keep the current status as the developer might prefer
                        // a special status like 'method not authorized'.
                    }
                } else {
                    Status status = getConditions().getStatus(getMethod(),
                            resultInfo);

                    if (status != null) {
                        setStatus(status);
                    }
                }
            } else {
                Status status = getConditions().getStatus(getMethod(),
                        resultInfo);

                if (status != null) {
                    setStatus(status);
                }
            }

            if ((Method.GET.equals(getMethod()) || Method.HEAD
                    .equals(getMethod()))
                    && resultInfo instanceof Representation) {
                result = (Representation) resultInfo;
            } else if ((getStatus() != null) && getStatus().isSuccess()) {
                // Conditions were passed successfully, continue the normal
                // processing.
                if (isNegotiated()) {
                    // Reset the list of variants, as the method differs.
                    getVariants().clear();
                    result = doNegotiatedHandle();
                } else {
                    result = doHandle();
                }
            }
        } else {
            if (isNegotiated()) {
                result = doNegotiatedHandle();
            } else {
                result = doHandle();
            }
        }

        return result;
    }

    /**
     * Returns a descriptor of the response entity returned by a
     * {@link Method#GET} call.
     * 
     * @return The response entity.
     * @throws ResourceException
     */
    private RepresentationInfo doGetInfo() throws ResourceException {
        RepresentationInfo result = null;
        AnnotationInfo annotationInfo = getAnnotation(Method.GET);

        if (annotationInfo != null) {
            result = doHandle(annotationInfo, null);
        } else {
            result = getInfo();
        }

        return result;
    }

    /**
     * Returns a descriptor of the response entity returned by a negotiated
     * {@link Method#GET} call.
     * 
     * @param variant
     *            The selected variant descriptor.
     * @return The response entity descriptor.
     * @throws ResourceException
     */
    private RepresentationInfo doGetInfo(Variant variant)
            throws ResourceException {
        RepresentationInfo result = null;

        if (variant != null) {
            if (variant instanceof VariantInfo) {
                result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                        variant);
            } else if (variant instanceof RepresentationInfo) {
                result = (RepresentationInfo) variant;
            } else {
                result = getInfo(variant);
            }
        } else {
            result = doGetInfo();
        }

        return result;
    }

    /**
     * Effectively handles a call without content negotiation of the response
     * entity. The default behavior is to dispatch the call to one of the
     * {@link #get()}, {@link #post(Representation)},
     * {@link #put(Representation)}, {@link #delete()}, {@link #head()} or
     * {@link #options()} methods.
     * 
     * @return The response entity.
     * @throws ResourceException
     */
    protected Representation doHandle() throws ResourceException {
        Representation result = null;
        Method method = getMethod();

        if (method == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No method specified");
        } else {
            if (method.equals(Method.PUT)) {
                result = put(getRequestEntity());
            } else if (isExisting()) {
                if (method.equals(Method.GET)) {
                    result = get();
                } else if (method.equals(Method.POST)) {
                    result = post(getRequestEntity());
                } else if (method.equals(Method.DELETE)) {
                    result = delete();
                } else if (method.equals(Method.HEAD)) {
                    result = head();
                } else if (method.equals(Method.OPTIONS)) {
                    result = options();
                } else {
                    result = doHandle(method, getRequestEntity());
                }
            } else {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        return result;
    }

    /**
     * Effectively handles a call with content negotiation of the response
     * entity using an annotated method.
     * 
     * @param annotationInfo
     *            The annotation descriptor.
     * @param variant
     *            The response variant expected (can be null).
     * @return The response entity.
     * @throws ResourceException
     */
    private Representation doHandle(AnnotationInfo annotationInfo,
            Variant variant) throws ResourceException {
        Representation result = null;
        Class<?>[] parameterTypes = annotationInfo.getJavaInputTypes();

        // Invoke the annotated method and get the resulting object.
        Object resultObject = null;
        try {
            if (parameterTypes.length > 0) {
                List<Object> parameters = new ArrayList<Object>();
                Object parameter = null;

                for (Class<?> parameterType : parameterTypes) {
                    if (Variant.class.equals(parameterType)) {
                        parameters.add(variant);
                    } else {
                        if (getRequestEntity() != null
                                && getRequestEntity().isAvailable()
                                && getRequestEntity().getSize() != 0) {
                            // Assume there is content to be read.
                            // NB: it does not handle the case where the size is
                            // unknown, but there is no content.
                            parameter = toObject(getRequestEntity(),
                                    parameterType);

                            if (parameter == null) {
                                throw new ResourceException(
                                        Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
                            }
                        } else {
                            parameter = null;
                        }

                        parameters.add(parameter);
                    }
                }

                resultObject = annotationInfo.getJavaMethod().invoke(this,
                        parameters.toArray());
            } else {
                resultObject = annotationInfo.getJavaMethod().invoke(this);
            }
        } catch (IllegalArgumentException e) {
            throw new ResourceException(e);
        } catch (IllegalAccessException e) {
            throw new ResourceException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof ResourceException) {
                throw (ResourceException) e.getTargetException();
            }

            throw new ResourceException(e.getTargetException());
        }

        if (resultObject != null) {
            result = toRepresentation(resultObject, variant);
        }

        return result;
    }

    /**
     * Handles a call and checks the request's method and entity. If the method
     * is not supported, the response status is set to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}. If the request's entity
     * is no supported, the response status is set to
     * {@link Status#CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE}.
     * 
     * @param method
     *            The request method.
     * @param entity
     *            The request entity (can be null, or unavailable).
     * @return The response entity.
     * @throws ResourceException
     */
    private Representation doHandle(Method method, Representation entity) {
        Representation result = null;
        if (getAnnotation(method) != null) {
            // We know the method is supported, let's check the entity.
            AnnotationInfo annotationInfo = getAnnotation(method, entity);
            if (annotationInfo != null) {
                result = doHandle(annotationInfo, null);
            } else {
                // The request entity is not supported.
                setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
        return result;
    }

    /**
     * Effectively handles a call with content negotiation of the response
     * entity. The default behavior is to dispatch the call to one of the
     * {@link #get(Variant)}, {@link #post(Representation,Variant)},
     * {@link #put(Representation,Variant)}, {@link #delete(Variant)},
     * {@link #head(Variant)} or {@link #options(Variant)} methods.
     * 
     * @param variant
     *            The response variant expected.
     * @return The response entity.
     * @throws ResourceException
     */
    protected Representation doHandle(Variant variant) throws ResourceException {
        Representation result = null;
        Method method = getMethod();

        if (method == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No method specified");
        } else {
            if (method.equals(Method.PUT)) {
                result = put(getRequestEntity(), variant);
            } else if (isExisting()) {
                if (method.equals(Method.GET)) {
                    if (variant instanceof Representation) {
                        result = (Representation) variant;
                    } else {
                        result = get(variant);
                    }
                } else if (method.equals(Method.POST)) {
                    result = post(getRequestEntity(), variant);
                } else if (method.equals(Method.DELETE)) {
                    result = delete(variant);
                } else if (method.equals(Method.HEAD)) {
                    if (variant instanceof Representation) {
                        result = (Representation) variant;
                    } else {
                        result = head(variant);
                    }
                } else if (method.equals(Method.OPTIONS)) {
                    if (variant instanceof Representation) {
                        result = (Representation) variant;
                    } else {
                        result = options(variant);
                    }
                } else if (variant instanceof VariantInfo) {
                    result = doHandle(((VariantInfo) variant)
                            .getAnnotationInfo(), variant);
                } else {
                    setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        return result;
    }

    /**
     * Effectively handles a call with content negotiation of the response
     * entity. The default behavior is to dispatch the call to call a matching
     * annotated method or one of the {@link #get(Variant)},
     * {@link #post(Representation,Variant)},
     * {@link #put(Representation,Variant)}, {@link #delete(Variant)},
     * {@link #head(Variant)} or {@link #options(Variant)} methods.<br>
     * <br>
     * If no acceptable variant is found, the
     * {@link Status#CLIENT_ERROR_NOT_ACCEPTABLE} status is set.
     * 
     * @return The response entity.
     * @throws ResourceException
     */
    protected Representation doNegotiatedHandle() throws ResourceException {
        Representation result = null;

        if ((getVariants() != null) && (!getVariants().isEmpty())) {
            Variant preferredVariant = getClientInfo().getPreferredVariant(
                    getVariants(), getMetadataService());

            if (preferredVariant == null) {
                // No variant was found matching the client preferences
                setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
                result = describeVariants();
            } else {
                // Update the variant dimensions used for content negotiation
                updateDimensions();
                result = doHandle(preferredVariant);
            }
        } else {
            // No variant declared for this method.
            result = doHandle();
        }

        return result;
    }

    /**
     * Returns a full representation. This method is only invoked if content
     * negotiation has been disabled as indicated by the {@link #isNegotiated()}
     * , otherwise the {@link #get(Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @return The resource's representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    protected Representation get() throws ResourceException {
        Representation result = null;
        AnnotationInfo annotationInfo = getAnnotation(Method.GET);

        if (annotationInfo != null) {
            result = doHandle(annotationInfo, null);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Returns a full representation for a given variant. A variant parameter is
     * passed to indicate which representation should be returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the {@link #get()}
     * method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.<br>
     * 
     * @param variant
     *            The variant whose full representation must be returned.
     * @return The resource's representation.
     * @see #get(Variant)
     * @throws ResourceException
     */
    protected Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant instanceof VariantInfo) {
            result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                    variant);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Returns the first annotation descriptor matching the given method.
     * 
     * @param method
     *            The method to match.
     * @return The annotation descriptor.
     */
    private AnnotationInfo getAnnotation(Method method) {
        return getAnnotation(method, null);
    }

    /**
     * Returns the first annotation descriptor matching the given method.
     * 
     * @param method
     *            The method to match.
     * @param entity
     *            The request entity or null.
     * @return The annotation descriptor.
     */
    private AnnotationInfo getAnnotation(Method method, Representation entity) {
        if (isAnnotated()) {
            return AnnotationUtils.getAnnotation(getAnnotations(), method,
                    entity, getMetadataService(), getConverterService());
        }

        return null;
    }

    /**
     * Returns the annotation descriptors.
     * 
     * @return The annotation descriptors.
     */
    private List<AnnotationInfo> getAnnotations() {
        return isAnnotated() ? AnnotationUtils.getAnnotations(getClass())
                : null;
    }

    /**
     * Returns information about the resource's representation. Those metadata
     * are important for conditional method processing. The advantage over the
     * complete {@link Representation} class is that it is much lighter to
     * create. This method is only invoked if content negotiation has been
     * disabled as indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #getInfo(Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to invoke the {@link #get()} method.
     * 
     * @return Information about the resource's representation.
     * @throws ResourceException
     */
    protected RepresentationInfo getInfo() throws ResourceException {
        return get();
    }

    /**
     * Returns information about the resource's representation. Those metadata
     * are important for conditional method processing. The advantage over the
     * complete {@link Representation} class is that it is much lighter to
     * create. A variant parameter is passed to indicate which representation
     * should be returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #getInfo(Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to invoke the {@link #get(Variant)} method.
     * 
     * @param variant
     *            The variant whose representation information must be returned.
     * @return Information about the resource's representation.
     * @throws ResourceException
     */
    protected RepresentationInfo getInfo(Variant variant)
            throws ResourceException {
        return get(variant);
    }

    /**
     * Returns the callback invoked after sending the response.
     * 
     * @return The callback invoked after sending the response.
     */
    public Uniform getOnSent() {
        return getResponse().getOnSent();
    }

    /**
     * Returns the preferred variant among a list of available variants. The
     * selection is based on the client preferences using the
     * {@link ClientInfo#getPreferredVariant(List, MetadataService)} method.
     * 
     * @param variants
     *            The available variants.
     * @return The preferred variant.
     */
    protected Variant getPreferredVariant(List<Variant> variants) {
        Variant result = null;

        // If variants were found, select the best matching one
        if ((variants != null) && (!variants.isEmpty())) {
            result = getClientInfo().getPreferredVariant(
                    variants,
                    (getApplication() == null) ? null : getApplication()
                            .getMetadataService());
        }

        return result;
    }

    /**
     * Returns a modifiable list of exposed variants for the current request
     * method. You can declare variants manually by updating the result list ,
     * by overriding this method. By default, the variants will be provided
     * based on annotated methods.
     * 
     * @return The modifiable list of variants.
     */
    public List<Variant> getVariants() {
        return getVariants(getMethod());
    }

    /**
     * Returns a modifiable list of exposed variants for the given method. You
     * can declare variants manually by updating the result list , by overriding
     * this method. By default, the variants will be provided based on annotated
     * methods.
     * 
     * @param method
     *            The method.
     * @return The modifiable list of variants.
     */
    protected List<Variant> getVariants(Method method) {
        List<Variant> result = this.variants;

        if (result == null) {
            result = new ArrayList<Variant>();

            // Add annotation-based variants in priority
            if (isAnnotated() && hasAnnotations()) {
                List<Variant> annoVariants = null;
                method = (Method.HEAD.equals(method)) ? Method.GET : method;

                for (AnnotationInfo annotationInfo : getAnnotations()) {
                    if (annotationInfo.isCompatible(method, getRequestEntity(),
                            getMetadataService(), getConverterService())) {
                        annoVariants = annotationInfo.getResponseVariants(
                                getMetadataService(), getConverterService());

                        if (annoVariants != null) {
                            for (Variant v : annoVariants) {
                                result.add(new VariantInfo(v, annotationInfo));
                            }
                        }
                    }
                }
            }

            this.variants = result;
        }

        return result;
    }

    /**
     * Handles any call to this resource. The default implementation check the
     * {@link #isConditional()} and {@link #isNegotiated()} method to determine
     * which one of the {@link #doConditionalHandle()},
     * {@link #doNegotiatedHandle()} and {@link #doHandle()} methods should be
     * invoked. It also catches any {@link ResourceException} thrown and updates
     * the response status using the
     * {@link #setStatus(Status, Throwable, String)} method.<br>
     * <br>
     * After handling, if the status is set to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}, then
     * {@link #updateAllowedMethods()} is invoked to give the resource a chance
     * to inform the client about the allowed methods.
     * 
     * @return The response entity.
     */
    @Override
    public Representation handle() {
        Representation result = null;

        // If the resource is not available after initialization and if this a
        // retrieval method, then return a "not found" response.
        if (!isExisting() && getMethod().isSafe()) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            try {
                if (isConditional()) {
                    result = doConditionalHandle();
                } else if (isNegotiated()) {
                    result = doNegotiatedHandle();
                } else {
                    result = doHandle();
                }

                if (!getResponse().isEntityAvailable()) {
                    // If the user manually set the entity, keep it
                    getResponse().setEntity(result);
                }

                if (Status.CLIENT_ERROR_METHOD_NOT_ALLOWED.equals(getStatus())) {
                    updateAllowedMethods();
                } else if (Method.GET.equals(getMethod())
                        && Status.SUCCESS_OK.equals(getStatus())
                        && (getResponseEntity() == null || !getResponseEntity()
                                .isAvailable())) {
                    getLogger()
                            .fine(
                                    "A response with a 200 (Ok) status should have an entity. Changing the status to 204 (No content).");
                    setStatus(Status.SUCCESS_NO_CONTENT);
                }
            } catch (Throwable t) {
                doCatch(t);
            }
        }

        return result;
    }

    /**
     * Indicates if annotations were defined on this resource.
     * 
     * @return True if annotations were defined on this resource.
     */
    private boolean hasAnnotations() {
        return (getAnnotations() != null) && (!getAnnotations().isEmpty());
    }

    /**
     * Returns a representation whose metadata will be returned to the client.
     * This method is only invoked if content negotiation has been disabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #head(Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @return The resource's representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    protected Representation head() throws ResourceException {
        return get();
    }

    /**
     * Returns a representation whose metadata will be returned to the client. A
     * variant parameter is passed to indicate which representation should be
     * returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the {@link #head()}
     * method is invoked.<br>
     * <br>
     * The default implementation directly returns the variant if it is already
     * an instance of {@link Representation}. In other cases, you need to
     * override this method in order to provide your own implementation. *
     * 
     * @param variant
     *            The variant whose full representation must be returned.
     * @return The resource's representation.
     * @see #get(Variant)
     * @throws ResourceException
     */
    protected Representation head(Variant variant) throws ResourceException {
        return get(variant);
    }

    /**
     * Indicates if annotations are supported. The default value is true.
     * 
     * @return True if annotations are supported.
     */
    public boolean isAnnotated() {
        return annotated;
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
        return getResponse().isAutoCommitting();
    }

    /**
     * Indicates if the response has already been committed.
     * 
     * @return True if the response has already been committed.
     */
    public boolean isCommitted() {
        return getResponse().isCommitted();
    }

    /**
     * Indicates if conditional handling is enabled. The default value is true.
     * 
     * @return True if conditional handling is enabled.
     */
    public boolean isConditional() {
        return conditional;
    }

    /**
     * Indicates if the identified resource exists. The default value is true.
     * 
     * @return True if the identified resource exists.
     */
    public boolean isExisting() {
        return existing;
    }

    /**
     * Indicates if the authenticated client user associated to the current
     * request is in the given role name.
     * 
     * @param roleName
     *            The role name to test.
     * @return True if the authenticated subject is in the given role.
     */
    public boolean isInRole(String roleName) {
        return getClientInfo().getRoles().contains(
                getApplication().getRole(roleName));
    }

    /**
     * Indicates if content negotiation of response entities is enabled. The
     * default value is true.
     * 
     * @return True if content negotiation of response entities is enabled.
     */
    public boolean isNegotiated() {
        return this.negotiated;
    }

    /**
     * Indicates the communication options available for this resource. This
     * method is only invoked if content negotiation has been disabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #options(Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @return The optional response entity.
     */
    protected Representation options() throws ResourceException {
        Representation result = null;
        AnnotationInfo annotationInfo = getAnnotation(Method.OPTIONS);

        // Updates the list of allowed methods
        updateAllowedMethods();

        if (annotationInfo != null) {
            result = doHandle(annotationInfo, null);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Indicates the communication options available for this resource. A
     * variant parameter is passed to indicate which representation should be
     * returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #options()} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.<br>
     * 
     * @param variant
     *            The variant of the response entity.
     * @return The optional response entity.
     * @see #get(Variant)
     */
    protected Representation options(Variant variant) throws ResourceException {
        Representation result = null;

        // Updates the list of allowed methods
        updateAllowedMethods();

        if (variant instanceof VariantInfo) {
            result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                    variant);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Posts a representation to the resource at the target URI reference. This
     * method is only invoked if content negotiation has been disabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #post(Representation, Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @param entity
     *            The posted entity.
     * @return The optional response entity.
     * @throws ResourceException
     * @see #get(Variant)
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    protected Representation post(Representation entity)
            throws ResourceException {
        return doHandle(Method.POST, entity);
    }

    /**
     * Posts a representation to the resource at the target URI reference. A
     * variant parameter is passed to indicate which representation should be
     * returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #post(Representation)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.<br>
     * 
     * @param entity
     *            The posted entity.
     * @param variant
     *            The variant of the response entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5"
     *      >HTTP POST method</a>
     */
    protected Representation post(Representation entity, Variant variant)
            throws ResourceException {
        Representation result = null;

        if (variant instanceof VariantInfo) {
            result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                    variant);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Creates or updates a resource with the given representation as new state
     * to be stored. This method is only invoked if content negotiation has been
     * disabled as indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #put(Representation, Variant)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
     * 
     * @param entity
     *            The representation to store.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    protected Representation put(Representation entity)
            throws ResourceException {
        return doHandle(Method.PUT, entity);
    }

    /**
     * Creates or updates a resource with the given representation as new state
     * to be stored. A variant parameter is passed to indicate which
     * representation should be returned if any.<br>
     * <br>
     * This method is only invoked if content negotiation has been enabled as
     * indicated by the {@link #isNegotiated()}, otherwise the
     * {@link #put(Representation)} method is invoked.<br>
     * <br>
     * The default behavior is to set the response status to
     * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.<br>
     * 
     * @param representation
     *            The representation to store.
     * @param variant
     *            The variant of the response entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see #get(Variant)
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6"
     *      >HTTP PUT method</a>
     */
    protected Representation put(Representation representation, Variant variant)
            throws ResourceException {
        Representation result = null;

        if (variant instanceof VariantInfo) {
            result = doHandle(((VariantInfo) variant).getAnnotationInfo(),
                    variant);
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return result;
    }

    /**
     * Permanently redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *            The target URI reference.
     */
    public void redirectPermanent(Reference targetRef) {
        if (getResponse() != null) {
            getResponse().redirectPermanent(targetRef);
        }
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
        if (getResponse() != null) {
            getResponse().redirectPermanent(targetUri);
        }
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
        if (getResponse() != null) {
            getResponse().redirectSeeOther(targetRef);
        }
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
        if (getResponse() != null) {
            getResponse().redirectSeeOther(targetUri);
        }
    }

    /**
     * Temporarily redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *            The target reference.
     */
    public void redirectTemporary(Reference targetRef) {
        if (getResponse() != null) {
            getResponse().redirectTemporary(targetRef);
        }
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
        if (getResponse() != null) {
            getResponse().redirectTemporary(targetUri);
        }
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
        if (getResponse() != null) {
            getResponse().setAllowedMethods(allowedMethods);
        }
    }

    /**
     * Indicates if annotations are supported. The default value is true.
     * 
     * @param annotated
     *            Indicates if annotations are supported.
     */
    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    /**
     * Indicates if the response should be automatically committed.
     * 
     * @param autoCommitting
     *            True if the response should be automatically committed
     */
    public void setAutoCommitting(boolean autoCommitting) {
        getResponse().setAutoCommitting(autoCommitting);
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
        if (getResponse() != null) {
            getResponse().setChallengeRequests(requests);
        }
    }

    /**
     * Indicates if the response has already been committed.
     * 
     * @param committed
     *            True if the response has already been committed.
     */
    public void setCommitted(boolean committed) {
        getResponse().setCommitted(committed);
    }

    /**
     * Indicates if conditional handling is enabled. The default value is true.
     * 
     * @param conditional
     *            True if conditional handling is enabled.
     */
    public void setConditional(boolean conditional) {
        this.conditional = conditional;
    }

    /**
     * Sets the cookie settings provided by the server.
     * 
     * @param cookieSettings
     *            The cookie settings provided by the server.
     * @see Response#setCookieSettings(Series)
     */
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        if (getResponse() != null) {
            getResponse().setCookieSettings(cookieSettings);
        }
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
        if (getResponse() != null) {
            getResponse().setDimensions(dimensions);
        }
    }

    /**
     * Indicates if the identified resource exists. The default value is true.
     * 
     * @param exists
     *            Indicates if the identified resource exists.
     */
    public void setExisting(boolean exists) {
        this.existing = exists;
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
        if (getResponse() != null) {
            getResponse().setLocationRef(locationRef);
        }
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
        if (getResponse() != null) {
            getResponse().setLocationRef(locationUri);
        }
    }

    /**
     * Indicates if content negotiation of response entities is enabled. The
     * default value is true.
     * 
     * @param negotiateContent
     *            True if content negotiation of response entities is enabled.
     */
    public void setNegotiated(boolean negotiateContent) {
        this.negotiated = negotiateContent;
    }

    /**
     * Sets the callback invoked after sending the response.
     * 
     * @param onSentCallback
     *            The callback invoked after sending the response.
     */
    public void setOnSent(Uniform onSentCallback) {
        getResponse().setOnSent(onSentCallback);
    }

    /**
     * Sets the server-specific information.
     * 
     * @param serverInfo
     *            The server-specific information.
     * @see Response#setServerInfo(ServerInfo)
     */
    public void setServerInfo(ServerInfo serverInfo) {
        if (getResponse() != null) {
            getResponse().setServerInfo(serverInfo);
        }
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @see Response#setStatus(Status)
     */
    public void setStatus(Status status) {
        if (getResponse() != null) {
            getResponse().setStatus(status);
        }
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
        if (getResponse() != null) {
            getResponse().setStatus(status, message);
        }
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
        if (getResponse() != null) {
            getResponse().setStatus(status, throwable);
        }
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
        if (getResponse() != null) {
            getResponse().setStatus(status, throwable, message);
        }
    }

    /**
     * Invoked when the list of allowed methods needs to be updated. The
     * {@link #getAllowedMethods()} or the {@link #setAllowedMethods(Set)}
     * methods should be used. The default implementation lists the annotated
     * methods.
     */
    public void updateAllowedMethods() {
        getAllowedMethods().clear();
        List<AnnotationInfo> annotations = getAnnotations();

        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (!getAllowedMethods().contains(
                        annotationInfo.getRestletMethod())) {
                    getAllowedMethods().add(annotationInfo.getRestletMethod());
                }
            }
        }
    }

    /**
     * Update the dimensions that were used for content negotiation. By default,
     * it adds the {@link Dimension#CHARACTER_SET}, {@link Dimension#ENCODING},
     * {@link Dimension#LANGUAGE}and {@link Dimension#MEDIA_TYPE} constants.
     */
    protected void updateDimensions() {
        getDimensions().add(Dimension.CHARACTER_SET);
        getDimensions().add(Dimension.ENCODING);
        getDimensions().add(Dimension.LANGUAGE);
        getDimensions().add(Dimension.MEDIA_TYPE);
    }

}
