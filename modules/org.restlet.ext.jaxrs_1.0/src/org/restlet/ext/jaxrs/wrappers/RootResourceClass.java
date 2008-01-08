/*
 * Copyright 2005-2007 Noelios Consulting.
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

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.MatchingResult;
import org.restlet.ext.jaxrs.exceptions.IllegalTypeException;

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
     */
    public RootResourceClass(Class<?> jaxRsClass) {
        super(jaxRsClass);
        constructor = findJaxRsConstructor();
    }

    /**
     * 
     * @param matchingResult
     * @param allTemplParamsEnc
     * @param restletRequ
     * @return
     * @throws Exception
     */
    public ResourceObject createInstance(MatchingResult matchingResult,
            MultivaluedMap<String, String> allTemplParamsEnc, Request restletRequ) throws Exception {
        Object[] args;
        if (constructor.getParameterTypes().length == 0)
            args = new Object[0];
        else
            args = getParameterValues(constructor.getParameterAnnotations(), constructor
                    .getParameterTypes(), matchingResult, restletRequ, allTemplParamsEnc);
        return new ResourceObject(constructor.newInstance(args), this);
    }

    /**
     * @return Returns the constructor to use for the given root resource class
     *         (See JSR-311-Spec, section 2.3)
     */
    private Constructor<?> findJaxRsConstructor() {
        Constructor<?> constructor = null;
        int constructorParamNo = Integer.MIN_VALUE;
        for (Constructor<?> constr : getJaxRsClass().getConstructors()) {
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

    /**
     * Checks if the parameters for the constructor are valid for a JAX-RS root
     * resource class.
     * 
     * @param paramAnnotationss
     * @param parameterTypes
     * @returns true, if the
     * @throws IllegalTypeException
     *                 If a parameter is annotated with {@link HttpContext},
     *                 but the type is invalid (must be UriInfo, Request or
     *                 HttpHeaders).
     */
    private boolean checkParamAnnotations(Constructor<?> constr) {
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

    private boolean checkParameterAnnotation(Annotation[] parameterAnnotations,
            Class<?> parameterType) {
        // This method has the same structure as in the method
        // AbstractJaxRsWrapper#getParameterValue(...)
        if (parameterAnnotations.length == 0)
            return false;
        for (Annotation annotation : parameterAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                continue;
            } else if (annotationType.equals(PathParam.class)) {
                continue;
            } else if (annotationType.equals(HttpContext.class)) {
                if (parameterType.equals(UriInfo.class))
                    continue;
                if (parameterType.equals(Request.class))
                    continue;
                if (parameterType.equals(HttpHeaders.class))
                    continue;
                throw new IllegalTypeException(
                        "The Type of a parameter annotated with @HttpContext must be UriInfo, Request or HttpHeaders.");
            } else if (annotationType.equals(MatrixParam.class)) {
                continue;
            } else if (annotationType.equals(QueryParam.class)) {
                continue;
            }
            return false;
        }
        return true;
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