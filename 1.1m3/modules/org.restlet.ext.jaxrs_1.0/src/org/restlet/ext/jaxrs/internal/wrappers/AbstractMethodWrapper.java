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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.core.CallContext;
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
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * An abstract wrapper class. Contains some a static methods to use from
 * everywhere, otherwise not intended for public use
 * 
 * @author Stephan Koops
 */
public abstract class AbstractMethodWrapper extends AbstractJaxRsWrapper {

    /**
     * the Java method that should be called. This method could be different
     * from the method containing the annotations, see section 2.5 "Annotation
     * Inheritance" of JSR-311-spec.
     * 
     * @see #annotatedMethod
     */
    Method executeMethod;

    /**
     * the Java method that should be referenced for annotations. This method
     * could be different from the method is called fro executing, see section
     * 2.5 "Annotation Inheritance" of JSR-311-spec.
     * 
     * @see #executeMethod
     */
    Method annotatedMethod;

    /**
     * is true, if the wrapped java method or its class is annotated with
     * &#64;Path.
     */
    boolean leaveEncoded;

    ResourceClass resourceClass;

    AbstractMethodWrapper(Method executeMethod, Method annotatedMethod,
            ResourceClass resourceClass) throws IllegalPathOnMethodException,
            IllegalArgumentException {
        super(PathRegExp.createForMethod(annotatedMethod));
        this.executeMethod = executeMethod;
        this.executeMethod.setAccessible(true);
        this.annotatedMethod = annotatedMethod;
        this.annotatedMethod.setAccessible(true);
        this.resourceClass = resourceClass;
        if (resourceClass.leaveEncoded
                || annotatedMethod.isAnnotationPresent(Encoded.class))
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
        return annotatedMethod.getAnnotations();
    }

    /**
     * Returns the generic return type of the wrapped method.
     * 
     * @return the generic return type of the wrapped method.
     * @see Method#getGenericReturnType()
     */
    public Type getGenericReturnType() {
        return executeMethod.getGenericReturnType();
    }

    /**
     * Returns the return type of the wrapped method.
     * 
     * @return the return type of the wrapped method.
     * @see Method#getReturnType()
     */
    public Class<? extends Object> getReturnType() {
        return executeMethod.getReturnType();
    }

    /**
     * @return Returns the name of the method
     */
    public String getName() {
        Class<?>[] paramTypes = this.executeMethod.getParameterTypes();
        StringBuilder stb = new StringBuilder();
        stb.append(this.executeMethod.getName());
        stb.append('(');
        Util.append(stb, paramTypes);
        stb.append(')');
        return stb.toString();
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    @Override
    public PathRegExp getPathRegExp() {
        return super.getPathRegExp();
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
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param mbrs
     *                The Set of all available {@link MessageBodyReader}s in
     *                the {@link JaxRsRouter}.
     * @param logger
     * @return the unwrapped returned onject by the wrapped method.
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
     */
    public Object invoke(ResourceObject resourceObject,
            CallContext callContext, MessageBodyReaderSet mbrs, Logger logger)
            throws MethodInvokeException, InvocationTargetException,
            MissingAnnotationException, NoMessageBodyReaderException,
            ConvertRepresentationException, ConvertHeaderParamException,
            ConvertPathParamException, ConvertMatrixParamException,
            ConvertQueryParamException, ConvertCookieParamException {
        Annotation[][] parameterAnnotationss = annotatedMethod
                .getParameterAnnotations();
        Class<?>[] paramTypes = executeMethod.getParameterTypes();
        Type[] paramGenericTypes = executeMethod.getGenericParameterTypes();
        Object[] args = WrapperUtil.getParameterValues(paramTypes, paramGenericTypes,
                parameterAnnotationss, leaveEncoded, callContext, mbrs, logger);
        try {
            Object jaxRsResourceObj = resourceObject.getJaxRsResourceObject();
            return executeMethod.invoke(jaxRsResourceObj, args);
        } catch (IllegalArgumentException e) {
            throw new MethodInvokeException(
                    "Could not invoke " + executeMethod, e);
        } catch (IllegalAccessException e) {
            throw new MethodInvokeException(
                    "Could not invoke " + executeMethod, e);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "["
                + this.executeMethod.getDeclaringClass().getSimpleName() + "."
                + this.executeMethod.getName() + "(__)]";
    }
}