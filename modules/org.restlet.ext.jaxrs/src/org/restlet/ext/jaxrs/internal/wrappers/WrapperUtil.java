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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;

/**
 * Utility methods for the wrappers.
 * 
 * @author Stephan Koops
 */
public class WrapperUtil {

    private static final String JAX_RS_PACKAGE_PREFIX = "javax.ws.rs";

    /**
     * Checks, if the given annotation is annotated with at least one JAX-RS
     * related annotation.
     * 
     * @param javaMethod
     *            Java method, class or something like that.
     * @return true, if the given accessible object is annotated with any
     *         JAX-RS-Annotation.
     */
    static boolean checkForJaxRsAnnotations(Method javaMethod) {
        for (final Annotation annotation : javaMethod.getAnnotations()) {
            final Class<? extends Annotation> annoType = annotation
                    .annotationType();
            if (annoType.getName().startsWith(JAX_RS_PACKAGE_PREFIX)) {
                return true;
            }
            if (annoType.isAnnotationPresent(HttpMethod.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the parameters for the constructor are valid for a JAX-RS root
     * resource class.
     * 
     * @param paramAnnotationss
     * @param parameterTypes
     * @returns true, if the
     * @throws IllegalTypeException
     *             If a parameter is annotated with {@link Context}, but the
     *             type is invalid (must be UriInfo, Request or HttpHeaders).
     */
    private static boolean checkParamAnnotations(Constructor<?> constr) {
        final Annotation[][] paramAnnotationss = constr
                .getParameterAnnotations();
        final Class<?>[] parameterTypes = constr.getParameterTypes();
        for (int i = 0; i < paramAnnotationss.length; i++) {
            final Annotation[] parameterAnnotations = paramAnnotationss[i];
            final Class<?> parameterType = parameterTypes[i];
            final boolean ok = checkParameterAnnotation(parameterAnnotations,
                    parameterType);
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks, if the annotations are valid for a runtime environment handled
     * constructor.
     * 
     * @param parameterAnnotations
     * @param parameterType
     * @return
     */
    private static boolean checkParameterAnnotation(
            Annotation[] parameterAnnotations, Class<?> parameterType) {
        if (parameterAnnotations.length == 0) {
            return false;
        }
        for (final Annotation annotation : parameterAnnotations) {
            final Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                continue;
            } else if (annotationType.equals(PathParam.class)) {
                continue;
            } else if (annotationType.equals(Context.class)) {
                if (parameterType.equals(UriInfo.class)) {
                    continue;
                }
                if (parameterType.equals(Request.class)) {
                    continue;
                }
                if (parameterType.equals(HttpHeaders.class)) {
                    continue;
                }
                if (parameterType.equals(SecurityContext.class)) {
                    continue;
                }
                return false;
            } else if (annotationType.equals(MatrixParam.class)) {
                continue;
            } else if (annotationType.equals(QueryParam.class)) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Converts the given mimes to a List of MediaTypes. Will never returns
     * null.
     * 
     * @param mimes
     * @return Returns an unmodifiable List of MediaTypes
     */
    public static List<MediaType> convertToMediaTypes(String[] mimes) {
        final List<MediaType> mediaTypes;
        mediaTypes = new ArrayList<MediaType>(mimes.length);
        for (String mime : mimes) {
            if (mime == null) {
                mediaTypes.add(MediaType.ALL);
            } else {
                mediaTypes.add(MediaType.valueOf(mime));
            }
        }
        return Collections.unmodifiableList(mediaTypes);
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param constructor
     * @param args
     * @return the created instance
     * @throws InvocationTargetException
     * @throws InstantiateException
     */
    public static Object createInstance(Constructor<?> constructor,
            Object... args) throws InvocationTargetException,
            InstantiateException {
        try {
            return constructor.newInstance(args);
        } catch (IllegalArgumentException e) {
            throw new InstantiateException("Could not instantiate "
                    + constructor.getDeclaringClass(), e);
        } catch (InstantiationException e) {
            throw new InstantiateException("Could not instantiate "
                    + constructor.getDeclaringClass(), e);
        } catch (IllegalAccessException e) {
            throw new InstantiateException("Could not instantiate "
                    + constructor.getDeclaringClass(), e);
        }
    }

    /**
     * Finds the constructor to use by the JAX-RS runtime.
     * 
     * @param jaxRsClass
     *            the root resource or provider class.
     * @param rrcOrProvider
     *            "root resource class" or "provider"
     * @return Returns the constructor to use for the given root resource class
     *         or provider. If no constructor could be found, null is returned.
     *         Than try {@link Class#newInstance()}
     * @throws MissingConstructorException
     */
    public static Constructor<?> findJaxRsConstructor(Class<?> jaxRsClass,
            String rrcOrProvider) throws MissingConstructorException {
        Constructor<?> constructor = null;
        int constructorParamNo = Integer.MIN_VALUE;
        for (final Constructor<?> constr : jaxRsClass.getConstructors()) {
            if (!Modifier.isPublic(constr.getModifiers())) {
                continue;
            }
            final int constrParamNo = constr.getParameterTypes().length;
            // TODo warn if multiple constrs are possible (see spec f. details)
            if (constrParamNo <= constructorParamNo) {
                continue; // ignore this constructor
            }
            if (!checkParamAnnotations(constr)) {
                continue; // ignore this constructor
            }
            constructor = constr;
            constructorParamNo = constrParamNo;
        }
        if (constructor != null) {
            return constructor;
        }
        throw new MissingConstructorException(jaxRsClass, rrcOrProvider);
    }

    /**
     * Returns the HTTP method related to the given java method.
     * 
     * @param javaMethod
     * @return
     */
    static org.restlet.data.Method getHttpMethod(Method javaMethod) {
        for (final Annotation annotation : javaMethod.getAnnotations()) {
            final Class<? extends Annotation> annoType = annotation
                    .annotationType();
            final HttpMethod httpMethodAnnot = annoType
                    .getAnnotation(HttpMethod.class);
            if (httpMethodAnnot != null) {
                // Annotation of Annotation of the method is the HTTP-Method
                final String httpMethodName = httpMethodAnnot.value();
                return org.restlet.data.Method.valueOf(httpMethodName);
                // NICE check if another designator is available: reject or warn
            }
        }
        return null;
    }

    /**
     * Returns the value from the given Parameter. If the given parameter is
     * null, null will returned. If the parameter is not null, but it's value,
     * "" is returned.
     * 
     * @param parameter
     * @return the value from the given Parameter. If the given parameter is
     *         null, null will returned. If the parameter is not null, but it's
     *         value, "" is returned.
     */
    public static String getValue(Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        final String paramValue = parameter.getValue();
        if (paramValue == null) {
            return "";
        }
        return paramValue;
    }

    /**
     * Checks, if the given method is a bean setter and annotated with the given
     * annotation. If it is a bean setter, the accessible attribute of is set
     * the method is set to true.
     * 
     * @param method
     * @param annotationClass
     * @return true, if the method is a bean setter, or false if not
     * @throws SecurityException
     */
    public static boolean isBeanSetter(Method method,
            Class<? extends Annotation> annotationClass)
            throws SecurityException {
        if (method.isAnnotationPresent(annotationClass)
                && method.getName().startsWith("set")
                && (method.getParameterTypes().length == 1)) {
            return true;
        }
        return false;
    }

    /**
     * Checks, if the method is volatile(the return type of a sub class differs
     * from the return type of the superclass, but is compatibel).
     * 
     * @param javaMethod
     * @return true, if the method is volatile, otherwise false.
     */
    static boolean isVolatile(Method javaMethod) {
        return Modifier.isVolatile(javaMethod.getModifiers());
    }
}