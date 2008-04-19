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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isBeanSetter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Helper class to inject into fields for &#64;*Param into root resource
 * classes.
 * 
 * @author Stephan Koops
 */
class IntoRrcInjector extends ContextInjector {

    // LATER refactor and use InjectionAim, 
    // take a look at sect 3.2 "Fields and Bean Properties" for Except handling
    
    private static final Logger localLogger = Logger.getAnonymousLogger();

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    /**
     * Fields of the wrapped JAX-RS resource class to inject a cookie parameter.
     */
    private Field[] cookieParamFields;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a cookie parameter.
     */
    private Method[] cookieParamSetters;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a header parameter.
     */
    private Field[] headerParamFields;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a header parameter.
     */
    private Method[] headerParamSetters;

    /**
     * is true, if the resource class is annotated with &#64;Path. Is available
     * after constructor was running.
     */
    private boolean leaveEncoded;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a matrix parameter.
     */
    private Field[] matrixParamFields;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a matrix parameter.
     */
    private Method[] matrixParamSetters;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a path parameter.
     */
    private Field[] pathParamFields;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a path parameter.
     */
    private Method[] pathParamSetters;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a query parameter.
     */
    private Field[] queryParamFields;

    /**
     * Fields of the wrapped JAX-RS resource class to inject a query parameter.
     */
    private Method[] queryParamSetters;

    /**
     * @param jaxRsClass
     * @param leaveEncoded
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     */
    IntoRrcInjector(Class<?> jaxRsClass, boolean leaveEncoded, MessageBodyWorkers mbWorkers, Collection<ContextResolver<?>> allResolvers) {
        super(jaxRsClass, mbWorkers, allResolvers);
        this.leaveEncoded = leaveEncoded;
        this.init(jaxRsClass);
    }

    /**
     * @param field
     * @return
     */
    private Class<?> getConvTo(Field field) {
        return field.getType();
    }

    /**
     * @param beanSetter
     * @return
     */
    private Class<?> getConvTo(Method beanSetter) {
        return beanSetter.getParameterTypes()[0];
    }

    /**
     * @param fieldOrBeanSetter
     * @param convTo
     * @param paramGenericType
     * @param callContext
     * @return
     * @throws ConvertCookieParamException
     */
    private Object getCookieParamValue(AccessibleObject fieldOrBeanSetter,
            Class<?> convTo, Type paramGenericType, CallContext callContext)
            throws ConvertCookieParamException {
        CookieParam headerParam = fieldOrBeanSetter
                .getAnnotation(CookieParam.class);
        DefaultValue defaultValue = fieldOrBeanSetter
                .getAnnotation(DefaultValue.class);
        Object value = WrapperUtil.getCookieParamValue(convTo,
                paramGenericType, headerParam, defaultValue, callContext);
        return value;
    }

    /**
     * @param qpf
     * @return
     */
    private Type getGenericType(Field qpf) {
        return qpf.getGenericType();
    }

    /**
     * @param qps
     * @return
     */
    private Type getGenericType(Method qps) {
        return qps.getGenericParameterTypes()[0];
    }

    /**
     * @param fieldOrBeanSetter
     * @param convTo
     * @param paramGenericType
     * @param callContext
     * @return
     * @throws ConvertHeaderParamException
     */
    private Object getHeaderParamValue(AccessibleObject fieldOrBeanSetter,
            Class<?> convTo, Type paramGenericType, CallContext callContext)
            throws ConvertHeaderParamException {
        HeaderParam headerParam = fieldOrBeanSetter
                .getAnnotation(HeaderParam.class);
        DefaultValue defaultValue = fieldOrBeanSetter
                .getAnnotation(DefaultValue.class);
        Object value = WrapperUtil.getHeaderParamValue(convTo,
                paramGenericType, headerParam, defaultValue, callContext);
        return value;
    }

