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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;

import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.core.HttpContextImpl;
import org.restlet.ext.jaxrs.core.UnmodifiableMultivaluedMap;
import org.restlet.ext.jaxrs.exceptions.IllegalOrNoAnnotationException;
import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.NoMessageBodyReadersException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.impl.PathRegExp;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.resource.Representation;

/**
 * An abstract wrapper class. contains some useful static methods.
 * 
 * @author Stephan Koops
 */
public abstract class AbstractJaxRsWrapper {

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    /**
     * Implementation of function R(A) in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5.1 Converting URI Templates to Regular Expressions
     * 
     * @param ensureStartSlash
     * @param path
     * 
     * @return
     */
    private static PathRegExp convertPathToRegularExpression(Path template,
            boolean ensureStartSlash) {
        if (template == null)
            return new PathRegExp("", true);
        String pathTemplate = getPathTemplate(template);
        if (ensureStartSlash)
            pathTemplate = Util.ensureStartSlash(pathTemplate);
        // LATER Path.encode auch bearbeiten
        return new PathRegExp(pathTemplate, template.limited());
    }

    @SuppressWarnings("unchecked")
    private static Collection<Class<? extends Annotation>> createValidAnnotations() {
        return Arrays.asList(Context.class, HeaderParam.class,
                MatrixParam.class, QueryParam.class, PathParam.class);
    }

