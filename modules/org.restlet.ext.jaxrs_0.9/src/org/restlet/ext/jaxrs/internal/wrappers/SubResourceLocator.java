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
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

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
     *                The Java method wich creats the sub resource
     * @param annotatedMethod
     *                the message containing the annotations for this sub
     *                resource locator.
     * @param resourceClass
     *                the wrapped resource class.
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param entityProviders
     *                all entity providers
     * @param allCtxResolvers
     *                all ContextResolvers
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     * @throws IllegalPathOnMethodException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException if the annotated method is null
     * @throws IllegalTypeException
     *                 if one of the parameters annotated with &#64;{@link Context}
     *                 has a type that must not be annotated with &#64;{@link Context}.
     */
    SubResourceLocator(Method javaMethod, Method annotatedMethod,
            ResourceClass resourceClass, ThreadLocalizedContext tlContext,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalPathOnMethodException, IllegalArgumentException,
            MissingAnnotationException, IllegalMethodParamTypeException {
        super(javaMethod, annotatedMethod, resourceClass, tlContext,
                entityProviders, allCtxResolvers, extensionBackwardMapping,
                false, logger);
    }

    /**
     * Creates a sub resource
     * 
     * @param resourceObject
     *                the wrapped resource object.
     * @param wrapperFactory
     *                factory to create wrappers.
     * @param logger
     *                The logger to use
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
            WrapperFactory wrapperFactory, Logger logger)
            throws InvocationTargetException, WebApplicationException,
            InstantiateException, ConvertRepresentationException,
            IllegalArgumentException, MissingAnnotationException {
        Object subResObj;
        try {
            subResObj = internalInvoke(resourceObject);
        } catch (IllegalArgumentException e) {
            throw new InstantiateException(executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new InstantiateException(executeMethod, e);
        }
        if (subResObj == null) {
            logger
                    .warning("The sub resource object is null. That is not allowed");
            ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
            rb.entity("The sub resource object is null. That is not allowed");
            throw new WebApplicationException(rb.build());
        }
        ResourceClass resourceClass;
        resourceClass = wrapperFactory.getResourceClass(subResObj.getClass());
        return new ResourceObject(subResObj, resourceClass);
    }
}