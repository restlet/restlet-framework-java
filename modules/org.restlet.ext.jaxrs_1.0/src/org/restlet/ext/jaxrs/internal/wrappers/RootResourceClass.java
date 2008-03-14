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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ws.rs.Encoded;
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
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;

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

    /**
     * Checks, if the class is public and so on.
     * 
     * @param jaxRsClass
     *                JAX-RS root resource class or JAX-RS provider.
     * @param typeName
     *                "root resource class" or "provider"
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     */
    private static void checkClassForPathAnnot(Class<?> jaxRsClass,
            String typeName) throws MissingAnnotationException {
        if (!jaxRsClass.isAnnotationPresent(Path.class)) {
            String msg = "The "
                    + typeName
                    + " "
                    + jaxRsClass.getName()
                    + " is not annotated with @Path. The class will be ignored.";
            throw new MissingAnnotationException(msg);
        }
    }

    /**
     * Checks, if the class is concrete.
     * 
     * @param jaxRsClass
     *                JAX-RS root resource class or JAX-RS provider.
     * @param typeName
     *                "root resource class" or "provider"
     * @throws IllegalArgumentException
     *                 if the class is not concrete.
     */
    public static void checkClassConcrete(Class<?> jaxRsClass, String typeName)
            throws IllegalArgumentException {
        int modifiers = jaxRsClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("The " + typeName + " "
                    + jaxRsClass.getName() + " is not concrete");
        }
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

    /**
     * Checks, if the annotations are valid for a runtime environment handled
     * constructor.
     * 
     * @param parameterAnnotations
     * @param parameterType
     * @return
     */
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
     * @throws NoMessageBodyReaderException
     * @throws ConvertParameterException
     * @throws InstantiateRootRessourceException
     *                 if the class could not be instantiated.
     * @throws InvocationTargetException
     */
    public static Object createInstance(Constructor<?> constructor,
            boolean leaveEncoded, CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter) throws MissingAnnotationException,
            NoMessageBodyReaderException, ConvertParameterException,
            InstantiateRootRessourceException, InvocationTargetException {
        Object[] args;
        if (constructor.getParameterTypes().length == 0) {
            args = new Object[0];
        } else {
            args = getParameterValues(constructor.getParameterTypes(),
                    constructor.getGenericParameterTypes(), constructor
                            .getParameterAnnotations(), leaveEncoded,
                    callContext, jaxRsRouter);
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

    /**
     * @param jaxRsClass
     * @return Returns the constructor to use for the given root resource class
     *         (See JSR-311-Spec, section 2.3). If no constructor could be
     *         found, null is returned. Than try {@link Class#newInstance()}
     * @throws IllegalTypeException
     */
    public static Constructor<?> findJaxRsConstructor(Class<?> jaxRsClass) {
        // REQUESTED use only public constructors of provider and rrcs?
        Constructor<?> constructor = null;
        int constructorParamNo = Integer.MIN_VALUE;
        for (Constructor<?> constr : jaxRsClass.getConstructors()) {
            if (!Modifier.isPublic(constr.getModifiers()))
                continue;
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

    private Constructor<?> constructor;

    /**
     * is true, if the constructor (or the root resource class) is annotated
     * with &#64;Path. Is available after constructor was running.
     */
    private boolean constructorLeaveEncoded;

    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *                the root resource class to wrap
     * @see WrapperFactory#getRootResourceClass(Class)
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     */
    RootResourceClass(Class<?> jaxRsClass, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException {
        super(jaxRsClass, logger, logger);
        checkClassConcrete(getJaxRsClass(), "root resource class");
        checkClassForPathAnnot(jaxRsClass, "root resource class");
        this.constructor = findJaxRsConstructor(getJaxRsClass());
        this.constructorLeaveEncoded = leaveEncoded
                || constructor.isAnnotationPresent(Encoded.class);
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
     * @throws ConvertParameterException
     * @throws InvocationTargetException
     * @throws RequestHandledException
     * @throws InstantiateRootRessourceException
     * @throws MissingAnnotationException
     * @throws NoMessageBodyReaderException
     * @throws MethodInvokeException
     *                 if the method annotated with &#64;{@link PostConstruct}
     *                 could not be called or throws an exception.
     */
    public ResourceObject createInstance(CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter)
            throws ConvertParameterException, MissingAnnotationException,
            InstantiateRootRessourceException, NoMessageBodyReaderException,
            InvocationTargetException, MethodInvokeException {
        Constructor<?> constructor = this.constructor;
        Object instance = createInstance(constructor, constructorLeaveEncoded,
                callContext, jaxRsRouter);
        ResourceObject rootResourceObject = new ResourceObject(instance, this);
        try {
            rootResourceObject.init(callContext);
        } catch (InjectException e) {
            throw new InstantiateRootRessourceException(e);
        }
        return rootResourceObject;
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
}