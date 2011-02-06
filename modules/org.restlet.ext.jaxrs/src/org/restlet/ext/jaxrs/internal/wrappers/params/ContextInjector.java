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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isBeanSetter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.restlet.ext.jaxrs.ExtendedUriInfo;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedExtendedUriInfo;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.AbstractParamGetter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * Helper class to inject into fields annotated with &#64;{@link Context}.
 * 
 * @author Stephan Koops
 * @see IntoRrcInjector
 */
public class ContextInjector {

    static class BeanSetter implements InjectionAim {

        private final Method beanSetter;

        private BeanSetter(Method beanSetter) {
            this.beanSetter = beanSetter;
            this.beanSetter.setAccessible(true);
        }

        /**
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see InjectionAim#injectInto(Object, Object, boolean)
         */
        public void injectInto(Object resource, Object toInject,
                boolean allMustBeAvailable) throws IllegalArgumentException,
                InjectException, InvocationTargetException {
            Util.inject(resource, this.beanSetter, toInject);
        }

    }

    /**
     * {@link Injector}, that injects the same object in every resource. Is is
     * used for the &#64;{@link Context} objects.
     */
    private static class EverSameInjector implements Injector {

        private final Object injectable;

        private final InjectionAim injectionAim;

        private EverSameInjector(InjectionAim injectionAim, Object injectable) {
            this.injectionAim = injectionAim;
            this.injectable = injectable;
        }

