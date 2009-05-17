package org.restlet.engine.resource;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.Method;

/**
 * Utilities to manipulate Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /** Annotation info cache. */
    private static final ConcurrentMap<Class<?>, List<AnnotationInfo>> cache = new ConcurrentHashMap<Class<?>, List<AnnotationInfo>>();

    /**
     * Clears the annotation descriptors cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Computes the annotation descriptors for the given class.
     * 
     * @param clazz
     *            The class to introspect.
     * @return The annotation descriptors.
     */
    private static List<AnnotationInfo> addAnnotationDescriptors(
            List<AnnotationInfo> descriptors, Class<?> clazz) {
        List<AnnotationInfo> result = descriptors;

        // Add the annotation descriptor
        if (result == null) {
            result = new ArrayList<AnnotationInfo>();
        }

        for (java.lang.reflect.Method javaMethod : clazz.getMethods()) {
            for (Annotation annotation : javaMethod.getAnnotations()) {

                Annotation methodAnnotation = annotation.annotationType()
                        .getAnnotation(org.restlet.engine.Method.class);

                if (methodAnnotation != null) {
                    Method restletMethod = Method
                            .valueOf(((org.restlet.engine.Method) methodAnnotation)
                                    .value());

                    String toString = annotation.toString();
                    int startIndex = annotation.annotationType()
                            .getCanonicalName().length() + 8;
                    int endIndex = toString.length() - 1;
                    String value = toString.substring(startIndex, endIndex);
                    if ("".equals(value)) {
                        value = null;
                    }

                    result.add(new AnnotationInfo(restletMethod, javaMethod,
                            value));
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
        for (AnnotationInfo annotationInfo : annotations) {
            if (annotationInfo.getJavaMethod().equals(javaMethod)) {
                return annotationInfo;
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
        for (AnnotationInfo annotationInfo : annotations) {
            if (annotationInfo.getRestletMethod().equals(restletMethod)) {
                return annotationInfo;
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
    public static List<AnnotationInfo> getAnnotationDescriptors(Class<?> clazz) {
        List<AnnotationInfo> result = cache.get(clazz);

        if (result == null) {
            result = addAnnotationDescriptors(result, clazz);
            List<AnnotationInfo> prev = cache.putIfAbsent(clazz, result);

            if (prev != null) {
                result = prev;
            }

            // Inspect the implemented interfaces for annotations
            Class<?>[] interfaces = clazz.getInterfaces();

            if (interfaces != null) {
                for (Class<?> interfaceClass : interfaces) {
                    result = addAnnotationDescriptors(result, interfaceClass);
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
