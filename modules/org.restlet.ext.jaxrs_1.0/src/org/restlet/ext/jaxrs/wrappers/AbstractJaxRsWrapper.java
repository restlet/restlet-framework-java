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

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.core.CallContext;
import org.restlet.ext.jaxrs.exceptions.IllegalOrNoAnnotationException;
import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.NoMessageBodyReadersException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.impl.PathRegExp;
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
     *                the type of the parameter to convert to
     * @param paramValue
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded. Must be FALSE for
     *                {@link HeaderParam}s.
     * @param jaxRsRouter
     * @return
     * @throws InstantiateParameterException
     * @throws WebApplicationException
     * @see PathParam
     * @see MatrixParam
     * @see QueryParam
     * @see HeaderParam
     */
    private static Object convertParamValueFromParam(Class<?> paramClass,
            String paramValue, boolean leaveEncoded,
            HiddenJaxRsRouter jaxRsRouter)
            throws InstantiateParameterException, WebApplicationException {
        if(!leaveEncoded && paramValue != null)
            paramValue = Reference.decode(paramValue);
        if (paramClass.equals(String.class)) // optimization
            return paramValue;
        if (paramClass.isPrimitive())
            return getParamValueForPrimitive(paramClass, paramValue,
                    jaxRsRouter);
        try {
            Constructor<?> constr = paramClass.getConstructor(String.class);
            return constr.newInstance(paramValue);
        } catch (Exception e) {
            // try valueOf(String) as next step
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
     * @param jaxRsRouter
     * @throws WebApplicationException
     * @throws InstantiateParameterException
     */
    private static Object getParamValueForPrimitive(Class<?> paramClass,
            String paramValue, HiddenJaxRsRouter jaxRsRouter)
            throws WebApplicationException, InstantiateParameterException {
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
        if (paramClass == Void.TYPE) {
            String message = "a method return parameter type was void, but this could not be here";
            jaxRsRouter.getLogger().warning(message);
            throw new WebApplicationException(500);
        }
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
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param jaxRsRouter
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded.
     * @param indexForExcMessages
     *                the index of the parameter, for exception messages.
     * @return the parameter value
     * @throws IllegalOrNoAnnotationException
     *                 Thrown, when no valid annotation was found. For
     *                 (Sub)ResourceMethods this is one times allowed; than the
     *                 given request entity should taken as parameter.
     * @throws InstantiateParameterException
     * @throws WebApplicationException
     */
    private static Object getParameterValue(Annotation[] paramAnnotations,
            Class<?> paramClass, CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter, boolean leaveEncoded,
            int indexForExcMessages) throws IllegalOrNoAnnotationException,
            InstantiateParameterException, WebApplicationException {
        for (Annotation annotation : paramAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                String headerParamValue = Util.getHttpHeaders(
                        callContext.getRequest()).getFirstValue(
                        ((HeaderParam) annotation).value(), true);
                return convertParamValueFromParam(paramClass, headerParamValue,
                        false, jaxRsRouter);
            }
            if (annotationType.equals(PathParam.class)) {
                String pathParamValue = callContext
                        .getLastTemplParamEnc((PathParam) annotation);
                return convertParamValueFromParam(paramClass, pathParamValue,
                        leaveEncoded, jaxRsRouter);
            }
            if (annotationType.equals(Context.class)) {
                return callContext;
                // TODO Jerome: I need to know if the restlet Request was
                // authenticated.
            }
            if (annotationType.equals(MatrixParam.class)) {
                String pathParamValue = callContext
                        .getLastMatrixParamEnc((MatrixParam) annotation);
                return convertParamValueFromParam(paramClass, pathParamValue,
                        leaveEncoded, jaxRsRouter);
            }
            if (annotationType.equals(QueryParam.class)) {
                Form form = Converter.toFormEncoded(callContext.getRequest()
                        .getResourceRef().getQuery(), jaxRsRouter.getLogger());
                String queryParamValue = form.getFirstValue(
                                ((QueryParam) annotation).value());
                return convertParamValueFromParam(paramClass, queryParamValue,
                        true, jaxRsRouter); // leaveEncoded = true -> not change
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
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
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
            boolean leaveEncoded, CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter)
            throws IllegalOrNoAnnotationException,
            InstantiateParameterException, RequestHandledException,
            NoMessageBodyReadersException, WebApplicationException {
        int paramNo = parameterTypes.length;
        if (paramNo == 0)
            return new Object[0];
        Object[] args = new Object[paramNo];
        boolean annotRequired = false;
        for (int i = 0; i < args.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Object arg;
            try {
                arg = getParameterValue(parameterAnnotationss[i], paramType,
                        callContext, jaxRsRouter, leaveEncoded, i);
            } catch (IllegalOrNoAnnotationException ionae) {
                if (annotRequired)
                    throw ionae;
                annotRequired = true;
                Representation entity = callContext.getRequest().getEntity();
                if (entity == null) {
                    arg = null;
                } else {
                    MediaType mediaType = entity.getMediaType();
                    MessageBodyReaderSet mbrs = jaxRsRouter
                            .getMessageBodyReaders();
                    if (mbrs == null)
                        throw new NoMessageBodyReadersException();
                    MessageBodyReader mbr = mbrs.getBest(mediaType, paramType);
                    if (mbr == null) {
                        // TODO JSR311: what, if no MessageBodyReader?
                        callContext.getResponse().setStatus(
                                Status.CLIENT_ERROR_NOT_ACCEPTABLE);
                        throw new RequestHandledException();
                    }
                    MultivaluedMap<String, String> httpHeaders = Util
                            .getJaxRsHttpHeaders(callContext.getRequest());
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