    /**
     * Converts the given paramValue (found in the path, query, matrix or
     * header) into the given paramClass.
     * 
     * @param paramClass
     * @param paramValue
     * @return
     * @throws InstantiateParameterException
     * @throws
     * @throws WebApplicationException
     * @see PathParam
     * @see MatrixParam
     * @see QueryParam
     * @see HeaderParam
     */
    private static Object getParameterValueFromParam(Class<?> paramClass,
            String paramValue) throws InstantiateParameterException,
            WebApplicationException {
        if (paramClass.equals(String.class))
            return paramValue;
        if (paramClass.isPrimitive())
            return getParamValueForPrimitive(paramClass, paramValue);
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
            throw new InstantiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new InstantiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        }
        try {
            return valueOf.invoke(null, paramValue);
        } catch (IllegalArgumentException e) {
            throw new InstantiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new InstantiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new InstantiateParameterException("Could not convert "
                    + paramValue + " to a " + paramClass.getName(), e);
        }
    }

    /**
     * @param paramClass
     * @param paramValue
     * @throws WebApplicationException
     * @throws
     */
    private static Object getParamValueForPrimitive(Class<?> paramClass,
            String paramValue) throws WebApplicationException, InstantiateParameterException {
        try {
            if (paramClass == Integer.TYPE)
                return new Integer(paramValue);
            if (paramClass == Double.TYPE)
                return new Double(paramValue);
            if (paramClass == Float.TYPE)
                return new Float(paramValue);
            if (paramClass == Byte.TYPE)
                return new Byte(paramValue);
            if (paramClass == Long.TYPE)
                return new Long(paramValue);
            if (paramClass == Short.TYPE)
                return new Short(paramValue);
            if (paramClass == Character.TYPE) {
                if (paramValue.length() == 1)
                    return paramValue.charAt(0);
                throw InstantiateParameterException.primitive(paramClass,
                        paramValue, null);
            }
            if (paramClass == Boolean.TYPE) {
                if (paramValue.equalsIgnoreCase("true"))
                    return Boolean.TRUE;
                if (paramValue.equalsIgnoreCase("false"))
                    return Boolean.FALSE;
                throw InstantiateParameterException.primitive(paramClass,
                        paramValue, null);
            }
        } catch (IllegalArgumentException e) {
            throw InstantiateParameterException.primitive(paramClass,
                    paramValue, e);
        }
        if (paramClass == Void.TYPE)
            // TODO log, this can not be
            throw new WebApplicationException(500);
        // TODO log: new primitive type found !
        throw new WebApplicationException(500);
    }

    /**
     * Returns the path from the annotation. It will be encoded if necessary. If
     * it should not be encoded, this method checks, if all characters are
     * valid.
     * 
     * @param path
     *                The {@link Path} annotation. Must not be null.
     * @return the encoded path template
     * @see Path#encode()
     */
    public static String getPathTemplate(Path path) {
        String pathTemplate = path.value();
        if (path.encode())
            return Util.encodeNotBraces(pathTemplate, false);
        Util.checkForInvalidUriChars(pathTemplate, -1, "path template");
        return pathTemplate;
    }

    private PathRegExp pathRegExp;

    AbstractJaxRsWrapper(Path path) {
        this.pathRegExp = convertPathToRegularExpression(path, true);
    }

    /**
     * Returns the parameter value for a parameter of a JAX-RS method or
     * constructor.
     * 
     * @param paramAnnotations
     *                annotations on the parameters
     * @param paramClass
     *                the wished type
     * @param restletRequest
     *                the Restlet request
     * @param restletResponse
     *                the Restlet response
     * @param context
     *                an already created HttpContextImpl and returned or null
     * @param allTemplParamsEnc
     *                Contains all Parameters, that are read from the called
     *                URI.
     * @param indexForExcMessages
     *                the index of the parameter, for exception messages.
     * @param authenticator
     *                Authenticator for the roles, see
     *                {@link SecurityContext#isUserInRole(String)}.
     * @return the parameter value
     * @throws IllegalOrNoAnnotationException
     *                 Thrown, when no valid annotation was found. For
     *                 (Sub)ResourceMethods this is one times allowed; than the
     *                 given request entity should taken as parameter.
     * @throws InstantiateParameterException
     * @throws
     * @throws WebApplicationException
     */
    private static Object getParameterValue(Annotation[] paramAnnotations,
            Class<?> paramClass, Request restletRequest,
            Response restletResponse, HttpContextImpl context,
            MultivaluedMap<String, String> allTemplParamsEnc,
            int indexForExcMessages, Authenticator authenticator)
            throws IllegalOrNoAnnotationException,
            InstantiateParameterException, WebApplicationException {
        for (Annotation annotation : paramAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                String headerParamValue = Util
                        .getHttpHeaders(restletRequest)
                        .getFirstValue(((HeaderParam) annotation).value(), true);
                return getParameterValueFromParam(paramClass, headerParamValue);
            }
            if (annotationType.equals(PathParam.class)) {
                String pathParamValue = allTemplParamsEnc
                        .getFirst(((PathParam) annotation).value());
                // TODO PathParam: @Encode verwenden
                return getParameterValueFromParam(paramClass, pathParamValue);
            }
            if (annotationType.equals(Context.class)) {
                if (context != null)
                    return context;
                return new HttpContextImpl(restletRequest,
                        new UnmodifiableMultivaluedMap<String, String>(
                                allTemplParamsEnc, false), restletResponse,
                        authenticator);
                // TODO Jerome: I need to know if the restlet Request was
                // authenticated.
            }
            if (annotationType.equals(MatrixParam.class)) {
                // TODO MatrixParameter
                throw new NotYetImplementedException();
            }
            if (annotationType.equals(QueryParam.class)) {
                String queryParamValue = restletRequest.getResourceRef()
                        .getQueryAsForm().getFirstValue(
                                ((QueryParam) annotation).value());
                return getParameterValueFromParam(paramClass, queryParamValue);
            }
        }
        throw new IllegalOrNoAnnotationException("The " + indexForExcMessages
                + ". parameter requires one of the following annotations: "
                + VALID_ANNOTATIONS);
    }

    /**
     * Returns the parameter value array for a JAX-RS method or constructor.
     * 
     * @param parameterAnnotationss
     *                the array of arrays of annotations for the method or
     *                constructor.
     * @param parameterTypes
     *                the array of types for the method or constructor.
     * @param restletRequest
     *                the Restlet request
     * @param restletResponse
     *                the Restlet response
     * @param allTemplParamsEnc
     *                Contains all Parameters, that are read from the called
     *                URI.
     * @param authenticator
     *                Authenticator for roles, see
     *                {@link SecurityContext#isUserInRole(String)}
     * @param mbrs
     *                The Set of {@link MessageBodyReader}s.
     * @return the parameter array
     * @throws IllegalOrNoAnnotationException
     * @throws InstantiateParameterException
     * @throws RequestHandledException
     * @throws NoMessageBodyReadersException
     * @throws
     * @throws WebApplicationException
     */
    protected static Object[] getParameterValues(
            Annotation[][] parameterAnnotationss, Class<?>[] parameterTypes,
            Request restletRequest, Response restletResponse,
            MultivaluedMap<String, String> allTemplParamsEnc,
            Authenticator authenticator, MessageBodyReaderSet mbrs)
            throws IllegalOrNoAnnotationException,
            InstantiateParameterException, RequestHandledException,
            NoMessageBodyReadersException, WebApplicationException {
        int paramNo = parameterTypes.length;
        if (paramNo == 0)
            return new Object[0];
        Object[] args = new Object[paramNo];
        boolean annotRequired = false;
        HttpContextImpl httpContext = null; // cached
        for (int i = 0; i < args.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Object arg;
            try {
                arg = getParameterValue(parameterAnnotationss[i], paramType,
                        restletRequest, restletResponse, httpContext,
                        allTemplParamsEnc, i, authenticator);
                if (httpContext == null && arg instanceof HttpContextImpl)
                    httpContext = (HttpContextImpl) arg;
            } catch (IllegalOrNoAnnotationException ionae) {
                if (annotRequired)
                    throw ionae;
                annotRequired = true;
                if (mbrs == null) {
                    throw new NoMessageBodyReadersException();
                }
                Representation entity = restletRequest.getEntity();
                if (entity == null) {
                    arg = null;
                } else {
                    MediaType mediaType = entity.getMediaType();
                    MessageBodyReader mbr = mbrs.getBest(mediaType, paramType);
                    if (mbr == null) {
                        // TODO JSR311: what, if no MessageBodyReader?
                        restletResponse
                                .setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
                        throw new RequestHandledException();
                    }
                    MultivaluedMap<String, String> httpHeaders = Util
                            .getJaxRsHttpHeaders(restletRequest);
                    try {
                        javax.ws.rs.core.MediaType jaxRsMediaType = Converter
                                .toJaxRsMediaType(mediaType, entity
                                        .getCharacterSet());
                        arg = mbr.readFrom(paramType, jaxRsMediaType,
                                httpHeaders, entity.getStream());
                    } catch (IOException e) {
                        throw new InstantiateParameterException(
                                "Can not instatiate parameter of type "
                                        + paramType.getName(), e);
                    }
                }
            }
            args[i] = arg;
        }
        return args;
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    public final PathRegExp getPathRegExp() {
        return this.pathRegExp;
    }
}
