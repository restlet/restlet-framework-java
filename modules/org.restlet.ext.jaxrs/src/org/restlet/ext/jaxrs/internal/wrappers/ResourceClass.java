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

package org.restlet.ext.jaxrs.internal.wrappers;

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.checkForJaxRsAnnotations;
import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.getHttpMethod;
import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.isVolatile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

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

    /**
     * The resource methods of this resource class. (It is initialized in
     * method.)
     * {@link #initResourceMethodsAndLocators(ThreadLocalizedContext, JaxRsProviders, ExtensionBackwardMapping, Logger)}
     */
    private final Collection<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();

    /**
     * The resource methods and sub resource locators of this resource class.
     * (It is initialized in method.)
     * {@link #initResourceMethodsAndLocators(ThreadLocalizedContext, JaxRsProviders, ExtensionBackwardMapping, Logger)}
     */
    private final Collection<ResourceMethodOrLocator> resourceMethodsAndLocators = new ArrayList<ResourceMethodOrLocator>();

    /**
     * The sub resource locators of this resource class. (It is initialized in
     * method.)
     * {@link #initResourceMethodsAndLocators(ThreadLocalizedContext, JaxRsProviders, ExtensionBackwardMapping, Logger)}
     */
    private final Collection<SubResourceLocator> subResourceLocators = new ArrayList<SubResourceLocator>();

    /**
     * Creates a new root resource class wrapper. Will not set the path, because
     * it is not available for a normal resource class.
     * 
     * @param jaxRsClass
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     *                all entity providers
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                The logger to log warnings, if the class is not valid.
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     * @see ResourceClasses#getResourceClass(Class)
     */
    ResourceClass(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException {
        super();
        this.leaveEncoded = jaxRsClass.isAnnotationPresent(Encoded.class);
        this.jaxRsClass = jaxRsClass;
        initResourceMethodsAndLocators(tlContext, jaxRsProviders,
                extensionBackwardMapping, logger);
    }

    /**
     * Creates a new root resource class wrapper. To be used by subclass
     * {@link RootResourceClass}.
     * 
     * @param jaxRsClass
     * @param jaxRsProviders
     *                all entity providers
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     * @throws IllegalArgumentException
     * @throws IllegalPathOnClassException
     * @throws MissingAnnotationException
     *                 if &#64;{@link Path} is missing on the jaxRsClass
     * @see ResourceClasses#getResourceClass(Class)
     */
    protected ResourceClass(Class<?> jaxRsClass, JaxRsProviders jaxRsProviders,
            ThreadLocalizedContext tlContext,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, IllegalPathOnClassException,
            MissingAnnotationException {
        super(PathRegExp.createForClass(jaxRsClass));
        this.leaveEncoded = jaxRsClass.isAnnotationPresent(Encoded.class);
        this.jaxRsClass = jaxRsClass;
        this.initResourceMethodsAndLocators(tlContext, jaxRsProviders,
                extensionBackwardMapping, logger);
    }

    /**
     * Warn, if one of the message parameters is primitive.
     * 
     * @param execMethod
     * @param logger
     */
    private void checkForPrimitiveParameters(Method execMethod, Logger logger) {
        final Class<?>[] paramTypes = execMethod.getParameterTypes();
        for (final Class<?> paramType : paramTypes) {
            if (paramType.isPrimitive()) {
                logger.config("The method " + execMethod
                        + " contains a primitive parameter " + paramType + ".");
                logger
                        .config("It is recommended to use it's wrapper class. If no value could be read from the request, now you would got the default value. If you use the wrapper class, you would get null.");
                break;
            }
        }
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (!(anotherObject instanceof ResourceClass)) {
            return false;
        }
        final ResourceClass otherResourceClass = (ResourceClass) anotherObject;
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
        if (allowedMethods != null) {
            return allowedMethods;
        }
        allowedMethods = new HashSet<org.restlet.data.Method>(6);
        for (final ResourceMethod rm : getMethodsForPath(remainingPath)) {
            allowedMethods.add(rm.getHttpMethod());
        }
        if (!allowedMethods.isEmpty()) {
            if (allowedMethods.contains(org.restlet.data.Method.GET)) {
                allowedMethods.add(org.restlet.data.Method.HEAD);
            }
        }
        final Set<org.restlet.data.Method> unmodifiable = Collections
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
        if (javaMethod == null) {
            return null;
        }
        final boolean useMethod = checkForJaxRsAnnotations(javaMethod);
        if (useMethod) {
            return javaMethod;
        }
        final Class<?> methodClass = javaMethod.getDeclaringClass();
        final Class<?> superclass = methodClass.getSuperclass();
        final Method scMethod = getMethodFromClass(superclass, javaMethod);
        Method annotatedMeth = getAnnotatedJavaMethod(scMethod);
        if (annotatedMeth != null) {
            return annotatedMeth;
        }
        final Class<?>[] interfaces = methodClass.getInterfaces();
        for (final Class<?> interfaze : interfaces) {
            final Method ifMethod = getMethodFromClass(interfaze, javaMethod);
            annotatedMeth = getAnnotatedJavaMethod(ifMethod);
            if (annotatedMeth != null) {
                return annotatedMeth;
            }
        }
        return null;
    }

    /**
     * @return Returns the wrapped root resource class.
     */
    public final Class<?> getJaxRsClass() {
        return this.jaxRsClass;
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
        if (clazz == null) {
            return null;
        }
        final String methodName = subClassMethod.getName();
        final Class<?>[] parameterTypes = subClassMethod.getParameterTypes();
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
        final List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        for (final ResourceMethod method : this.resourceMethods) {
            final PathRegExp methodPath = method.getPathRegExp();
            if (remainingPath.isEmptyOrSlash()) {
                if (methodPath.isEmptyOrSlash()) {
                    resourceMethods.add(method);
                }
            } else {
                if (methodPath.matchesWithEmpty(remainingPath)) {
                    resourceMethods.add(method);
                }
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
     * @return Return the sub resource methods of the given class.
     */
    public final Iterable<ResourceMethod> getResourceMethods() {
        return this.resourceMethods;
    }

    /**
     * @return Returns the sub resource locatores and sub resource methods.
     */
    public final Collection<ResourceMethodOrLocator> getResourceMethodsAndLocators() {
        return this.resourceMethodsAndLocators;
    }

    /**
     * @return Returns the sub resource locators of the given class.
     */
    public final Iterable<SubResourceLocator> getSubResourceLocators() {
        return this.subResourceLocators;
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
        return !this.resourceMethodsAndLocators.isEmpty();
    }

    private void initResourceMethodsAndLocators(
            ThreadLocalizedContext tlContext, JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException {
        for (final Method execMethod : jaxRsClass.getMethods()) {
            final Method annotatedMethod = getAnnotatedJavaMethod(execMethod);
            if (annotatedMethod == null) {
                continue;
            }
            final Path path = annotatedMethod.getAnnotation(Path.class);
            org.restlet.data.Method httpMethod;
            httpMethod = getHttpMethod(annotatedMethod);
            try {
                if (httpMethod != null) {
                    if (isVolatile(execMethod)) {
                        continue;
                    }
                    ResourceMethod subResMeth;
                    try {
                        subResMeth = new ResourceMethod(execMethod,
                                annotatedMethod, this, httpMethod, tlContext,
                                jaxRsProviders, extensionBackwardMapping,
                                logger);
                    } catch (IllegalMethodParamTypeException e) {
                        String message = "Ignore method "
                                + execMethod
                                + ": An annotated parameter of the resource method "
                                + annotatedMethod + " is has an illegal type";
                        logger.log(Level.WARNING, message, e);
                        continue;
                    } catch (IllegalParamTypeException e) {
                        String message = "Ignore method " + execMethod + ": "
                                + e.getMessage();
                        logger.log(Level.WARNING, message, e);
                        continue;
                    }
                    this.resourceMethods.add(subResMeth);
                    this.resourceMethodsAndLocators.add(subResMeth);
                    checkForPrimitiveParameters(execMethod, logger);
                } else {
                    if (path != null) {
                        if (isVolatile(execMethod)) {
                            continue;
                        }
                        SubResourceLocator subResLoc;
                        try {
                            subResLoc = new SubResourceLocator(execMethod,
                                    annotatedMethod, this, tlContext,
                                    jaxRsProviders, extensionBackwardMapping,
                                    logger);
                        } catch (IllegalMethodParamTypeException e) {
                            String message = "Ignore method "
                                    + execMethod
                                    + ": An annotated parameter of the resource method "
                                    + annotatedMethod
                                    + " is has an illegal type";
                            logger.log(Level.WARNING, message, e);
                            continue;
                        } catch (IllegalParamTypeException e) {
                            String message = "Ignore method " + execMethod
                                    + ": " + e.getMessage();
                            logger.log(Level.WARNING, message, e);
                            continue;
                        }
                        this.subResourceLocators.add(subResLoc);
                        this.resourceMethodsAndLocators.add(subResLoc);
                        checkForPrimitiveParameters(execMethod, logger);
                    }
                }
                // NICE warn, if @Consumes, @Produces or another
                // non-useful annotation is available on a method to ignore.
            } catch (IllegalPathOnMethodException e) {
                logger.warning("The method " + annotatedMethod
                        + " is annotated with an illegal path: " + e.getPath()
                        + ". Ignoring this method. (" + e.getMessage() + ")");
            }
        }
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