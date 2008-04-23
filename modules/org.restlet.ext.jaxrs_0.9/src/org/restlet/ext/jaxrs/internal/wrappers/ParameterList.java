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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.ContextInjector.Injector;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.ParamValueIter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.util.Series;

/**
 * Contains a list of parameters for JAX-RS constructors, (sub) resource methods
 * and sub resource locators.
 * 
 * @author Stephan Koops
 */
public class ParameterList {

    /**
     * Abstract super class for access to the entity or to &#64;*Param.
     */
    abstract static class AbstractInjectObjectGetter implements
            InjectObjectGetter {
    
        protected final Class<?> convToCl;
    
        protected final Type convToGen;
    
        protected final ThreadLocalizedContext tlContext;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        AbstractInjectObjectGetter(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
            if (fieldOrBeanSetter instanceof Field) {
                Field field = ((Field) fieldOrBeanSetter);
                this.convToCl = field.getType();
                this.convToGen = field.getGenericType();
            } else if (fieldOrBeanSetter instanceof Method) {
                Method beanSetter = ((Method) fieldOrBeanSetter);
                this.convToCl = beanSetter.getParameterTypes()[0];
                this.convToGen = beanSetter.getGenericParameterTypes()[0];
            } else {
                throw new IllegalArgumentException(
                        "The fieldOrBeanSetter must be a Field or a method");
            }
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        AbstractInjectObjectGetter(Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
            this.convToCl = convToCl;
            this.convToGen = convToGen;
        }
    
        /**
         * Returns the value for this param.
         * 
         * @return
         * @throws InvocationTargetException
         */
        public abstract Object getValue() throws InvocationTargetException,
                ConvertRepresentationException;
    }

    static class CookieParamInjector extends ParamInjector {

        private final CookieParam cookieParam;

        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        CookieParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext) {
            super(fieldOrBeanSetter, tlContext);
            cookieParam = this.fieldOrBeanSetter
                    .getAnnotation(CookieParam.class);
        }

        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        CookieParamInjector(CookieParam cookieParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext) {
            super(defaultValue, convToCl, convToGen, tlContext);
            this.cookieParam = cookieParam;
        }

        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.IntoRrcInjector.ParamInjector#getParamValue()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getParamValue() {
            String cookieName = cookieParam.value();
            Collection<Cookie> coll = null;
            boolean isCookie = false; // javax.ws.rs.core.Cookie requested
            boolean toArray = false;
            if (convToCl.equals(Cookie.class)) {
                isCookie = true;
            } else if (convToCl.isArray()) {
                Class<?> paramClass2 = convToCl.getComponentType();
                if (paramClass2.equals(Cookie.class)) {
                    coll = new ArrayList<Cookie>();
                    toArray = true;
                    isCookie = true;
                }
            } else if (convToGen instanceof ParameterizedType) {
                ParameterizedType parametrizedType = (ParameterizedType) convToGen;
                Type[] argTypes = parametrizedType.getActualTypeArguments();
                if (argTypes.length == 1 && argTypes[0].equals(Cookie.class)) {
                    coll = WrapperUtil.createColl(parametrizedType);
                    if (coll != null)
                        isCookie = true;
                }
            }
            Series<org.restlet.data.Cookie> cookies;
            cookies = tlContext.get().getRequest().getCookies();
            if (isCookie) {
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
                if (toArray)
                    return Util.toArray(coll, Cookie.class);
                return coll;
            }
            try {
                return WrapperUtil.convertParamValuesFromParam(convToCl,
                        convToGen, new ParamValueIter((Series) cookies
                                .subList(cookieName)), WrapperUtil
                                .getValue(cookies.getFirst(cookieName)),
                        defaultValue, true);
                // leaveEncoded = true -> not change
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
    abstract static class EncParamInjector extends ParamInjector {
    
        protected final boolean leaveEncoded;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        EncParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(fieldOrBeanSetter, tlContext);
            this.leaveEncoded = leaveEncoded;
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        EncParamInjector(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext,
                boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext);
            this.leaveEncoded = leaveEncoded;
        }
    }

    static class HeaderParamInjector extends ParamInjector {
    
        private final HeaderParam headerParam;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        HeaderParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext) {
            super(fieldOrBeanSetter, tlContext);
            this.headerParam = this.fieldOrBeanSetter
                    .getAnnotation(HeaderParam.class);
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        HeaderParamInjector(HeaderParam headerParam, DefaultValue defaultValue,
                Class<?> convToCl, Type paramGenericType,
                ThreadLocalizedContext tlContext) {
            super(defaultValue, convToCl, paramGenericType, tlContext);
            this.headerParam = headerParam;
        }
    
        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.IntoRrcInjector.ParamInjector#getParamValue()
         */
        @Override
        public Object getParamValue() {
            Form httpHeaders = Util
                    .getHttpHeaders(tlContext.get().getRequest());
            String headerName = headerParam.value();
            try {
                return WrapperUtil.convertParamValuesFromParam(convToCl,
                        convToGen, new ParamValueIter(httpHeaders.subList(
                                headerName, true)), WrapperUtil
                                .getValue(httpHeaders
                                        .getFirst(headerName, true)),
                        defaultValue, true);
            } catch (ConvertParameterException e) {
                throw new ConvertHeaderParamException(e);
            }
        }
    }

    static interface InjectObjectGetter {
    
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

    static class MatrixParamInjector extends EncParamInjector {
    
        private final MatrixParam matrixParam;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        MatrixParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(fieldOrBeanSetter, tlContext, leaveEncoded);
            this.matrixParam = fieldOrBeanSetter
                    .getAnnotation(MatrixParam.class);
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        MatrixParamInjector(MatrixParam matrixParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.matrixParam = matrixParam;
        }
    
        /**
         * @see IntoRrcInjector.ParamInjector#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER de/encode: Encoded of field or bean setter
            CallContext callContext = tlContext.get();
            String matrixParamValue = callContext
                    .getLastMatrixParamEnc(matrixParam);
            Iterator<String> matrixParamValues = callContext
                    .matrixParamEncIter(matrixParam);
            try {
                return WrapperUtil.convertParamValuesFromParam(convToCl,
                        convToGen, matrixParamValues, matrixParamValue,
                        defaultValue, leaveEncoded);
            } catch (ConvertParameterException e) {
                throw new ConvertMatrixParamException(e);
            }
        }
    }

    /**
     * Abstract super class for access to &#64;*Param.
     */
    abstract static class ParamInjector extends AbstractInjectObjectGetter
            implements Injector, InjectObjectGetter {
    
        protected final DefaultValue defaultValue;
    
        protected final AccessibleObject fieldOrBeanSetter;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         * 
         * @param fieldOrBeanSetter
         * @param tlContext
         */
        ParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext) {
            super(fieldOrBeanSetter, tlContext);
            this.fieldOrBeanSetter = fieldOrBeanSetter;
            this.defaultValue = this.fieldOrBeanSetter
                    .getAnnotation(DefaultValue.class);
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         * 
         * @param fieldOrBeanSetter
         * @param tlContext
         */
        ParamInjector(DefaultValue defaultValue, Class<?> convToCl,
                Type convToGen, ThreadLocalizedContext tlContext) {
            super(convToCl, convToGen, tlContext);
            this.fieldOrBeanSetter = null;
            this.defaultValue = defaultValue;
        }
    
        /**
         * @return
         */
        public abstract Object getParamValue();
    
        @Override
        public Object getValue() {
            return getParamValue();
        }
    
        public void injectInto(Object resource)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            Util.inject(resource, fieldOrBeanSetter, getParamValue());
        }
    }

    static class PathParamInjector extends EncParamInjector {
    
        private final PathParam pathParam;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        PathParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(fieldOrBeanSetter, tlContext, leaveEncoded);
            this.pathParam = this.fieldOrBeanSetter
                    .getAnnotation(PathParam.class);
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        PathParamInjector(PathParam pathParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.pathParam = pathParam;
        }
    
        /**
         * @see ParameterList.ParamInjector#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER de/encode: Encoded of field or bean setter
            CallContext callContext = tlContext.get();
            // LATER Path-Param: List<String> (see PathParamTest.testGet3())
            if (convToCl.equals(PathSegment.class)) {
                throw new NotYetImplementedException(
                        "Sorry, @PathParam(..) PathSegment is not yet supported");
                // LATER @PathParam("x") PathSegment allowed.
            }
            String pathParamValue = callContext.getLastPathParamEnc(pathParam);
            Iterator<String> ppvIter = callContext.pathParamEncIter(pathParam);
            try {
                return WrapperUtil.convertParamValuesFromParam(convToCl,
                        convToGen, ppvIter, pathParamValue, defaultValue,
                        leaveEncoded);
            } catch (ConvertParameterException e) {
                throw new ConvertPathParamException(e);
            }
        }
    }

    static class QueryParamInjector extends EncParamInjector {
    
        private final QueryParam queryParam;
    
        /**
         * Constructor, if you want to use {@link #injectInto(Object)}.
         */
        QueryParamInjector(AccessibleObject fieldOrBeanSetter,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(fieldOrBeanSetter, tlContext, leaveEncoded);
            this.queryParam = this.fieldOrBeanSetter
                    .getAnnotation(QueryParam.class);
        }
    
        /**
         * Constructor to be used, if only {@link #getParamValue()} is needed.
         */
        QueryParamInjector(QueryParam queryParam, DefaultValue defaultValue,
                Class<?> convToCl, Type convToGen,
                ThreadLocalizedContext tlContext, boolean leaveEncoded) {
            super(defaultValue, convToCl, convToGen, tlContext, leaveEncoded);
            this.queryParam = queryParam;
        }
    
        /**
         * @see ParameterList.ParamInjector#getParamValue()
         */
        @Override
        public Object getParamValue() {
            // LATER de/encode: Encoded of field or bean setter
            Reference resourceRef = tlContext.get().getRequest()
                    .getResourceRef();
            String queryString = resourceRef.getQuery();
            Form form = Converter.toFormEncoded(queryString, localLogger);
            String paramName = queryParam.value();
            List<Parameter> parameters = form.subList(paramName);
            try {
                Parameter firstQueryParam = form.getFirst(paramName);
                String queryParamValue = WrapperUtil.getValue(firstQueryParam);
                ParamValueIter queryParamValueIter;
                queryParamValueIter = new ParamValueIter(parameters);
                return WrapperUtil.convertParamValuesFromParam(convToCl,
                        convToGen, queryParamValueIter, queryParamValue,
                        defaultValue, leaveEncoded);
            } catch (ConvertParameterException e) {
                throw new ConvertQueryParamException(e);
            }
        }
    }

    private static final Logger localLogger = Logger.getAnonymousLogger();

    private static final Collection<Class<? extends Annotation>> VALID_ANNOTATIONS = createValidAnnotations();

    @SuppressWarnings("unchecked")
    static Collection<Class<? extends Annotation>> createValidAnnotations() {
        return Arrays.asList(Context.class, HeaderParam.class,
                MatrixParam.class, QueryParam.class, PathParam.class,
                CookieParam.class);
    }

    /**
     * Returns the given annotation, or null if not available.
     * 
     * @param annotations
     * @param annoType
     * @return
     */
    @SuppressWarnings("unchecked")
    static <A extends Annotation> A getAnnos(Annotation[] annotations,
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

    private final ParameterList.InjectObjectGetter[] parameters;

    /**
     * @param parameterTypes
     * @param genParamTypes
     * @param paramAnnoss
     * @param tlContext
     * @param leaveEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param paramsAllowed
     *                true, if &#64;*Params are allowed as parameter, otherwise
     *                false.
     * @param entityAllowed
     *                true, if the entity is allowed as parameter, otherwise
     *                false.
     * @param logger
     * @throws MissingAnnotationException
     */
    public ParameterList(Class<?>[] parameterTypes, Type[] genParamTypes,
            Annotation[][] paramAnnoss, ThreadLocalizedContext tlContext,
            boolean leaveEncoded, EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            boolean paramsAllowed, boolean entityAllowed, Logger logger)
            throws MissingAnnotationException {
        this.paramCount = parameterTypes.length;
        this.parameters = new ParameterList.InjectObjectGetter[paramCount];
        boolean entityAlreadyRequired = false;
        for (int i = 0; i < paramCount; i++) {
            Class<?> parameterType = parameterTypes[i];
            Type genParamType = genParamTypes[i];
            Annotation[] paramAnnos = paramAnnoss[i];
            Context conntextAnno = getAnnos(paramAnnos, Context.class);
            if (conntextAnno != null) {
                parameters[i] = new AbstractMethodWrapper.ContextHolder(
                        ContextInjector.getInjectObject(tlContext,
                                entityProviders, allCtxResolvers,
                                parameterType, genParamType));
                continue;
            }
            if (paramsAllowed) {
                DefaultValue defValue;
                defValue = getAnnos(paramAnnos, DefaultValue.class);
                if (!leaveEncoded)
                    leaveEncoded = ParameterList.getLeaveEncoded(paramAnnos);
                CookieParam cookieParam = getAnnos(paramAnnos,
                        CookieParam.class);
                HeaderParam headerParam = getAnnos(paramAnnos,
                        HeaderParam.class);
                MatrixParam matrixParam = getAnnos(paramAnnos,
                        MatrixParam.class);
                PathParam pathParam = getAnnos(paramAnnos, PathParam.class);
                QueryParam queryParam = getAnnos(paramAnnos, QueryParam.class);
                if (pathParam != null) {
                    parameters[i] = new ParameterList.PathParamInjector(pathParam, defValue,
                            parameterType, genParamType, tlContext,
                            leaveEncoded);
                    continue;
                }
                if (cookieParam != null) {
                    parameters[i] = new CookieParamInjector(cookieParam,
                            defValue, parameterType, genParamType, tlContext);
                    continue;
                }
                if (headerParam != null) {
                    parameters[i] = new ParameterList.HeaderParamInjector(headerParam,
                            defValue, parameterType, genParamType, tlContext);
                    continue;
                }
                if (matrixParam != null) {
                    parameters[i] = new ParameterList.MatrixParamInjector(matrixParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveEncoded);
                    continue;
                }
                if (queryParam != null) {
                    parameters[i] = new ParameterList.QueryParamInjector(queryParam,
                            defValue, parameterType, genParamType, tlContext,
                            leaveEncoded);
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
            parameters[i] = new AbstractMethodWrapper.EntityGetter(
                    parameterType, genParamType, tlContext, entityProviders,
                    paramAnnos, logger);
            entityAlreadyRequired = true;
        }
    }

    /**
     * @param constr
     * @param tlContext
     * @param leaveEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param logger
     * @throws MissingAnnotationException
     */
    public ParameterList(Constructor<?> constr,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers, Logger logger)
            throws MissingAnnotationException {
        this(constr.getParameterTypes(), constr.getGenericParameterTypes(),
                constr.getParameterAnnotations(), tlContext, leaveEncoded,
                entityProviders, allCtxResolvers, true, false, logger);
    }

    /**
     * @param executeMethod
     * @param annotatedMethod
     * @param tlContext
     * @param leaveEncoded
     * @param entityProviders
     * @param allCtxResolvers
     * @param entityAllowed
     * @param logger
     * @throws MissingAnnotationException
     */
    ParameterList(Method executeMethod, Method annotatedMethod,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            boolean entityAllowed, Logger logger)
            throws MissingAnnotationException {
        this(executeMethod.getParameterTypes(), executeMethod
                .getGenericParameterTypes(), annotatedMethod
                .getParameterAnnotations(), tlContext, leaveEncoded,
                entityProviders, allCtxResolvers, true, entityAllowed, logger);
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