    /**
     * @param fieldOrBeanSetter
     * @param convTo
     * @param paramGenericType
     * @param callContext
     * @return
     * @throws ConvertMatrixParamException
     */
    private Object getMatrixParamValue(AccessibleObject fieldOrBeanSetter,
            Class<?> convTo, Type paramGenericType, CallContext callContext)
            throws ConvertMatrixParamException {
        MatrixParam headerParam = fieldOrBeanSetter
                .getAnnotation(MatrixParam.class);
        DefaultValue defaultValue = fieldOrBeanSetter
                .getAnnotation(DefaultValue.class);
        // LATER Encoded of field or bean setter
        Object value = WrapperUtil.getMatrixParamValue(convTo,
                paramGenericType, headerParam, leaveEncoded, defaultValue,
                callContext);
        return value;
    }

    /**
     * @param fieldOrBeanSetter
     * @param convTo
     * @param paramGenericType
     * @param callContext
     * @return
     * @throws ConvertPathParamException
     */
    private Object getPathParamValue(AccessibleObject fieldOrBeanSetter,
            Class<?> convTo, Type paramGenericType, CallContext callContext)
            throws ConvertPathParamException {
        PathParam headerParam = fieldOrBeanSetter.getAnnotation(PathParam.class);
        DefaultValue defaultValue = fieldOrBeanSetter.getAnnotation(DefaultValue.class);
        // LATER Encoded of field or bean setter
        Object value = WrapperUtil.getPathParamValue(convTo, paramGenericType,
                headerParam, leaveEncoded, defaultValue, callContext);
        return value;
    }

