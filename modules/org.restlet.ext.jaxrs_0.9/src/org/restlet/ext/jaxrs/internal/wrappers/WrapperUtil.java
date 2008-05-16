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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ContextResolverCollection;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ReturnNullContextResolver;

/**
 * Utility methods for the wrappers.
 * 
 * @author Stephan Koops
 */
public class WrapperUtil {

    private static final String COLL_PARAM_NOT_DEFAULT = "The collection type Collection is not supported for parameters. Use List, Set or SortedSet";

    private static final Boolean DEFAULT_BOOLEAN = Boolean.FALSE;

    private static final Byte DEFAULT_BYTE = (byte) 0;

    private static final Character DEFAULT_CHAR = new Character('\0');

    private static final Double DEFAULT_DOUBLE = 0d;

    private static final Float DEFAULT_FLOAT = 0.0f;

    private static final Integer DEFAULT_INT = 0;

    private static final Long DEFAULT_LONG = new Long(0);

    private static final Short DEFAULT_SHORT = 0;

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
     * Converts the given value without any decoding.
     * 
     * @param paramClass
     * @param paramValue
     * @param defaultValue
     * @return
     * @throws ConvertParameterException
     * @throws WebApplicationException
     *                 if the conversion method throws an
     *                 WebApplicationException.
     */
    private static Object convertParamValueFromParam(Class<?> paramClass,
            String paramValue, DefaultValue defaultValue)
            throws ConvertParameterException, WebApplicationException {
        WebApplicationException constructorWae = null;
        try {
            Constructor<?> constr = paramClass.getConstructor(String.class);
            return constr.newInstance(paramValue);
        } catch (WebApplicationException wae) {
            constructorWae = wae;
        } catch (Exception e) {
            // try valueOf(String) as next step
        }
        Method valueOf;
        try {
            valueOf = paramClass.getMethod("valueOf", String.class);
        } catch (SecurityException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (NoSuchMethodException e) {
            throw ConvertParameterException.object(paramClass, paramValue, e);
        }
        try {
            return valueOf.invoke(null, paramValue);
        } catch (IllegalArgumentException e) {
            if (constructorWae != null)
                throw constructorWae;
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (IllegalAccessException e) {
            if (constructorWae != null)
                throw constructorWae;
            throw ConvertParameterException.object(paramClass, paramValue, e);
        } catch (InvocationTargetException ite) {
            if (constructorWae != null)
                throw constructorWae;
            Throwable cause = ite.getCause();
            if (cause instanceof WebApplicationException)
                throw (WebApplicationException) cause;
            if ((paramValue == null || paramValue.length() <= 0)
                    && (ite.getCause() instanceof IllegalArgumentException)) {
                if (defaultValue == null)
                    return null;
                else {
                    String dfv = defaultValue.value();
                    return convertParamValueFromParam(paramClass, dfv, null);
                }
            }
            throw ConvertParameterException.object(paramClass, paramValue, ite);
        }
    }

    /**
     * Converts the given paramValue (found in the path, query, matrix or
     * header) into the given paramClass.
     * 
     * @param paramClass
     *                the type of the parameter to convert to
     * @param paramValue
     * @param defaultValue
     *                see {@link DefaultValue}
     * @param leaveEncoded
     *                if true, leave {@link QueryParam}s, {@link MatrixParam}s
     *                and {@link PathParam}s encoded. Must be FALSE for
     *                {@link HeaderParam}s.
     * @param jaxRsRouter
     * @return
     * @throws ConvertParameterException
     * @see PathParam
     * @see MatrixParam
     * @see QueryParam
     * @see HeaderParam
     */
    private static Object convertParamValueFromParam(Class<?> paramClass,
            String paramValue, DefaultValue defaultValue, boolean leaveEncoded)
            throws ConvertParameterException {
        if (!leaveEncoded && paramValue != null)
            paramValue = Reference.decode(paramValue);
        else if (paramValue == null && defaultValue != null)
            paramValue = defaultValue.value();
        if (paramClass.equals(String.class)) // optimization
            return paramValue;
        if (paramClass.isPrimitive()) {
            if (paramValue != null && paramValue.length() <= 0)
                paramValue = defaultValue.value();
            return getParamValueForPrimitive(paramClass, paramValue);
        }
        return convertParamValueFromParam(paramClass, paramValue, defaultValue);
    }

    /**
     * @param paramClass
     * @param paramGenericType
     * @param paramValueIter
     *                the values to use if multiples are required
     * @param paramValue
     *                the value, if only one is needed.
     * @param defaultValue
     * @param leaveEncoded
     * @return
     * @throws ConvertParameterException
     */
    public static Object convertParamValuesFromParam(Class<?> paramClass,
            Type paramGenericType, Iterator<String> paramValueIter,
            String paramValue, DefaultValue defaultValue, boolean leaveEncoded)
            throws ConvertParameterException {
        boolean toArray = false;
        Collection<Object> coll = null;
        if (paramClass.isArray()) {
            coll = new ArrayList<Object>(1);
            toArray = true;
            paramClass = paramClass.getComponentType();
        } else if (paramGenericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) paramGenericType;
            coll = createColl(parameterizedType);
            paramClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        if (coll == null) { // no collection type
            return convertParamValueFromParam(paramClass, paramValue,
                    defaultValue, leaveEncoded);
        }
        while (paramValueIter.hasNext()) {
            String queryParamValue = paramValueIter.next();
            Object convertedValue = convertParamValueFromParam(paramClass,
                    queryParamValue, defaultValue, leaveEncoded);
            if (convertedValue != null)
                coll.add(convertedValue);
            defaultValue = null;
        }
        if (coll.isEmpty()) // add default value
            coll.add(convertParamValueFromParam(paramClass, paramValue,
                    defaultValue, leaveEncoded));
        if (toArray)
            return Util.toArray(coll, paramClass);
        return coll;
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
     * Creates the collection for the given
     * {@link ParameterizedType parametrized Type}.<br>
     * If the given type do not represent an collection, null is returned.
     * 
     * @param <A> 
     * @param type
     * @return the created collection or null.
     */
    public static <A> Collection<A> createColl(ParameterizedType type) {
        Type rawType = type.getRawType();
        if (rawType.equals(List.class))
            return new ArrayList<A>(1);
        else if (rawType.equals(Set.class))
            return new HashSet<A>(2);
        else if (rawType.equals(SortedSet.class))
            return new TreeSet<A>();
        else if (rawType.equals(Collection.class)) {
            Logger logger = Logger.getAnonymousLogger();
            logger.config(COLL_PARAM_NOT_DEFAULT);
            return new ArrayList<A>();
        }
        return null;
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
            }
        }
        return null;
    }

    /**
     * @param paramClass
     * @param paramValue
     * @throws ConvertParameterException
     */
    private static Object getParamValueForPrimitive(Class<?> paramClass,
            String paramValue) throws ConvertParameterException {
        try {
            if (paramClass == Integer.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_INT;
                return new Integer(paramValue);
            }
            if (paramClass == Double.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_DOUBLE;
                return new Double(paramValue);
            }
            if (paramClass == Float.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_FLOAT;
                return new Float(paramValue);
            }
            if (paramClass == Byte.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_BYTE;
                return new Byte(paramValue);
            }
            if (paramClass == Long.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_LONG;
                return new Long(paramValue);
            }
            if (paramClass == Short.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_SHORT;
                return new Short(paramValue);
            }
            if (paramClass == Character.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_CHAR;
                if (paramValue.length() == 1)
                    return paramValue.charAt(0);
                throw ConvertParameterException.primitive(paramClass,
                        paramValue, null);
            }
            if (paramClass == Boolean.TYPE) {
                if ((paramValue == null || paramValue.length() <= 0))
                    return DEFAULT_BOOLEAN;
                if (paramValue.equalsIgnoreCase("true"))
                    return Boolean.TRUE;
                if (paramValue.equalsIgnoreCase("false"))
                    return Boolean.FALSE;
                throw ConvertParameterException.primitive(paramClass,
                        paramValue, null);
            }
        } catch (IllegalArgumentException e) {
            throw ConvertParameterException
                    .primitive(paramClass, paramValue, e);
        }
        String warning;
        if (paramClass == Void.TYPE)
            warning = "an object should be converted to a void; but this could not be here";
        else
            warning = "an object should be converted to a " + paramClass
                    + ", but here are only primitives allowed.";
        Logger.getAnonymousLogger().warning(warning);
        ResponseBuilder rb = javax.ws.rs.core.Response.serverError();
        rb.entity(warning);
        throw new WebApplicationException(rb.build());
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
            method.setAccessible(true);
            return true;
        }
        return false;
    }
}