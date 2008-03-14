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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;

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
     * @throws IllegalPathOnMethodException
     */
    SubResourceLocator(Method javaMethod, Method annotatedMethod,
            ResourceClass resourceClass) throws IllegalPathOnMethodException {
        super(javaMethod, annotatedMethod, resourceClass);
    }

    /**
     * Creates a sub resource
     * 
     * @param resourceObject
     *                the wrapped resource object.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param jaxRsRouter
     * @return Returns the wrapped sub resource object.
     * @throws InvocationTargetException
     * @throws NoMessageBodyReaderException
     * @throws WebApplicationException
     * @throws MissingAnnotationException
     * @throws InstantiateRessourceException
     * @throws ConvertParameterException
     * @throws MethodInvokeException
     */
    public ResourceObject createSubResource(ResourceObject resourceObject,
            CallContext callContext, HiddenJaxRsRouter jaxRsRouter)
            throws InvocationTargetException, MissingAnnotationException,
            WebApplicationException, NoMessageBodyReaderException,
            InstantiateRessourceException, ConvertParameterException,
            MethodInvokeException {
        Object[] args;
        Class<?>[] parameterTypes = this.executeMethod.getParameterTypes();
        if (parameterTypes.length == 0)
            args = new Object[0];
        else
            args = getParameterValues(parameterTypes, executeMethod
                    .getGenericParameterTypes(), annotatedMethod
                    .getParameterAnnotations(), leaveEncoded, callContext,
                    jaxRsRouter);
        Object subResObj;
        try {
            subResObj = executeMethod.invoke(resourceObject
                    .getJaxRsResourceObject(), args);
        } catch (IllegalArgumentException e) {
            throw new InstantiateRessourceException(executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new InstantiateRessourceException(executeMethod, e);
        }
        if (subResObj == null) {
            jaxRsRouter.getLogger().warning(
                    "The sub resource object is null. That is not allowed");
            ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
            rb.entity("The sub resource object is null. That is not allowed");
            throw new WebApplicationException(rb.build());
        }
        WrapperFactory wrapperFactory = jaxRsRouter.getWrapperFactory();
        ResourceClass resourceClass = wrapperFactory.getResourceClass(subResObj
                .getClass());
        ResourceObject subResourceObject = new ResourceObject(subResObj,
                resourceClass);
        try {
            subResourceObject.init(callContext);
        } catch (InjectException e) {
            throw new InstantiateRessourceException(executeMethod, e);
        }
        return subResourceObject;
    }
}