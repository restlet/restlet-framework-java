/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.rebind;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.restlet.client.engine.util.emul.CopyOnWriteArrayList;

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

                    String value = extractAnnotationValue(annotation);

                    result = new AnnotationInfo(restletMethod, javaMethod,
                            value);
                }
            }
        }

        return result;
    }

    /**
     * Returns the annotation value.
     * @param annotation The annotation
     * @return the annotation value
     */
    private static String extractAnnotationValue(final Annotation annotation) {
        final String annotationCanonicalName = annotation.annotationType().getCanonicalName();
        final String annotationStringRepresentation = annotation.toString();

        // Drop the annotation canonical name
        final int index = annotationStringRepresentation.indexOf(annotationCanonicalName);
        final String annotationValueStringRepresentation = annotationStringRepresentation.substring(index + annotationCanonicalName.length());

        // Then extract the value, if any.
        final String result;
        if (annotationValueStringRepresentation.startsWith(("(value="))) { // (value=blabla) format: JDK 1.8 and below
            result = annotationValueStringRepresentation.substring("(value=".length(), annotationValueStringRepresentation.length() - 1);
        } else if (annotationValueStringRepresentation.startsWith(("(\""))) {
            result = annotationValueStringRepresentation.substring("(\"".length(), annotationValueStringRepresentation.length() - 2);
        } else if (annotationValueStringRepresentation.startsWith(("("))) {
            result = annotationValueStringRepresentation.substring("(".length(), annotationValueStringRepresentation.length() - 1);
        } else {
            result = annotationValueStringRepresentation;
        }

        return result.isEmpty()
                ? null
                : result;
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
