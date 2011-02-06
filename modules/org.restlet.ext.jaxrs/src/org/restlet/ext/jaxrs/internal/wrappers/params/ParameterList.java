/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
import java.util.Collections;
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
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.PathSegmentImpl;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Contains a list of parameters for JAX-RS constructors, (sub) resource methods
 * and sub resource locators.
 * 
 * @author Stephan Koops
 */
public class ParameterList {

    /**
     * Abstract super class for access to &#64;*Param.
     */
    abstract static class AbstractParamGetter implements ParamGetter {

        /**
         * The type of the collection. null, if this parameter do not represent
         * a collection.
         */
        protected final Class<Collection<?>> collType;

        /**
         * The class to convert to. If this object getter represents an *Param
         * annotated parameter, and it should be to an array or collection of
         * something, this value contains not the collection/array type, but the
         * generic type of it.
         */
        protected final Class<?> convertTo;

        /**
         * The default value for this parameter (if given)
         */
        protected final DefaultValue defaultValue;

        /**
         * True, if this parameter should be an array, otherwise false. If true,
         * the {@link #collType} must be set to a {@link List}.
         */
        protected final boolean isArray;

        protected final ThreadLocalizedContext tlContext;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        AbstractParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
            this.defaultValue = defaultValue;
            if (convToCl.isArray()) {
                this.convertTo = convToCl.getComponentType();
                this.collType = (Class) ArrayList.class;
                this.isArray = true;
            } else if (convToGen instanceof ParameterizedType) {
                final ParameterizedType parametrizedType = (ParameterizedType) convToGen;
                final Type[] argTypes = parametrizedType
                        .getActualTypeArguments();
                if (argTypes[0] instanceof Class) {
                    this.convertTo = (Class<?>) argTypes[0];
                } else {
                    throw new NotYetImplementedException(
                            "Sorry, only Class is supported, but is "
                                    + argTypes[0]);
                }
                // TEST @*Param with array/collection and generic parameter
                this.collType = collType(parametrizedType);
                this.isArray = false;
            } else {
                this.convertTo = convToCl;
                this.collType = null;
                this.isArray = false;
            }
        }

        protected Object convertParamValue(String firstHeader)
                throws ConvertParameterException {
            return convertParamValue(firstHeader, this.defaultValue);
        }

        /**
         * Converts the given paramValue (found in the path, query, matrix or
         * header) into the given paramClass.
         * 
         * @param paramValue
         * @param defaultValue
         * @return
         * @throws ConvertParameterException
         * @see PathParam
         * @see MatrixParam
         * @see QueryParam
         * @see HeaderParam
         * @see CookieParam
         */
        protected Object convertParamValue(String paramValue,
                DefaultValue defaultValue) throws ConvertParameterException {
            if (decode() && (paramValue != null)) {
                paramValue = Reference.decode(paramValue);
            } else if ((paramValue == null) && (defaultValue != null)) {
                paramValue = defaultValue.value();
            }
            if (this.convertTo.equals(String.class)) {
                return paramValue;
            }
            if (this.convertTo.isPrimitive()) {
                if ((paramValue != null) && (paramValue.length() <= 0)) {
                    paramValue = defaultValue.value();
                }
                return getParamValueForPrimitive(paramValue);
            }
            return convertParamValueInner(paramValue, defaultValue);
        }

