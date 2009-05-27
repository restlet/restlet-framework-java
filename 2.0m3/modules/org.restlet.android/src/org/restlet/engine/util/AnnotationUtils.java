package org.restlet.engine.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.Method;
import org.restlet.resource.UniformResource;

/**
 * Utilities to manipulate Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /** Annotation info cache. */
    private static final ConcurrentMap<Class<? extends UniformResource>, List<AnnotationInfo>> cache = new ConcurrentHashMap<Class<? extends UniformResource>, List<AnnotationInfo>>();

    /**
     * Clears the annotation descriptors cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Computes the annotation descriptors for the given class.
     * 
     * @param resourceClass
     *            The class to introspect.
     * @return The annotation descriptors.
     */
    private static List<AnnotationInfo> computeAnnotationDescriptors(
            Class<? extends UniformResource> resourceClass) {
        List<AnnotationInfo> result = new ArrayList<AnnotationInfo>();

        for (java.lang.reflect.Method javaMethod : resourceClass.getMethods()) {
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

                    // Add the annotation descriptor
                    result.add(new AnnotationInfo(restletMethod, javaMethod,
                            value));
                }
            }
        }

        return result;
    }

    /**
     * Returns the annotation descriptors for the given resource class.
     * 
     * @param resourceClass
     *            The resource class to introspect.
     * @return The list of annotation descriptors.
     */
    public static List<AnnotationInfo> getAnnotationDescriptors(
            Class<? extends UniformResource> resourceClass) {
        List<AnnotationInfo> result = cache.get(resourceClass);

        if (result == null) {
            result = computeAnnotationDescriptors(resourceClass);
            List<AnnotationInfo> prev = cache
                    .putIfAbsent(resourceClass, result);

            if (prev != null)
                result = prev;
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
