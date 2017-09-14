/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs.internal.wrappers.params;

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isBeanSetter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Providers;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
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
     * @param leaveClassEncoded
     * @param allProviders
     *            all entity providers.
     * @param extensionBackwardMapping
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalPathParamTypeException
     */
    public IntoRrcInjector(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, boolean leaveClassEncoded,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalFieldTypeException, IllegalBeanSetterTypeException,
            IllegalPathParamTypeException {
        super(jaxRsClass, tlContext, allProviders, extensionBackwardMapping);
        init(jaxRsClass, tlContext, leaveClassEncoded);
    }

    private Type getConvGenTo(AccessibleObject fieldOrBeanSetter) {
        if (fieldOrBeanSetter instanceof Field) {
            final Field field = ((Field) fieldOrBeanSetter);
            return field.getGenericType();
        } else if (fieldOrBeanSetter instanceof Method) {
            final Method beanSetter = ((Method) fieldOrBeanSetter);
            return beanSetter.getGenericParameterTypes()[0];
        } else {
            throw new IllegalArgumentException(
                    "The fieldOrBeanSetter must be a Field or a method");
        }
    }

    private Class<?> getConvTo(AccessibleObject fieldOrBeanSetter) {
        if (fieldOrBeanSetter instanceof Field) {
            final Field field = ((Field) fieldOrBeanSetter);
            return field.getType();
        } else if (fieldOrBeanSetter instanceof Method) {
            final Method beanSetter = ((Method) fieldOrBeanSetter);
            return beanSetter.getParameterTypes()[0];
        } else {
            throw new IllegalArgumentException(
                    "The fieldOrBeanSetter must be a Field or a method");
        }
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     * 
     * @param lcEnc
     *            leave class encoded
     * @throws IllegalPathParamTypeException
     */
    private void init(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            boolean lcEnc) throws IllegalPathParamTypeException {
        do {
            for (final Field field : jaxRsClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(PathParam.class)) {
                    add(field, newPathParamGetter(field, tlContext, lcEnc));
                } else if (field.isAnnotationPresent(CookieParam.class)) {
                    add(field, newCookieParamGetter(field, tlContext, lcEnc));
                } else if (field.isAnnotationPresent(HeaderParam.class)) {
                    add(field, newHeaderParamGetter(field, tlContext, lcEnc));
                } else if (field.isAnnotationPresent(MatrixParam.class)) {
                    add(field, newMatrixParamGetter(field, tlContext, lcEnc));
                } else if (field.isAnnotationPresent(QueryParam.class)) {
                    add(field, newQueryParamGetter(field, tlContext, lcEnc));
                }
            }
            for (final Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, PathParam.class)) {
                    add(method, newPathParamGetter(method, tlContext, lcEnc));
                } else if (isBeanSetter(method, CookieParam.class)) {
                    add(method, newCookieParamGetter(method, tlContext, lcEnc));
                } else if (isBeanSetter(method, HeaderParam.class)) {
                    add(method, newHeaderParamGetter(method, tlContext, lcEnc));
                } else if (isBeanSetter(method, MatrixParam.class)) {
                    add(method, newMatrixParamGetter(method, tlContext, lcEnc));
                } else if (isBeanSetter(method, QueryParam.class)) {
                    add(method, newQueryParamGetter(method, tlContext, lcEnc));
                }
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }

    private CookieParamGetter newCookieParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean annoSaysLeaveClassEncoded) {
        return new CookieParamGetter(
                fieldOrBeanSetter.getAnnotation(CookieParam.class),
                fieldOrBeanSetter.getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, annoSaysLeaveClassEncoded);
    }

    private HeaderParamGetter newHeaderParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean annoSaysLeaveClassEncoded) {
        return new HeaderParamGetter(
                fieldOrBeanSetter.getAnnotation(HeaderParam.class),
                fieldOrBeanSetter.getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, annoSaysLeaveClassEncoded);
    }

    private MatrixParamGetter newMatrixParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean leaveClassEncoded) {
        return new MatrixParamGetter(
                fieldOrBeanSetter.getAnnotation(MatrixParam.class),
                fieldOrBeanSetter.getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, leaveClassEncoded
                        || fieldOrBeanSetter.isAnnotationPresent(Encoded.class));
    }

    private PathParamGetter newPathParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean leaveClassEncoded)
            throws IllegalPathParamTypeException {
        return new PathParamGetter(
                fieldOrBeanSetter.getAnnotation(PathParam.class),
                fieldOrBeanSetter.getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, leaveClassEncoded
                        || fieldOrBeanSetter.isAnnotationPresent(Encoded.class));
    }

    private QueryParamGetter newQueryParamGetter(
            AccessibleObject fieldOrBeanSetter,
            ThreadLocalizedContext tlContext, boolean leaveClassEncoded) {
        return new QueryParamGetter(
                fieldOrBeanSetter.getAnnotation(QueryParam.class),
                fieldOrBeanSetter.getAnnotation(DefaultValue.class),
                getConvTo(fieldOrBeanSetter), getConvGenTo(fieldOrBeanSetter),
                tlContext, leaveClassEncoded
                        || fieldOrBeanSetter.isAnnotationPresent(Encoded.class));
    }
}
