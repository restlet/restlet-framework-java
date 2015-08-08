/**
 * Copyright 2005-2014 Restlet
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
import org.restlet.resource.Status;
import org.restlet.service.MetadataService;

// [excludes gwt]
/**
 * Utilities to manipulate Restlet annotations.
 *
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /** Annotation info cache. */
    private static final ConcurrentMap<Class<?>, List<AnnotationInfo>> cache = new ConcurrentHashMap<Class<?>, List<AnnotationInfo>>();

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
    private List<AnnotationInfo> addAnnotations(
            List<AnnotationInfo> descriptors, Class<?> clazz,
            Class<?> initialClass) {
        List<AnnotationInfo> result = descriptors;

        if (clazz != null && !ServerResource.class.equals(clazz)) {
            // Add the annotation descriptor
            if (result == null) {
                result = new CopyOnWriteArrayList<AnnotationInfo>();
            }

            // Inspect the current class
            addThrowableAnnotationDescriptors(result, clazz, initialClass);

            if (clazz.isInterface()) {
                for (java.lang.reflect.Method javaMethod : clazz.getMethods()) {
                    addMethodAnnotationDescriptors(result, clazz, initialClass,
                            javaMethod);
                }
            } else {
                for (java.lang.reflect.Method javaMethod : clazz
                        .getDeclaredMethods()) {
                    addMethodAnnotationDescriptors(result, clazz, initialClass,
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
     * Computes the annotation descriptors for the given Java method.
     * 
     * @param descriptors
     *            The annotation descriptors to update or null to create a new
     *            one.
     * @param clazz
     *            The class or interface that hosts the javaMethod.
     * @param initialClass
     *            The class or interface that runs the javaMethod.
     * @param javaMethod
     *            The Java method to inspect.
     * @return The annotation descriptors.
     */
    private List<AnnotationInfo> addMethodAnnotationDescriptors(
            List<AnnotationInfo> descriptors, Class<?> clazz,
            Class<?> initialClass, java.lang.reflect.Method javaMethod) {
        List<AnnotationInfo> result = descriptors;

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

                if (result == null) {
                    result = new CopyOnWriteArrayList<AnnotationInfo>();
                }

                result.add(new MethodAnnotationInfo(initialClass,
                        restletMethod, javaMethod, value));
            }
        }

        for (Class<?> exceptionClass : javaMethod.getExceptionTypes()) {
            for (Annotation annotation : exceptionClass.getAnnotations()) {
                org.restlet.resource.Status statusAnnotation = annotation
                        .annotationType().getAnnotation(
                                org.restlet.resource.Status.class);

                if (statusAnnotation != null) {
                    int code = statusAnnotation.value();
                    boolean serializable = statusAnnotation.serialize();

                    if (result == null) {
                        result = new CopyOnWriteArrayList<AnnotationInfo>();
                    }

                    result.add(new ThrowableAnnotationInfo(initialClass, code,
                            serializable));
                }
            }
        }

        return result;
    }

    /**
     * Computes the annotation descriptors for the given Java method.
     * 
     * @param descriptors
     *            The annotation descriptors to update or null to create a new
     *            one.
     * @param clazz
     *            The class or interface that hosts the javaMethod.
     * @param initialClass
     *            The class or interface that runs the javaMethod.
     * @return The annotation descriptors.
     */
    private List<AnnotationInfo> addThrowableAnnotationDescriptors(
            List<AnnotationInfo> descriptors, Class<?> clazz,
            Class<?> initialClass) {
        List<AnnotationInfo> result = descriptors;
        Annotation annotation = clazz
                .getAnnotation(org.restlet.resource.Status.class);

        if (annotation != null) {
            Status status = (Status) annotation;
            result.add(new ThrowableAnnotationInfo(initialClass,
                    status.value(), status.serialize()));
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
     * Returns the annotation descriptors for the given resource class.
     * 
     * @param clazz
     *            The resource class to introspect.
     * @return The list of annotation descriptors.
     */
    public synchronized List<AnnotationInfo> getAnnotations(Class<?> clazz) {
        List<AnnotationInfo> result = cache.get(clazz);

        if (result == null) {
            // Inspect the class itself for annotations
            result = addAnnotations(result, clazz, clazz);

            // Put the list in the cache if no one was previously present
            List<AnnotationInfo> prev = cache.putIfAbsent(clazz, result);

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
    public List<AnnotationInfo> getAnnotations(Class<?> clazz,
                                               java.lang.reflect.Method javaMethod) {
        return addMethodAnnotationDescriptors(null, clazz, clazz, javaMethod);
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
    public MethodAnnotationInfo getMethodAnnotation(
            List<AnnotationInfo> annotations,
            java.lang.reflect.Method javaMethod) {
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (annotationInfo instanceof MethodAnnotationInfo
                        && annotationInfo.getJavaMethod().equals(javaMethod)) {
                    return (MethodAnnotationInfo) annotationInfo;
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
    public MethodAnnotationInfo getMethodAnnotation(
            List<AnnotationInfo> annotations, Method restletMethod, Form query,
            Representation entity, MetadataService metadataService,
            org.restlet.service.ConverterService converterService)
            throws IOException {
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (annotationInfo instanceof MethodAnnotationInfo) {
                    if (((MethodAnnotationInfo) annotationInfo).isCompatible(
                            restletMethod, query, entity, metadataService,
                            converterService)) {
                        return (MethodAnnotationInfo) annotationInfo;
                    }
                }
            }
        }

        return null;
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
        return (methodAnnotation == null) ? null
                : Method.valueOf(((org.restlet.engine.connector.Method) methodAnnotation)
                        .value());
    }

    /**
     * Returns the status annotation descriptor if present or null.
     * 
     * @param clazz
     *            The class with the status attached.
     * @return The status annotation descriptor if present or null.
     */
    public ThrowableAnnotationInfo getThrowableAnnotationInfo(Class<?> clazz) {
        List<AnnotationInfo> annotationInfos = getAnnotations(clazz);

        for (AnnotationInfo annotationInfo : annotationInfos) {
            if (annotationInfo instanceof ThrowableAnnotationInfo) {
                return (ThrowableAnnotationInfo) annotationInfo;
            }
        }

        return null;
    }

    /**
     * Returns the {@link Throwable} class matching the given error code if
     * present or null.
     * 
     * @param javaMethod
     *            The method that holds {@link Throwable}.
     * @param errorCode
     *            The error code to match.
     * @return The {@link Throwable} class matching the given error code if
     *         present or null.
     */
    public ThrowableAnnotationInfo getThrowableAnnotationInfo(
            java.lang.reflect.Method javaMethod, int errorCode) {
        for (Class<?> clazz : javaMethod.getExceptionTypes()) {
            ThrowableAnnotationInfo tai = getThrowableAnnotationInfo(clazz);

            if (tai != null && tai.getStatus().getCode() == errorCode) {
                return tai;
            }
        }

        return null;
    }

    /**
     * Returns the {@link Throwable} class matching the given error code if
     * present or null.
     * 
     * @param javaMethod
     *            The method that holds {@link Throwable}.
     * @param errorCode
     *            The error code to match.
     * @return The {@link Throwable} class matching the given error code if
     *         present or null.
     */
    public Class<?> getThrowableClass(java.lang.reflect.Method javaMethod,
            int errorCode) {
        for (Class<?> clazz : javaMethod.getExceptionTypes()) {
            ThrowableAnnotationInfo tai = getThrowableAnnotationInfo(clazz);

            if (tai != null && tai.getStatus().getCode() == errorCode) {
                return clazz;
            }
        }

        return null;
    }

}
