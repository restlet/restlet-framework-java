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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * Instances represents a root resource class.
 * 
 * A Java class that uses JAX-RS annotations to implement a corresponding Web
 * resource, see chapter 3 of JAX-RS specification
 * 
 * @author Stephan Koops
 */
public class ResourceClass extends AbstractJaxRsWrapper {

    /**
     * Caches the allowed methods (unmodifiable) for given remainingParts.
     */
    private final Map<RemainingPath, Set<org.restlet.data.Method>> allowedMethods = new HashMap<RemainingPath, Set<org.restlet.data.Method>>();

    protected final Class<?> jaxRsClass;

    /**
     * is true, if the resource class is annotated with &#64;Path. Is available
     * after constructor was running.
     */
    private final boolean leaveEncoded;

    private Collection<SubResourceLocator> subResourceLocators;

    private Collection<ResourceMethod> subResourceMethods;

    private Collection<ResourceMethodOrLocator> subResourceMethodsAndLocators;

    /**
     * Creates a new root resource class wrapper. Will not set the path, because
     * it is not available for a normal resource class.
     * 
     * @param jaxRsClass
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link JaxRsRouter}.
     * @param entityProviders
     *                all entity providers
     * @param allCtxResolvers
     *                all ContextResolvers
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                The logger to log warnings, if the class is not valid.
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     * @see WrapperFactory#getResourceClass(Class)
     */
    ResourceClass(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers, ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException {
        super();
        this.leaveEncoded = jaxRsClass.isAnnotationPresent(Encoded.class);
        this.jaxRsClass = jaxRsClass;
        this.initResourceMethodsAndLocators(tlContext, entityProviders,
                allCtxResolvers, extensionBackwardMapping, logger);
    }

    /**
     * Creates a new root resource class wrapper.
     * 
     * @param jaxRsClass
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link JaxRsRouter}.
     * @param entityProviders
     *                all entity providers
     * @param allCtxResolvers
     *                all ContextResolvers
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     * @param sameLogger
     *                the subclass RootResourceClass must call this constructor.
     *                This Object is ignored.
     * @throws IllegalArgumentException
     * @throws IllegalPathOnClassException
     * @throws MissingAnnotationException
     *                 if &#64;{@link Path} is missing on the jaxRsClass
     * @see WrapperFactory#getResourceClass(Class)
     */
    protected ResourceClass(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers, ExtensionBackwardMapping extensionBackwardMapping,
            Logger logger, @SuppressWarnings("unused")
            Logger sameLogger) throws IllegalArgumentException,
            IllegalPathOnClassException, MissingAnnotationException {
        super(PathRegExp.createForClass(jaxRsClass));
        this.leaveEncoded = false; // LATER de/encode: leaveEncoded = false ?
        this.jaxRsClass = jaxRsClass;
        this.initResourceMethodsAndLocators(tlContext, entityProviders,
                allCtxResolvers, extensionBackwardMapping, logger);
    }

    /**
     * Warn, if one of the message parameters is primitive.
     * 
     * @param execMethod
     * @param logger
     */
    private void checkForPrimitiveParameters(Method execMethod, Logger logger) {
        Class<?>[] paramTypes = execMethod.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (paramType.isPrimitive()) {
                logger.config("The method " + execMethod
                        + " contains a primitive parameter " + paramType + ".");
                logger
                        .config("It is recommended to use it's wrapper class. If no value could be read from the request, now you would got the default value. If you use the wrapper class, you would get null.");
                break;
            }
        }
    }

