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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;

/**
 * This class wraps JAX-RS resource methods and sub resource methods.<br>
 * It does not wrap sub resource locators; see {@link SubResourceLocator}
 * 
 * @author Stephan Koops
 */
public class ResourceMethod extends AbstractMethodWrapper implements
        ResourceMethodOrLocator {

    /** @see ConsumeMime */
    private List<MediaType> consumedMimes;

    private org.restlet.data.Method httpMethod;

    /** @see ProduceMime */
    private List<MediaType> producedMimes;

    /**
     * Creates a wrapper for a resource method.
     * 
     * @param executeMethod
     *                the Java method to wrap.
     * @param annotatedMethod
     *                the java method that contains the annotations for this
     *                method.
     * @param resourceClass
     *                the wrapped class of the method.
     * @param httpMethod
     *                the HTTP method of the Java method. It will be checked be
     *                the {@link JaxRsRouter}, so avoiding double work. It will
     *                be requested from the javaMethod.
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link JaxRsRouter}.
     * @param entityProviders
     *                all entity providers
     * @param allCtxResolvers
     *                all ContextResolvers
     * @param logger
     * @throws IllegalPathOnMethodException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     */
    ResourceMethod(Method executeMethod, Method annotatedMethod,
            ResourceClass resourceClass, org.restlet.data.Method httpMethod,
            ThreadLocalizedContext tlContext, EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers, Logger logger)
            throws IllegalPathOnMethodException, IllegalArgumentException,
            MissingAnnotationException {
        super(executeMethod, annotatedMethod, resourceClass, tlContext,
                entityProviders, allCtxResolvers, true, logger);
        this.httpMethod = httpMethod;
    }

    /**
     * @return Returns an unmodifiable List with the MediaTypes the given
     *         resourceMethod consumes. If no consumeMime is given, this method
     *         returns a List with MediaType.ALL. Will never return null.
     */
    public List<MediaType> getConsumedMimes() {
        if (this.consumedMimes == null) {
            ConsumeMime consumeMime;
            consumeMime = this.annotatedMethod.getAnnotation(ConsumeMime.class);
            if (consumeMime == null)
                consumeMime = this.executeMethod.getDeclaringClass()
                        .getAnnotation(ConsumeMime.class);
            if (consumeMime == null)
                this.consumedMimes = Collections.singletonList(MediaType.ALL);
            else
                this.consumedMimes = WrapperUtil
                        .convertToMediaTypes(consumeMime.value());
        }
        return consumedMimes;
    }

    /**
     * @return Returns the HTTP method supported by the wrapped java method.
     */
    public org.restlet.data.Method getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * @return Returns an unmodifiable List of MediaTypes the given Resource
     *         Method. if the method is not annotated with {@link ProduceMime},
     *         than the {@link ProduceMime} of the Resource class is returned.
     *         If no {@link ProduceMime} can be found, an empty (also
     *         unmodifiable) List will returned.<br>
     *         This method never returns null.
     */
    public List<MediaType> getProducedMimes() {
        if (producedMimes == null) {
            ProduceMime produceMime;
            produceMime = this.annotatedMethod.getAnnotation(ProduceMime.class);
            if (produceMime == null)
                produceMime = this.executeMethod.getDeclaringClass()
                        .getAnnotation(ProduceMime.class);
            if (produceMime != null)
                this.producedMimes = WrapperUtil
                        .convertToMediaTypes(produceMime.value());
            else
                this.producedMimes = Collections.emptyList();
        }
        return producedMimes;
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
            throw new MethodInvokeException(
                    "Could not invoke " + executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new MethodInvokeException(
                    "Could not invoke " + executeMethod, e);
        }
    }

    /**
     * Check if this method supports the media type to produce for a request.
     * 
     * @param accMediaTypess
     *                The Media Types the client would accept, ordered by
     *                quality. See {@link SortedMetadata}
     * @return Returns true, if the give MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false.
     */
    public boolean isAcceptedMediaTypeSupported(
            SortedMetadata<MediaType> accMediaTypess) {
        if (accMediaTypess == null || accMediaTypess.isEmpty())
            return true;
        List<MediaType> prodMimes = getProducedMimes();
        if (prodMimes.isEmpty())
            return true;
        for (MediaType producedMediaType : prodMimes) {
            for (MediaType accMediaType : accMediaTypess)
                if (accMediaType.isCompatible(producedMediaType))
                    return true;
        }
        return false;
    }

    /**
     * @param resourceMethod
     *                the resource method to check
     * @param givenMediaType
     *                the MediaType of the request entity
     * @return Returns true, if the given MediaType is supported by the method,
     *         or no MediaType is given for the method, otherweise false;
     */
    public boolean isGivenMediaTypeSupported(MediaType givenMediaType) {
        if (givenMediaType == null)
            return true;
        for (MediaType consumedMime : this.getConsumedMimes()) {
            if (consumedMime.includes(givenMediaType))
                return true;
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
     *                the requested Method
     * @param alsoGet
     *                if true, than this method returns also true, if this
     *                method is GET. This functionality is needed for HEAD.
     * @return true, if this method supports the given HTTP method. Returns also
     *         true, if alsoGet is true and this method is true.
     * @throws IllegalArgumentException
     */
    public boolean isHttpMethodSupported(
            org.restlet.data.Method requestedMethod, boolean alsoGet)
            throws IllegalArgumentException {
        if (requestedMethod == null)
            throw new IllegalArgumentException(
                    "null is not a valid HTTP method");
        if (this.httpMethod == null)
            this.httpMethod = WrapperUtil.getHttpMethod(this.annotatedMethod);
        if (alsoGet && this.httpMethod.equals(org.restlet.data.Method.GET))
            return true;
        return this.httpMethod.equals(requestedMethod);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + executeMethod.toString()
                + ", " + this.httpMethod + "]";
    }
}