    /**
     * @param fieldOrbeanSetter
     * @param convTo
     * @param paramGenericType
     * @param callContext
     * @return
     * @throws ConvertQueryParamException
     */
    private Object getQueryParamValue(AccessibleObject fieldOrbeanSetter,
            Class<?> convTo, Type paramGenericType, CallContext callContext)
            throws ConvertQueryParamException {
        QueryParam headerParam = fieldOrbeanSetter.getAnnotation(QueryParam.class);
        DefaultValue defaultValue = fieldOrbeanSetter.getAnnotation(DefaultValue.class);
        // LATER Encoded of field or bean setter
        Object value = WrapperUtil.getQueryParamValue(convTo, paramGenericType,
                headerParam, leaveEncoded, defaultValue, callContext, localLogger);
        return value;
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     */
    private void init(Class<?> jaxRsClass) {
        initFields(jaxRsClass);
        initSetters(jaxRsClass);
    }

    /**
     * @param jaxRsClass
     * @throws SecurityException
     */
    private void initFields(Class<?> jaxRsClass) throws SecurityException {
        List<Field> ifCookieParam = new ArrayList<Field>(1);
        List<Field> ifHeaderParam = new ArrayList<Field>(1);
        List<Field> ifMatrixParam = new ArrayList<Field>(1);
        List<Field> ifPathParam = new ArrayList<Field>(1);
        List<Field> ifQueryParam = new ArrayList<Field>(1);
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PathParam.class))
                    ifPathParam.add(field);
                else if (field.isAnnotationPresent(CookieParam.class))
                    ifCookieParam.add(field);
                else if (field.isAnnotationPresent(HeaderParam.class))
                    ifHeaderParam.add(field);
                else if (field.isAnnotationPresent(MatrixParam.class))
                    ifMatrixParam.add(field);
                else if (field.isAnnotationPresent(QueryParam.class))
                    ifQueryParam.add(field);
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
        this.cookieParamFields = ifCookieParam.toArray(EMPTY_FIELD_ARRAY);
        this.headerParamFields = ifHeaderParam.toArray(EMPTY_FIELD_ARRAY);
        this.matrixParamFields = ifMatrixParam.toArray(EMPTY_FIELD_ARRAY);
        this.pathParamFields = ifPathParam.toArray(EMPTY_FIELD_ARRAY);
        this.queryParamFields = ifQueryParam.toArray(EMPTY_FIELD_ARRAY);
    }

    /**
     * @param jaxRsClass
     * @throws SecurityException
     */
    private void initSetters(Class<?> jaxRsClass) throws SecurityException {
        List<Method> cookieParmSetters = new ArrayList<Method>(1);
        List<Method> headerParmSetters = new ArrayList<Method>(1);
        List<Method> matrixParmSetters = new ArrayList<Method>(1);
        List<Method> pathParamSetters = new ArrayList<Method>(1);
        List<Method> queryParamSetters = new ArrayList<Method>(1);
        do {
            for (Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, PathParam.class))
                    pathParamSetters.add(method);
                else if (isBeanSetter(method, CookieParam.class))
                    cookieParmSetters.add(method);
                else if (isBeanSetter(method, HeaderParam.class))
                    headerParmSetters.add(method);
                else if (isBeanSetter(method, MatrixParam.class))
                    matrixParmSetters.add(method);
                else if (isBeanSetter(method, QueryParam.class))
                    queryParamSetters.add(method);
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
        this.cookieParamSetters = cookieParmSetters.toArray(EMPTY_METHOD_ARRAY);
        this.headerParamSetters = headerParmSetters.toArray(EMPTY_METHOD_ARRAY);
        this.matrixParamSetters = matrixParmSetters.toArray(EMPTY_METHOD_ARRAY);
        this.pathParamSetters = pathParamSetters.toArray(EMPTY_METHOD_ARRAY);
        this.queryParamSetters = queryParamSetters.toArray(EMPTY_METHOD_ARRAY);
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param resourceObject
     * @param tlContext
     *                The thread local wrapped {@link CallContext}
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     * @throws ConvertCookieParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertQueryParamException
     * @throws InvocationTargetException
     *                 if a injector bean setter throws an exception
     */
    protected void inject(ResourceObject resourceObject,
            ThreadLocalizedContext tlContext) throws InjectException,
            ConvertCookieParamException, ConvertHeaderParamException,
            ConvertMatrixParamException, ConvertPathParamException,
            ConvertQueryParamException, InvocationTargetException {
        Object jaxRsResObj = resourceObject.getJaxRsResourceObject();
        super.inject(jaxRsResObj, tlContext);
        CallContext callContext = tlContext.get();
        for (Field cpf : this.cookieParamFields) {
            Object value = getCookieParamValue(cpf, getConvTo(cpf),
                    getGenericType(cpf), callContext);
            Util.inject(jaxRsResObj, cpf, value);
        }
        for (Method cps : this.cookieParamSetters) {
            Object value = getCookieParamValue(cps, getConvTo(cps),
                    getGenericType(cps), callContext);
            Util.inject(jaxRsResObj, cps, value);
        }
        for (Field hpf : this.headerParamFields) {
            Object value = getHeaderParamValue(hpf, getConvTo(hpf),
                    getGenericType(hpf), callContext);
            Util.inject(jaxRsResObj, hpf, value);
        }
        for (Method hps : this.headerParamSetters) {
            Object value = getHeaderParamValue(hps, getConvTo(hps),
                    getGenericType(hps), callContext);
            Util.inject(jaxRsResObj, hps, value);
        }
        for (Field mpf : this.matrixParamFields) {
            Object value = getMatrixParamValue(mpf, getConvTo(mpf),
                    getGenericType(mpf), callContext);
            Util.inject(jaxRsResObj, mpf, value);
        }
        for (Method mps : this.matrixParamSetters) {
            Object value = getMatrixParamValue(mps, getConvTo(mps),
                    getGenericType(mps), callContext);
            Util.inject(jaxRsResObj, mps, value);
        }
        for (Field ppf : this.pathParamFields) {
            Object value = getPathParamValue(ppf, getConvTo(ppf),
                    getGenericType(ppf), callContext);
            Util.inject(jaxRsResObj, ppf, value);
        }
        for (Method pps : this.pathParamSetters) {
            Object value = getPathParamValue(pps, getConvTo(pps),
                    getGenericType(pps), callContext);
            Util.inject(jaxRsResObj, pps, value);
        }
        for (Field qpf : this.queryParamFields) {
            Object value = getQueryParamValue(qpf, getConvTo(qpf),
                    getGenericType(qpf), callContext);
            Util.inject(jaxRsResObj, qpf, value);
        }
        for (Method qps : this.queryParamSetters) {
            Object value = getQueryParamValue(qps, getConvTo(qps),
                    getGenericType(qps), callContext);
            Util.inject(jaxRsResObj, qps, value);
        }
    }
}