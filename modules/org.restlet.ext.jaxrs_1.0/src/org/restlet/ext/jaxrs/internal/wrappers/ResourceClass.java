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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.EMPTY_FIELD_ARRAY;
import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.getContextResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Instances represents a root resource class.
 * 
 * A Java class that uses JAX-RS annotations to implement a corresponding Web
 * resource, see chapter 3 of JAX-RS specification
 * 
 * @author Stephan Koops
 */
public class ResourceClass extends AbstractJaxRsWrapper {

    private static final String JAX_RS_PACKAGE_PREFIX = "javax.ws.rs";

    /**
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
     * @param jaxRsClass
     * @return the path annotation or null, if no is present and requirePath is
     *         false.
     * @throws MissingAnnotationException
     *                 if the
     * @throws IllegalArgumentException
     *                 if the jaxRsClass is null and requirePath is true.
     */
    public static Path getPathAnnotation(Class<?> jaxRsClass)
            throws MissingAnnotationException, IllegalArgumentException {
        if (jaxRsClass == null)
            throw new IllegalArgumentException(
                    "The jaxRsClass must not be null");
        Path path = jaxRsClass.getAnnotation(Path.class);
        if (path == null)
            throw new MissingAnnotationException(
                    "The root resource class does not have a @Path annotation");
        return path;
    }

    /**
     * @param resource
     * @return Returns the path template as String. Never returns null.
     * @throws IllegalPathOnClassException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     */
    public static String getPathTemplate(Class<?> resource)
            throws IllegalPathOnClassException, MissingAnnotationException,
            IllegalArgumentException {
        try {
            return getPathTemplate(getPathAnnotation(resource));
        } catch (IllegalPathException e) {
            throw new IllegalPathOnClassException(e);
        }
    }

