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

package org.restlet.ext.jaxrs.wrappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.core.CallContext;
import org.restlet.ext.jaxrs.exceptions.InjectException;
import org.restlet.ext.jaxrs.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.exceptions.NoMessageBodyReadersException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;

/**
 * Instances represents a root resource class.
 * 
 * A resource class annotated with
 * 
 * {@link Path}: Root resource classes provide the roots of the resource class
 * tree and provide access to sub-resources, see chapter 2 of JSR-311-Spec.
 * 
 * @author Stephan Koops
 * 
 */
public class RootResourceClass extends ResourceClass {

    private Constructor<?> constructor;

    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *                the root resource class to wrap
     * @see WrapperFactory#getRootResourceClass(Class)
     */
    RootResourceClass(Class<?> jaxRsClass, Logger logger) {
        super(jaxRsClass, true, logger);
        constructor = findJaxRsConstructor(getJaxRsClass());
    }

    /**
     * Checks if the parameters for the constructor are valid for a JAX-RS root
     * resource class.
     * 
     * @param paramAnnotationss
     * @param parameterTypes
     * @throws IllegalTypeException
     * @returns true, if the
     * @throws IllegalTypeException
     *                 If a parameter is annotated with {@link Context}, but
     *                 the type is invalid (must be UriInfo, Request or
     *                 HttpHeaders).
     */
    private static boolean checkParamAnnotations(Constructor<?> constr) {
        Annotation[][] paramAnnotationss = constr.getParameterAnnotations();
        Class<?>[] parameterTypes = constr.getParameterTypes();
        for (int i = 0; i < paramAnnotationss.length; i++) {
            Annotation[] parameterAnnotations = paramAnnotationss[i];
            Class<?> parameterType = parameterTypes[i];
            boolean ok = checkParameterAnnotation(parameterAnnotations,
                    parameterType);
            if (!ok)
                return false;
        }
        return true;
    }

    private static boolean checkParameterAnnotation(
            Annotation[] parameterAnnotations, Class<?> parameterType) {
        if (parameterAnnotations.length == 0)
            return false;
        for (Annotation annotation : parameterAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                continue;
            } else if (annotationType.equals(PathParam.class)) {
                continue;
            } else if (annotationType.equals(Context.class)) {
                if (parameterType.equals(UriInfo.class))
                    continue;
                if (parameterType.equals(Request.class))
                    continue;
                if (parameterType.equals(HttpHeaders.class))
                    continue;
                if (parameterType.equals(SecurityContext.class))
                    continue;
                return false;
            } else if (annotationType.equals(MatrixParam.class)) {
                continue;
            } else if (annotationType.equals(QueryParam.class)) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param jaxRsRouter
     * @return
     * @throws InstantiateParameterException
     * @throws InvocationTargetException
     * @throws RequestHandledException
     * @throws InstantiateRootRessourceException
     * @throws MissingAnnotationException
     */
    public ResourceObject createInstance(CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter)
            throws InstantiateParameterException, MissingAnnotationException,
            InstantiateRootRessourceException, RequestHandledException,
            InvocationTargetException {
        Constructor<?> constructor = this.constructor;
        Object instance = createInstance(constructor, leaveEncoded,
                callContext, jaxRsRouter);
        ResourceObject rootResourceObject = new ResourceObject(instance, this);
        try {
            rootResourceObject.injectDependencies(callContext);
        } catch (InjectException e) {
            throw new InstantiateRootRessourceException(e);
        }
        return rootResourceObject;
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param constructor
     *                the constructor to create an instance with.
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param jaxRsRouter
     * @return
     * @throws MissingAnnotationException
     * @throws RequestHandledException
     * @throws InstantiateParameterException
     * @throws InstantiateRootRessourceException
     *                 if the class could not be instantiated.
     * @throws InvocationTargetException
     */
    public static Object createInstance(Constructor<?> constructor,
            boolean leaveEncoded, CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter) throws MissingAnnotationException,
            RequestHandledException, InstantiateParameterException,
            InstantiateRootRessourceException, InvocationTargetException {
        Object[] args;
        if (constructor.getParameterTypes().length == 0) {
            args = new Object[0];
        } else {
            try {
                args = getParameterValues(constructor.getParameterTypes(),
                        constructor.getGenericParameterTypes(), constructor
                                .getParameterAnnotations(), leaveEncoded,
                        callContext, jaxRsRouter);
            } catch (NoMessageBodyReadersException e) {
                throw new MissingAnnotationException(
                        "the root resource class constructor ("
                                + constructor
                                + ") must have annotations on any parameters. (normally this excpetion could not occur)");
            }
        }
        try {
            return constructor.newInstance(args);
        } catch (IllegalArgumentException e) {
            throw new InstantiateRootRessourceException(
                    "Could not instantiate " + constructor.getDeclaringClass(),
                    e);
        } catch (InstantiationException e) {
            throw new InstantiateRootRessourceException(
                    "Could not instantiate " + constructor.getDeclaringClass(),
                    e);
        } catch (IllegalAccessException e) {
            throw new InstantiateRootRessourceException(
                    "Could not instantiate " + constructor.getDeclaringClass(),
                    e);
        }
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof RootResourceClass))
            return false;
        RootResourceClass otherRootResourceClass = (RootResourceClass) anotherObject;
        return this.jaxRsClass.equals(otherRootResourceClass.jaxRsClass);
    }

    /**
     * @param jaxRsClass
     * @return Returns the constructor to use for the given root resource class
     *         (See JSR-311-Spec, section 2.3)
     * @throws IllegalTypeException
     */
    public static Constructor<?> findJaxRsConstructor(Class<?> jaxRsClass) {
        Constructor<?> constructor = null;
        int constructorParamNo = Integer.MIN_VALUE;
        for (Constructor<?> constr : jaxRsClass.getConstructors()) {
            int constrParamNo = constr.getParameterTypes().length;
            if (constrParamNo <= constructorParamNo)
                continue; // ignore this constructor
            if (!checkParamAnnotations(constr))
                continue; // ignore this constructor
            constructor = constr;
            constructorParamNo = constrParamNo;
        }
        return constructor;
    }
}