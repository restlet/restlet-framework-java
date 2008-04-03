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

import java.lang.reflect.Field;
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
import org.restlet.ext.jaxrs.internal.wrappers.ContextResolver;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWorkers;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.ext.jaxrs.internal.core.CallContext;
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

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a cookie parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsCookieParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a header parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsHeaderParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a matrix parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsMatrixParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a path parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsPathParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a query parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsQueryParam;

    /**
     * is true, if the resource class is annotated with &#64;Path. Is available
     * after constructor was running.
     */
    private boolean leaveEncoded;

    /**
     * @param jaxRsClass
     * @param leaveEncoded
     */
    IntoRrcInjector(Class<?> jaxRsClass, boolean leaveEncoded) {
        super(jaxRsClass);
        this.leaveEncoded = leaveEncoded;
        this.init(jaxRsClass);
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param resourceObject
     * @param callContext
     *                The CallContext to get the dependencies from.
     * @param allResolv
     *                all available wrapped
     *                {@link javax.ws.rs.ext.ContextResolver}s.
     * @param messageBodyWorkers
     *                the {@link MessageBodyReader}s and
     *                {@link MessageBodyWriter}s.
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     * @throws ConvertCookieParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertQueryParamException
     */
    protected void inject(ResourceObject resourceObject,
            CallContext callContext, Collection<ContextResolver<?>> allResolv,
            MessageBodyWorkers messageBodyWorkers) throws InjectException,
            ConvertCookieParamException, ConvertHeaderParamException,
            ConvertMatrixParamException, ConvertPathParamException,
            ConvertQueryParamException {
        Object jaxRsResObj = resourceObject.getJaxRsResourceObject();
        super.inject(jaxRsResObj, callContext, allResolv, messageBodyWorkers);
        for (Field cpf : this.injectFieldsCookieParam) {
            CookieParam headerParam = cpf.getAnnotation(CookieParam.class);
            DefaultValue defaultValue = cpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = cpf.getType();
            Type paramGenericType = cpf.getGenericType();
            Object value = WrapperUtil.getCookieParamValue(convTo,
                    paramGenericType, headerParam, defaultValue, callContext);
            Util.inject(jaxRsResObj, cpf, value);
        }
        for (Field hpf : this.injectFieldsHeaderParam) {
            HeaderParam headerParam = hpf.getAnnotation(HeaderParam.class);
            DefaultValue defaultValue = hpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = hpf.getType();
            Type paramGenericType = hpf.getGenericType();
            Object value = WrapperUtil.getHeaderParamValue(convTo,
                    paramGenericType, headerParam, defaultValue, callContext);
            Util.inject(jaxRsResObj, hpf, value);
        }
        for (Field mpf : this.injectFieldsMatrixParam) {
            MatrixParam headerParam = mpf.getAnnotation(MatrixParam.class);
            DefaultValue defaultValue = mpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = mpf.getType();
            Type paramGenericType = mpf.getGenericType();
            Object value = WrapperUtil.getMatrixParamValue(convTo,
                    paramGenericType, headerParam, leaveEncoded, defaultValue,
                    callContext);
            Util.inject(jaxRsResObj, mpf, value);
        }
        for (Field ppf : this.injectFieldsPathParam) {
            PathParam headerParam = ppf.getAnnotation(PathParam.class);
            // REQUESTED forbid @DefaultValue on @PathParam
            Class<?> convTo = ppf.getType();
            Type paramGenericType = ppf.getGenericType();
            Object value = WrapperUtil.getPathParamValue(convTo,
                    paramGenericType, headerParam, leaveEncoded, callContext);
            Util.inject(jaxRsResObj, ppf, value);
        }
        for (Field cpf : this.injectFieldsQueryParam) {
            QueryParam headerParam = cpf.getAnnotation(QueryParam.class);
            DefaultValue defaultValue = cpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = cpf.getType();
            Type paramGenericType = cpf.getGenericType();
            Object value = WrapperUtil.getQueryParamValue(convTo,
                    paramGenericType, headerParam, leaveEncoded, defaultValue,
                    callContext, Logger.getAnonymousLogger());
            Util.inject(jaxRsResObj, cpf, value);
        }
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     */
    private void init(Class<?> jaxRsClass) {
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
        this.injectFieldsCookieParam = ifCookieParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsHeaderParam = ifHeaderParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsMatrixParam = ifMatrixParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsPathParam = ifPathParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsQueryParam = ifQueryParam.toArray(EMPTY_FIELD_ARRAY);
    }
}