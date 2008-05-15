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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.wrappers.ParameterList.CookieParamInjector;
import org.restlet.ext.jaxrs.internal.wrappers.ParameterList.HeaderParamInjector;
import org.restlet.ext.jaxrs.internal.wrappers.ParameterList.MatrixParamInjector;
import org.restlet.ext.jaxrs.internal.wrappers.ParameterList.PathParamInjector;
import org.restlet.ext.jaxrs.internal.wrappers.ParameterList.QueryParamInjector;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * Helper class to inject into fields for &#64;*Param into root resource
 * classes.
 * 
 * @author Stephan Koops
 */
class IntoRrcInjector extends ContextInjector {

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
    IntoRrcInjector(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            boolean leaveEncoded, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        super(jaxRsClass, tlContext, mbWorkers, allResolvers,
                extensionBackwardMapping);
        this.init(jaxRsClass, tlContext, leaveEncoded);
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     * 
     * @param jaxRsClass
     * @param tlContext
     * @param leaveEncoded
     */
    private void init(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            boolean leaveEncoded) {
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PathParam.class))
                    add(new PathParamInjector(field, tlContext, leaveEncoded));
                else if (field.isAnnotationPresent(CookieParam.class))
                    add(new CookieParamInjector(field, tlContext));
                else if (field.isAnnotationPresent(HeaderParam.class))
                    add(new HeaderParamInjector(field, tlContext));
                else if (field.isAnnotationPresent(MatrixParam.class))
                    add(new MatrixParamInjector(field, tlContext, leaveEncoded));
                else if (field.isAnnotationPresent(QueryParam.class))
                    add(new QueryParamInjector(field, tlContext, leaveEncoded));
            }
            for (Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, PathParam.class))
                    add(new PathParamInjector(method, tlContext, leaveEncoded));
                else if (isBeanSetter(method, CookieParam.class))
                    add(new CookieParamInjector(method, tlContext));
                else if (isBeanSetter(method, HeaderParam.class))
                    add(new HeaderParamInjector(method, tlContext));
                else if (isBeanSetter(method, MatrixParam.class))
                    add(new MatrixParamInjector(method, tlContext, leaveEncoded));
                else if (isBeanSetter(method, QueryParam.class))
                    add(new QueryParamInjector(method, tlContext, leaveEncoded));
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }
}