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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.getContextResolver;
import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isBeanSetter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

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

    /**
     * {@link Injector}, that injects the same object in every resource. Is is
     * used for the &#64;{@link Context} objects.
     */
    private static class EverSameInjector implements Injector {
        private Object injectable;

        private InjectionAim injectionAim;

        private EverSameInjector(InjectionAim injectionAim, Object injectable) {
            this.injectionAim = injectionAim;
            this.injectable = injectable;
        }

        /**
         * @see Injector#injectInto(java.lang.Object)
         */
        public void injectInto(Object resource)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            injectionAim.injectInto(resource, injectable);
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

    private static class GetLastPathSegment implements PathSegment {

        private ThreadLocalizedContext tlContext;

        GetLastPathSegment(ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
        }

        private PathSegment getLast() {
            List<PathSegment> pss = tlContext.getPathSegments();
            return pss.get(pss.size() - 1);
        }

        /**
         * @see javax.ws.rs.core.PathSegment#getMatrixParameters()
         */
        public MultivaluedMap<String, String> getMatrixParameters() {
            return getLast().getMatrixParameters();
        }

        /**
         * @see javax.ws.rs.core.PathSegment#getPath()
         */
        public String getPath() {
            return getLast().getPath();
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

    static interface Injector {

        /**
         * @param resource
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see ContextInjector.InjectionAim#injectInto(Object, Object)
         */
        public abstract void injectInto(Object resource)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException;

    }

    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * @param declaringClass
     * @param genericType
     * @param tlContext
     * @param mbWorkers
     * @param allCtxResolvers
     * @param extensionBackwardMapping
     * @param aim
     * @return
     */
    static Object getInjectObject(Class<?> declaringClass, Type genericType,
            ThreadLocalizedContext tlContext, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        if (declaringClass.equals(MessageBodyWorkers.class))
            return mbWorkers;
        if (declaringClass.equals(ContextResolver.class))
            return getContextResolver(genericType, allCtxResolvers);
        if (declaringClass.equals(ExtensionBackwardMapping.class))
            return extensionBackwardMapping;
        if (declaringClass.equals(PathSegment.class)) {
            String msg = "The use of PathSegment annotated with @Context is not standard.";
            logger.config(msg);
            return new GetLastPathSegment(tlContext);
        }
        // NICE also allow for ClientInfo und Conditions. Proxies needed for
        // fields and bean setters.
        return tlContext;
    }

    static EverSameInjector getInjector(Class<?> declaringClass,
            Type genericType, InjectionAim aim,
            ThreadLocalizedContext tlContext, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        return new EverSameInjector(aim, getInjectObject(declaringClass,
                genericType, tlContext, mbWorkers, allResolvers,
                extensionBackwardMapping));
    }

    /**
     * This {@link List} contains the fields in this class which are annotated
     * to inject ever the same object.
     * 
     * @see javax.ws.rs.ext.ContextResolver
     * @see MessageBodyWorkers
     */
    private final List<Injector> injEverSameAims = new ArrayList<Injector>();

    /**
     * @param jaxRsClass
     * @param tlContext
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @throws ImplementationException
     */
    public ContextInjector(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        this.init(jaxRsClass, tlContext, mbWorkers, allResolvers,
                extensionBackwardMapping);
    }

    protected void add(Injector injector) {
        this.injEverSameAims.add(injector);
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     * 
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link JaxRsRouter}.
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     */
    private void init(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping) {
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Context.class)) {
                    InjectionAim aim = new FieldWrapper(field);
                    Class<?> declaringClass = field.getType();
                    Type genericType = field.getGenericType();
                    EverSameInjector injector = getInjector(declaringClass,
                            genericType, aim, tlContext, mbWorkers,
                            allResolvers, extensionBackwardMapping);
                    injEverSameAims.add(injector);
                }
            }
            for (Method method : jaxRsClass.getDeclaredMethods()) {
                if (isBeanSetter(method, Context.class)) {
                    BeanSetter aim = new BeanSetter(method);
                    Class<?> paramClass = method.getParameterTypes()[0];
                    Type genericType = method.getGenericParameterTypes()[0];
                    EverSameInjector injector = getInjector(paramClass,
                            genericType, aim, tlContext, mbWorkers,
                            allResolvers, extensionBackwardMapping);
                    injEverSameAims.add(injector);
                }
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param jaxRsResObj
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     * @throws InvocationTargetException
     *                 if a setter throws an exception
     */
    public void injectInto(Object jaxRsResObj) throws InjectException,
            InvocationTargetException {
        for (Injector contextResolverAim : this.injEverSameAims) {
            contextResolverAim.injectInto(jaxRsResObj);
        }
    }
}