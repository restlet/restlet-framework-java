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
package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * Contains a list of parameters for JAX-RS constructors, (sub) resource methods
 * and sub resource locators.
 * 
 * @author Stephan Koops
 */
public class ParameterList {

    static class CookieParamGetter extends NoEncParamGetter {

        private final CookieParam cookieParam;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * @param annoSaysLeaveEncoded
         *                to check if the annotation is available.
         */
        CookieParamGetter(CookieParam cookieParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean annoSaysLeaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext,
                    annoSaysLeaveEncoded);
            this.cookieParam = cookieParam;
        }

        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.IntoRrcInjector.AbstractParamGetter#getParamValue()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getParamValue() {
            String cookieName = cookieParam.value();
            Series<org.restlet.data.Cookie> cookies;
            cookies = tlContext.get().getRequest().getCookies();
            if (convertTo.equals(Cookie.class)) {
                Collection<Cookie> coll = createColl();
                for (org.restlet.data.Cookie rc : cookies) {
                    if (!rc.getName().equals(cookieName))
                        continue;
                    Cookie cookie = Converter.toJaxRsCookie(rc);
                    if (coll == null) // no collection requested
                        return cookie;
                    coll.add(cookie);
                }
                if (coll == null)
                    return null;
                if (coll.isEmpty()) {
                    String value = defaultValue.value();
                    coll.add(new Cookie(cookieName, value));
                }
                if (isArray)
                    return Util.toArray(coll, Cookie.class);
                return coll;
            }
            try {
                if (this.collType == null) { // no collection parameter
                    String firstCookieValue = WrapperUtil.getValue(cookies
                            .getFirst(cookieName));
                    return convertParamValue(firstCookieValue);
                }
                return convertParamValues(new ParamValueIter((Series) cookies
                        .subList(cookieName)));
            } catch (ConvertParameterException e) {
                throw new ConvertCookieParamException(e);
            }
        }
    }

    /**
     * Abstract super class for access to the entity or to &#64;*Param where
     * encoded is allowed (&#64;{@link PathParam}, &#64;{@link MatrixParam}
     * and &#64;{@link QueryParam}).
     */
    abstract static class EncParamGetter extends AbstractParamGetter {

        private final boolean encode;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        EncParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext, boolean encode) {
            super(defaultValue, convToCl, convToGen, tlContext);
            this.encode = encode;
        }

        @Override
        protected boolean encode() {
            return this.encode;
        }
    }

    static class HeaderParamGetter extends NoEncParamGetter {

        private final HeaderParam headerParam;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * 
         * @param annoSaysLeaveEncoded
         *                to check if the annotation is available.
         */
        HeaderParamGetter(HeaderParam headerParam, DefaultValue defaultValue,
                Class<?> convToCl, Type paramGenericType,
                ThreadLocalizedContext tlContext, boolean annoSaysLeaveEncoded) {
            super(defaultValue, convToCl, paramGenericType, tlContext,
                    annoSaysLeaveEncoded);
            this.headerParam = headerParam;
        }

        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.IntoRrcInjector.AbstractParamGetter#getParamValue()
         */
        @Override
        public Object getParamValue() {
            Form httpHeaders = Util
                    .getHttpHeaders(tlContext.get().getRequest());
            String headerName = headerParam.value();
            try {
                if (this.collType == null) { // no collection parameter
                    String firstHeader = WrapperUtil.getValue(httpHeaders
                            .getFirst(headerName, true));
                    return convertParamValue(firstHeader);
                }
                return convertParamValues(new ParamValueIter(httpHeaders
                        .subList(headerName, true)));
            } catch (ConvertParameterException e) {
                throw new ConvertHeaderParamException(e);
            }
        }
    }

    static interface ParamGetter {

        /**
         * Returns the value for this param.
         * 
         * @return the value for this param.
         * @throws InvocationTargetException
         * @throws ConvertRepresentationException
         * @throws WebApplicationException
         */
        abstract Object getValue() throws InvocationTargetException,
                ConvertRepresentationException, WebApplicationException;
    }

    static class MatrixParamGetter extends EncParamGetter {

        private final MatrixParam matrixParam;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        MatrixParamGetter(MatrixParam matrixParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean encode) {
            super(defaultValue, convToCl, convToGen, tlContext, encode);
            this.matrixParam = matrixParam;
        }

        /**
         * @see IntoRrcInjector.AbstractParamGetter#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER test: de/encode: Encoded of field or bean setter
            CallContext callContext = tlContext.get();
            try {
                if (this.collType == null) { // no collection parameter
                    String matrixParamValue = callContext
                            .getLastMatrixParamEnc(matrixParam);
                    return convertParamValue(matrixParamValue);
                }
                Iterator<String> matrixParamValues = callContext
                        .matrixParamEncIter(matrixParam);
                return convertParamValues(matrixParamValues);
            } catch (ConvertParameterException e) {
                throw new ConvertMatrixParamException(e);
            }
        }
    }

    /**
     * Abstract super class for access to the entity or to &#64;*Param where
     * encoded is allowed (&#64;{@link PathParam}, &#64;{@link MatrixParam}
     * and &#64;{@link QueryParam}).
     */
    abstract static class NoEncParamGetter extends AbstractParamGetter {

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * @param annoSaysLeaveEncoded
         *                to check if the annotation is available.
         */
        NoEncParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext,
                boolean annoSaysLeaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext);
            checkForEncodedAnno(annoSaysLeaveEncoded);
        }

        /**
         * Checks if the annotation &#64;{@link Encoded} is available on the
         * given field or bean setter. If yes, if logs a warning.
         * 
         * @param fieldOrBeanSetter
         */
        void checkForEncodedAnno(AccessibleObject fieldOrBeanSetter) {
            checkForEncodedAnno(fieldOrBeanSetter
                    .isAnnotationPresent(Encoded.class));
        }

        /**
         * Checks if the annotation &#64;{@link Encoded} is available on the
         * given field or bean setter. If yes, if logs a warning.
         * 
         * @param fieldOrBeanSetter
         */
        void checkForEncodedAnno(boolean annoSaysLeaveEncoded) {
            if (annoSaysLeaveEncoded)
                localLogger
                        .warning("You should not use @Encoded on a @HeaderParam or @CookieParam. Will ignore it");
        }

        @Override
        protected boolean encode() {
            return false;
        }
    }

    /**
     * Abstract super class for access to &#64;*Param.
     */
    abstract static class AbstractParamGetter implements ParamGetter {

        protected final ThreadLocalizedContext tlContext;

        /**
         * The class to convert to. If this object getter represents an *Param
         * annotated parameter, and it should be to an array or collection of
         * something, this value contains not the collection/array type, but the
         * generic type of it.
         */
        protected final Class<?> convertTo;

        /**
         * The type of the collection. null, if this parameter do not represent
         * a collection.
         */
        protected final Class<Collection<?>> collType;

        /**
         * The default value for this parameter (if given)
         */
        protected final DefaultValue defaultValue;

        /**
         * True, if this parameter should be an array, otherwise false. If true,
         * the {@link #collType} must be set to a {@link List}.
         */
        protected final boolean isArray;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * @param fieldOrBeanSetter
         * @param tlContext
         */
        @SuppressWarnings("unchecked")
        AbstractParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
            this.defaultValue = defaultValue;
            if (convToCl.isArray()) {
                this.convertTo = convToCl.getComponentType();
                this.collType = (Class) ArrayList.class;
                this.isArray = true;
            } else if (convToGen instanceof ParameterizedType) {
                ParameterizedType parametrizedType = (ParameterizedType) convToGen;
                Type[] argTypes = parametrizedType.getActualTypeArguments();
                if (argTypes[0] instanceof Class)
                    this.convertTo = (Class<?>) argTypes[0];
                else
                    throw new NotYetImplementedException(
                            "Sorry, only Class is supported, but is "
                                    + argTypes[0]);
                // TESTEN with generic parameter
                this.collType = collType(parametrizedType);
                this.isArray = false;
            } else {
                this.convertTo = convToCl;
                this.collType = null;
                this.isArray = false;
            }
        }

        /**
         * @return an new created instance of {@link #collType}. Returns null,
         *         if collType is null.
         */
        @SuppressWarnings("unchecked")
        protected <A> Collection<A> createColl() {
            try {
                if (this.collType != null)
                    return (Collection) collType.newInstance();
                return null;
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not instantiate the collection type " + collType,
                        e);
            }
        }

        protected abstract boolean encode();

        /**
         * @return
         */
        public abstract Object getParamValue();

        public Object getValue() {
            return getParamValue();
        }

        /**
         * @param firstHeader
         * @return
         * @throws ConvertParameterException
         */
        protected Object convertParamValue(String firstHeader)
                throws ConvertParameterException {
            return convertParamValue(firstHeader, defaultValue);
        }

        /**
         * Converts the given value without any decoding.
         * 
         * @param paramValue
         * @param defaultValue
         * 
         * @return
         * @throws ConvertParameterException
         * @throws WebApplicationException
         *                 if the conversion method throws an
         *                 WebApplicationException.
         */
        private Object convertParamValueInner(String paramValue,
                DefaultValue defaultValue) throws ConvertParameterException,
                WebApplicationException {
            WebApplicationException constructorWae = null;
            try {
                Constructor<?> constr = this.convertTo
                        .getConstructor(String.class);
                return constr.newInstance(paramValue);
            } catch (WebApplicationException wae) {
                constructorWae = wae;
            } catch (Exception e) {
                // try valueOf(String) as next step
            }
            Method valueOf;
            try {
                valueOf = this.convertTo.getMethod("valueOf", String.class);
            } catch (SecurityException e) {
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            } catch (NoSuchMethodException e) {
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            }
            try {
                return valueOf.invoke(null, paramValue);
            } catch (IllegalArgumentException e) {
                if (constructorWae != null)
                    throw constructorWae;
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            } catch (IllegalAccessException e) {
                if (constructorWae != null)
                    throw constructorWae;
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            } catch (InvocationTargetException ite) {
                if (constructorWae != null)
                    throw constructorWae;
                Throwable cause = ite.getCause();
                if (cause instanceof WebApplicationException)
                    throw (WebApplicationException) cause;
                if ((paramValue == null || paramValue.length() <= 0)
                        && (ite.getCause() instanceof IllegalArgumentException)) {
                    if (defaultValue == null)
                        return null;
                    else {
                        String dfv = defaultValue.value();
                        return convertParamValueInner(dfv, null);
                    }
                }
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, ite);
            }
        }

        /**
         * Converts the given paramValue (found in the path, query, matrix or
         * header) into the given paramClass.
         * 
         * @param paramValue
         * @param defaultValue
         *                see {@link DefaultValue}
         * @param jaxRsRouter
         * @return
         * @throws ConvertParameterException
         * @see PathParam
         * @see MatrixParam
         * @see QueryParam
         * @see HeaderParam
         */
        protected Object convertParamValue(String paramValue,
                DefaultValue defaultValue) throws ConvertParameterException {
            if (encode() && paramValue != null)
                paramValue = Reference.decode(paramValue);
            else if (paramValue == null && defaultValue != null)
                paramValue = defaultValue.value();
            if (convertTo.equals(String.class)) // optimization
                return paramValue;
            if (convertTo.isPrimitive()) {
                if (paramValue != null && paramValue.length() <= 0)
                    paramValue = defaultValue.value();
                return getParamValueForPrimitive(paramValue);
            }
            return convertParamValueInner(paramValue, defaultValue);
        }

        /**
         * @param paramValueIter
         * @return
         * @throws ConvertParameterException
         */
        protected Object convertParamValues(Iterator<String> paramValueIter)
                throws ConvertParameterException {
            Collection<Object> coll = createColl();
            while (paramValueIter.hasNext()) {
                String queryParamValue = paramValueIter.next();
                Object convertedValue = convertParamValue(queryParamValue, null);
                if (convertedValue != null)
                    coll.add(convertedValue);
            }
            if (coll.isEmpty()) // add default value
                coll.add(convertParamValue(null));
            if (isArray)
                return Util.toArray(coll, this.convertTo);
            return coll;
        }

        /**
         * @param paramValue
         * @throws ConvertParameterException
         */
        protected Object getParamValueForPrimitive(String paramValue)
                throws ConvertParameterException {
            try {
                if (this.convertTo == Integer.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_INT;
                    return new Integer(paramValue);
                }
                if (this.convertTo == Double.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_DOUBLE;
                    return new Double(paramValue);
                }
                if (this.convertTo == Float.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_FLOAT;
                    return new Float(paramValue);
                }
                if (this.convertTo == Byte.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_BYTE;
                    return new Byte(paramValue);
                }
                if (this.convertTo == Long.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_LONG;
                    return new Long(paramValue);
                }
                if (this.convertTo == Short.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_SHORT;
                    return new Short(paramValue);
                }
                if (this.convertTo == Character.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_CHAR;
                    if (paramValue.length() == 1)
                        return paramValue.charAt(0);
                    throw ConvertParameterException.primitive(this.convertTo,
                            paramValue, null);
                }
                if (this.convertTo == Boolean.TYPE) {
                    if ((paramValue == null || paramValue.length() <= 0))
                        return DEFAULT_BOOLEAN;
                    if (paramValue.equalsIgnoreCase("true"))
                        return Boolean.TRUE;
                    if (paramValue.equalsIgnoreCase("false"))
                        return Boolean.FALSE;
                    throw ConvertParameterException.primitive(this.convertTo,
                            paramValue, null);
                }
            } catch (IllegalArgumentException e) {
                throw ConvertParameterException.primitive(this.convertTo,
                        paramValue, e);
            }
            String warning;
            if (this.convertTo == Void.TYPE)
                warning = "an object should be converted to a void; but this could not be here";
            else
                warning = "an object should be converted to a "
                        + this.convertTo
                        + ", but here are only primitives allowed.";
            Logger.getAnonymousLogger().warning(warning);
            ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
            rb.entity(warning);
            throw new WebApplicationException(rb.build());
        }
    }

    static class PathParamGetter extends EncParamGetter {

        private final PathParam pathParam;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        PathParamGetter(PathParam pathParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean encode) {
            super(defaultValue, convToCl, convToGen, tlContext, encode);
            this.pathParam = pathParam;
        }

        /**
         * @see AbstractParamGetter#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER test: de/encode: Encoded of field or bean setter
            CallContext callContext = tlContext.get();
            // LATER @PathParam(...) List<String> (see PathParamTest.testGet3())
            if (this.convertTo.equals(PathSegment.class)) { // FIXME
                throw new NotYetImplementedException(
                        "Sorry, @PathParam(..) PathSegment is not yet supported. You may use the non default @Context PathSegment to return the last of the available path segments (decoded), or UriInfo.getPathSegments(boolean)");
                // LATER @PathParam("x") PathSegment allowed.
            }
            try {
                if (this.collType == null) { // no collection parameter
                    String pathParamValue = callContext
                            .getLastPathParamEnc(pathParam);
                    return convertParamValue(pathParamValue);
                }
                Iterator<String> ppvIter = callContext
                        .pathParamEncIter(pathParam);
                return convertParamValues(ppvIter);
            } catch (ConvertParameterException e) {
                throw new ConvertPathParamException(e);
            }
        }
    }

    static class QueryParamGetter extends EncParamGetter {

        private final QueryParam queryParam;

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * @param encode
         *                TODO
         */
        QueryParamGetter(QueryParam queryParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean encode) {
            super(defaultValue, convToCl, convToGen, tlContext, encode);
            this.queryParam = queryParam;
        }

        /**
         * @see AbstractParamGetter#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER test: de/encode: Encoded of field or bean setter
            Reference resourceRef = tlContext.get().getRequest()
                    .getResourceRef();
            String queryString = resourceRef.getQuery();
            Form form = Converter.toFormEncoded(queryString, localLogger);
            String paramName = queryParam.value();
            List<Parameter> parameters = form.subList(paramName);
            try {
                if (this.collType == null) { // no collection parameter
                    Parameter firstQueryParam = form.getFirst(paramName);
                    String queryParamValue = WrapperUtil
                            .getValue(firstQueryParam);
                    return convertParamValue(queryParamValue);
                }
                ParamValueIter queryParamValueIter;
                queryParamValueIter = new ParamValueIter(parameters);
                return convertParamValues(queryParamValueIter);
            } catch (ConvertParameterException e) {
                throw new ConvertQueryParamException(e);
            }
        }
    }

    private static final String COLL_PARAM_NOT_DEFAULT = "The collection type Collection is not supported for parameters. Use List, Set or SortedSet";

    private static final Boolean DEFAULT_BOOLEAN = Boolean.FALSE;

    private static final Byte DEFAULT_BYTE = (byte) 0;

    private static final Character DEFAULT_CHAR = new Character('\0');

    private static final Double DEFAULT_DOUBLE = 0d;

    private static final Float DEFAULT_FLOAT = 0.0f;

    private static final Integer DEFAULT_INT = 0;

    private static final Long DEFAULT_LONG = new Long(0);

    private static final Short DEFAULT_SHORT = 0;

    private static final Logger localLogger = Logger.getAnonymousLogger();

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    /**
     * @return the collection type for the given
     *         {@link ParameterizedType parametrized Type}.<br>
     *         If the given type do not represent an collection, null is
     *         returned.
     */
    @SuppressWarnings("unchecked")
    private static Class<Collection<?>> collType(ParameterizedType type) {
        Type rawType = type.getRawType();
        if (rawType.equals(List.class))
            return (Class) ArrayList.class;
        else if (rawType.equals(Set.class))
            return (Class) HashSet.class;
        else if (rawType.equals(SortedSet.class))
            return (Class) TreeSet.class;
        else if (rawType.equals(Collection.class)) {
            Logger logger = Logger.getAnonymousLogger();
            logger.config(ParameterList.COLL_PARAM_NOT_DEFAULT);
            return (Class) ArrayList.class;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static Collection<Class<? extends Annotation>> createValidAnnotations() {
        return Arrays.asList(Context.class, HeaderParam.class,
                MatrixParam.class, QueryParam.class, PathParam.class,
                CookieParam.class);
    }

    /**
     * Returns the given annotation, if it is available in the given array of
     * annotations.
     */
    @SuppressWarnings("unchecked")
    static <A extends Annotation> A getAnno(Annotation[] annotations,
            Class<A> annoType) {
        for (Annotation annot : annotations) {
            Class<? extends Annotation> annotationType = annot.annotationType();
            if (annotationType.equals(annoType))
                return (A) annot;
        }
        return null;
    }

    /**
     * Returns true, if one of the annotations is &#64;{@link Encoded}
     * 
     * @param annotations
     * @return
     */
    static boolean getLeaveEncoded(Annotation[] annotations) {
        for (Annotation annot : annotations) {
            Class<? extends Annotation> annotationType = annot.annotationType();
            if (annotationType.equals(Encoded.class))
                return true;
        }
        return false;
    }

    private final int paramCount;

    private final ParamGetter[] parameters;

    /**
     * @param parameterTypes
     * @param genParamTypes
     * @param paramAnnoss
     * @param tlContext
     * @param leaveAllEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param extensionBackwardMapping
     * @param paramsAllowed
     *                true, if &#64;*Params are allowed as parameter, otherwise
     *                false. LATER paramsAllowed not needed
     * @param entityAllowed
     *                true, if the entity is allowed as parameter, otherwise
     *                false.
     * @param logger
     * @throws MissingAnnotationException
     */
    private ParameterList(Class<?>[] parameterTypes, Type[] genParamTypes,
            Annotation[][] paramAnnoss, ThreadLocalizedContext tlContext,
            boolean leaveAllEncoded, EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean paramsAllowed, boolean entityAllowed, Logger logger)
            throws MissingAnnotationException {
        this.paramCount = parameterTypes.length;
        this.parameters = new ParamGetter[paramCount];
        boolean entityAlreadyRequired = false;
        for (int i = 0; i < paramCount; i++) {
            Class<?> parameterType = parameterTypes[i];
            Type genParamType = genParamTypes[i];
            Annotation[] paramAnnos = paramAnnoss[i];
            Context conntextAnno = getAnno(paramAnnos, Context.class);
            if (conntextAnno != null) {
                parameters[i] = new ContextHolder(ContextInjector
                        .getInjectObject(parameterType, genParamType,
                                tlContext, entityProviders, allCtxResolvers,
                                extensionBackwardMapping));
                continue;
            }
            if (paramsAllowed) {
                boolean leaveThisEncoded = getLeaveEncoded(paramAnnos);
                DefaultValue defValue = getAnno(paramAnnos, DefaultValue.class);
                CookieParam cookieParam = getAnno(paramAnnos, CookieParam.class);
                HeaderParam headerParam = getAnno(paramAnnos, HeaderParam.class);
                MatrixParam matrixParam = getAnno(paramAnnos, MatrixParam.class);
                PathParam pathParam = getAnno(paramAnnos, PathParam.class);
                QueryParam queryParam = getAnno(paramAnnos, QueryParam.class);
                if (pathParam != null) {
                    parameters[i] = new PathParamGetter(pathParam, defValue,
                            parameterType, genParamType, tlContext,
                            !leaveAllEncoded && !leaveThisEncoded);
                    continue;
                }
                if (cookieParam != null) {
                    parameters[i] = new CookieParamGetter(cookieParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveThisEncoded);
                    continue;
                }
                if (headerParam != null) {
                    parameters[i] = new HeaderParamGetter(headerParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveThisEncoded);
                    continue;
                }
                if (matrixParam != null) {
                    parameters[i] = new MatrixParamGetter(matrixParam,
                            defValue, parameterType, genParamType, tlContext,
                            !leaveAllEncoded && !leaveThisEncoded);
                    continue;
                }
                if (queryParam != null) {
                    parameters[i] = new QueryParamGetter(queryParam, defValue,
                            parameterType, genParamType, tlContext,
                            !leaveAllEncoded && !leaveThisEncoded);
                    continue;
                }
            }
            // could only be the entity
            if (!entityAllowed) {
                throw new MissingAnnotationException(
                        "All parameters requires one of the following annotations: "
                                + VALID_ANNOTATIONS);
            }
            if (entityAlreadyRequired)
                throw new MissingAnnotationException(
                        "The entity is already read.  The "
                                + i
                                + ". parameter requires one of the following annotations: "
                                + VALID_ANNOTATIONS);
            if (Representation.class.isAssignableFrom(parameterType)) {
                parameters[i] = ReprEntityGetter.create(parameterType,
                        genParamType, logger);
            }
            if (parameters[i] == null) {
                parameters[i] = new EntityGetter(parameterType, genParamType,
                        tlContext, entityProviders, paramAnnos);
            }
            entityAlreadyRequired = true;
        }
    }

    /**
     * @param constr
     * @param tlContext
     * @param leaveEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param extensionBackwardMapping
     * @param logger
     * @throws MissingAnnotationException
     */
    public ParameterList(Constructor<?> constr,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws MissingAnnotationException {
        this(constr.getParameterTypes(), constr.getGenericParameterTypes(),
                constr.getParameterAnnotations(), tlContext, leaveEncoded,
                entityProviders, allCtxResolvers, extensionBackwardMapping,
                true, false, logger);
    }

    /**
     * @param executeMethod
     * @param annotatedMethod
     * @param tlContext
     * @param leaveEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param extensionBackwardMapping
     * @param entityAllowed
     * @param logger
     * @throws MissingAnnotationException
     */
    public ParameterList(Method executeMethod, Method annotatedMethod,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean entityAllowed, Logger logger)
            throws MissingAnnotationException {
        this(executeMethod.getParameterTypes(), executeMethod
                .getGenericParameterTypes(), annotatedMethod
                .getParameterAnnotations(), tlContext, leaveEncoded,
                entityProviders, allCtxResolvers, extensionBackwardMapping,
                true, entityAllowed, logger);
    }

    /**
     * Returns the concrete parameter array for the current request.
     * 
     * @return the concrete parameter array for the current request.
     * @throws InvocationTargetException
     * @throws ConvertRepresentationException
     * @throws WebApplicationException
     */
    public Object[] get() throws ConvertRepresentationException,
            InvocationTargetException, WebApplicationException {
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < this.paramCount; i++) {
            args[i] = parameters[i].getValue();
        }
        return args;
    }
}