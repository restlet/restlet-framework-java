/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.engine.resource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.service.MetadataService;

// [excludes gwt]
/**
 * Utilities to manipulate Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /** Annotation info cache. */
    private final ConcurrentMap<Class<?>, List<MethodAnnotationInfo>> cache = new ConcurrentHashMap<Class<?>, List<MethodAnnotationInfo>>();

    /** Current instance. */
    private static AnnotationUtils instance = new AnnotationUtils();

    /** Returns the current instance of AnnotationUtils. */
    public static AnnotationUtils getInstance() {
        return instance;
    }

    /**
     * Protected constructor.
     */
    protected AnnotationUtils() {
    }

    /**
     * Computes the annotation descriptors for the given Java method.
     * 
     * @param descriptors
     *            The annotation descriptors to update or null to create a new
     *            one.
     * @param resourceClass
     *            The class or interface that hosts the javaMethod.
     * @param initialResourceClass
     *            The class or interface that runs the javaMethod.
     * @param javaMethod
     *            The Java method to inspect.
     * @return The annotation descriptors.
     */
    private List<MethodAnnotationInfo> addAnnotationDescriptors(
            List<MethodAnnotationInfo> descriptors, Class<?> resourceClass,
            Class<?> initialResourceClass, java.lang.reflect.Method javaMethod) {
        List<MethodAnnotationInfo> result = descriptors;

        // Add the annotation descriptor
        if (result == null) {
            result = new CopyOnWriteArrayList<MethodAnnotationInfo>();
        }

        for (Annotation annotation : javaMethod.getAnnotations()) {
            Annotation methodAnnotation = annotation.annotationType()
                    .getAnnotation(org.restlet.engine.connector.Method.class);

            Method restletMethod = getRestletMethod(annotation,
                    methodAnnotation);
            if (restletMethod != null) {

                String toString = annotation.toString();
                int startIndex = annotation.annotationType().getCanonicalName()
                        .length() + 8;
                int endIndex = toString.length() - 1;
                String value = null;

                if (endIndex > startIndex) {
                    value = toString.substring(startIndex, endIndex);

                    if ("".equals(value)) {
                        value = null;
                    }
                }

                result.add(new MethodAnnotationInfo(initialResourceClass,
                        restletMethod, javaMethod, value));

            }
        }

        return result;
    }

    /**
     * Computes the annotation descriptors for the given class or interface.
     * 
     * @param descriptors
     *            The annotation descriptors to update or null to create a new
     *            one.
     * @param clazz
     *            The class or interface to introspect.
     * @param initialClass
     *            The class or interface that runs the javaMethod.
     * @return The annotation descriptors.
     */
    private List<MethodAnnotationInfo> addAnnotations(
            List<MethodAnnotationInfo> descriptors, Class<?> clazz,
            Class<?> initialClass) {
        List<MethodAnnotationInfo> result = descriptors;

        if (clazz != null && !ServerResource.class.equals(clazz)) {
            // Add the annotation descriptor
            if (result == null) {
                result = new CopyOnWriteArrayList<MethodAnnotationInfo>();
            }

            // Inspect the current class
            if (clazz.isInterface()) {
                for (java.lang.reflect.Method javaMethod : clazz.getMethods()) {
                    addAnnotationDescriptors(result, clazz, initialClass,
                            javaMethod);
                }
            } else {
                for (java.lang.reflect.Method javaMethod : clazz
                        .getDeclaredMethods()) {
                    addAnnotationDescriptors(result, clazz, initialClass,
                            javaMethod);
                }
            }

            // Inspect the implemented interfaces for annotations
            Class<?>[] interfaces = clazz.getInterfaces();

            if (interfaces != null) {
                for (Class<?> interfaceClass : interfaces) {
                    result = addAnnotations(result, interfaceClass,
                            initialClass);
                }
            }

            // Add the annotations from the super class.
            addAnnotations(result, clazz.getSuperclass(), initialClass);
        }

        return result;
    }

    /**
     * Clears the annotation descriptors cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Returns the first annotation descriptor matching the given Java method.
     * 
     * @param annotations
     *            The list of annotations.
     * @param javaMethod
     *            The method to match.
     * @return The annotation descriptor.
     */
    public MethodAnnotationInfo getAnnotation(List<MethodAnnotationInfo> annotations,
            java.lang.reflect.Method javaMethod) {
        if (annotations != null) {
            for (MethodAnnotationInfo annotationInfo : annotations) {
                if (annotationInfo.getJavaMethod().equals(javaMethod)) {
                    return annotationInfo;
                }
            }
        }

        return null;
    }

    /**
     * Returns the first annotation descriptor matching the given Restlet
     * method.
     * 
     * @param annotations
     *            The list of annotations.
     * @param restletMethod
     *            The method to match.
     * @param query
     *            The query parameters.
     * @param entity
     *            The request entity to match or null if no entity is provided.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return The annotation descriptor.
     * @throws IOException
     */
    public MethodAnnotationInfo getAnnotation(List<MethodAnnotationInfo> annotations,
            Method restletMethod, Form query, Representation entity,
            MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        if (annotations != null) {
            for (MethodAnnotationInfo annotationInfo : annotations) {
                if (annotationInfo.isCompatible(restletMethod, query, entity,
                        metadataService, converterService)) {
                    return annotationInfo;
                }
            }
        }

        return null;
    }

    /**
     * Returns the annotation descriptors for the given resource class.
     * 
     * @param clazz
     *            The resource class to introspect.
     * @return The list of annotation descriptors.
     */
    public List<MethodAnnotationInfo> getAnnotations(Class<?> clazz) {
        List<MethodAnnotationInfo> result = cache.get(clazz);

        if (result == null) {
            // Inspect the class itself for annotations
            result = addAnnotations(result, clazz, clazz);

            // Put the list in the cache if no one was previously present
            List<MethodAnnotationInfo> prev = cache.putIfAbsent(clazz, result);

            if (prev != null) {
                // Reuse the previous entry
                result = prev;
            }
        }

        return result;
    }

    /**
     * Returns the annotation descriptors for the given resource class.
     * 
     * @param javaMethod
     *            The Java method.
     * @return The list of annotation descriptors.
     */
    public List<MethodAnnotationInfo> getAnnotations(Class<?> clazz,
            java.lang.reflect.Method javaMethod) {
        return addAnnotationDescriptors(null, clazz, clazz, javaMethod);
    }

    /**
     * Returns an instance of {@link Method} according to the given annotations.
     * 
     * @param annotation
     *            Java annotation.
     * @param methodAnnotation
     *            Annotation that corresponds to a Restlet method.
     * @return An instance of {@link Method} according to the given annotations.
     */
    protected Method getRestletMethod(Annotation annotation,
            Annotation methodAnnotation) {
        return methodAnnotation == null ? null
                : Method.valueOf(((org.restlet.engine.connector.Method) methodAnnotation)
                        .value());
    }

}
