/**
 * Copyright 2005-2020 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.rebind;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.client.data.Method;

/**
 * Utilities to manipulate Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /** Annotation info cache. */
    private static final ConcurrentMap<Class<?>, List<AnnotationInfo>> cache = new ConcurrentHashMap<Class<?>, List<AnnotationInfo>>();

    /**
     * Computes the annotation descriptors for the given class or interface.
     * 
     * @param clazz
     *            The class or interface to introspect.
     * @return The annotation descriptors.
     */
    private static List<AnnotationInfo> addAnnotations(
            List<AnnotationInfo> descriptors, Class<?> clazz) {
        List<AnnotationInfo> result = descriptors;

        // Add the annotation descriptor
        if (result == null) {
            result = new CopyOnWriteArrayList<AnnotationInfo>();
        }

        AnnotationInfo anno = null;
        for (java.lang.reflect.Method javaMethod : clazz.getMethods()) {
            anno = getAnnotation(javaMethod);

            if (anno != null) {
                result.add(anno);
            }
        }

        return result;
    }

    /**
     * Clears the annotation descriptors cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Returns the annotation descriptor for the given resource class.
     * 
     * @param javaMethod
     *            The Java method.
     * @return The annotation descriptor.
     */
    public static AnnotationInfo getAnnotation(
            java.lang.reflect.Method javaMethod) {
        AnnotationInfo result = null;

        if (javaMethod != null) {
            for (Annotation annotation : javaMethod.getAnnotations()) {
                Annotation methodAnnotation = annotation.annotationType()
                        .getAnnotation(org.restlet.client.engine.connector.Method.class);

                if (methodAnnotation != null) {
                    Method restletMethod = Method
                            .valueOf(((org.restlet.client.engine.connector.Method) methodAnnotation)
                                    .value());

                    String toString = annotation.toString();
                    int startIndex = annotation.annotationType()
                            .getCanonicalName().length() + 8;
                    int endIndex = toString.length() - 1;
                    String value = toString.substring(startIndex, endIndex);
                    if ("".equals(value)) {
                        value = null;
                    }

                    result = new AnnotationInfo(restletMethod, javaMethod,
                            value);
                }
            }
        }

        return result;
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
    public static AnnotationInfo getAnnotation(
            List<AnnotationInfo> annotations,
            java.lang.reflect.Method javaMethod) {
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
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
     * @return The annotation descriptor.
     */
    public static AnnotationInfo getAnnotation(
            List<AnnotationInfo> annotations, Method restletMethod) {
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (annotationInfo.getRestletMethod().equals(restletMethod)) {
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
    public static List<AnnotationInfo> getAnnotations(Class<?> clazz) {
        List<AnnotationInfo> result = cache.get(clazz);

        if (result == null) {
            result = addAnnotations(result, clazz);
            List<AnnotationInfo> prev = cache.putIfAbsent(clazz, result);

            if (prev != null) {
                result = prev;
            }

            // Inspect the implemented interfaces for annotations
            Class<?>[] interfaces = clazz.getInterfaces();

            if (interfaces != null) {
                for (Class<?> interfaceClass : interfaces) {
                    result = addAnnotations(result, interfaceClass);
                }
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AnnotationUtils() {
    }

}
