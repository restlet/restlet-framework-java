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

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.ext.jaxrs.InstantiateException;
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
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * A method of a resource class that is used to locate sub-resources of the
 * corresponding resource, see section 3.3.1. of the JAX-RS specification.
 * 
 * @author Stephan Koops
 */
public class SubResourceLocator extends AbstractMethodWrapper implements
        ResourceMethodOrLocator {
    /**
     * Creates a new wrapper for the given sub resource locator.
     * 
     * @param javaMethod
     *            The Java method which creats the sub resource
     * @param annotatedMethod
     *            the message containing the annotations for this sub resource
     *            locator.
     * @param resourceClass
     *            the wrapped resource class.
     * @param tlContext
     *            the {@link ThreadLocalizedContext} of the
     *            {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     *            all providers
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * @param logger
     * @throws IllegalPathOnMethodException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     *             if the annotated method is null
     * @throws IllegalPathParamTypeException
     */
    SubResourceLocator(Method javaMethod, Method annotatedMethod,
            ResourceClass resourceClass, ThreadLocalizedContext tlContext,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalPathOnMethodException, IllegalArgumentException,
            MissingAnnotationException, IllegalMethodParamTypeException,
            IllegalPathParamTypeException {
        super(javaMethod, annotatedMethod, resourceClass, tlContext,
                jaxRsProviders, extensionBackwardMapping, false, logger);
    }

    /**
     * Creates a sub resource
     * 
     * @param resourceObject
     *            the wrapped resource object.
     * @param resourceClasses
     *            factory to create wrappers.
     * @param logger
     *            The logger to use
     * @return Returns the wrapped sub resource object.
     * @throws InvocationTargetException
     * @throws NoMessageBodyReaderException
     * @throws WebApplicationException
     * @throws MissingAnnotationException
     * @throws InstantiateException
     * @throws ConvertCookieParamException
     * @throws ConvertQueryParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertRepresentationException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     */
    public ResourceObject createSubResource(ResourceObject resourceObject,
            ResourceClasses resourceClasses, Logger logger)
            throws InvocationTargetException, WebApplicationException,
            InstantiateException, ConvertRepresentationException,
            IllegalArgumentException, MissingAnnotationException {
        Object subResObj;
        try {
            subResObj = internalInvoke(resourceObject);
        } catch (IllegalArgumentException e) {
            throw new InstantiateException(this.executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new InstantiateException(this.executeMethod, e);
        }
        if (subResObj == null) {
            logger.warning("The sub resource object is null. That is not allowed");
            final ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
            rb.entity("The sub resource object is null. That is not allowed");
            throw new WebApplicationException(rb.build());
        }
        ResourceClass resourceClass;
        resourceClass = resourceClasses.getResourceClass(subResObj.getClass());
        return new ResourceObject(subResObj, resourceClass);
    }
}