    /**
     * Checks, if the method is not volatile and public. If the method is not
     * public, a warning is logged and true returned. If the method is volatile
     * (this occurs, if the return type of a sub class differs from the return
     * type of the superclass, but is compatibel), true is returned, but no
     * message logged. Otherwise anything is ok and false is returned.
     * 
     * @param javaMethod
     * @param logger
     *                The Logger to log the warning
     * @return true, if the method is not public, false if it is public.
     */
    private boolean checkResMethodVolatileOrNotPublic(Method javaMethod,
            Logger logger) {
        final int modifiers = javaMethod.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            logger.warning("The method " + javaMethod + " must be public");
            return false;
        }
        if (Modifier.isVolatile(modifiers)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof ResourceClass))
            return false;
        ResourceClass otherResourceClass = (ResourceClass) anotherObject;
        return this.jaxRsClass.equals(otherResourceClass.jaxRsClass);
    }

    /**
     * Returns the allowed methods on the remainingPart. This method is used for
     * a OPTIONS request, if no special java method in the root resource class
     * was found for the given remainingPart.
     * 
     * @param remainingPath
     * @return an unmodifiable {@link Set} of the allowed methods.
     */
    public Set<org.restlet.data.Method> getAllowedMethods(
            RemainingPath remainingPath) {
        Set<org.restlet.data.Method> allowedMethods = this.allowedMethods
                .get(remainingPath);
        if (allowedMethods != null)
            return allowedMethods;
        allowedMethods = new HashSet<org.restlet.data.Method>(6);
        for (ResourceMethod rm : getMethodsForPath(remainingPath))
            allowedMethods.add(rm.getHttpMethod());
        if (!allowedMethods.isEmpty())
            if (allowedMethods.contains(org.restlet.data.Method.GET))
                allowedMethods.add(org.restlet.data.Method.HEAD);
        Set<org.restlet.data.Method> unmodifiable = Collections
                .unmodifiableSet(allowedMethods);
        this.allowedMethods.put(remainingPath, unmodifiable);
        return unmodifiable;
    }

    /**
     * Returns the method with the annotations, corresponding to the given
     * method. If the given method contains any JAX-RS annotations, it is
     * returned. If it is not annotated with JAX-RS annotations, this method
     * looks recursive in the subclass and the implemented interfaces, until it
     * found one. This would be returned.
     * 
     * @param javaMethod
     *                The java method to look for annotations
     * @return the founded method, or null, if no method with annotations was
     *         found. Returns also null, if null was given.
     */
    private Method getAnnotatedJavaMethod(Method javaMethod) {
        if (javaMethod == null)
            return null;
        boolean useMethod = WrapperUtil.checkForJaxRsAnnotations(javaMethod);
        if (useMethod)
            return javaMethod;
        Class<?> methodClass = javaMethod.getDeclaringClass();
        Class<?> superclass = methodClass.getSuperclass();
        Method scMethod = getMethodFromClass(superclass, javaMethod);
        Method annotatedMeth = getAnnotatedJavaMethod(scMethod);
        if (annotatedMeth != null)
            return annotatedMeth;
        Class<?>[] interfaces = methodClass.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            Method ifMethod = getMethodFromClass(interfaze, javaMethod);
            annotatedMeth = getAnnotatedJavaMethod(ifMethod);
            if (annotatedMeth != null)
                return annotatedMeth;
        }
        return null;
    }

    /**
     * @return Returns the wrapped root resource class.
     */
    public final Class<?> getJaxRsClass() {
        return jaxRsClass;
    }

    /**
     * Looks for the method with the same signature as the given method in the
     * given class.
     * 
     * @param clazz
     *                The Class to look for the method.
     * @param subClassMethod
     *                the Method to look for it's signature in the given class.
     * @return the method in the given class, with the same signature as given
     *         method, or null if such method is not available. Returns also
     *         null, if the given class is null.
     */
    private Method getMethodFromClass(Class<?> clazz, Method subClassMethod) {
        if (clazz == null)
            return null;
        String methodName = subClassMethod.getName();
        Class<?>[] parameterTypes = subClassMethod.getParameterTypes();
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Return all resource methods for the given path, ignoring HTTP method,
     * consumed or produced mimes and so on.
     * 
     * @param resourceObject
     *                The resource object
     * @param remainingPath
     *                the path
     * @return The ist of ResourceMethods
     */
    public Collection<ResourceMethod> getMethodsForPath(
            RemainingPath remainingPath) {
        // NICE results may be chached, if any method is returned.
        // The 404 case will be called rarely and produce a lot of cached data.
        List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        Iterable<ResourceMethod> subResourceMethods = this
                .getSubResourceMethods();
        for (ResourceMethod method : subResourceMethods) {
            PathRegExp methodPath = method.getPathRegExp();
            if (remainingPath.isEmptyOrSlash()) {
                if (methodPath.isEmptyOrSlash())
                    resourceMethods.add(method);
            } else {
                if (methodPath.matchesWithEmpty(remainingPath))
                    resourceMethods.add(method);
            }
        }
        return resourceMethods;
    }

    /**
     * @return returns the name of the wrapped class
     */
    public String getName() {
        return this.jaxRsClass.getName();
    }

    /**
     * @return Returns the sub resource locators of the given class.
     */
    public final Iterable<SubResourceLocator> getSubResourceLocators() {
        return subResourceLocators;
    }

    /**
     * @return Return the sub resource methods of the given class.
     */
    public final Iterable<ResourceMethod> getSubResourceMethods() {
        return this.subResourceMethods;
    }

    /**
     * @return Returns the sub resource locatores and sub resource methods.
     */
    public final Collection<ResourceMethodOrLocator> getSubResourceMethodsAndLocators() {
        return this.subResourceMethodsAndLocators;
    }

    @Override
    public int hashCode() {
        return this.jaxRsClass.hashCode();
    }

    /**
     * @return Returns true if the wrapped resource class has sub resource
     *         methods or sub resource locators.
     */
    public final boolean hasSubResourceMethodsOrLocators() {
        return !this.getSubResourceMethodsAndLocators().isEmpty();
    }

    private void initResourceMethodsAndLocators(
            ThreadLocalizedContext tlContext, EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException {
        Collection<ResourceMethodOrLocator> srmls = new ArrayList<ResourceMethodOrLocator>();
        Collection<ResourceMethod> subRsesMeths = new ArrayList<ResourceMethod>();
        Collection<SubResourceLocator> subResLocs = new ArrayList<SubResourceLocator>();
        Method[] classMethods = jaxRsClass.getDeclaredMethods();
        for (Method execMethod : classMethods) {
            Method annotatedMethod = getAnnotatedJavaMethod(execMethod);
            if (annotatedMethod == null)
                continue;
            Path path = annotatedMethod.getAnnotation(Path.class);
            org.restlet.data.Method httpMethod;
            httpMethod = WrapperUtil.getHttpMethod(annotatedMethod);
            try {
                if (httpMethod != null) {
                    if (!checkResMethodVolatileOrNotPublic(execMethod, logger))
                        continue;
                    ResourceMethod subResMeth = new ResourceMethod(execMethod,
                            annotatedMethod, this, httpMethod, tlContext,
                            entityProviders, allCtxResolvers,
                            extensionBackwardMapping, logger);
                    subRsesMeths.add(subResMeth);
                    srmls.add(subResMeth);
                    checkForPrimitiveParameters(execMethod, logger);
                } else {
                    if (path != null) {
                        if (!checkResMethodVolatileOrNotPublic(execMethod,
                                logger))
                            continue;
                        SubResourceLocator subResLoc = new SubResourceLocator(
                                execMethod, annotatedMethod, this, tlContext,
                                entityProviders, allCtxResolvers, extensionBackwardMapping, logger);
                        subResLocs.add(subResLoc);
                        srmls.add(subResLoc);
                        checkForPrimitiveParameters(execMethod, logger);
                    }
                }
            } catch (IllegalPathOnMethodException e) {
                logger.warning("The method " + annotatedMethod
                        + " is annotated with an illegal path: " + e.getPath()
                        + ". Ignoring this method. (" + e.getMessage() + ")");
            }
        }
        this.subResourceLocators = subResLocs;
        this.subResourceMethods = subRsesMeths;
        this.subResourceMethodsAndLocators = srmls;
    }

    /**
     * @return the leaveEncoded
     */
    boolean isLeaveEncoded() {
        return this.leaveEncoded;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.jaxRsClass + "]";
    }
}