        /**
         * @see Injector#injectInto(java.lang.Object, boolean)
         */
        public void injectInto(Object resource, boolean allMustBeAvailable)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            this.injectionAim.injectInto(resource, this.injectable,
                    allMustBeAvailable);
        }
    }

    static class FieldWrapper implements InjectionAim {

        private final Field field;

        private FieldWrapper(Field field) {
            this.field = field;
            this.field.setAccessible(true);
        }

        /**
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see InjectionAim#injectInto(Object, Object, boolean)
         */
        public void injectInto(Object resource, Object toInject,
                boolean allMustBeAvailable) throws IllegalArgumentException,
                InjectException, InvocationTargetException {
            Util.inject(resource, this.field, toInject);
        }
    }

    private static class GetLastPathSegment implements PathSegment {

        private final ThreadLocalizedContext tlContext;

        GetLastPathSegment(ThreadLocalizedContext tlContext) {
            this.tlContext = tlContext;
        }

        private PathSegment getLast() {
            final List<PathSegment> pss = this.tlContext.getPathSegments();
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
         * @param allMustBeAvailable
         * @throws IllegalArgumentException
         * @throws InjectException
         * @throws InvocationTargetException
         * @see FieldWrapper#set(Object, Object)
         * @see Method#invoke(Object, Object...)
         */
        void injectInto(Object resource, Object toInject,
                boolean allMustBeAvailable) throws IllegalArgumentException,
                InjectException, InvocationTargetException;
    }

    static interface Injector {

        /**
         * @param resource
         * @param allMustBeAvailable
         * @throws InvocationTargetException
         * @throws InjectException
         * @throws IllegalArgumentException
         * @see InjectionAim#injectInto(Object, Object, boolean)
         */
        public abstract void injectInto(Object resource,
                boolean allMustBeAvailable) throws IllegalArgumentException,
                InjectException, InvocationTargetException;

    }

    private class ParamValueInjector implements Injector {

        private final AccessibleObject fieldOrBeanSetter;

        private final AbstractParamGetter iog;

        ParamValueInjector(AccessibleObject fieldOrBeanSetter,
                AbstractParamGetter iog) {
            this.fieldOrBeanSetter = fieldOrBeanSetter;
            this.fieldOrBeanSetter.setAccessible(true);
            this.iog = iog;
        }

        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.params.ContextInjector.Injector#injectInto(java.lang.Object,
         *      boolean)
         */
        public void injectInto(Object resource, boolean allMustBeAvailable)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            Util.inject(resource, this.fieldOrBeanSetter, this.iog
                    .getParamValue());
        }
    }

    /**
     * @author Stephan Koops
     */
    private static final class UriInfoInjector implements Injector {

        private final InjectionAim aim;

        private final ThreadLocalizedUriInfo uriInfo;

        UriInfoInjector(InjectionAim aim, ThreadLocalizedContext tlContext) {
            this.aim = aim;
            this.uriInfo = new ThreadLocalizedUriInfo(tlContext);
        }

        public void injectInto(Object resource, boolean allMustBeAvailable)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            this.uriInfo.saveStateForCurrentThread(allMustBeAvailable);
            this.aim.injectInto(resource, this.uriInfo, allMustBeAvailable);
        }
    }

    /**
     * @author Stephan Koops
     */
    private static final class ExtendedUriInfoInjector implements Injector {

        private final InjectionAim aim;

        private final ThreadLocalizedExtendedUriInfo uriInfo;

        ExtendedUriInfoInjector(InjectionAim aim,
                ThreadLocalizedContext tlContext) {
            this.aim = aim;
            this.uriInfo = new ThreadLocalizedExtendedUriInfo(tlContext);
        }

        public void injectInto(Object resource, boolean allMustBeAvailable)
                throws IllegalArgumentException, InjectException,
                InvocationTargetException {
            this.uriInfo.saveStateForCurrentThread(allMustBeAvailable);
            this.aim.injectInto(resource, this.uriInfo, allMustBeAvailable);
        }
    }

    private static Logger logger = org.restlet.Context.getCurrentLogger();

    /**
     * @param declaringClass
     *            the class / interface to injecto into; must not be
     *            {@link UriInfo}
     * @param tlContext
     * @param providers
     * @param extensionBackwardMapping
     * @param aim
     * @return
     * @throws IllegalTypeException
     *             if the given class is not valid to be annotated with &#64;
     *             {@link Context}.
     * @throws ImplementationException
     *             the declaringClass must not be {@link UriInfo}
     */
    static Object getInjectObject(Class<?> declaringClass,
            ThreadLocalizedContext tlContext, Providers providers,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalTypeException, ImplementationException {
        if (declaringClass.equals(Providers.class)) {
            return providers;
        }
        if (declaringClass.equals(ContextResolver.class)) {
            // NICE also throw, where the error occurs.
            throw new IllegalTypeException(
                    "The ContextResolver is not allowed for @Context annotated fields yet. Use javax.ws.rs.ext.Providers#getContextResolver(...)");
        }
        if (declaringClass.equals(ExtensionBackwardMapping.class)) {
            return extensionBackwardMapping;
        }
        if (declaringClass.equals(PathSegment.class)) {
            String msg = "The use of PathSegment annotated with @Context is not standard.";
            logger.config(msg);
            return new GetLastPathSegment(tlContext);
        }
        if (declaringClass.equals(SecurityContext.class)
                || declaringClass.equals(HttpHeaders.class)
                || declaringClass.equals(Request.class)) {
            return tlContext;
        }
        if (declaringClass.equals(UriInfo.class)) {
            throw new ImplementationException(
                    "You must not call the method ContextInjector.getInjectObject(.......) with class UriInfo");
        }
        String declaringClassName = declaringClass.getName();
        // compare names to avoid ClassNotFoundExceptions, if the Servlet-API is
        // not in the classpath
        if (declaringClassName.equals("javax.servlet.http.HttpServletRequest")
                || declaringClassName
                        .equals("javax.servlet.http.HttpServletResponse")) {
            throw new NotYetImplementedException(
                    "The returnin of Servlet depending Context is not implemented for now.");
        }
        // NICE also allow injection of ClientInfo and Conditions. Proxies are
        // required, because the injected objects must be thread local.
        throw new IllegalTypeException(declaringClass
                + " must not be annotated with @Context");
    }

    /**
     * 
     * @param declaringClass
     * @param aim
     * @param tlContext
     * @param allProviders
     * @param extensionBackwardMapping
     * @return
     * @throws IllegalTypeException
     *             if the given class is not valid to be annotated with &#64;
     *             {@link Context}.
     */
    static Injector getInjector(Class<?> declaringClass, InjectionAim aim,
            ThreadLocalizedContext tlContext, Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalTypeException {
        if (declaringClass.equals(UriInfo.class)) {
            return new UriInfoInjector(aim, tlContext);
        }
        if (declaringClass.equals(ExtendedUriInfo.class)) {
            return new ExtendedUriInfoInjector(aim, tlContext);
        }

        return new EverSameInjector(aim, getInjectObject(declaringClass,
                tlContext, allProviders, extensionBackwardMapping));
    }

    /**
     * This {@link List} contains the fields in this class which are annotated
     * to inject ever the same object.
     * 
     * @see javax.ws.rs.ext.ContextResolver
     * @see Providers
     */
    private final List<Injector> injEverSameAims = new ArrayList<Injector>();

    /**
     * @param jaxRsClass
     * @param tlContext
     * @param providers
     *            all entity providers.
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * @throws IllegalBeanSetterTypeException
     *             if one of the bean setters annotated with &#64;
     *             {@link Context} has a type that must not be annotated with
     *             &#64;{@link Context}.
     * @throws IllegalFieldTypeException
     *             if one of the fields annotated with &#64;{@link Context} has
     *             a type that must not be annotated with &#64;{@link Context}.
     * @throws ImplementationException
     */
    public ContextInjector(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, Providers providers,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalFieldTypeException, IllegalBeanSetterTypeException {
        this.init(jaxRsClass, tlContext, providers, extensionBackwardMapping);
    }

    protected void add(AccessibleObject fieldOrBeanSetter,
            AbstractParamGetter iog) {
        this.injEverSameAims
                .add(new ParamValueInjector(fieldOrBeanSetter, iog));
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     * 
     * @param tlContext
     *            the {@link ThreadLocalizedContext} of the
     *            {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param allProviders
     *            all entity providers.
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * 
     * @throws IllegalFieldTypeException
     *             if one of the fields annotated with &#64;{@link Context} has
     *             a type that must not be annotated with &#64;{@link Context}.
     * @throws IllegalBeanSetterTypeException
     *             if one of the bean setters annotated with &#64;
     *             {@link Context} has a type that must not be annotated with
     *             &#64;{@link Context}.
     */
    private void init(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalFieldTypeException, IllegalBeanSetterTypeException {
        do {
            try {
                for (final Field field : jaxRsClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Context.class)) {
                        InjectionAim aim = new FieldWrapper(field);
                        Class<?> declaringClass = field.getType();
                        Injector injector = getInjector(declaringClass, aim,
                                tlContext, allProviders,
                                extensionBackwardMapping);
                        this.injEverSameAims.add(injector);
                    }
                }
            } catch (SecurityException e) {
                // NICE handle SecurityException
                throw e;
            } catch (IllegalTypeException e) {
                throw new IllegalFieldTypeException(e);
            }
            try {
                for (final Method method : jaxRsClass.getDeclaredMethods()) {
                    if (isBeanSetter(method, Context.class)) {
                        BeanSetter aim = new BeanSetter(method);
                        Class<?> paramClass = method.getParameterTypes()[0];
                        Injector injector = getInjector(paramClass, aim,
                                tlContext, allProviders,
                                extensionBackwardMapping);
                        this.injEverSameAims.add(injector);
                    }
                }
            } catch (SecurityException e) {
                // NICE handle SecurityException
                throw e;
            } catch (IllegalTypeException e) {
                throw new IllegalBeanSetterTypeException(e);
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param jaxRsResObj
     * @param allMustBeAvailable
     *            if true, all information in &#64;{@link Context} annotated
     *            objects must be available, especially the ancestor resource
     *            info (false for singelton lifecycle)
     * @throws InjectException
     *             if the injection was not possible. See
     *             {@link InjectException#getCause()} for the reason.
     * @throws InvocationTargetException
     *             if a setter throws an exception
     */
    public void injectInto(Object jaxRsResObj, boolean allMustBeAvailable)
            throws InjectException, InvocationTargetException {
        for (final Injector injectAim : this.injEverSameAims) {
            injectAim.injectInto(jaxRsResObj, allMustBeAvailable);
        }
    }
}