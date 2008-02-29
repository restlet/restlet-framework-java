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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.core.CallContext;
import org.restlet.ext.jaxrs.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.exceptions.NoMessageBodyReadersException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.util.PathRegExp;
import org.restlet.ext.jaxrs.util.Util;

/**
 * An abstract wrapper class. Contains some a static methods to use from
 * everywhere, otherwise not intended for public use
 * 
 * @author Stephan Koops
 */
public abstract class AbstractMethodWrapper extends AbstractJaxRsWrapper {

    /**
     * @param method
     *                the java method to get the &#64;Path from
     * @param pathRequired
     * @return the &#64;Path annotation.
     * @throws IllegalArgumentException
     *                 if null was given.
     * @throws MissingAnnotationException
     *                 if the annotation is not present.
     */
    public static Path getPathAnnotation(Method method)
            throws IllegalArgumentException, MissingAnnotationException {
        if (method == null)
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        Path path = method.getAnnotation(Path.class);
        if (path == null)
            throw new MissingAnnotationException("The method "
                    + method.getName() + " does not have an annotation @Path");
        return path;
    }

    /**
     * @param method
     *                the java method to get the &#64;Path from
     * @return the &#64;Path annotation or null, if not present.
     * @throws IllegalArgumentException
     *                 if null was given
     */
    public static Path getPathAnnotationOrNull(Method method)
            throws IllegalArgumentException {
        if (method == null)
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        return method.getAnnotation(Path.class);
    }

    /**
     * Returns the path template of the given sub resource locator or sub
     * resource method.
     * 
     * @param method
     *                the java method
     * @return the path template
     * @throws IllegalPathOnMethodException
     * @throws IllegalArgumentException
     * @throws MissingAnnotationException
     */
    public static String getPathTemplate(Method method)
            throws IllegalArgumentException, IllegalPathOnMethodException,
            MissingAnnotationException {
        Path path = getPathAnnotation(method);
        try {
            return AbstractJaxRsWrapper.getPathTemplate(path);
        } catch (IllegalPathException e) {
            throw new IllegalPathOnMethodException(e);
        }
    }

    /**
     * the Java method that should be called. This method could be different
     * from the methods containing the annotations, see section 2.5 "Annotation
     * Inheritance" of JSR-311-spec.
     */
    Method javaMethod;

    /**
     * is true, if the wrapped java method or its class is annotated with
     * &#64;Path.
     */
    boolean leaveEncoded;

    ResourceClass resourceClass;

    AbstractMethodWrapper(Method javaMethod, ResourceClass resourceClass)
            throws IllegalPathOnMethodException, IllegalArgumentException {
        super(PathRegExp.createForMethod(javaMethod));
        this.javaMethod = javaMethod;
        this.resourceClass = resourceClass;
        if (resourceClass.leaveEncoded
                || javaMethod.isAnnotationPresent(Encoded.class))
            this.leaveEncoded = true;
        else
            this.leaveEncoded = false;
    }

    /**
     * Returns the array of
     * 
     * @return
     */
    public Annotation[] getAnnotations() {
        return javaMethod.getAnnotations();
    }

    /**
     * Returns the generic return type of the wrapped method.
     * 
     * @return the generic return type of the wrapped method.
     */
    public Type getGenericReturnType() {
        return javaMethod.getGenericReturnType();
    }

    /**
     * @return Returns the name of the method
     */
    public String getName() {
        Class<?>[] paramTypes = this.javaMethod.getParameterTypes();
        StringBuilder stb = new StringBuilder();
        stb.append(this.javaMethod.getName());
        stb.append('(');
        Util.append(stb, paramTypes);
        stb.append(')');
        return stb.toString();
    }

    /**
     * @return Retuns the resource class
     */
    public ResourceClass getResourceClass() {
        return this.resourceClass;
    }

    /**
     * Invokes the method and returned the created representation for the
     * response.
     * 
     * @param resourceObject
     * @param jaxRsRouter
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @return
     * @throws MethodInvokeException
     * @throws InvocationTargetException
     * @throws InstantiateParameterException
     * @throws NoMessageBodyReadersException
     * @throws RequestHandledException
     * @throws WebApplicationException
     * @throws MissingAnnotationException
     */
    public Object invoke(ResourceObject resourceObject,
            CallContext callContext, HiddenJaxRsRouter jaxRsRouter)
            throws MethodInvokeException, InvocationTargetException,
            MissingAnnotationException, WebApplicationException,
            RequestHandledException, NoMessageBodyReadersException,
            InstantiateParameterException {
        Annotation[][] parameterAnnotationss = javaMethod
                .getParameterAnnotations();
        Class<?>[] paramTypes = javaMethod.getParameterTypes();
        Type[] paramGenericTypes = javaMethod.getGenericParameterTypes();
        Object[] args = getParameterValues(paramTypes, paramGenericTypes,
                parameterAnnotationss, leaveEncoded, callContext, jaxRsRouter);
        try {
            Object jaxRsResourceObj = resourceObject.getJaxRsResourceObject();
            return this.javaMethod.invoke(jaxRsResourceObj, args);
        } catch (IllegalArgumentException e) {
            throw new MethodInvokeException("Could not invoke " + javaMethod, e);
        } catch (IllegalAccessException e) {
            throw new MethodInvokeException("Could not invoke " + javaMethod, e);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "["
                + this.javaMethod.getDeclaringClass().getSimpleName() + "."
                + this.javaMethod.getName() + "(__)]";
    }
}