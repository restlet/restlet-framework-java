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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.MatchingResult;
import org.restlet.ext.jaxrs.UriTemplateRegExp;
import org.restlet.ext.jaxrs.core.HttpContextImpl;
import org.restlet.ext.jaxrs.exceptions.CanNotIntatiateParameterException;
import org.restlet.ext.jaxrs.exceptions.IllegalAnnotationException;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.resource.Representation;

abstract class AbstractJaxRsWrapper {

    private UriTemplateRegExp uriTemplateRegExp;

    AbstractJaxRsWrapper(Path path) {
        this.uriTemplateRegExp = convertUriTemplateToRegularExpression(path,
                true);
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    public final UriTemplateRegExp getUriTemplateRegExp() {
        return this.uriTemplateRegExp;
    }

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    @SuppressWarnings("unchecked")
    private static Collection<Class<? extends Annotation>> createValidAnnotations() {
        return Arrays.asList(HttpContext.class, HeaderParam.class,
                MatrixParam.class, QueryParam.class, PathParam.class);
    }

    /**
     * Returns the parameter value array for a JAX-RS method or constructor.
     * 
     * @param parameterAnnotationss
     *                the array of arrays of annotations for the method or
     *                constructor.
     * @param parameterTypes
     *                the array of types for the method or constructor.
     * @param matchingResult
     *                the matching result contains the values of the path
     *                parameters.
     * @param restletRequest
     *                the Restlet request
     * @param allTemplParamsEnc Contains all Parameters, that are read from the called URI.
     * @return the parameter array
     */
    protected static Object[] getParameterValues(
            Annotation[][] parameterAnnotationss, Class<?>[] parameterTypes,
            MatchingResult matchingResult, Request restletRequest, MultivaluedMap<String, String> allTemplParamsEnc) {
        int paramNo = parameterTypes.length;
        if (paramNo == 0)
            return new Object[0];
        Object[] args = new Object[paramNo];
        boolean annotRequired = false;
        HttpContextImpl httpContext = null; // cached
        for (int i = 0; i < args.length; i++) {
            try {
                Object arg = getParameterValue(parameterAnnotationss[i],
                        parameterTypes[i], matchingResult, restletRequest, httpContext, allTemplParamsEnc, i);
                if(httpContext == null && arg instanceof HttpContextImpl)
                    httpContext = (HttpContextImpl)arg;
                args[i] = arg;
            } catch (IllegalAnnotationException e) {
                if (annotRequired)
                    throw e;
                annotRequired = true;
                args[i] = getParameterValueFromRepr(parameterTypes[i],
                        restletRequest.getEntity());
            }
        }
        return args;
    }

    /**
     * Returns the parameter value for a parameter of a JAX-RS method or
     * constructor.
     * @param paramAnnotations
     *                annotations on the parameters
     * @param paramClass the wished type
     * @param matchingResult the MatchingResult
     * @param restletRequest the Restlet request
     * @param httpContext an already created HttpContextImpl and returned or null
     * @param allTemplParamsEnc Contains all Parameters, that are read from the called URI.
     * @param indexForExcMessages the index of the parameter, for exception messages.
     * @return the parameter value
     * @throws IllegalAnnotationException
     *                 Thrown, when no valid annotation was found. For
     *                 (Sub)ResourceMethods this is one times allowed; than the
     *                 given request entity should taken as parameter.
     */
    private static Object getParameterValue(Annotation[] paramAnnotations,
            Class<?> paramClass, MatchingResult matchingResult,
            Request restletRequest, HttpContextImpl httpContext, MultivaluedMap<String, String> allTemplParamsEnc, int indexForExcMessages)
            throws IllegalAnnotationException {
        for (Annotation annotation : paramAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            // if something is added here, you have to check if you have to add
            // it in RootResourceClass#checkParamAnnotations(..)
            if (annotationType.equals(HeaderParam.class)) {
                String headerParamValue = Util.getHttpHeaders(restletRequest)
                        .getFirstValue(((HeaderParam) annotation).value());
                return getParameterValueFromParam(paramClass, headerParamValue);
            } else if (annotationType.equals(PathParam.class)) {
                String pathParamValue = matchingResult.getVariables().get(
                        ((PathParam) annotation).value());
                return getParameterValueFromParam(paramClass, pathParamValue);
            } else if (annotationType.equals(HttpContext.class)) {
                if(httpContext == null)
                    httpContext = new HttpContextImpl(restletRequest, allTemplParamsEnc);
                return httpContext;
            } else if (annotationType.equals(MatrixParam.class)) {
                throw new NotYetImplementedException();
            } else if (annotationType.equals(QueryParam.class)) {
                String queryParamValue = restletRequest.getResourceRef()
                        .getQueryAsForm().getFirstValue(
                                ((QueryParam) annotation).value());
                return getParameterValueFromParam(paramClass, queryParamValue);
            }
        }
        throw new IllegalAnnotationException("The " + indexForExcMessages
                + ". parameter requires one of the following annotations: "
                + VALID_ANNOTATIONS);
    }

    /**
     * Converts the given paramValue (found in the path, query, matrix or
     * header) into the given paramClass.
     * 
     * @param paramClass
     * @param paramValue
     * @return
     * @see PathParam
     * @see MatrixParam
     * @see QueryParam
     * @see HeaderParam
     */
    public static Object getParameterValueFromParam(Class<?> paramClass,
            String paramValue) {
        if (paramClass.equals(String.class))
            return paramValue;
        try {
            Constructor<?> constructor = paramClass
                    .getConstructor(String.class);
            return constructor.newInstance(paramValue);
        } catch (Exception e) {
            // try valueOf(String)
        }
        Method valueOf;
        try {
            valueOf = paramClass.getMethod("valueOf", String.class);
        } catch (SecurityException e) {
            throw new CanNotIntatiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new CanNotIntatiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        }
        try {
            return valueOf.invoke(null, paramValue);
        } catch (IllegalArgumentException e) {
            throw new CanNotIntatiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new CanNotIntatiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new CanNotIntatiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        }
    }

    /**
     * Returns the parameter value in the wished class from the Restlet
     * representation.
     * 
     * @param paramClass
     * @param representation
     * @return
     */
    public static Object getParameterValueFromRepr(Class<?> paramClass,
            Representation representation) {
        try {
            return getParameterValueFromParam(paramClass, representation
                    .getText());
        } catch (IOException e) {
            throw new CanNotIntatiateParameterException(
                    "Can not read the given representation", e);
        }
    }

    /**
     * Implementation of function R(A) in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5.1 Converting URI Templates to Regular Expressions
     * 
     * @param ensureStartSlash
     * @param path
     * 
     * @return
     */
    private static UriTemplateRegExp convertUriTemplateToRegularExpression(
            Path template, boolean ensureStartSlash) {
        if (template == null)
            return new UriTemplateRegExp("", true);
        String pathTemplate = template.value();
        if (ensureStartSlash)
            pathTemplate = Util.ensureStartSlash(pathTemplate);
        // LATER Path.encode auch bearbeiten
        return new UriTemplateRegExp(pathTemplate, template.limited());
    }
}