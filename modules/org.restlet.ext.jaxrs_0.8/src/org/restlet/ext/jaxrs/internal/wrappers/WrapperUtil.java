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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ContextResolverCollection;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ReturnNullContextResolver;

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
     *                Java method, class or something like that.
     * @return true, if the given accessible object is annotated with any
     *         JAX-RS-Annotation.
     */
    static boolean checkForJaxRsAnnotations(Method javaMethod) {
        for (Annotation annotation : javaMethod.getAnnotations()) {
            Class<? extends Annotation> annoType = annotation.annotationType();
            if (annoType.getName().startsWith(JAX_RS_PACKAGE_PREFIX))
                return true;
            if (annoType.isAnnotationPresent(HttpMethod.class))
                return true;
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
     *                 If a parameter is annotated with {@link Context}, but
     *                 the type is invalid (must be UriInfo, Request or
     *                 HttpHeaders).
     */
    private static boolean checkParamAnnotations(Constructor<?> constr) {
        Annotation[][] paramAnnotationss = constr.getParameterAnnotations();
        Class<?>[] parameterTypes = constr.getParameterTypes();
        for (int i = 0; i < paramAnnotationss.length; i++) {
            Annotation[] parameterAnnotations = paramAnnotationss[i];
            Class<?> parameterType = parameterTypes[i];
            boolean ok = checkParameterAnnotation(parameterAnnotations,
                    parameterType);
            if (!ok)
                return false;
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
        if (parameterAnnotations.length == 0)
            return false;
        for (Annotation annotation : parameterAnnotations) {
            Class<? extends Annotation> annotationType = annotation
                    .annotationType();
            if (annotationType.equals(HeaderParam.class)) {
                continue;
            } else if (annotationType.equals(PathParam.class)) {
                continue;
            } else if (annotationType.equals(Context.class)) {
                if (parameterType.equals(UriInfo.class))
                    continue;
                if (parameterType.equals(Request.class))
                    continue;
                if (parameterType.equals(HttpHeaders.class))
                    continue;
                if (parameterType.equals(SecurityContext.class))
                    continue;
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
     * Checks, if the method is volatile(the return type of a sub class differs
     * from the return type of the superclass, but is compatibel).
     * 
     * @param javaMethod
     * @return true, if the method is volatile, otherwise false.
     */
    static boolean isVolatile(Method javaMethod) {
        return Modifier.isVolatile(javaMethod.getModifiers());
    }

    /**
     * Converts the given mimes to a List of MediaTypes. Will never returns
     * null.
     * 
     * @param mimes
     * @return Returns an unmodifiable List of MediaTypes
     */
    public static List<MediaType> convertToMediaTypes(String[] mimes) {
        List<MediaType> mediaTypes = new ArrayList<MediaType>(mimes.length);
        for (String mime : mimes) {
            if (mime == null)
                mediaTypes.add(MediaType.ALL);
            else
                mediaTypes.add(MediaType.valueOf(mime));
        }
        return Collections.unmodifiableList(mediaTypes);
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param constructor
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws InstantiateException
     */
    public static Object createInstance(Constructor<?> constructor,
            Object[] args) throws InvocationTargetException,
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
     *                the root resource or provider class.
     * @param rrcOrProvider
     *                "root resource class" or "provider"
     * @return Returns the constructor to use for the given root resource class
     *         or provider. If no constructor could be found, null is returned.
     *         Than try {@link Class#newInstance()}
     * @throws MissingConstructorException
     */
    public static Constructor<?> findJaxRsConstructor(Class<?> jaxRsClass,
            String rrcOrProvider) throws MissingConstructorException {
        Constructor<?> constructor = null;
        int constructorParamNo = Integer.MIN_VALUE;
        for (Constructor<?> constr : jaxRsClass.getConstructors()) {
            if (!Modifier.isPublic(constr.getModifiers()))
                continue;
            int constrParamNo = constr.getParameterTypes().length;
            if (constrParamNo <= constructorParamNo)
                continue; // ignore this constructor
            if (!checkParamAnnotations(constr))
                continue; // ignore this constructor
            constructor = constr;
            constructorParamNo = constrParamNo;
        }
        if (constructor != null)
            return constructor;
        throw new MissingConstructorException(jaxRsClass, rrcOrProvider);
    }

    /**
     * Creates the {@link ContextResolver} to inject in the given field.
     * 
     * @param genType
     *                generic type of field {@link ContextResolver}.
     * @param allCtxResolvers
     * @return
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    public static ContextResolver<?> getContextResolver(Type genType,
            Collection<ContextResolver<?>> allCtxResolvers) {
        if (!(genType instanceof ParameterizedType))
            return ReturnNullContextResolver.get();
        Type t = ((ParameterizedType) genType).getActualTypeArguments()[0];
        if (!(t instanceof Class))
            return ReturnNullContextResolver.get();
        Class crType = (Class) t;
        List<ContextResolver<?>> returnResolvers = new ArrayList<javax.ws.rs.ext.ContextResolver<?>>();
        for (ContextResolver<?> cr : allCtxResolvers) {
            Class<?> crClaz = cr.getClass();
            Class<?> genClass = getCtxResGenClass(crClaz);
            if (genClass == null || !genClass.equals(crType))
                continue;
            try {
                Method getContext = crClaz.getMethod("getContext", Class.class);
                if (getContext.getReturnType().equals(crType))
                    returnResolvers.add(cr);
            } catch (SecurityException e) {
                throw new RuntimeException(
                        "sorry, the method getContext(Class) of ContextResolver "
                                + crClaz + " is not accessible");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "The ContextResolver "
                                + crClaz
                                + " is not valid, because it has no method getContext(Class)");
            }
        }
        if (returnResolvers.isEmpty())
            return ReturnNullContextResolver.get();
        if (returnResolvers.size() == 1)
            return returnResolvers.get(0);
        return new ContextResolverCollection(returnResolvers);
    }

    /**
     * Returns the generic class of the given {@link ContextResolver} class.
     * 
     * @param crClaz
     */
    private static Class<?> getCtxResGenClass(Class<?> crClaz) {
        Type[] crIfTypes = crClaz.getGenericInterfaces();
        for (Type crIfType : crIfTypes) {
            if (!(crIfType instanceof ParameterizedType))
                continue;
            Type t = ((ParameterizedType) crIfType).getActualTypeArguments()[0];
            if (!(t instanceof Class))
                continue;
            return (Class<?>) t;
        }
        return null;
    }

    /**
     * Returns the HTTP method related to the given java method.
     * 
     * @param javaMethod
     * @return
     */
    static org.restlet.data.Method getHttpMethod(Method javaMethod) {
        for (Annotation annotation : javaMethod.getAnnotations()) {
            Class<? extends Annotation> annoType = annotation.annotationType();
            HttpMethod httpMethodAnnot = annoType
                    .getAnnotation(HttpMethod.class);
            if (httpMethodAnnot != null) {
                // Annotation of Annotation of the method is the HTTP-Method
                String httpMethodName = httpMethodAnnot.value();
                return org.restlet.data.Method.valueOf(httpMethodName);
                // NICE check if another designator is available: reject or warn
            }
        }
        return null;
    }

    /**
     * Returns the value from the given Parameter. If the given parameter is
     * null, null will returned. If the parameter is not null, but it's value, ""
     * is returned.
     * 
     * @param parameter
     * @return the value from the given Parameter. If the given parameter is
     *         null, null will returned. If the parameter is not null, but it's
     *         value, "" is returned.
     */
    public static String getValue(Parameter parameter) {
        if (parameter == null)
            return null;
        String paramValue = parameter.getValue();
        if (paramValue == null)
            return "";
        return paramValue;
    }

    /**
     * Checks, if the given method is a bean setter and annotated with the given
     * annotation. If it is a bean setter, the accessible attribute of is set
     * the method is set to true.
     * 
     * @param method
     * @param annotationClass
     * @return
     * @throws SecurityException
     */
    public static boolean isBeanSetter(Method method,
            Class<? extends Annotation> annotationClass)
            throws SecurityException {
        if (method.isAnnotationPresent(annotationClass)
                && method.getName().startsWith("set")
                && method.getParameterTypes().length == 1) {
            return true;
        }
        return false;
    }
}