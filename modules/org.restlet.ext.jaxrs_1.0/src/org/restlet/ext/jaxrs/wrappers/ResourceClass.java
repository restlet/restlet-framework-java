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

package org.restlet.ext.jaxrs.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.restlet.data.Method;
import org.restlet.ext.jaxrs.impl.PathRegExp;
import org.restlet.ext.jaxrs.util.Util;

/**
 * Instances represents a root resource class.
 * 
 * A Java class that uses JAX-RS annotations to implement a corresponding Web
 * resource, see chapter 2 of JSR-311-Spec.
 * 
 * @author Stephan Koops
 * 
 */
public class ResourceClass extends AbstractJaxRsWrapper {

    /**
     * @param jaxRsClass
     * @param requirePath
     * @return the path annotation or null, if no is present.
     * @throws IllegalArgumentException
     *                 if the jaxRsClass is null.
     */
    public static Path getPathAnnotation(Class<?> jaxRsClass,
            boolean requirePath) throws IllegalArgumentException {
        if (jaxRsClass == null)
            throw new IllegalArgumentException(
                    "The jaxRsClass must not be null");
        Path path = jaxRsClass.getAnnotation(Path.class);
        if (requirePath && path == null)
            throw new IllegalArgumentException(
                    "The root resource class does not have a @Path annotation");
        return path;
    }

    /**
     * Returns the path template of the given root resource class
     * 
     * @param rootResourceClass
     * @return the path template
     * @throws IllegalArgumentException
     *                 if the rootResourceClass is not annotated with
     * @Path
     * @see Path
     */
    public static String getPathTemplate(Class<?> rootResourceClass)
            throws IllegalArgumentException {
        Path path = rootResourceClass.getAnnotation(Path.class);
        if (path == null)
            throw new IllegalArgumentException(
                    "The class "
                            + rootResourceClass.getName()
                            + " is not a root resource class, because it is not annotated with @Path");
        return AbstractJaxRsWrapper.getPathTemplate(path);
    }

    /**
     * Caches the allowed methods (unmodifiable) for given remainingParts.
     */
    private Map<String, Set<Method>> allowedMethods = new HashMap<String, Set<Method>>();

    protected Class<?> jaxRsClass;

    private Collection<SubResourceLocator> subResourceLocators;

    private Collection<SubResourceMethod> subResourceMethods;

    private Collection<SubResourceMethodOrLocator> subResourceMethodsAndLocators;

    /**
     * Creates a new root resource class wrapper. Will not set the path, because
     * it is not available for a normal resource class.
     * 
     * @param jaxRsClass
     */
    public ResourceClass(Class<?> jaxRsClass) {
        super(null);
        this.jaxRsClass = jaxRsClass;
    }

    /**
     * Creates a new root resource class wrapper.
     * 
     * @param jaxRsClass
     * @param requirePath
     *                the subclass RootResourceClass must give true here, other
     *                classes must give false
     */
    protected ResourceClass(Class<?> jaxRsClass, boolean requirePath) {
        super(getPathAnnotation(jaxRsClass, requirePath));
        this.jaxRsClass = jaxRsClass;
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
     * Returns the allowed methods on the remainingPart. Is used for a OPTIONS
     * request, if no special java method in the root resource class was found
     * for the given remainingPart.
     * 
     * @param remainingPath
     * @return an unmodifiable {@link Set} of the allowed methods.
     */
    public Set<Method> getAllowedMethods(String remainingPath) {
        Set<Method> allowedMethods = this.allowedMethods.get(remainingPath);
        if(allowedMethods != null)
            return allowedMethods;
        allowedMethods = new HashSet<Method>(6);
        for (ResourceMethod rm : getMethodsForPath(remainingPath))
            allowedMethods.add(rm.getHttpMethod());
        if (!allowedMethods.isEmpty())
            if (allowedMethods.contains(Method.GET))
                allowedMethods.add(Method.HEAD);
        Set<Method> unmodifiable = Collections.unmodifiableSet(allowedMethods);
        this.allowedMethods.put(remainingPath, unmodifiable);
        return unmodifiable;
    }

    /**
     * @return Returns the wrapped root resource class.
     */
    public final Class<?> getJaxRsClass() {
        return jaxRsClass;
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
    public Collection<ResourceMethod> getMethodsForPath(String remainingPath) {
        // LATER results may be chached, if any method is returned.
        // The 404 case will be called rarely and produce a lot of cached data.
        List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        Iterable<SubResourceMethod> subResourceMethods = this
                .getSubResourceMethods();
        for (SubResourceMethod method : subResourceMethods) {
            PathRegExp methodPath = method.getPathRegExp();
            if (Util.isEmptyOrSlash(remainingPath)) {
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
     * @return Returns the sub resource locators of the given class.
     */
    public final Iterable<SubResourceLocator> getSubResourceLocators() {
        if (this.subResourceLocators == null)
            internalSetSubResourceMethodsAndLocators();
        return subResourceLocators;
    }

    /**
     * @return Return the sub resource methods of the given class.
     */
    public final Iterable<SubResourceMethod> getSubResourceMethods() {
        if (this.subResourceMethods == null)
            internalSetSubResourceMethodsAndLocators();
        return this.subResourceMethods;
    }

    /**
     * @return Returns the sub resource locatores and sub resource methods.
     */
    public final Collection<SubResourceMethodOrLocator> getSubResourceMethodsAndLocators() {
        if (this.subResourceMethodsAndLocators == null)
            internalSetSubResourceMethodsAndLocators();
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

    private void internalSetSubResourceMethodsAndLocators() {
        Collection<SubResourceMethodOrLocator> srmls = new ArrayList<SubResourceMethodOrLocator>();
        Collection<SubResourceMethod> srms = new ArrayList<SubResourceMethod>();
        Collection<SubResourceLocator> srls = new ArrayList<SubResourceLocator>();
        java.lang.reflect.Method[] classMethods = jaxRsClass.getMethods();
        // TODO JSR311: muss der auch mit nicht-public-Methoden umgehen koennen?
        // Wenn ja, dann muss ich wohl ein Proxy bauen, der im gleichen Package
        // sitzt. Tests entsprechend anpassen
        // classMethods = jaxRsClass.getDeclaredMethods();
        // TODO z.Zt. werden alle Methoden geladen, auch die nicht-public
        for (java.lang.reflect.Method javaMethod : classMethods) {
            Path path = javaMethod.getAnnotation(Path.class);
            org.restlet.data.Method httpMethod = ResourceMethod
                    .getHttpMethod(javaMethod);
            if (httpMethod != null) {
                SubResourceMethod srm = new SubResourceMethod(javaMethod, path,
                        this, httpMethod);
                srms.add(srm);
                srmls.add(srm);
            } else {
                if (path != null) {
                    SubResourceLocator srl = new SubResourceLocator(javaMethod,
                            path, this);
                    srls.add(srl);
                    srmls.add(srl);
                }
            }
        }
        this.subResourceLocators = srls;
        this.subResourceMethods = srms;
        this.subResourceMethodsAndLocators = srmls;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.jaxRsClass + "]";
    }
}