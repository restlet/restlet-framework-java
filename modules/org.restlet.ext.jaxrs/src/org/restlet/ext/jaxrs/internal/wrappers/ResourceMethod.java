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

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Variant;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * This class wraps JAX-RS resource methods and sub resource methods.<br>
 * It does not wrap sub resource locators; see {@link SubResourceLocator}
 * 
 * @author Stephan Koops
 */
public class ResourceMethod extends AbstractMethodWrapper implements
        ResourceMethodOrLocator {
    // NICE a subset of MessageBodyReaders could be cached here.
    // . . . . . . . . . . (the concrete media type of the request could differ)
    // NICE check, which subset of MessageBodyWriters could be cached here.
    // . . (the media type is available via @Produces on the entity provider)

    /**
     * the Java method that should be referenced for annotations. This method
     * could be different from the method is called for executing, see section
     * 3.6 "Annotation Inheritance" of JSR-311-spec.
     * 
     * @see AbstractMethodWrapper#executeMethod
     */
    private final Method annotatedMethod;

    /** @see Consumes */
    private final List<MediaType> consumedMimes;

    private final org.restlet.data.Method httpMethod;

    /** @see Produces */
    private final List<MediaType> producedMimes;

    /**
     * Contains the list of supported {@link Variant}s (lazy initialized by
     * {@link #getSupportedVariants()}.
     */
    private final Collection<Variant> supportedVariants;

    /**
     * Creates a wrapper for a resource method.
     * 
     * @param executeMethod
     *            the Java method to wrap.
     * @param annotatedMethod
     *            the java method that contains the annotations for this method.
     * @param resourceClass
     *            the wrapped class of the method.
     * @param httpMethod
     *            the HTTP method of the Java method. It will be checked be the
     *            {@link org.restlet.ext.jaxrs.JaxRsRestlet}, so avoiding double
     *            work. It will be requested from the javaMethod.
     * @param tlContext
     *            the {@link ThreadLocalizedContext} of the
     *            {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     *            all entity providers
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * @param logger
     * @throws IllegalPathOnMethodException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     *             if the annotated method is null
     * @throws IllegalMethodParamTypeException
     *             if one of the method parameters annotated with &#64;
     *             {@link Context} has a type that must not be annotated with
     *             &#64;{@link Context}.
     * @throws IllegalPathParamTypeException
     */
    ResourceMethod(Method executeMethod, Method annotatedMethod,
            ResourceClass resourceClass, org.restlet.data.Method httpMethod,
            ThreadLocalizedContext tlContext, JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalPathOnMethodException, IllegalArgumentException,
            MissingAnnotationException, IllegalMethodParamTypeException,
            IllegalPathParamTypeException {
        super(executeMethod, annotatedMethod, resourceClass, tlContext,
                jaxRsProviders, extensionBackwardMapping, true, logger);
        this.annotatedMethod = annotatedMethod;
        if (httpMethod != null) {
            this.httpMethod = httpMethod;
        } else {
            this.httpMethod = WrapperUtil.getHttpMethod(this.annotatedMethod);
        }
        this.consumedMimes = createConsumedMimes();
        this.producedMimes = createProducedMimes();
        this.supportedVariants = createSupportedVariants();
    }

    /**
     * Creates the list of the consumed mimes from the
     * {@link AbstractMethodWrapper#annotatedMethod} and the
     * {@link AbstractMethodWrapper#executeMethod} to be stored in the final
     * instance variable {@link #consumedMimes}.
     */
    private List<MediaType> createConsumedMimes() {
        Consumes consumes;
        consumes = this.annotatedMethod.getAnnotation(Consumes.class);
        if (consumes == null) {
            consumes = this.executeMethod.getDeclaringClass().getAnnotation(
                    Consumes.class);
        }
        if (consumes != null) {
            return WrapperUtil.convertToMediaTypes(consumes.value());
        }

        return Collections.singletonList(MediaType.ALL);
    }

    /**
     * Creates the list of the produced mimes from the
     * {@link AbstractMethodWrapper#annotatedMethod} and the
     * {@link AbstractMethodWrapper#executeMethod} to be stored in the final
     * instance variable {@link #producedMimes}.
     */
    private List<MediaType> createProducedMimes() {
        Produces produces;
        produces = this.annotatedMethod.getAnnotation(Produces.class);
        if (produces == null) {
            produces = this.executeMethod.getDeclaringClass().getAnnotation(
                    Produces.class);
        }
        if (produces != null) {
            return WrapperUtil.convertToMediaTypes(produces.value());
        }

        return Collections.emptyList();
    }

    /**
     * Creates the list of the supported variants from the
     * {@link #getProducedMimes()}to be stored in the final instance variable
     * {@link #supportedVariants}.
     */
    private Collection<Variant> createSupportedVariants() {
        final Collection<Variant> supportedVariants = new ArrayList<Variant>();
        for (final MediaType mediaType : getProducedMimes()) {
            javax.ws.rs.core.MediaType mt;
            mt = Converter.toJaxRsMediaType(mediaType);
            supportedVariants.add(new Variant(mt, null, null));
        }
        return supportedVariants;
    }

    /**
     * Returns the array of the annotations on the Java method
     * 
     * @return the array of the annotations on the Java method
     * @see Method#getAnnotations()
     */
    public Annotation[] getAnnotations() {
        return this.annotatedMethod.getAnnotations();
    }

    /**
     * @return Returns an unmodifiable List with the MediaTypes the given
     *         resourceMethod consumes. If no consumeMime is given, this method
     *         returns a List with MediaType.ALL. Will never return null.
     */
    public List<MediaType> getConsumedMimes() {
        return this.consumedMimes;
    }

    /**
     * Returns the generic return type of the wrapped method.
     * 
     * @return the generic return type of the wrapped method.
     * @see Method#getGenericReturnType()
     */
    public Type getGenericReturnType() {
        return this.executeMethod.getGenericReturnType();
    }

    /**
     * @return Returns the HTTP method supported by the wrapped java method.
     */
    public org.restlet.data.Method getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * @return Returns an unmodifiable List of MediaTypes the given Resource
     *         Method. if the method is not annotated with {@link Produces},
     *         than the {@link Produces} of the Resource class is returned. If
     *         no {@link Produces} can be found, an empty (also unmodifiable)
     *         List will returned.<br>
     *         This method never returns null.
     */
    public List<MediaType> getProducedMimes() {
        return this.producedMimes;
    }

    /**
     * Returns the {@link Variant}s supported by this resource method.
     * 
     * @return the {@link Variant}s supported by this resource method.
     */
    public Collection<Variant> getSupportedVariants() {
        return this.supportedVariants;
    }

    /**
     * Invokes the method and returned the created representation for the
     * response.
     * 
     * @param resourceObject
     * @return the unwrapped returned object by the wrapped method.
     * @throws MethodInvokeException
     * @throws InvocationTargetException
     * @throws NoMessageBodyReaderException
     * @throws MissingAnnotationException
     * @throws ConvertCookieParamException
     * @throws ConvertQueryParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertRepresentationException
     * @throws WebApplicationException
     */
    public Object invoke(ResourceObject resourceObject)
            throws MethodInvokeException, InvocationTargetException,
            ConvertRepresentationException, WebApplicationException {
        try {
            return internalInvoke(resourceObject);
        } catch (IllegalArgumentException e) {
            throw new MethodInvokeException("Could not invoke "
                    + this.executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new MethodInvokeException("Could not invoke "
                    + this.executeMethod, e);
        }
    }

    /**
     * Check if this method supports the media type to produce for a request.
     * 
     * @param accMediaTypess
     *            The Media Types the client would accept, ordered by quality.
     *            See {@link SortedMetadata}
     * @return Returns true, if the give MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false.
     */
    public boolean isAcceptedMediaTypeSupported(
            SortedMetadata<MediaType> accMediaTypess) {
        if ((accMediaTypess == null) || accMediaTypess.isEmpty()) {
            return true;
        }
        final List<MediaType> prodMimes = getProducedMimes();
        if (prodMimes.isEmpty()) {
            return true;
        }
        for (final MediaType producedMediaType : prodMimes) {
            for (final MediaType accMediaType : accMediaTypess) {
                if (accMediaType.isCompatible(producedMediaType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param resourceMethod
     *            the resource method to check
     * @param givenMediaType
     *            the MediaType of the request entity
     * @return Returns true, if the given MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false;
     */
    public boolean isGivenMediaTypeSupported(MediaType givenMediaType) {
        if (givenMediaType == null) {
            return true;
        }
        for (final MediaType consumedMime : getConsumedMimes()) {
            if (consumedMime.includes(givenMediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param resourceMethod
     * @param requestedMethod
     * @return true, if the gien java method is annotated with a runtime
     *         designator for the given requested Method. If the requested
     *         method is null, than the method returns true, when the method is
     *         annotated with any runtime desginator.
     * @see #annotatedWithMethodDesignator(Method)
     */
    public boolean isHttpMethodSupported(org.restlet.data.Method requestedMethod) {
        return isHttpMethodSupported(requestedMethod, false);
    }

    /**
     * Checks, if this method suppors the given HTTP method.
     * 
     * @param requestedMethod
     *            the requested Method
     * @param alsoGet
     *            if true, than this method returns also true, if this method is
     *            GET. This functionality is needed for HEAD.
     * @return true, if this method supports the given HTTP method. Returns also
     *         true, if alsoGet is true and this method is true.
     * @throws IllegalArgumentException
     */
    public boolean isHttpMethodSupported(
            org.restlet.data.Method requestedMethod, boolean alsoGet)
            throws IllegalArgumentException {
        if (requestedMethod == null) {
            throw new IllegalArgumentException(
                    "null is not a valid HTTP method");
        }
        if (alsoGet && this.httpMethod.equals(org.restlet.data.Method.GET)) {
            return true;
        }
        return this.httpMethod.equals(requestedMethod);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "["
                + this.executeMethod.toString() + ", " + this.httpMethod + "]";
    }
}