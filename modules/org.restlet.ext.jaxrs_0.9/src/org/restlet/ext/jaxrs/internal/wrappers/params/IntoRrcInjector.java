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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isBeanSetter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.CookieParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.HeaderParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.MatrixParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.PathParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.QueryParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * Helper class to inject into fields for &#64;*Param into root resource
 * classes.
 * 
 * @author Stephan Koops
 */
public class IntoRrcInjector extends ContextInjector {

    /**
     * @param jaxRsClass
     * @param tlContext
     * @param leaveEncoded
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     */
    public IntoRrcInjector(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, boolean leaveEncoded,
            MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        super(jaxRsClass, tlContext, mbWorkers, allResolvers,
                extensionBackwardMapping);
        this.init(jaxRsClass, tlContext, leaveEncoded);
    }

    private Type getConvGenTo(AccessibleObject fieldOrBeanSetter) {
        if (fieldOrBeanSetter instanceof Field) {
            Field field = ((Field) fieldOrBeanSetter);
            return field.getGenericType();
        } else if (fieldOrBeanSetter instanceof Method) {
            Method beanSetter = ((Method) fieldOrBeanSetter);
            return beanSetter.getGenericParameterTypes()[0];
        } else {
            throw new IllegalArgumentException(
                    "The fieldOrBeanSetter must be a Field or a method");
        }
    }

    private Class<?> getConvTo(AccessibleObject fieldOrBeanSetter) {
        if (fieldOrBeanSetter instanceof Field) {
            Field field = ((Field) fieldOrBeanSetter);
            return field.getType();
        } else if (fieldOrBeanSetter instanceof Method) {
            Method beanSetter = ((Method) fieldOrBeanSetter);
            return beanSetter.getParameterTypes()[0];
        } else {
            throw new IllegalArgumentException(
                    "The fieldOrBeanSetter must be a Field or a method");
        }
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     */
    private void init(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            boolean leaveEncoded) {
        boolean encode = !leaveEncoded;
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PathParam.class))
                    add(field, newPathParamGetter(field, tlContext, encode));
                else if (field.isAnnotationPresent(CookieParam.class))
                    add(field, newCookieParamGetter(field, tlContext,
                            leaveEncoded));
                else if (field.isAnnotationPresent(HeaderParam.class))
                    add(field, newHeaderParamGetter(field, tlContext,
                            leaveEncoded));
                else if (field.isAnnotationPresent(MatrixParam.class))
                    add(field, newMatrixParamGetter(field, tlContext, encode));
                else if (field.isAnnotationPresent(QueryParam.class))
                    add(field, newQueryParamGetter(field, tlContext, encode));
            }
            for (Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, PathParam.class))
                    add(method, newPathParamGetter(method, tlContext, encode));
                else if (isBeanSetter(method, CookieParam.class))
                    add(method, newCookieParamGetter(method, tlContext,
                            leaveEncoded));
                else if (isBeanSetter(method, HeaderParam.class))
                    add(method, newHeaderParamGetter(method, tlContext,
                            leaveEncoded));
                else if (isBeanSetter(method, MatrixParam.class))
                    add(method, newMatrixParamGetter(method, tlContext, encode));
                else if (isBeanSetter(method, QueryParam.class))
                    add(method, newQueryParamGetter(method, tlContext, encode));
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }

    private CookieParamGetter newCookieParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean annoSaysLeaveEncoded) {
        return new CookieParamGetter(fieldOrBeanSetter
                .getAnnotation(CookieParam.class), fieldOrBeanSetter
                .getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, annoSaysLeaveEncoded);
    }

    private HeaderParamGetter newHeaderParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean annoSaysLeaveEncoded) {
        return new HeaderParamGetter(fieldOrBeanSetter
                .getAnnotation(HeaderParam.class), fieldOrBeanSetter
                .getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, annoSaysLeaveEncoded);
    }

    private MatrixParamGetter newMatrixParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean encode) {
        return new MatrixParamGetter(fieldOrBeanSetter
                .getAnnotation(MatrixParam.class), fieldOrBeanSetter
                .getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, encode);
    }

    private PathParamGetter newPathParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean encode) {
        return new PathParamGetter(fieldOrBeanSetter
                .getAnnotation(PathParam.class), fieldOrBeanSetter
                .getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, encode);
    }

    private QueryParamGetter newQueryParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean encode) {
        return new QueryParamGetter(fieldOrBeanSetter
                .getAnnotation(QueryParam.class), fieldOrBeanSetter
                .getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, encode);
    }
}