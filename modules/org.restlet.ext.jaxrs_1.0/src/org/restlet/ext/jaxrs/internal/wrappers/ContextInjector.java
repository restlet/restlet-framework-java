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
import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.getContextResolver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Helper class to inject into fields annotated with &#64;{@link Context}.
 * 
 * @author Stephan Koops
 * @see IntoRrcInjector
 */
public class ContextInjector {

    static class BeanSetter implements InjectionAim {

        private Method beanSetter;

        private BeanSetter(Method beanSetter) {
            this.beanSetter = beanSetter;
        }

        /**
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see ContextInjector.InjectionAim#injectInto(Object, Object)
         */
        public void injectInto(Object resource, Object toInject)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            Util.inject(resource, beanSetter, toInject);
        }

    }

    static class FieldWrapper implements InjectionAim {

        private Field field;

        private FieldWrapper(Field field) {
            this.field = field;
        }

        /**
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see ContextInjector.InjectionAim#injectInto(Object, Object)
         */
        public void injectInto(Object resource, Object toInject)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            Util.inject(resource, field, toInject);
        }
    }

    class InjectEverSame {
        private InjectionAim injectionAim;

        private Object injectable;

        private InjectEverSame(InjectionAim injectionAim, Object injectable) {
            this.injectionAim = injectionAim;
            this.injectable = injectable;
        }

        /**
         * @param resource
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see ContextInjector.InjectionAim#injectInto(Object, Object)
         */
        public void injectInto(Object resource)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            injectionAim.injectInto(resource, injectable);
        }
    }

    /**
     * Represents a field or a bean setter, where the runtime injects something
     * in.
     */
    static interface InjectionAim {

        /**
         * Inject the toInject into this field or bean setter on object
         * resource.
         * 
         * @param resource
         * @param toInject
         * @throws IllegalArgumentException
         * @throws InjectException
         * @throws InvocationTargetException
         * @see FieldWrapper#set(Object, Object)
         * @see Method#invoke(Object, Object...)
         */
        void injectInto(Object resource, Object toInject)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException;
    }

    /**
     * This array contains the fields and bean setter in the managed in which
     * are annotated to inject an {@link CallContext}.
     * 
     * @see UriInfo
     * @see SecurityContext
     * @see Request
     * @see HttpHeaders
     */
    private final List<InjectionAim> requDependAims = new ArrayList<InjectionAim>();

    /**
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext}.
     * 
     * @see javax.ws.rs.ext.ContextResolver
     * @see MessageBodyWorkers
     */
    private final List<InjectEverSame> injEverSameAims = new ArrayList<InjectEverSame>();

    /**
     * @param jaxRsClass
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @throws ImplementationException
     */
    public ContextInjector(Class<?> jaxRsClass, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers) {
        init(jaxRsClass, mbWorkers, allResolvers);
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     * 
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     */
    private void init(Class<?> jaxRsClass, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers) {
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Context.class)) {
                    InjectionAim aim = new FieldWrapper(field);
                    Class<?> declaringClass = field.getType();
                    if (declaringClass.equals(MessageBodyWorkers.class)) {
                        addMbwsAim(aim, mbWorkers);
                    } else if (declaringClass.equals(ContextResolver.class)) {
                        Type genericType = field.getGenericType();
                        addCtxRslvrAim(genericType, aim, allResolvers);
                    } else {
                        requDependAims.add(aim);
                    }
                }
            }
            for (Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, Context.class)) {
                    BeanSetter aim = new BeanSetter(method);
                    Class<?> paramClass = method.getParameterTypes()[0];
                    if (paramClass.equals(MessageBodyWorkers.class)) {
                        addMbwsAim(aim, mbWorkers);
                    } else if (paramClass.equals(ContextResolver.class)) {
                        Type genericType = method.getGenericParameterTypes()[0];
                        addCtxRslvrAim(genericType, aim, allResolvers);
                    } else {
                        requDependAims.add(aim);
                    }
                }
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }

    /**
     * @param genericType
     * @param aim
     * @param allResolvers
     */
    private void addCtxRslvrAim(Type genericType, InjectionAim aim,
            Collection<ContextResolver<?>> allResolvers) {
        ContextResolver<?> cr = getContextResolver(genericType, allResolvers);
        injEverSameAims.add(new InjectEverSame(aim, cr));
    }

    /**
     * @param aim
     * @param mbWorkers
     */
    private void addMbwsAim(InjectionAim aim, MessageBodyWorkers mbWorkers) {
        injEverSameAims.add(new InjectEverSame(aim, mbWorkers));
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param jaxRsResObj
     * @param tlContext
     *                The thread local wrapped CallContext to get the
     *                dependencies from.
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     * @throws InvocationTargetException
     *                 if a setter throws an exception
     */
    public void inject(Object jaxRsResObj, ThreadLocalizedContext tlContext)
            throws InjectException, InvocationTargetException {
        if (tlContext == null)
            throw new IllegalArgumentException("context must not be null");
        for (InjectionAim contextAim : this.requDependAims) {
            contextAim.injectInto(jaxRsResObj, tlContext);
        }
        for (InjectEverSame contextResolverAim : this.injEverSameAims) {
            contextResolverAim.injectInto(jaxRsResObj);
        }
    }
}