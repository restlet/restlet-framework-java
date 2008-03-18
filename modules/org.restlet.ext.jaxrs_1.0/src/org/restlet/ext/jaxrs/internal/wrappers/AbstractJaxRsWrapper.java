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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.EncodeOrCheck;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * An abstract wrapper class. Contains some useful static methods.
 * 
 * @author Stephan Koops
 */
public abstract class AbstractJaxRsWrapper {

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    /**
     * Converts the given paramValue (found in the path, query, matrix or
     * header) into the given paramClass.
     * 
     * @param paramClass
     *                the type of the parameter to convert to
     * @param paramValue
     * @param defaultValue
     *                see {@link DefaultValue}
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded. Must be FALSE for
     *                {@link HeaderParam}s.
     * @param jaxRsRouter
     * @return
     * @throws ConvertParameterException
     * @see PathParam
     * @see MatrixParam
     * @see QueryParam
     * @see HeaderParam
     */
    private static Object convertParamValueFromParam(Class<?> paramClass,
            String paramValue, DefaultValue defaultValue, boolean leaveEncoded)
            throws ConvertParameterException {
        if (!leaveEncoded && paramValue != null)
            paramValue = Reference.decode(paramValue);
        else if (paramValue == null && defaultValue != null)
            paramValue = defaultValue.value();
        if (paramClass.equals(String.class)) // optimization
            return paramValue;
        if (paramClass.isPrimitive())
            return getParamValueForPrimitive(paramClass, paramValue);
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
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (NoSuchMethodException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        }
        try {
            return valueOf.invoke(null, paramValue);
        } catch (IllegalArgumentException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (IllegalAccessException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (InvocationTargetException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        }
    }

    /**
     * @param paramClass
     * @param paramGenericType
     * @param paramValues
     *                the values to use if multiples are required
     * @param paramValue
     *                th value, if only one is needed.
     * @param defaultValue
     * @param leaveEncoded
     * @return
     * @throws ConvertParameterException
     */
    private static Object convertParamValuesFromParam(Class<?> paramClass,
            Type paramGenericType, Iterator<String> paramValues,
            String paramValue, DefaultValue defaultValue, boolean leaveEncoded)
            throws ConvertParameterException {
        boolean toArray = false;
        Collection<Object> coll = null;
        if (paramGenericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) paramGenericType;
            paramClass = (Class<?>) genericArrayType.getGenericComponentType();
            coll = new ArrayList<Object>(1);
        } else if (paramGenericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) paramGenericType;
            Type rawType = parameterizedType.getRawType();
            if (rawType.equals(Collection.class) || rawType.equals(List.class))
                coll = new ArrayList<Object>(1);
            else if (rawType.equals(Set.class))
                coll = new HashSet<Object>(2);
            else if (rawType.equals(SortedSet.class))
                coll = new TreeSet<Object>();
            paramClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        if (coll != null) {
            while (paramValues.hasNext()) {
                String queryParamValue = paramValues.next();
                Object paramValue_ = convertParamValueFromParam(paramClass,
                        queryParamValue, defaultValue, leaveEncoded);
                if (paramValue_ != null)
                    coll.add(paramValue_);
                defaultValue = null;
            }
            if (toArray)
                return coll.toArray();
            return coll;
        }
        // no collection type
        return convertParamValueFromParam(paramClass, paramValue, defaultValue,
                leaveEncoded);
    }

    /**
     * Converts the Restlet request {@link Representation} to the type requested
     * by the resource method.
     * 
     * @param callContext
     *                the call context, containing the entity.
     * @param paramType
     *                the type to convert to.
     * @param genericType
     *                The generic {@link Type} to convert to.
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param jaxRsRouter
     * @return
     * @throws NoMessageBodyReaderException
     * @throws ConvertParameterException
     */
    @SuppressWarnings("unchecked")
    private static Object convertRepresentation(CallContext callContext,
            Class<?> paramType, Type genericType, Annotation[] annotations,
            HiddenJaxRsRouter jaxRsRouter) throws NoMessageBodyReaderException,
            ConvertParameterException {
        Representation entity = callContext.getRequest().getEntity();
        if (entity == null)
            return null;
        if (Representation.class.isAssignableFrom(paramType)) {
            Object repr = createConcreteRepresentationInstance(paramType,
                    entity, jaxRsRouter.getLogger());
            if (repr != null)
                return repr;
        }
        MediaType mediaType = entity.getMediaType();
        MessageBodyReaderSet mbrs = jaxRsRouter.getMessageBodyReaders();
        MessageBodyReader<?> mbr = mbrs.getBest(mediaType, paramType,
                genericType, annotations);
        if (mbr == null)
            throw new NoMessageBodyReaderException(mediaType, paramType);
        MultivaluedMap<String, String> httpHeaders = Util
                .getJaxRsHttpHeaders(callContext.getRequest());
        try {
            javax.ws.rs.core.MediaType jaxRsMediaType = Converter
                    .toJaxRsMediaType(mediaType, entity.getCharacterSet());
            return mbr.readFrom((Class) paramType, genericType, jaxRsMediaType,
                    annotations, httpHeaders, entity.getStream());
        } catch (IOException e) {
            throw ConvertParameterException.object(paramType,
                    "the message body", e);
        }
    }

    /**
     * @param entity
     * @return the created representation, or null, if it coud not be converted.
     * @throws ConvertParameterException
     */
    private static Object createConcreteRepresentationInstance(
            Class<?> paramType, Representation entity, Logger logger)
            throws ConvertParameterException {
        if (paramType.equals(Representation.class))
            return entity;
        Constructor<?> constr;
        try {
            constr = paramType.getConstructor(Representation.class);
        } catch (SecurityException e) {
            logger.warning("The constructor " + paramType
                    + "(Representation) is not accessable.");
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
        try {
            return constr.newInstance(entity);
        } catch (Exception e) {
            throw ConvertParameterException.object(paramType,
                    "the message body", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<Class<? extends Annotation>> createValidAnnotations() {
        return Arrays.asList(Context.class, HeaderParam.class,
                MatrixParam.class, QueryParam.class, PathParam.class,
                CookieParam.class);
    }

    /**
     * @param paramClass
     *                the class to convert to
     * @param paramGenericType
     *                the generic type to convert to
     * @param cookieParam
     *                the {@link CookieParam} annotation
     * @param defaultValue
     *                the default value
     * @param callContext
     *                the {@link CallContext}
     * @param jaxRsRouter
     * @return the cookie parameter, converted to type paramClass
     * @throws ConvertParameterException
     */
    @SuppressWarnings("unchecked")
    static Object getCookieParamValue(Class<?> paramClass,
            Type paramGenericType, CookieParam cookieParam,
            DefaultValue defaultValue, CallContext callContext)
            throws ConvertParameterException {
        Series<Cookie> cookies = callContext.getRequest().getCookies();
        String cookieName = cookieParam.value();
        return convertParamValuesFromParam(paramClass, paramGenericType,
                new ParamValueIter((Series) cookies.subList(cookieName)),
                getValue(cookies.getFirst(cookieName)), defaultValue, true);
        // leaveEncoded = true -> not change
    }

    private static class ParamValueIter implements Iterator<String> {
        private Iterator<Parameter> paramIter;

        private ParamValueIter(Iterator<Parameter> paramIter) {
            this.paramIter = paramIter;
        }

        private ParamValueIter(Iterable<Parameter> parameters) {
            this.paramIter = parameters.iterator();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return paramIter.hasNext();
        }

        /**
         * @see java.util.Iterator#next()
         */
        public String next() {
            return getValue(paramIter.next());
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            paramIter.remove();
        }
    }

    /**
     * @param paramClass
     * @param paramGenericType
     *                the generic type to convert to
     * @param annotation
     * @param defaultValue
     * @param callContext
     * @param jaxRsRouter
     * @return
     * @throws ConvertParameterException
     */
    static Object getHeaderParamValue(Class<?> paramClass,
            Type paramGenericType, HeaderParam annotation,
            DefaultValue defaultValue, CallContext callContext)
            throws ConvertParameterException {
        Form httpHeaders = Util.getHttpHeaders(callContext.getRequest());
        String headerName = annotation.value();
        return convertParamValuesFromParam(paramClass, paramGenericType,
                new ParamValueIter(httpHeaders.subList(headerName, true)),
                getValue(httpHeaders.getFirst(headerName, true)), defaultValue,
                false);
    }

    /**
     * @param paramClass
     * @param paramGenericType
     *                the generic type to convert to
     * @param matrixParam
     * @param leaveEncoded
     * @param defaultValue
     * @param callContext
     * @param jaxRsRouter
     * @return
     * @throws ConvertParameterException
     */
    static Object getMatrixParamValue(Class<?> paramClass,
            Type paramGenericType, MatrixParam matrixParam,
            boolean leaveEncoded, DefaultValue defaultValue,
            CallContext callContext) throws ConvertParameterException {
        String matrixParamValue = callContext
                .getLastMatrixParamEnc(matrixParam);
        Iterator<String> matrixParamValues = callContext
                .matrixParamEncIter(matrixParam);
        return convertParamValuesFromParam(paramClass, paramGenericType,
                matrixParamValues, matrixParamValue, defaultValue, leaveEncoded);
    }

    /**
     * Returns the parameter value for a parameter of a JAX-RS method or
     * constructor.
     * 
     * @param paramAnnotations
     *                annotations on the paramIter
     * @param paramClass
     *                the wished type
     * @param genericParamType
     *                the generic type to convert to
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded.
     * @param indexForExcMessages
     *                the index of the parameter, for exception messages.
     * @param jaxRsRouter
     * @return the parameter value
     * @throws MissingAnnotationException
     *                 Thrown, when no valid annotation was found. For
     *                 (Sub)ResourceMethods this is one times allowed; than the
     *                 given request entity should taken as parameter.
     * @throws ConvertParameterException
     */
    private static Object getParameterValue(Annotation[] paramAnnotations,
            Class<?> paramClass, Type paramGenericType,
            CallContext callContext, Logger logger, boolean leaveEncoded,
            int indexForExcMessages) throws MissingAnnotationException,
            ConvertParameterException {
        DefaultValue defaultValue = null;
        for (Annotation annot : paramAnnotations) {
            Class<? extends Annotation> annotationType = annot.annotationType();
            if (annotationType.equals(DefaultValue.class))
                defaultValue = (DefaultValue) annot;
            else if (!leaveEncoded && annotationType.equals(Encoded.class))
                leaveEncoded = true;
        }
        for (Annotation annotation : paramAnnotations) {
            Class<? extends Annotation> annoType = annotation.annotationType();
            if (annoType.equals(Context.class)) {
                if (paramClass.equals(ClientInfo.class))
                    return callContext.getRequest().getClientInfo();
                else if (paramClass.equals(Conditions.class))
                    return callContext.getRequest().getConditions();
                else
                    return callContext;
            }
            if (annoType.equals(HeaderParam.class)) {
                return getHeaderParamValue(paramClass, paramGenericType,
                        (HeaderParam) annotation, defaultValue, callContext);
            }
            if (annoType.equals(PathParam.class)) {
                return getPathParamValue(paramClass, paramGenericType,
                        (PathParam) annotation, leaveEncoded, defaultValue,
                        callContext);
            }
            if (annoType.equals(MatrixParam.class)) {
                return getMatrixParamValue(paramClass, paramGenericType,
                        (MatrixParam) annotation, leaveEncoded, defaultValue,
                        callContext);
            }
            if (annoType.equals(QueryParam.class)) {
                return getQueryParamValue(paramClass, paramGenericType,
                        (QueryParam) annotation, defaultValue, callContext,
                        logger);
            }
            if (annoType.equals(CookieParam.class)) {
                return getCookieParamValue(paramClass, paramGenericType,
                        (CookieParam) annotation, defaultValue, callContext);
            }
        }
        throw new MissingAnnotationException("The " + indexForExcMessages
                + ". parameter requires one of the following annotations: "
                + VALID_ANNOTATIONS);
    }

    /**
     * Returns the parameter value array for a JAX-RS method or constructor.
     * 
     * @param paramTypes
     *                the array of types for the method or constructor.
     * @param paramGenericTypes
     *                The generic {@link Type} to convert to.
     * @param paramAnnotationss
     *                the array of arrays of annotations for the method or
     *                constructor.
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param jaxRsRouter
     *                The {@link JaxRsRouter}. May be null, but should not be.
     * @param mbrs
     *                The Set of {@link MessageBodyReader}s.
     * 
     * @return the parameter array
     * @throws MissingAnnotationException
     * @throws ConvertParameterException
     * @throws NoMessageBodyReaderException
     */
    protected static Object[] getParameterValues(Class<?>[] paramTypes,
            Type[] paramGenericTypes, Annotation[][] paramAnnotationss,
            boolean leaveEncoded, CallContext callContext,
            HiddenJaxRsRouter jaxRsRouter) throws MissingAnnotationException,
            ConvertParameterException, NoMessageBodyReaderException {
        int paramNo = paramTypes.length;
        if (paramNo == 0)
            return new Object[0];
        Object[] args = new Object[paramNo];
        boolean annotRequired = false;
        Logger logger = jaxRsRouter != null ? jaxRsRouter.getLogger() : Logger
                .getAnonymousLogger();
        for (int i = 0; i < args.length; i++) {
            Class<?> paramType = paramTypes[i];
            Type paramGenericType = paramGenericTypes[i];
            Object arg;
            Annotation[] paramAnnotations = paramAnnotationss[i];
            try {
                arg = getParameterValue(paramAnnotations, paramType,
                        paramGenericType, callContext, logger, leaveEncoded, i);
            } catch (MissingAnnotationException ionae) {
                if (annotRequired)
                    throw ionae;
                annotRequired = true;
                arg = convertRepresentation(callContext, paramType,
                        paramGenericType, paramAnnotations, jaxRsRouter);
            }
            args[i] = arg;
        }
        return args;
    }

    /**
     * @param paramClass
     * @param paramValue
     * @throws ConvertParameterException
     */
    private static Object getParamValueForPrimitive(Class<?> paramClass,
            String paramValue) throws ConvertParameterException {
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
                throw ConvertParameterException.primitive(paramClass,
                        paramValue, null);
            }
            if (paramClass == Boolean.TYPE) {
                if (paramValue.equalsIgnoreCase("true"))
                    return Boolean.TRUE;
                if (paramValue.equalsIgnoreCase("false"))
                    return Boolean.FALSE;
                throw ConvertParameterException.primitive(paramClass,
                        paramValue, null);
            }
        } catch (IllegalArgumentException e) {
            throw ConvertParameterException
                    .primitive(paramClass, paramValue, e);
        }
        String warning;
        if (paramClass == Void.TYPE)
            warning = "an object should be converted to a void; but this could not be here";
        else
            warning = "an object should be converted to a " + paramClass
                    + ", but here are only primitives allowed.";
        Logger.getAnonymousLogger().warning(warning);
        ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
        rb.entity(warning);
        throw new WebApplicationException(rb.build());
    }

    /**
     * @param paramClass
     * @param paramGenericType
     *                the generic type to convert to
     * @param pathParam
     * @param leaveEncoded
     * @param defaultValue
     * @param callContext
     * @param logger
     * @return
     * @throws ConvertParameterException
     */
    static Object getPathParamValue(Class<?> paramClass, Type paramGenericType,
            PathParam pathParam, boolean leaveEncoded,
            DefaultValue defaultValue, CallContext callContext)
            throws ConvertParameterException {
        // TODO Path-Param: List<String>
        // TODO @PathParam("x") PathSegment allowed
        // REQUEST What do @PathParam-value means, if it should be PathSegment?
        // allow without? ignore it?
        if(paramGenericType == null)
            "".toString();
        String pathParamValue = callContext.getLastTemplParamEnc(pathParam);
        return convertParamValueFromParam(paramClass, pathParamValue,
                defaultValue, leaveEncoded);
    }

    /**
     * Returns the path from the annotation. It will be encoded if necessary. If
     * it should not be encoded, this method checks, if all characters are
     * valid.
     * 
     * @param path
     *                The {@link Path} annotation. Must not be null.
     * @return the encoded path template
     * @throws IllegalPathException
     * @see Path#encode()
     */
    public static String getPathTemplate(Path path) throws IllegalPathException {
        // LATER EncodeOrCheck.path(CharSequence)
        String pathTemplate = path.value();
        if (pathTemplate.contains(";"))
            throw new IllegalPathException(path,
                    "A path must not contain a semicolon");
        if (path.encode()) {
            pathTemplate = EncodeOrCheck.encodeNotBraces(pathTemplate, false)
                    .toString();
        } else {
            try {
                EncodeOrCheck.checkForInvalidUriChars(pathTemplate, -1,
                        "path template");
            } catch (IllegalArgumentException iae) {
                throw new IllegalPathException(path, iae);
            }
        }
        return pathTemplate;
    }

    /**
     * @param paramClass
     * @param paramGenericType
     *                the generic type to convert to
     * @param queryParam
     * @param defaultValue
     * @param callContext
     * @param logger
     * @return
     * @throws ConvertParameterException
     */
    static Object getQueryParamValue(Class<?> paramClass,
            Type paramGenericType, QueryParam queryParam,
            DefaultValue defaultValue, CallContext callContext, Logger logger)
            throws ConvertParameterException {
        Reference resourceRef = callContext.getRequest().getResourceRef();
        String queryString = resourceRef.getQuery();
        Form form = Converter.toFormEncoded(queryString, logger);
        String paramName = queryParam.value();
        List<Parameter> parameters = form.subList(paramName);
        return convertParamValuesFromParam(paramClass, paramGenericType,
                new ParamValueIter(parameters), getValue(form
                        .getFirst(paramName)), defaultValue, true);
        // TODO Encode or not of @QueryParam: leaveEncoded = true -> not change
    }

    /**
     * Returns the value from the given Parameter. If the given parameter is
     * null, null will returned. If the parameter is not null, but it's value, ""
     * is returned.
     * 
     * @param parameter
     * @return the value from the given Parameter. If the given parameter is
     *         null, null will returned. If the parameter is not null, but it's
     *         value, "" is returned.
     */
    private static String getValue(Parameter parameter) {
        if (parameter == null)
            return null;
        String paramValue = parameter.getValue();
        if (paramValue == null)
            return "";
        return paramValue;
    }

    private PathRegExp pathRegExp;

    /**
     * Creates a new AbstractJaxRsWrapper without a path.
     */
    AbstractJaxRsWrapper() {
        this.pathRegExp = PathRegExp.EMPTY;
    }

    /**
     * Creates a new AbstractJaxRsWrapper with a given {@link PathRegExp}.
     * 
     * @param pathRegExp
     *                must not be null.
     */
    AbstractJaxRsWrapper(PathRegExp pathRegExp) throws ImplementationException {
        if (pathRegExp == null)
            throw new ImplementationException("The PathRegExp must not be null");
        this.pathRegExp = pathRegExp;
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    protected PathRegExp getPathRegExp() {
        return this.pathRegExp;
    }
}