    /**
     * Caches the allowed methods (unmodifiable) for given remainingParts.
     */
    private Map<RemainingPath, Set<org.restlet.data.Method>> allowedMethods = new HashMap<RemainingPath, Set<org.restlet.data.Method>>();

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext}.<br>
     * (Array is round about 10 times faster than the list.)
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see UriInfo
     * @see SecurityContext
     * @see Request
     * @see HttpHeaders
     */
    private Field[] injectFieldsCallContext;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link ContextResolver}.
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see ContextResolver
     */
    private Field[] injectFieldsContextResolvers;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link MessageBodyWorkers}.
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see MessageBodyWorkers
     */
    private Field[] injectFieldsMbWorkers;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext}.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsClientInfo;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext}.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsConditions;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a cookie parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsCookieParam;

    // LATER chaching, not new for every resource class creation

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a header parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsHeaderParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a matrix parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsMatrixParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a path parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsPathParam;

    /**
     * <p>
     * Fields of the wrapped JAX-RS resource class to inject a query parameter.
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsQueryParam;

    protected Class<?> jaxRsClass;

    /**
     * is true, if the resource class is annotated with &#64;Path. Is available
     * after constructor was running.
     */
    boolean leaveEncoded;

    private Collection<SubResourceLocator> subResourceLocators;

    private Collection<ResourceMethod> subResourceMethods;

    private Collection<ResourceMethodOrLocator> subResourceMethodsAndLocators;

    /**
     * Creates a new root resource class wrapper. Will not set the path, because
     * it is not available for a normal resource class.
     * 
     * @param jaxRsClass
     * @param logger
     *                The logger to log warnings, if the class is not valid.
     * @see WrapperFactory#getResourceClass(Class)
     */
    ResourceClass(Class<?> jaxRsClass, Logger logger) {
        super();
        this.init(jaxRsClass, logger);
    }

    /**
     * Creates a new root resource class wrapper.
     * 
     * @param jaxRsClass
     * @param logger
     * @param sameLogger
     *                the subclass RootResourceClass must call this constructor.
     *                This Object is ignored.
     * @throws IllegalArgumentException
     * @throws IllegalPathOnClassException
     * @throws MissingAnnotationException
     *                 if &#64;{@link Path} is missing on the jaxRsClass
     * @see WrapperFactory#getResourceClass(Class)
     */
    protected ResourceClass(Class<?> jaxRsClass, Logger logger,
            @SuppressWarnings("unused")
            Logger sameLogger) throws IllegalArgumentException,
            IllegalPathOnClassException, MissingAnnotationException {
        super(PathRegExp.createForClass(jaxRsClass));
        this.init(jaxRsClass, logger);
    }

    /**
     * Checks, if the method is not volatile and public. If the method is not
     * public, a warning is logged and true returned. If the method is volatile
     * (this occurs, if the return type of a sub class differs from the return
     * type of the superclass, but is compatibel), true is returned, but no
     * message logged. Otherwise anything is ok and false is returned.
     * 
     * @param javaMethod
     * @param logger
     *                The Logger to log the warning
     * @return true, if the method is not public, false if it is public.
     */
    private boolean checkResMethodVolatileOrNotPublic(Method javaMethod,
            Logger logger) {
        final int modifiers = javaMethod.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            logger.warning("The method " + javaMethod + " must be public");
            return false;
        }
        if (Modifier.isVolatile(modifiers)) {
            return false;
        }
        return true;
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
    public Set<org.restlet.data.Method> getAllowedMethods(
            RemainingPath remainingPath) {
        Set<org.restlet.data.Method> allowedMethods = this.allowedMethods
                .get(remainingPath);
        if (allowedMethods != null)
            return allowedMethods;
        allowedMethods = new HashSet<org.restlet.data.Method>(6);
        for (ResourceMethod rm : getMethodsForPath(remainingPath))
            allowedMethods.add(rm.getHttpMethod());
        if (!allowedMethods.isEmpty())
            if (allowedMethods.contains(org.restlet.data.Method.GET))
                allowedMethods.add(org.restlet.data.Method.HEAD);
        Set<org.restlet.data.Method> unmodifiable = Collections
                .unmodifiableSet(allowedMethods);
        this.allowedMethods.put(remainingPath, unmodifiable);
        return unmodifiable;
    }

    /**
     * Returns the method with the annotations, corresponding to the given
     * method. If the given method contains any JAX-RS annotations, it is
     * returned. If it is not annotated with JAX-RS annotations, this method
     * looks recursive in the subclass and the implemented interfaces, until it
     * found one. This would be returned.
     * 
     * @param javaMethod
     *                The java method to look for annotations
     * @return the founded method, or null, if no method with annotations was
     *         found. Returns also null, if null was given.
     */
    private Method getAnnotatedJavaMethod(Method javaMethod) {
        if (javaMethod == null)
            return null;
        boolean useMethod = checkForJaxRsAnnotations(javaMethod);
        if (useMethod)
            return javaMethod;
        Class<?> methodClass = javaMethod.getDeclaringClass();
        Class<?> superclass = methodClass.getSuperclass();
        Method scMethod = getMethodFromClass(superclass, javaMethod);
        Method annotatedMeth = getAnnotatedJavaMethod(scMethod);
        if (annotatedMeth != null)
            return annotatedMeth;
        Class<?>[] interfaces = methodClass.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            Method ifMethod = getMethodFromClass(interfaze, javaMethod);
            annotatedMeth = getAnnotatedJavaMethod(ifMethod);
            if (annotatedMeth != null)
                return annotatedMeth;
        }
        return null;
    }

    /**
     * @return Returns the wrapped root resource class.
     */
    public final Class<?> getJaxRsClass() {
        return jaxRsClass;
    }

    /**
     * Looks for the method with the same signature as the given method in the
     * given class.
     * 
     * @param clazz
     *                The Class to look for the method.
     * @param subClassMethod
     *                the Method to look for it's signature in the given class.
     * @return the method in the given class, with the same signature as given
     *         method, or null if such method is not available. Returns also
     *         null, if the given class is null.
     */
    private Method getMethodFromClass(Class<?> clazz, Method subClassMethod) {
        if (clazz == null)
            return null;
        String methodName = subClassMethod.getName();
        Class<?>[] parameterTypes = subClassMethod.getParameterTypes();
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
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
    public Collection<ResourceMethod> getMethodsForPath(
            RemainingPath remainingPath) {
        // LATER results may be chached, if any method is returned.
        // The 404 case will be called rarely and produce a lot of cached data.
        List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        Iterable<ResourceMethod> subResourceMethods = this
                .getSubResourceMethods();
        for (ResourceMethod method : subResourceMethods) {
            PathRegExp methodPath = method.getPathRegExp();
            if (remainingPath.isEmptyOrSlash()) {
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
     * @return returns the name of the wrapped class
     */
    public String getName() {
        return this.jaxRsClass.getName();
    }

    /**
     * @return Returns the sub resource locators of the given class.
     */
    public final Iterable<SubResourceLocator> getSubResourceLocators() {
        return subResourceLocators;
    }

    /**
     * @return Return the sub resource methods of the given class.
     */
    public final Iterable<ResourceMethod> getSubResourceMethods() {
        return this.subResourceMethods;
    }

    /**
     * @return Returns the sub resource locatores and sub resource methods.
     */
    public final Collection<ResourceMethodOrLocator> getSubResourceMethodsAndLocators() {
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

    /**
     * @param jaxRsClass
     * @param logger
     * @throws SecurityException
     * @throws IllegalPathOnMethodException
     */
    private void init(Class<?> jaxRsClass, Logger logger) {
        this.jaxRsClass = jaxRsClass;
        this.leaveEncoded = jaxRsClass.isAnnotationPresent(Encoded.class);
        initResourceMethodsAndLocators(logger);
        initInjectFields();
    }

    /**
     * initiates the fields to cache thie fields that needs injection.
     * 
     * @throws SecurityException
     */
    private void initInjectFields() {
        List<Field> ifContext = new ArrayList<Field>(3);
        List<Field> ifMbWorkers = new ArrayList<Field>(1);
        List<Field> ifContRs = new ArrayList<Field>(1);
        List<Field> ifClientInfo = new ArrayList<Field>(1);
        List<Field> ifConditions = new ArrayList<Field>(1);
        List<Field> ifCookieParam = new ArrayList<Field>(1);
        List<Field> ifHeaderParam = new ArrayList<Field>(1);
        List<Field> ifMatrixParam = new ArrayList<Field>(1);
        List<Field> ifPathParam = new ArrayList<Field>(1);
        List<Field> ifQueryParam = new ArrayList<Field>(1);
        Class<?> jaxRsClass2 = this.jaxRsClass;
        do {
            for (Field field : jaxRsClass2.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Context.class)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.equals(ClientInfo.class))
                        ifClientInfo.add(field);
                    else if (fieldType.equals(Conditions.class))
                        ifConditions.add(field);
                    else if (fieldType.equals(MessageBodyWorkers.class))
                        ifMbWorkers.add(field);
                    else if (fieldType.equals(ContextResolver.class))
                        ifContRs.add(field);
                    else
                        ifContext.add(field);
                } else if (Util.isAnnotationPresentExt(field, PathParam.class))
                    ifPathParam.add(field);
                else if (Util.isAnnotationPresentExt(field, CookieParam.class))
                    ifCookieParam.add(field);
                else if (Util.isAnnotationPresentExt(field, HeaderParam.class))
                    ifHeaderParam.add(field);
                else if (Util.isAnnotationPresentExt(field, MatrixParam.class))
                    ifMatrixParam.add(field);
                else if (Util.isAnnotationPresentExt(field, QueryParam.class))
                    ifQueryParam.add(field);
            }
            jaxRsClass2 = jaxRsClass2.getSuperclass();
        } while (jaxRsClass2 != null);
        this.injectFieldsCallContext = ifContext.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsMbWorkers = ifMbWorkers.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsContextResolvers = ifContRs.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsClientInfo = ifClientInfo.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsConditions = ifConditions.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsCookieParam = ifCookieParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsHeaderParam = ifHeaderParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsMatrixParam = ifMatrixParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsPathParam = ifPathParam.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsQueryParam = ifQueryParam.toArray(EMPTY_FIELD_ARRAY);
    }

    private void initResourceMethodsAndLocators(Logger logger) {
        Collection<ResourceMethodOrLocator> srmls = new ArrayList<ResourceMethodOrLocator>();
        Collection<ResourceMethod> subRsesMeths = new ArrayList<ResourceMethod>();
        Collection<SubResourceLocator> subResLocs = new ArrayList<SubResourceLocator>();
        Method[] classMethods = jaxRsClass.getDeclaredMethods();
        for (Method execMethod : classMethods) {
            Method annotatedMethod = getAnnotatedJavaMethod(execMethod);
            if (annotatedMethod == null)
                continue;
            Path path = annotatedMethod.getAnnotation(Path.class);
            org.restlet.data.Method httpMethod;
            httpMethod = ResourceMethod.getHttpMethod(annotatedMethod);
            try {
                if (httpMethod != null) {
                    if (!checkResMethodVolatileOrNotPublic(execMethod, logger))
                        continue;
                    ResourceMethod subResMeth = new ResourceMethod(execMethod,
                            annotatedMethod, this, httpMethod);
                    subRsesMeths.add(subResMeth);
                    srmls.add(subResMeth);
                    checkForPrimitiveParameters(execMethod, logger);
                } else {
                    if (path != null) {
                        if (!checkResMethodVolatileOrNotPublic(execMethod,
                                logger))
                            continue;
                        SubResourceLocator subResLoc = new SubResourceLocator(
                                execMethod, annotatedMethod, this);
                        subResLocs.add(subResLoc);
                        srmls.add(subResLoc);
                        checkForPrimitiveParameters(execMethod, logger);
                    }
                }
            } catch (IllegalPathOnMethodException e) {
                logger.warning("The method " + annotatedMethod
                        + " is annotated with an illegal path: " + e.getPath()
                        + ". Ignoring this method. (" + e.getMessage() + ")");
            }
        }
        this.subResourceLocators = subResLocs;
        this.subResourceMethods = subRsesMeths;
        this.subResourceMethodsAndLocators = srmls;
    }

    /**
     * Warn, if one of the message parameters is primitive.
     * 
     * @param execMethod
     * @param logger
     */
    private void checkForPrimitiveParameters(Method execMethod, Logger logger) {
        Class<?>[] paramTypes = execMethod.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (paramType.isPrimitive()) {
                logger.config("The method " + execMethod
                        + " contains a primitive parameter " + paramType + ".");
                logger
                        .config("It is recommended to use it's wrapper class. If no value could be read from the request, now you would got the default value. If you use the wrapper class, you would get null.");
                break;
            }
        }
    }

    /**
     * Initiates the resource object:
     * <ul>
     * <li>Injects all the supported dependencies into the the given resource
     * object of this class.</li>
     * <li>Calls the method annotated with &#64;{@link PostConstruct}, see
     * JSR-250.</li>
     * </ul>
     * 
     * @param resourceObject
     * @param callContext
     *                The CallContext to get the dependencies from.
     * @param allResolvers
     *                all available wrapped {@link ContextResolver}s.
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     * @throws ConvertCookieParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertQueryParamException
     */
    void init(ResourceObject resourceObject, CallContext callContext,
            Collection<org.restlet.ext.jaxrs.internal.wrappers.ContextResolver<?>> allResolvers)
            throws InjectException, WebApplicationException,
            ConvertCookieParamException, ConvertHeaderParamException,
            ConvertMatrixParamException, ConvertPathParamException,
            ConvertQueryParamException {
        Object jaxRsResObj = resourceObject.getJaxRsResourceObject();
        for (Field contextField : this.injectFieldsCallContext) {
            Util.inject(jaxRsResObj, contextField, callContext);
        }
        for (Field field : this.injectFieldsContextResolvers) {
            ContextResolver<?> contextResolver;
            contextResolver = getContextResolver(field, allResolvers);
            Util.inject(jaxRsResObj, field, contextResolver);
        }
        for (Field mbwField : this.injectFieldsMbWorkers) {
            Object messageBodyWorkers = null;
            Util.inject(jaxRsResObj, mbwField, messageBodyWorkers);
            // TODO inject MessageBodyWorker to resource classes
        }
        for (Field clientInfoField : this.injectFieldsClientInfo) {
            ClientInfo clientInfo = callContext.getRequest().getClientInfo();
            Util.inject(jaxRsResObj, clientInfoField, clientInfo);
        }
        for (Field conditionsField : this.injectFieldsConditions) {
            Conditions conditions = callContext.getRequest().getConditions();
            Util.inject(jaxRsResObj, conditionsField, conditions);
        }
        for (Field cpf : this.injectFieldsCookieParam) {
            CookieParam headerParam = cpf.getAnnotation(CookieParam.class);
            DefaultValue defaultValue = cpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = cpf.getType();
            Type paramGenericType = cpf.getGenericType();
            Object value = getCookieParamValue(convTo, paramGenericType,
                    headerParam, defaultValue, callContext);
            Util.inject(jaxRsResObj, cpf, value);
        }
        for (Field hpf : this.injectFieldsHeaderParam) {
            HeaderParam headerParam = hpf.getAnnotation(HeaderParam.class);
            DefaultValue defaultValue = hpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = hpf.getType();
            Type paramGenericType = hpf.getGenericType();
            Object value = getHeaderParamValue(convTo, paramGenericType,
                    headerParam, defaultValue, callContext);
            Util.inject(jaxRsResObj, hpf, value);
        }
        for (Field mpf : this.injectFieldsMatrixParam) {
            MatrixParam headerParam = mpf.getAnnotation(MatrixParam.class);
            DefaultValue defaultValue = mpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = mpf.getType();
            Type paramGenericType = mpf.getGenericType();
            Object value = getMatrixParamValue(convTo, paramGenericType,
                    headerParam, leaveEncoded, defaultValue, callContext);
            Util.inject(jaxRsResObj, mpf, value);
        }
        for (Field ppf : this.injectFieldsPathParam) {
            PathParam headerParam = ppf.getAnnotation(PathParam.class);
            DefaultValue defaultValue = ppf.getAnnotation(DefaultValue.class);
            Class<?> convTo = ppf.getType();
            Type paramGenericType = ppf.getGenericType();
            Object value = getPathParamValue(convTo, paramGenericType,
                    headerParam, leaveEncoded, defaultValue, callContext);
            Util.inject(jaxRsResObj, ppf, value);
        }
        for (Field cpf : this.injectFieldsQueryParam) {
            QueryParam headerParam = cpf.getAnnotation(QueryParam.class);
            DefaultValue defaultValue = cpf.getAnnotation(DefaultValue.class);
            Class<?> convTo = cpf.getType();
            Type paramGenericType = cpf.getGenericType();
            Object value = getQueryParamValue(convTo, paramGenericType,
                    headerParam, leaveEncoded, defaultValue, callContext,
                    Logger.getAnonymousLogger());
            Util.inject(jaxRsResObj, cpf, value);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.jaxRsClass + "]";
    }
}