        /**
         * Converts the given value without any decoding.
         * 
         * @param paramValue
         * @param defaultValue
         * @return
         * @throws ConvertParameterException
         * @throws WebApplicationException
         *             if the conversion method throws an
         *             WebApplicationException.
         */
        private Object convertParamValueInner(String paramValue,
                DefaultValue defaultValue) throws ConvertParameterException,
                WebApplicationException {
            WebApplicationException constructorWae = null;
            try {
                final Constructor<?> constr = this.convertTo
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
                if (constructorWae != null) {
                    throw constructorWae;
                }
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            } catch (IllegalAccessException e) {
                if (constructorWae != null) {
                    throw constructorWae;
                }
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, e);
            } catch (InvocationTargetException ite) {
                if (constructorWae != null) {
                    throw constructorWae;
                }
                final Throwable cause = ite.getCause();
                if (cause instanceof WebApplicationException) {
                    throw (WebApplicationException) cause;
                }
                if (((paramValue == null) || (paramValue.length() <= 0))
                        && (ite.getCause() instanceof IllegalArgumentException)) {
                    if (defaultValue == null) {
                        return null;
                    }

                    final String dfv = defaultValue.value();
                    return convertParamValueInner(dfv, null);
                }
                throw ConvertParameterException.object(this.convertTo,
                        paramValue, ite);
            }
        }

        protected Object convertParamValues(Iterator<String> paramValueIter)
                throws ConvertParameterException {
            final Collection<Object> coll = createColl();
            while (paramValueIter.hasNext()) {
                final String queryParamValue = paramValueIter.next();
                final Object convertedValue = convertParamValue(
                        queryParamValue, null);
                if (convertedValue != null) {
                    coll.add(convertedValue);
                }
            }
            if (coll.isEmpty()) {
                coll.add(convertParamValue(null));
            }
            if (this.isArray) {
                return Util.toArray(coll, this.convertTo);
            }

            return unmodifiable(coll);
        }

        /**
         * @return an new created instance of {@link #collType}. Returns null,
         *         if collType is null.
         */
        @SuppressWarnings("unchecked")
        protected <A> Collection<A> createColl() {
            try {
                if (this.collType != null) {
                    return (Collection<A>) this.collType.newInstance();
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not instantiate the collection type "
                                + this.collType, e);
            }
        }

        protected <A> Collection<A> unmodifiable(Collection<A> coll) {
            if (coll instanceof List<?>)
                return Collections.unmodifiableList((List<A>) coll);
            if (coll instanceof SortedSet<?>)
                return Collections.unmodifiableSortedSet((SortedSet<A>) coll);
            if (coll instanceof Set<?>)
                return Collections.unmodifiableSet((Set<A>) coll);
            return Collections.unmodifiableCollection(coll);
        }

        /**
         * 
         * @return
         * @deprecated Use {@link #decoding()} instead.
         */
        protected boolean decode() {
            return decoding();
        }

        protected abstract boolean decoding();

        /**
         * @return the concrete value of this parameter for the current request.
         */
        public abstract Object getParamValue();

        protected Object getParamValueForPrimitive(String paramValue)
                throws ConvertParameterException {
            try {
                if (this.convertTo == Integer.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_INT;
                    }
                    return new Integer(paramValue);
                }
                if (this.convertTo == Double.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_DOUBLE;
                    }
                    return new Double(paramValue);
                }
                if (this.convertTo == Float.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_FLOAT;
                    }
                    return new Float(paramValue);
                }
                if (this.convertTo == Byte.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_BYTE;
                    }
                    return new Byte(paramValue);
                }
                if (this.convertTo == Long.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_LONG;
                    }
                    return new Long(paramValue);
                }
                if (this.convertTo == Short.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_SHORT;
                    }
                    return new Short(paramValue);
                }
                if (this.convertTo == Character.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_CHAR;
                    }
                    if (paramValue.length() == 1) {
                        return paramValue.charAt(0);
                    }
                    throw ConvertParameterException.primitive(this.convertTo,
                            paramValue, null);
                }
                if (this.convertTo == Boolean.TYPE) {
                    if (((paramValue == null) || (paramValue.length() <= 0))) {
                        return DEFAULT_BOOLEAN;
                    }
                    if (paramValue.equalsIgnoreCase("true")) {
                        return Boolean.TRUE;
                    }
                    if (paramValue.equalsIgnoreCase("false")) {
                        return Boolean.FALSE;
                    }
                    throw ConvertParameterException.primitive(this.convertTo,
                            paramValue, null);
                }
            } catch (IllegalArgumentException e) {
                throw ConvertParameterException.primitive(this.convertTo,
                        paramValue, e);
            }
            String warning;
            if (this.convertTo == Void.TYPE) {
                warning = "an object should be converted to a void; but this could not be here";
            } else {
                warning = "an object should be converted to a "
                        + this.convertTo
                        + ", but here are only primitives allowed.";
            }
            localLogger.warning(warning);
            final ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
            rb.entity(warning);
            throw new WebApplicationException(rb.build());
        }

        public Object getValue() {
            return getParamValue();
        }
    }

    static class CookieParamGetter extends NoEncParamGetter {

        private final CookieParam cookieParam;

        /**
         * @param annoSaysLeaveClassEncoded
         *            to check if the annotation is available, but should not
         *            be.
         */
        CookieParamGetter(CookieParam cookieParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext,
                boolean annoSaysLeaveClassEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext,
                    annoSaysLeaveClassEncoded);
            this.cookieParam = cookieParam;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object getParamValue() {
            final String cookieName = this.cookieParam.value();
            Series<org.restlet.data.Cookie> cookies;
            cookies = this.tlContext.get().getRequest().getCookies();
            if (this.convertTo.equals(Cookie.class)) {
                final Collection<Cookie> coll = createColl();
                for (final org.restlet.data.Cookie rc : cookies) {
                    if (!rc.getName().equals(cookieName)) {
                        continue;
                    }
                    final Cookie cookie = Converter.toJaxRsCookie(rc);
                    if (coll == null) {
                        return cookie;
                    }
                    coll.add(cookie);
                }
                if (coll == null) {
                    return null;
                }
                if (coll.isEmpty()) {
                    final String value = this.defaultValue.value();
                    coll.add(new Cookie(cookieName, value));
                }
                if (this.isArray) {
                    return Util.toArray(coll, Cookie.class);
                }
                return coll;
            }
            try {
                if (this.collType == null) { // no collection parameter
                    final String firstCookieValue = WrapperUtil
                            .getValue(cookies.getFirst(cookieName));
                    return convertParamValue(firstCookieValue);
                }
                return convertParamValues(new ParamValueIter(
                        (Series) cookies.subList(cookieName)));
            } catch (ConvertParameterException e) {
                throw new ConvertCookieParamException(e);
            }
        }
    }

    /**
     * Abstract super class for access to the entity or to &#64;*Param where
     * encoded is allowed (&#64;{@link PathParam}, &#64;{@link MatrixParam} and
     * &#64;{@link QueryParam}).
     */
    abstract static class EncParamGetter extends AbstractParamGetter {

        private final boolean decoding;

        EncParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext,
                boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext);
            this.decoding = !leaveEncoded;
        }

        @Override
        protected boolean decoding() {
            return this.decoding;
        }

    }

    static abstract class FormOrQueryParamGetter extends EncParamGetter {

        FormOrQueryParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext,
                boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
        }

        /**
         * @param form
         * @param paramName
         * @return
         * @throws ConvertQueryParamException
         */
        Object getParamValue(final Form form, final String paramName)
                throws ConvertParameterException {
            final List<Parameter> parameters = form.subList(paramName);
            if (this.collType == null) { // no collection parameter
                final Parameter firstFormParam = form.getFirst(paramName);
                final String queryParamValue = WrapperUtil
                        .getValue(firstFormParam);
                return convertParamValue(queryParamValue);
            }
            ParamValueIter queryParamValueIter;
            queryParamValueIter = new ParamValueIter(parameters);
            return convertParamValues(queryParamValueIter);
        }
    }

    static class FormParamGetter extends FormOrQueryParamGetter {

        private final FormParam formParam;

        private static Form form;

        FormParamGetter(FormParam formParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.formParam = formParam;
        }

        @Override
        public Object getParamValue() {
            Representation entity = this.tlContext.get().getRequest()
                    .getEntity();
            if (entity != null && entity.isAvailable()) {
                form = new Form(entity);
            }

            final String paramName = this.formParam.value();
            try {
                return super.getParamValue(form, paramName);
            } catch (ConvertParameterException e) {
                throw new ConvertQueryParamException(e);
            }
        }
    }

    static class HeaderParamGetter extends NoEncParamGetter {

        private final HeaderParam headerParam;

        /**
         * @param annoSaysLeaveClassEncoded
         *            to check if the annotation is available.
         */
        HeaderParamGetter(HeaderParam headerParam, DefaultValue defaultValue,
                Class<?> convToCl, Type paramGenericType,
                ThreadLocalizedContext tlContext,
                boolean annoSaysLeaveClassEncoded) {
            super(defaultValue, convToCl, paramGenericType, tlContext,
                    annoSaysLeaveClassEncoded);
            this.headerParam = headerParam;
        }

        @Override
        public Object getParamValue() {
            final Form httpHeaders = Util.getHttpHeaders(this.tlContext.get()
                    .getRequest());
            final String headerName = this.headerParam.value();
            try {
                if (this.collType == null) { // no collection parameter
                    final String firstHeader = WrapperUtil.getValue(httpHeaders
                            .getFirst(headerName, true));
                    return convertParamValue(firstHeader);
                }
                return convertParamValues(new ParamValueIter(
                        httpHeaders.subList(headerName, true)));
            } catch (ConvertParameterException e) {
                throw new ConvertHeaderParamException(e);
            }
        }
    }

    static class MatrixParamGetter extends EncParamGetter {

        private final MatrixParam matrixParam;

        MatrixParamGetter(MatrixParam matrixParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.matrixParam = matrixParam;
        }

        @Override
        public Object getParamValue() {
            final CallContext callContext = this.tlContext.get();
            try {
                if (this.collType == null) { // no collection parameter
                    final String matrixParamValue = callContext
                            .getLastMatrixParamEnc(this.matrixParam);
                    return convertParamValue(matrixParamValue);
                }
                Iterator<String> matrixParamValues;
                matrixParamValues = callContext
                        .matrixParamEncIter(this.matrixParam);
                return convertParamValues(matrixParamValues);
            } catch (ConvertParameterException e) {
                throw new ConvertMatrixParamException(e);
            }
        }
    }

    /**
     * Abstract super class for access to the entity or to &#64;*Param where
     * encoded is allowed (&#64;{@link PathParam}, &#64;{@link MatrixParam} and
     * &#64;{@link QueryParam}).
     */
    abstract static class NoEncParamGetter extends AbstractParamGetter {

        /**
         * @param annoSaysLeaveEncoded
         *            to check if the annotation is available.
         */
        NoEncParamGetter(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext,
                boolean annoSaysLeaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext);
            checkForEncodedAnno(annoSaysLeaveEncoded);
        }

        /**
         * Checks if the annotation &#64;{@link Encoded} is available on the
         * given field or bean setter. If yes, a warning is logged.
         */
        void checkForEncodedAnno(AccessibleObject fieldOrBeanSetter) {
            checkForEncodedAnno(fieldOrBeanSetter
                    .isAnnotationPresent(Encoded.class));
        }

        /**
         * Checks if the annotation &#64;{@link Encoded} is available on the
         * given field or bean setter. If yes, this method logs a warning.
         */
        void checkForEncodedAnno(boolean annoSaysLeaveEncoded) {
            if (annoSaysLeaveEncoded) {
                localLogger
                        .warning("You should not use @Encoded on a @HeaderParam or @CookieParam. Will ignore it");
            }
        }

        @Override
        protected boolean decoding() {
            return false;
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
        public Object getValue() throws InvocationTargetException,
                ConvertRepresentationException, WebApplicationException;
    }

    static class PathParamGetter extends EncParamGetter {

        private final PathParam pathParam;

        PathParamGetter(PathParam pathParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded)
                throws IllegalPathParamTypeException {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            if ((this.collType != null)
                    && (!this.convertTo.equals(PathSegment.class))) {
                throw new IllegalPathParamTypeException(
                        "The type of a @PathParam annotated parameter etc. must not be a collection type or array, if the type parameter is not PathSegment");
            }
            this.pathParam = pathParam;
        }

        /**
         * Creates a {@link PathSegment}.
         * 
         * @param pathSegmentEnc
         * @return
         * @throws IllegalArgumentException
         */
        private PathSegment createPathSegment(final String pathSegmentEnc)
                throws IllegalArgumentException {
            return new PathSegmentImpl(pathSegmentEnc, this.decode(), -1);
        }

        @Override
        public Object getParamValue() {
            final CallContext callContext = this.tlContext.get();
            if (this.convertTo.equals(PathSegment.class)) {
                if (this.collType == null) { // no collection parameter
                    final String pathSegmentEnc = callContext
                            .getLastPathSegmentEnc(this.pathParam);
                    return createPathSegment(pathSegmentEnc);
                }
                final Iterator<String> pathSegmentEncIter;
                pathSegmentEncIter = callContext
                        .pathSegementEncIter(this.pathParam);
                final Collection<Object> coll = createColl();
                while (pathSegmentEncIter.hasNext()) {
                    final String pathSegmentEnc = pathSegmentEncIter.next();
                    coll.add(createPathSegment(pathSegmentEnc));
                }
                if (this.isArray) {
                    return Util.toArray(coll, this.convertTo);
                }

                return unmodifiable(coll);
            }
            try {
                final String pathParamValue;
                pathParamValue = callContext.getLastPathParamEnc(pathParam);
                return convertParamValue(pathParamValue);
            } catch (ConvertParameterException e) {
                throw new ConvertPathParamException(e);
            }
        }
    }

    static class QueryParamGetter extends FormOrQueryParamGetter {

        private final QueryParam queryParam;

        QueryParamGetter(QueryParam queryParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.queryParam = queryParam;
        }

        @Override
        public Object getParamValue() {
            final Reference resourceRef = this.tlContext.get().getRequest()
                    .getResourceRef();
            final String queryString = resourceRef.getQuery();
            final Form form = Converter.toFormEncoded(queryString);
            final String paramName = this.queryParam.value();
            try {
                return super.getParamValue(form, paramName);
            } catch (ConvertParameterException e) {
                throw new ConvertQueryParamException(e);
            }
        }
    }

    /**
     * @author Stephan Koops
     */
    private static class UriInfoGetter implements ParamGetter {

        private final boolean availableMandatory;

        private final ThreadLocalizedUriInfo uriInfo;

        private UriInfoGetter(ThreadLocalizedContext tlContext,
                boolean availableMandatory) {
            this.uriInfo = new ThreadLocalizedUriInfo(tlContext);
            this.availableMandatory = availableMandatory;
        }

        public Object getValue() throws InvocationTargetException,
                ConvertRepresentationException, WebApplicationException {
            this.uriInfo.saveStateForCurrentThread(this.availableMandatory);
            return this.uriInfo;
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

    private static final Logger localLogger = org.restlet.Context
            .getCurrentLogger();

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    /**
     * @return the collection type for the given {@link ParameterizedType
     *         parametrized Type}.<br>
     *         If the given type do not represent an collection, null is
     *         returned.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Class<Collection<?>> collType(ParameterizedType type) {
        final Type rawType = type.getRawType();
        if (rawType.equals(List.class)) {
            return (Class) ArrayList.class;
        } else if (rawType.equals(Set.class)) {
            return (Class) HashSet.class;
        } else if (rawType.equals(SortedSet.class)) {
            return (Class) TreeSet.class;
        } else if (rawType.equals(Collection.class)) {
            localLogger.config(ParameterList.COLL_PARAM_NOT_DEFAULT);
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
        for (final Annotation annot : annotations) {
            final Class<? extends Annotation> annotationType = annot
                    .annotationType();
            if (annotationType.equals(annoType)) {
                return (A) annot;
            }
        }
        return null;
    }

    /**
     * Returns true, if one of the annotations is &#64;{@link Encoded}
     */
    static boolean getLeaveEncoded(Annotation[] annotations) {
        for (final Annotation annot : annotations) {
            final Class<? extends Annotation> annotationType = annot
                    .annotationType();
            if (annotationType.equals(Encoded.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * must call the {@link EntityGetter} first, if &#64;{@link FormParam} is
     * used. A value less than zero means, that no special handling is needed.
     */
    private final int entityPosition;

    /** shortcut for {@link #parameters}.length */
    private final int paramCount;

    /** @see #paramCount */
    private final ParamGetter[] parameters;

    /**
     * @param parameterTypes
     * @param genParamTypes
     * @param paramAnnoss
     * @param tlContext
     * @param leaveAllEncoded
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     * @param paramsAllowed
     *            true, if &#64;*Params are allowed as parameter, otherwise
     *            false.
     * @param entityAllowed
     *            true, if the entity is allowed as parameter, otherwise false.
     * @param logger
     * @param allMustBeAvailable
     *            if true, all values must be available (for singeltons creation
     *            it must be false)
     * @throws MissingAnnotationException
     * @throws IllegalTypeException
     *             if the given class is not valid to be annotated with &#64;
     *             {@link Context}.
     * @throws IllegalPathParamTypeException
     */
    private ParameterList(Class<?>[] parameterTypes, Type[] genParamTypes,
            Annotation[][] paramAnnoss, ThreadLocalizedContext tlContext,
            boolean leaveAllEncoded, JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean paramsAllowed, boolean entityAllowed, Logger logger,
            boolean allMustBeAvailable) throws MissingAnnotationException,
            IllegalTypeException, IllegalPathParamTypeException {
        this.paramCount = parameterTypes.length;
        this.parameters = new ParamGetter[this.paramCount];
        boolean entityAlreadyRead = false;
        int entityPosition = -1;
        for (int i = 0; i < this.paramCount; i++) {
            final Class<?> parameterType = parameterTypes[i];
            final Type genParamType = genParamTypes[i];
            final Annotation[] paramAnnos = paramAnnoss[i];
            final Context conntextAnno = getAnno(paramAnnos, Context.class);
            if (conntextAnno != null) {
                if (parameterType.equals(UriInfo.class)) {
                    this.parameters[i] = new UriInfoGetter(tlContext,
                            allMustBeAvailable);
                } else {
                    this.parameters[i] = new ContextHolder(
                            ContextInjector.getInjectObject(parameterType,
                                    tlContext, jaxRsProviders,
                                    extensionBackwardMapping));
                }
                continue;
            }
            if (paramsAllowed) {
                final boolean leaveThisEncoded = getLeaveEncoded(paramAnnos);
                final DefaultValue defValue = getAnno(paramAnnos,
                        DefaultValue.class);
                final CookieParam cookieParam = getAnno(paramAnnos,
                        CookieParam.class);
                final HeaderParam headerParam = getAnno(paramAnnos,
                        HeaderParam.class);
                final MatrixParam matrixParam = getAnno(paramAnnos,
                        MatrixParam.class);
                final PathParam pathParam = getAnno(paramAnnos, PathParam.class);
                final QueryParam queryParam = getAnno(paramAnnos,
                        QueryParam.class);
                final FormParam formParam = getAnno(paramAnnos, FormParam.class);
                if (pathParam != null) {
                    this.parameters[i] = new PathParamGetter(pathParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveAllEncoded || leaveThisEncoded);
                    continue;
                } else if (cookieParam != null) {
                    this.parameters[i] = new CookieParamGetter(cookieParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveThisEncoded);
                    continue;
                } else if (headerParam != null) {
                    this.parameters[i] = new HeaderParamGetter(headerParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveThisEncoded);
                    continue;
                } else if (matrixParam != null) {
                    this.parameters[i] = new MatrixParamGetter(matrixParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveAllEncoded || leaveThisEncoded);
                    continue;
                } else if (queryParam != null) {
                    this.parameters[i] = new QueryParamGetter(queryParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveAllEncoded || leaveThisEncoded);
                    continue;
                } else if (formParam != null) {
                    this.parameters[i] = new FormParamGetter(formParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveAllEncoded || leaveThisEncoded);
                    continue;
                }
            }
            // could only be the entity here
            if (!entityAllowed) {
                throw new MissingAnnotationException(
                        "All parameters requires one of the following annotations: "
                                + VALID_ANNOTATIONS);
            }
            if (entityAlreadyRead) {
                throw new MissingAnnotationException(
                        "The entity is already read.  The " + i
                                + ". parameter requires one of "
                                + "the following annotations: "
                                + VALID_ANNOTATIONS);
            }
            if (Representation.class.isAssignableFrom(parameterType)) {
                this.parameters[i] = ReprEntityGetter.create(parameterType,
                        genParamType, logger);
            }
            if (this.parameters[i] == null) {
                this.parameters[i] = new EntityGetter(parameterType,
                        genParamType, tlContext, jaxRsProviders, paramAnnos);
            }
            entityPosition = i;
            entityAlreadyRead = true;
        }
        this.entityPosition = entityPosition;
    }

    /**
     * @param constr
     * @param tlContext
     * @param leaveEncoded
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     * @param paramsAllowed
     * @param logger
     * @param allMustBeAvailable
     * @throws MissingAnnotationException
     * @throws IllegalTypeException
     *             if one of the parameters contains a &#64;{@link Context} on
     *             an type that must not be annotated with &#64;{@link Context}.
     * @throws IllegalPathParamTypeException
     */
    public ParameterList(Constructor<?> constr,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean paramsAllowed, Logger logger, boolean allMustBeAvailable)
            throws MissingAnnotationException, IllegalTypeException,
            IllegalPathParamTypeException {
        this(constr.getParameterTypes(), constr.getGenericParameterTypes(),
                constr.getParameterAnnotations(), tlContext, leaveEncoded,
                jaxRsProviders, extensionBackwardMapping, paramsAllowed, false,
                logger, allMustBeAvailable);
    }

    /**
     * @param executeMethod
     * @param annotatedMethod
     * @param tlContext
     * @param leaveEncoded
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     * @param entityAllowed
     * @param logger
     * @throws MissingAnnotationException
     * @throws IllegalTypeException
     *             if one of the parameters contains a &#64;{@link Context} on
     *             an type that must not be annotated with &#64;{@link Context}.
     * @throws IllegalPathParamTypeException
     */
    public ParameterList(Method executeMethod, Method annotatedMethod,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean entityAllowed, Logger logger)
            throws MissingAnnotationException, IllegalTypeException,
            IllegalPathParamTypeException {
        this(executeMethod.getParameterTypes(), executeMethod
                .getGenericParameterTypes(), annotatedMethod
                .getParameterAnnotations(), tlContext, leaveEncoded,
                jaxRsProviders, extensionBackwardMapping, true, entityAllowed,
                logger, true);
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
        final Object[] args = new Object[this.parameters.length];
        if (this.entityPosition >= 0) {
            args[entityPosition] = this.parameters[entityPosition].getValue();
        }
        for (int i = 0; i < this.paramCount; i++) {
            if (i != this.entityPosition) {
                args[i] = this.parameters[i].getValue();
            }
        }
        return args;
    }
}