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

package org.restlet.ext.jaxrs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.core.CallContext;
import org.restlet.ext.jaxrs.core.HttpHeaders;
import org.restlet.ext.jaxrs.exceptions.IllegalOrNoAnnotationException;
import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.exceptions.JaxRsException;
import org.restlet.ext.jaxrs.exceptions.JaxRsRuntimeException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.impl.MatchingResult;
import org.restlet.ext.jaxrs.impl.PathRegExp;
import org.restlet.ext.jaxrs.impl.WrappedRequestForHttpHeaders;
import org.restlet.ext.jaxrs.provider.JaxRsOutputRepresentation;
import org.restlet.ext.jaxrs.provider.StringProvider;
import org.restlet.ext.jaxrs.util.RemainingPath;
import org.restlet.ext.jaxrs.util.SortedMetadata;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.ext.jaxrs.util.WrappedClassLoadException;
import org.restlet.ext.jaxrs.util.WrappedLoadException;
import org.restlet.ext.jaxrs.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.wrappers.HiddenJaxRsRouter;
import org.restlet.ext.jaxrs.wrappers.MessageBodyReader;
import org.restlet.ext.jaxrs.wrappers.MessageBodyReaderSet;
import org.restlet.ext.jaxrs.wrappers.MessageBodyWriter;
import org.restlet.ext.jaxrs.wrappers.MessageBodyWriterSet;
import org.restlet.ext.jaxrs.wrappers.ResourceClass;
import org.restlet.ext.jaxrs.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.wrappers.SubResourceLocator;
import org.restlet.ext.jaxrs.wrappers.SubResourceMethod;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * <p>
 * The router choose the JAX-RS resource class and method to use for a request.
 * This class has methods {@link #attach(Class)} and {@link #detach(Class)} like
 * the Restlet {@link Router}. The variable names in this class are often the
 * same as in the JAX-RS-Definition.
 * </p>
 * <p>
 * This class is a subclass of {@link JaxRsRouterHelpMethods}. The methods to
 * handle exceptions while identifying the method that should handle the request
 * and in other situations are moved to that super class. So this class contains
 * only the real logic code and is more well arranged.
 * </p>
 * 
 * LATER The class JaxRsRouter is not thread save while attach or detach
 * classes.
 * 
 * @see <a href="https://jsr311.dev.java.net/"> Java Service Request 311</a>
 *      Because the specification is just under development the link is not set
 *      to the PDF.
 * 
 * @author Stephan Koops
 */
public class JaxRsRouter extends JaxRsRouterHelpMethods implements
        HiddenJaxRsRouter {

    /**
     * Creates a guarded JaxRsRouter. The credentials and the roles are checked
     * by the Authenticator.
     * 
     * @param context
     *                the context from the parent
     * @param authenticator
     *                the Authenticator which checks the credentials and the
     *                roles. Must not be null; see {@link AllowAllAuthenticator},
     *                {@link ForbidAllAuthenticator} or
     *                {@link ThrowExcAuthenticator}.
     * @param loadAllRootResourceClasses
     *                if true, all accessible root resource classes are loaded.
     * @param loadAllProviders
     *                if true, all accessible providers are loaded.
     * @param challangeScheme
     *                the {@link ChallengeScheme}
     * @param realmName
     *                the name of the realm, presented to the client while
     *                requesting the credentials.S
     * @return Returns the Guard. you can attach root resource classes directly.
     *         If you want to set other properties in the {@link JaxRsRouter},
     *         use the method {@link JaxRsGuard#getNext()}.
     * @see JaxRsGuard#getNext()
     */
    public static JaxRsGuard getGuarded(Context context,
            Authenticator authenticator, boolean loadAllRootResourceClasses,
            boolean loadAllProviders, ChallengeScheme challangeScheme,
            String realmName) {
        JaxRsGuard guard = new JaxRsGuard(context, challangeScheme, realmName,
                authenticator);
        guard.setNext(new JaxRsRouter(context, authenticator,
                loadAllRootResourceClasses, loadAllProviders));
        return guard;
        // LATER make some resources accessable guarded and other not.
    }

    /**
     * This set must only changed by adding or removing a root resource class to
     * this JaxRsRouter.
     * 
     * @see #attach(Class)
     * @see #detach(Class)
     */
    private Set<RootResourceClass> rootResourceClasses = new HashSet<RootResourceClass>();

    private Authenticator authenticator;

    private MessageBodyReaderSet messageBodyReaders = new MessageBodyReaderSet();

    private MessageBodyWriterSet messageBodyWriters = new MessageBodyWriterSet();

    /**
     * Creates a new JaxRsRouter with the given Context.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}
     * @param authenticator
     *                The Authenticator, must not be null. If you don't need the
     *                authentification, you can use the
     *                {@link ForbidAllAuthenticator}, the
     *                {@link AllowAllAuthenticator} or the
     *                {@link ThrowExcAuthenticator}.
     * @param loadAllRootResourceClasses
     *                If true, all accessible root resource classes are loaded.
     *                This feature is not ready implemented and tested. See also
     *                {@link #attach(Class)}
     * @param loadAllProviders
     *                If true, all accessible providers are loaded. This feature
     *                is not ready implemented and tested. See also
     *                {@link #addMessageBodyReader(Class)},
     *                {@link #addMessageBodyWriter(Class)} and
     *                {@link #addProvidersFromPackage(ClassLoader, boolean, String...)}
     */
    public JaxRsRouter(Context context, Authenticator authenticator,
            boolean loadAllRootResourceClasses, boolean loadAllProviders) {
        super(context);
        this.setAuthenticator(authenticator);
        this.loadDefaultProviders();
        if (loadAllRootResourceClasses || loadAllProviders)
            JaxRsClassesLoader.loadFromClasspath(this,
                    loadAllRootResourceClasses, loadAllProviders);
    }

    /**
     * Creates a new JaxRsRouter with the given Context. Only the default
     * providers are loaded by default.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}
     * @param authenticator
     *                The Authenticator, must not be null. If you don't need the
     *                authentification, you can use the
     *                {@link ForbidAllAuthenticator}, the
     *                {@link AllowAllAuthenticator} or the
     *                {@link ThrowExcAuthenticator}.
     * @see #JaxRsRouter(Context, Authenticator, boolean, boolean)
     */
    public JaxRsRouter(Context context, Authenticator authenticator) {
        this(context, authenticator, false, false);
    }

    /**
     * Creates a new JaxRsRouter with the given Context. Only the default
     * providers are loaded by default. If a resource class wants to check if a
     * user has a role, the request is returned with HTTP status 500 (Internal
     * Server Error).
     * 
     * @see SecurityContext#isUserInRole(String)
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}
     * @see #JaxRsRouter(Context, Authenticator, boolean, boolean)
     * @see #JaxRsRouter(Context, Authenticator)
     */
    public JaxRsRouter(Context context) {
        this(context, ThrowExcAuthenticator.getInstance(), false, false);
    }

    private void loadDefaultProviders() throws WrappedClassLoadException,
            WrappedLoadException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        JaxRsClassesLoader.loadFromPackage(classLoader, true, this, false,
                true, StringProvider.class.getPackage().getName());
    }

    /**
     * Will use the given JAX-RS root resource class.
     * 
     * @param rootResourceClass
     *                the JAX-RS root resource class to add.
     * @throws IllegalArgumentException
     * @see #detach(Class)
     * @see #getRootResourceClasses()
     */
    public void attach(Class<?> rootResourceClass)
            throws IllegalArgumentException {
        RootResourceClass newRrc = new RootResourceClass(rootResourceClass);
        PathRegExp uriTempl = newRrc.getPathRegExp();
        for (RootResourceClass rrc : this.rootResourceClasses) {
            if (rrc.getJaxRsClass().equals(rootResourceClass))
                return;// true;
            if (rrc.getPathRegExp().equals(uriTempl))
                throw new IllegalArgumentException(
                        "There is already a root resource class with path "
                                + uriTempl.getPathPattern());
        }
        rootResourceClasses.add(newRrc);
    }

    /**
     * If the automatic loading of the {@link Provider}s doesn't work, you can
     * use this method to load the providers as described in
     * {@link JaxRsClassesLoader}.
     * 
     * @param classLoader
     *                The class loader that reaches the files
     *                META-INF/services/javax.ws.rs.ext.MessageBodyWriter and
     *                META-INF/services/javax.ws.rs.ext.MessageBodyWriter with a
     *                list of the providers.
     * @param throwOnException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ClassNotFoundException
     * @see #loadProvidersLogExc(Class)
     */
    public void loadProviders(ClassLoader classLoader, boolean throwOnException)
            throws IllegalArgumentException, IOException,
            ClassNotFoundException {
        JaxRsClassesLoader.loadProvidersFromFile(classLoader, throwOnException,
                this);
    }

    /**
     * If the automatic loading of the {@link Provider}s doesn't work, you can
     * use this method to load the providers as described in
     * {@link JaxRsClassesLoader}. Occurred Exceptions were logged.
     * 
     * @param classLoader
     *                The class loader that reaches the files
     *                META-INF/services/javax.ws.rs.ext.MessageBodyWriter and
     *                META-INF/services/javax.ws.rs.ext.MessageBodyWriter with a
     *                list of the providers.
     * @see #loadProviders(Class, boolean)
     */
    public void loadProvidersLogExc(ClassLoader classLoader) {
        try {
            JaxRsClassesLoader.loadProvidersFromFile(classLoader, false, this);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not load providers", e);
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.WARNING, "Could not load providers", e);
        }
    }

    /**
     * Detaches the JAX-RS root resource class from this router.
     * 
     * @param rootResourceClass
     *                The JAX-RS root resource class to detach.
     * @return true, if the given root resource class was in the set and is
     *         removed, or false, if not.
     * @see #attach(Class)
     */
    public boolean detach(Class<?> rootResourceClass) {
        if (rootResourceClass == null)
            return false;
        Iterator<RootResourceClass> rrcIter = rootResourceClasses.iterator();
        while (rrcIter.hasNext()) {
            RootResourceClass rrc = rrcIter.next();
            if (rrc.getJaxRsClass().equals(rootResourceClass)) {
                rrcIter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a {@link javax.ws.rs.ext.MessageBodyReader} class to this
     * JaxRsRouter. Typically you don't need this method, because it is done on
     * construction time or by {@link #addProvidersFromPackage(String...)}.
     * 
     * @param messageBodyReaderClass
     *                The {@link javax.ws.rs.ext.MessageBodyReader} class to add
     *                to the JaxRsRouter.
     * @throws JaxRsRuntimeException
     *                 if the MessageBodyReader could not be added to the
     *                 JaxRsRouter.
     */
    public void addMessageBodyReader(Class<?> messageBodyReaderClass)
            throws JaxRsRuntimeException {
        Constructor<?> constructor = RootResourceClass
                .findJaxRsConstructor(messageBodyReaderClass);
        Object provider;
        try {
            provider = RootResourceClass.createInstance(constructor, false,
                    null, this);
        } catch (InstantiateParameterException e) {
            // should be not possible here
            throw new JaxRsRuntimeException(
                    "Could not instantiate the root resource class", e);
        } catch (JaxRsException e) {
            String message = "MessageBodyReader could not be instantiated";
            if (e.getMessage() != null)
                message += ": " + e.getMessage();
            throw new JaxRsRuntimeException(message, e);
        } catch (RequestHandledException e) {
            throw new JaxRsRuntimeException(
                    "MessageBodyReader could not be instantiated");
        } catch (InvocationTargetException e) {
            throw new JaxRsRuntimeException(
                    "The MessageBodyReader constructor throwed an Exception", e
                            .getCause());
        }
        this.messageBodyReaders.add(new MessageBodyReader(
                (javax.ws.rs.ext.MessageBodyReader<?>) provider));
    }

    /**
     * Loads all providers from in given package, accessable from a given class
     * loader.
     * 
     * @param classLoader
     *                The {@link ClassLoader}, which could access the
     *                providers. See {@link Class#getClassLoader()}.
     * @param throwOnExc
     * @param packageName
     * @throws WrappedLoadException
     *                 if a package could not be loaded, independent of
     *                 throwOnExc.
     * @throws WrappedClassLoadException
     *                 If a class could not be loaded and throwOnExc is true.
     */
    public void addProvidersFromPackage(ClassLoader classLoader,
            boolean throwOnExc, String... packageName)
            throws WrappedClassLoadException, WrappedLoadException {
        JaxRsClassesLoader.loadFromPackage(classLoader, throwOnExc, this,
                false, true, packageName);
    }

    /**
     * Adds a {@link javax.ws.rs.ext.MessageBodyWriter} class to this
     * JaxRsRouter. Typically you don't need this method, because it is done on
     * construction time or by {@link #addProvidersFromPackage(String...)}.
     * 
     * @param messageBodyWriterClass
     *                The {@link javax.ws.rs.ext.MessageBodyWriter} class to add
     *                to the JaxRsRouter.
     * @throws IllegalArgumentException
     *                 If no instance of the provider could created.
     */
    public void addMessageBodyWriter(Class<?> messageBodyWriterClass)
            throws IllegalArgumentException {
        Constructor<?> constructor = RootResourceClass
                .findJaxRsConstructor(messageBodyWriterClass);
        Object provider;
        try {
            provider = RootResourceClass.createInstance(constructor, false,
                    null, this);
        } catch (InstantiateParameterException e) {
            // should be not possible here
            throw new JaxRsRuntimeException(
                    "Could not instantiate the MessageBodyWriter", e);
        } catch (IllegalOrNoAnnotationException e) {
            throw new JaxRsRuntimeException(
                    "Could not instantiate the MessageBodyWriter", e);
        } catch (InstantiateRootRessourceException e) {
            throw new JaxRsRuntimeException(
                    "Could not instantiate the MessageBodyWriter", e);
        } catch (RequestHandledException e) {
            throw new JaxRsRuntimeException(
                    "Could not instantiate the MessageBodyWriter", e);
        } catch (InvocationTargetException e) {
            throw new JaxRsRuntimeException(
                    "Could not instantiate the MessageBodyWriter", e.getCause());
        }
        this.messageBodyWriters.add(new MessageBodyWriter(
                (javax.ws.rs.ext.MessageBodyWriter<?>) provider));
    }

    /**
     * Handles a call by invoking the next Restlet if it is available.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        try {
            CallContext callContext = new CallContext(request, response,
                    this.authenticator);
            try {
                ResObjAndMeth resObjAndMeth;
                try {
                    resObjAndMeth = matchingRequestToResourceMethod(callContext);
                } catch (CouldNotFindMethodException e) {
                    e.errorRestlet.handle(request, response);
                    response.setEntity(new StringRepresentation(e.getMessage(),
                            MediaType.TEXT_PLAIN, Language.ENGLISH));
                    return;
                }
                callContext.setReadOnly();
                ResourceMethod resourceMethod = resObjAndMeth.resourceMethod;
                ResourceObject resourceObject = resObjAndMeth.resourceObject;
                invokeMethodAndHandleResult(resourceMethod, resourceObject,
                        callContext);
            } catch (WebApplicationException e) {
                handleWebAppExc(e, callContext, null);
                // Exception was handled and data were set into the Response.
            }
        } catch (RequestHandledException e) {
            // Exception was handled and data were set into the Response.
        }
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods.
     * 
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @return (Sub)Resource Method
     * @throws CouldNotFindMethodException
     * @throws RequestHandledException
     */
    private ResObjAndMeth matchingRequestToResourceMethod(
            CallContext callContext) throws CouldNotFindMethodException,
            RequestHandledException {
        Request restletRequest = callContext.getRequest();
        RemainingPath u = new RemainingPath(restletRequest.getResourceRef()
                .getRemainingPart());
        RrcAndRemPath rcat = identifyRootResourceClass(u, callContext);
        ResObjAndRemPath resourceObjectAndPath = obtainObjectThatHandleRequest(
                rcat, callContext);
        MediaType givenMediaType = restletRequest.getEntity().getMediaType();
        ResObjAndMeth method = identifyMethodThatHandleRequest(
                resourceObjectAndPath, callContext, givenMediaType);
        return method;
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods, Part 1.
     * 
     * @return The identified root resource class, the remaning path after
     *         identifying and the matched template parameters; see
     *         {@link RrcAndRemPath}.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @throws CouldNotFindMethodException
     */
    private RrcAndRemPath identifyRootResourceClass(RemainingPath u,
            CallContext callContext) throws CouldNotFindMethodException {
        // 1. Identify the root resource class:
        // (a)
        // c: Set<Class>: root resource classes
        // e: Set<RegExp>
        // Map<UriTemplateRegExp, Class> eAndCs = new HashMap();
        Collection<RootResourceClass> eAndCs = new ArrayList<RootResourceClass>();
        // (a) and (b) and (c) Filter E
        for (RootResourceClass rootResourceClass : this.rootResourceClasses) {
            // Map.Entry<UriTemplateRegExp, Class> eAndC = eAndCIter.next();
            // UriTemplateRegExp regExp = eAndC.getKey();
            // Class clazz = eAndC.getValue();
            MatchingResult matchingResult = rootResourceClass.getPathRegExp()
                    .match(u);
            if (matchingResult == null)
                continue;
            if (!Util.isEmptyOrSlash(matchingResult.getFinalMatchingGroup())
                    && !rootResourceClass.hasSubResourceMethodsOrLocators())
                continue;
            else
                eAndCs.add(rootResourceClass);
        }
        // (d)
        if (eAndCs.isEmpty())
            throwRootResourceNotFound(u);
        // (e) and (f)
        RootResourceClass tClass = getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eAndCs);
        // (f)
        PathRegExp rMatch = tClass.getPathRegExp();
        MatchingResult matchResult = rMatch.match(u);
        u = matchResult.getFinalCapturingGroup();
        addMrVarsToMap(matchResult, callContext);
        return new RrcAndRemPath(tClass, u);
    }

    /**
     * @param matchResult
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     */
    private void addMrVarsToMap(MatchingResult matchResult,
            CallContext callContext) {
        Map<String, String> variables = matchResult.getVariables();
        for (Map.Entry<String, String> varEntry : variables.entrySet()) {
            String key = varEntry.getKey();
            String value = varEntry.getValue();
            callContext.addTemplParamsEnc(key, value);
        }
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5, Part 2
     * 
     * @param rrcAndRemPath
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @return Resource Object
     * @throws RequestHandledException
     */
    private ResObjAndRemPath obtainObjectThatHandleRequest(
            RrcAndRemPath rrcAndRemPath, CallContext callContext)
            throws CouldNotFindMethodException, RequestHandledException,
            WebApplicationException {
        RemainingPath u = rrcAndRemPath.u;
        RootResourceClass resClass = rrcAndRemPath.rrc;
        PathRegExp rMatch = resClass.getPathRegExp();
        ResourceObject o;
        // LATER Do I use dynamic proxies, to inject instance variables?
        try {
            o = resClass.createInstance(callContext, this);
        } catch (InstantiateParameterException e) {
            throw new WebApplicationException(e, 404);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        }
        // Part 2
        for (;;) // (j)
        {
            // (a) If U is null or '/' go to step 3
            if (u.isEmptyOrSlash()) {
                return new ResObjAndRemPath(o, u);
            }
            // (b) Set C = class ofO,E = {}
            Collection<ResourceMethodOrLocator> eWithMethod = new ArrayList<ResourceMethodOrLocator>();
            // (c) and (d) Filter E: remove members do not match U or final
            // match not empty
            for (ResourceMethodOrLocator methodOrLocator : resClass
                    .getSubResourceMethodsAndLocators()) {
                MatchingResult matchResult = methodOrLocator.getPathRegExp()
                        .match(u);
                if (matchResult == null)
                    continue;
                if (!Util.isEmptyOrSlash(matchResult.getFinalMatchingGroup()))
                    continue;
                eWithMethod.add(methodOrLocator);
            }
            // (e) If E is empty -> HTTP 404
            if (eWithMethod.isEmpty())
                throwResourceNotFound(o, u);
            // (f) and (g) sort E, use first member of E
            ResourceMethodOrLocator firstMeth = getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eWithMethod);

            rMatch = firstMeth.getPathRegExp();
            MatchingResult matchingResult = rMatch.match(u);

            addMrVarsToMap(matchingResult, callContext);

            // (h) When Method is resource method
            if (firstMeth instanceof SubResourceMethod)
                return new ResObjAndRemPath(o, u);
            // (g) and (i)
            u = matchingResult.getFinalCapturingGroup();
            SubResourceLocator subResourceLocator = (SubResourceLocator) firstMeth;
            try {
                o = subResourceLocator.createSubResource(o, callContext, this);
            } catch (WebApplicationException e) {
                throw e;
            } catch (InstantiateParameterException e) {
                throw new WebApplicationException(e, 404);
            } catch (Exception e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            }
            // (j) Go to step 2a (repeat for)
        }
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods, Part 3.
     * 
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * 
     * @return Resource Object and Method, that handle the request.
     * @throws RequestHandledException
     *                 for example if the method was OPTIONS, but no special
     *                 Resource Method for OPTIONS is available.
     * @throws ResourceMethodNotFoundException
     */
    private ResObjAndMeth identifyMethodThatHandleRequest(
            ResObjAndRemPath resObjAndRemPath, CallContext callContext,
            MediaType givenMediaType) throws CouldNotFindMethodException,
            RequestHandledException {
        org.restlet.data.Method httpMethod = callContext.getRequest()
                .getMethod();
        // 3. Identify the method that will handle the request:
        // (a)
        ResourceObject resObj = resObjAndRemPath.resourceObject;
        RemainingPath u = resObjAndRemPath.u;
        // (a) 1
        ResourceClass resourceClass = resObj.getResourceClass();
        Collection<ResourceMethod> resourceMethods = resourceClass
                .getMethodsForPath(u);
        if (resourceMethods.isEmpty())
            throwResourceMethodNotFound(resourceClass, u);
        // (a) 2: remove methods not support the given method
        boolean alsoGet = httpMethod.equals(Method.HEAD);
        removeNotSupportedHttpMethod(resourceMethods, httpMethod, alsoGet);
        if (resourceMethods.isEmpty()) {
            if (httpMethod.equals(Method.OPTIONS)) {
                Set<Method> allowedMethods = resourceClass.getAllowedMethods(u);
                callContext.getResponse().getAllowedMethods().addAll(
                        allowedMethods);
                throw new RequestHandledException();
            }
            throwMethodNotAllowed(httpMethod, resourceClass, u);
        }
        // (a) 3
        if (givenMediaType != null) {
            Iterator<ResourceMethod> methodIter = resourceMethods.iterator();
            while (methodIter.hasNext()) {
                ResourceMethod resourceMethod = methodIter.next();
                if (!resourceMethod.isGivenMediaTypeSupported(givenMediaType))
                    methodIter.remove();
            }
            if (resourceMethods.isEmpty())
                throwUnsupportedMediaType(httpMethod, resourceClass, u);
        }
        // (a) 4
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        Iterator<ResourceMethod> methodIter = resourceMethods.iterator();
        while (methodIter.hasNext()) {
            ResourceMethod resourceMethod = methodIter.next();
            if (!resourceMethod.isAcceptedMediaTypeSupported(accMediaTypes))
                methodIter.remove();
        }
        if (resourceMethods.isEmpty()) {
            throwNoResourceMethodForAccMediaTypes(httpMethod, resourceClass, u);
        }
        // (b) and (c)
        ResourceMethod bestResourceMethod = getBestMethod(resourceMethods,
                givenMediaType, accMediaTypes, httpMethod);
        MatchingResult mr = bestResourceMethod.getPathRegExp().match(u);
        addMrVarsToMap(mr, callContext);
        return new ResObjAndMeth(resObj, bestResourceMethod);
    }

    /**
     * Removes the ResourceMethods doesn't support the given method
     * 
     * @param resourceMethods
     * @param httpMethod
     * @param alsoGet
     */
    private void removeNotSupportedHttpMethod(
            Collection<ResourceMethod> resourceMethods,
            org.restlet.data.Method httpMethod, boolean alsoGet) {
        Iterator<ResourceMethod> methodIter = resourceMethods.iterator();
        while (methodIter.hasNext()) {
            ResourceMethod resourceMethod = methodIter.next();
            if (!resourceMethod.isHttpMethodSupported(httpMethod, alsoGet))
                methodIter.remove();
        }
    }

    /**
     * Sort by using the media type of input data as the primary key and the
     * media type of output data as the secondary key.<br>
     * Sorting of media types follows the general rule: x/y < x/* < *<!---->/*,
     * i.e. a method that explicitly lists one of the requested media types is
     * sorted before a method that lists *<!---->/*. Quality parameter values
     * are also used such that x/y;q=1.0 < x/y;q=0.7. <br/> See JSR-311 Spec,
     * section 2.5, Part 3b+c. <br/> Never returns null.
     * 
     * @param resourceMethods
     *                the resourceMethods that provide the required mediaType
     * @param givenMediaType
     *                The MediaType of the given entity.
     * @param accMediaTypes
     *                The accepted MediaTypes
     * @param httpMethod
     *                The HTTP method of the request.
     * @return Returns the method who best matches the given and accepted media
     *         type in the request, or null
     * @throws CouldNotFindMethodException
     */
    private ResourceMethod getBestMethod(
            Collection<ResourceMethod> resourceMethods,
            MediaType givenMediaType, SortedMetadata<MediaType> accMediaTypes,
            Method httpMethod) throws CouldNotFindMethodException {
        SortedMetadata<MediaType> givenMediaTypes;
        if (givenMediaType != null)
            givenMediaTypes = SortedMetadata.singleton(givenMediaType);
        else
            givenMediaTypes = null;
        // mms = methods that support the given MediaType
        Map<ResourceMethod, List<MediaType>> mms = findMethodSupportsMime(
                resourceMethods, ConsOrProdMime.CONSUME_MIME, givenMediaTypes);
        if (mms.isEmpty())
            throw new WebApplicationException(500);
        if (mms.size() == 1)
            return Util.getFirstKey(mms);
        // check for method with best ProduceMime (secondary key)
        // mms = Methods support given MediaType and requested MediaType
        mms = findMethodSupportsMime(mms.keySet(), ConsOrProdMime.PRODUCE_MIME,
                accMediaTypes);
        if (mms.isEmpty())
            throw new WebApplicationException(500);
        if (mms.size() == 1)
            return Util.getFirstKey(mms);
        for (MediaType accMediaType : accMediaTypes) {
            ResourceMethod bestMethod = null;
            for (Map.Entry<ResourceMethod, List<MediaType>> mm : mms.entrySet()) {
                for (MediaType methodMediaType : mm.getValue()) {
                    if (accMediaType.includes(methodMediaType)) {
                        ResourceMethod currentMethod = mm.getKey();
                        if (bestMethod == null) {
                            bestMethod = currentMethod;
                        } else {
                            if (httpMethod.equals(Method.HEAD)) {
                                // special handling for HEAD
                                Method bestMethodHttp = bestMethod
                                        .getHttpMethod();
                                if (bestMethodHttp.equals(Method.GET)
                                        && currentMethod.getHttpMethod()
                                                .equals(Method.HEAD)) {
                                    // ignore HEAD method
                                } else if (bestMethod.getHttpMethod().equals(
                                        Method.HEAD)
                                        && currentMethod.getHttpMethod()
                                                .equals(Method.GET)) {
                                    bestMethod = currentMethod;
                                } else {
                                    // TODO JSR311: it is not an internal
                                    // server error in
                                    // SimpleTrainTest.testGetTextAll()
                                    throwMultipleResourceMethods(bestMethod,
                                            currentMethod);
                                }
                            } else {
                                throwMultipleResourceMethods(bestMethod,
                                        currentMethod);
                            }
                        }
                    }
                }
            }
            if (bestMethod != null)
                return bestMethod;
        }
        throw new WebApplicationException(500);
    }

    /**
     * @param resourceMethods
     * @param consumeOrPr_mime
     * @param mediaType
     * @return
     */
    private Map<ResourceMethod, List<MediaType>> findMethodSupportsMime(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty())
            return findMethodsSupportAllTypes(resourceMethods, inOut);
        Map<ResourceMethod, List<MediaType>> mms;
        mms = findMethodsSupportTypeAndSubType(resourceMethods, inOut,
                mediaTypes);
        if (mms.isEmpty()) {
            mms = findMethodsSupportType(resourceMethods, inOut, mediaTypes);
            if (mms.isEmpty())
                mms = findMethodsSupportAllTypes(resourceMethods, inOut);
        }
        return mms;
    }

    /**
     * @param resourceMethods
     * @param inOut
     * @param mediaType
     * @return Never returns null.
     */
    private Map<ResourceMethod, List<MediaType>> findMethodsSupportTypeAndSubType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (ResourceMethod resourceMethod : resourceMethods) {
            List<MediaType> mimes = getConsOrProdMimes(resourceMethod, inOut);
            for (MediaType resMethMediaType : mimes) {
                for (MediaType mediaType : mediaTypes)
                    if (resMethMediaType.equals(mediaType, true))
                        returnMethods.put(resourceMethod, mimes);
            }
        }
        return returnMethods;
    }

    /**
     * @param resourceMethod
     * @param inOut
     * @return
     */
    private List<MediaType> getConsOrProdMimes(ResourceMethod resourceMethod,
            ConsOrProdMime inOut) {
        if (inOut.equals(ConsOrProdMime.CONSUME_MIME))
            return resourceMethod.getConsumedMimes();
        List<MediaType> producedMimes = resourceMethod.getProducedMimes();
        if (producedMimes.isEmpty())
            return Util.createList(MediaType.ALL);
        return producedMimes;
    }

    private Map<ResourceMethod, List<MediaType>> findMethodsSupportType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (ResourceMethod resourceMethod : resourceMethods) {
            List<MediaType> mimes = getConsOrProdMimes(resourceMethod, inOut);
            for (MediaType resMethMediaType : mimes) {
                for (MediaType mediaType : mediaTypes) {
                    String resMethMainType = resMethMediaType.getMainType();
                    String wishedMainType = mediaType.getMainType();
                    if (resMethMainType.equals(wishedMainType))
                        returnMethods.put(resourceMethod, mimes);
                }
            }
        }
        return returnMethods;
    }

    private Map<ResourceMethod, List<MediaType>> findMethodsSupportAllTypes(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut) {
        Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (ResourceMethod resourceMethod : resourceMethods) {
            List<MediaType> mimes = getConsOrProdMimes(resourceMethod, inOut);
            for (MediaType resMethMediaType : mimes) {
                if (resMethMediaType.equals(MediaType.ALL))
                    returnMethods.put(resourceMethod, mimes);
            }
        }
        return returnMethods;
    }

    private enum ConsOrProdMime {
        /**
         * Declares that the methods etc. for the consume mime shoud be used
         */
        CONSUME_MIME,

        /**
         * Declares that the methods etc. for the produced mime shoud be used
         */
        PRODUCE_MIME
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5, Part 2f+2g
     * 
     * @param eWithMethod
     *                Collection of SubResourceMethods and SubResourceLocators
     * @return null, if the Map is null or empty
     * @throws CouldNotFindMethodException
     */
    private ResourceMethodOrLocator getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<ResourceMethodOrLocator> eWithMethod)
            throws CouldNotFindMethodException {
        if (eWithMethod == null || eWithMethod.isEmpty())
            return null;
        Iterator<ResourceMethodOrLocator> srmlIter = eWithMethod.iterator();
        ResourceMethodOrLocator bestSrml = srmlIter.next();
        if (eWithMethod.size() == 1)
            return bestSrml;
        int bestSrmlChars = Integer.MIN_VALUE;
        int bestSrmlNoCaptGroups = Integer.MIN_VALUE;
        for (ResourceMethodOrLocator srml : eWithMethod) {
            int srmlNoLitChars = srml.getPathRegExp().getNumberOfLiteralChars();
            int srmlNoCaptGroups = srml.getPathRegExp()
                    .getNumberOfCapturingGroups();
            if (srmlNoLitChars > bestSrmlChars) {
                bestSrml = srml;
                bestSrmlChars = srmlNoLitChars;
                bestSrmlNoCaptGroups = srmlNoCaptGroups;
                continue;
            }
            if (srmlNoLitChars == bestSrmlChars) {
                if (srmlNoCaptGroups > bestSrmlNoCaptGroups) {
                    bestSrml = srml;
                    bestSrmlChars = srmlNoLitChars;
                    bestSrmlNoCaptGroups = srmlNoCaptGroups;
                    continue;
                }
                if (srmlNoCaptGroups == bestSrmlNoCaptGroups) {
                    if (srml.getPathRegExp().equals(bestSrml.getPathRegExp())) {
                        // different Java methods for the same resource, but
                        // perhaps for different HTTP methods
                        continue;
                    }
                    throwMultipleResourceMethods(bestSrml, srml);
                }
            }
        }
        return bestSrml;
    }

    /**
     * See JSR-311-Spec, Section 2.5 Matching Requests to Resource Methods, item
     * 1.e
     * 
     * @param rrcs
     *                Collection of root resource classes
     * @return null, if the Map is null or empty
     * @throws CouldNotFindMethodException
     */
    private RootResourceClass getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<RootResourceClass> rrcs)
            throws CouldNotFindMethodException {
        if (rrcs == null || rrcs.isEmpty())
            return null;
        Iterator<RootResourceClass> rrcIter = rrcs.iterator();
        RootResourceClass bestRrc = rrcIter.next();
        if (rrcs.size() == 1)
            return bestRrc;
        int bestRrcChars = Integer.MIN_VALUE;
        int bestRrcNoCaptGroups = Integer.MIN_VALUE;
        for (RootResourceClass rrc : rrcs) {
            int rrcNoLitChars = rrc.getPathRegExp().getNumberOfLiteralChars();
            int rrcNoCaptGroups = rrc.getPathRegExp()
                    .getNumberOfCapturingGroups();
            if (rrcNoLitChars > bestRrcChars) {
                bestRrc = rrc;
                bestRrcChars = rrcNoLitChars;
                bestRrcNoCaptGroups = rrcNoCaptGroups;
                continue;
            }
            if (rrcNoLitChars == bestRrcChars) {
                if (rrcNoCaptGroups > bestRrcNoCaptGroups) {
                    bestRrc = rrc;
                    bestRrcChars = rrcNoLitChars;
                    bestRrcNoCaptGroups = rrcNoCaptGroups;
                    continue;
                }
                if (rrcNoCaptGroups == bestRrcNoCaptGroups) {
                    // TODO JSR311: What happens, if both are equals?
                    throwMultipleRootResourceClasses(bestRrc, rrc);
                }
            }
        }
        return bestRrc;
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param resourceMethod
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param methodName
     * @param logMessage
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     * @throws RequestHandledException
     */
    private RequestHandledException handleExecption(Throwable exception,
            AbstractMethodWrapper resourceMethod, CallContext callContext,
            String logMessage) throws RequestHandledException {
        if (exception instanceof InvocationTargetException)
            exception = exception.getCause();
        if (exception instanceof WebApplicationException) {
            WebApplicationException webAppExc = (WebApplicationException) exception;
            throw handleWebAppExc(webAppExc, callContext, resourceMethod);
        }
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        getLogger().log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * Handles the given {@link WebApplicationException}.
     * 
     * @param webAppExc
     *                The {@link WebApplicationException} to handle
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     */
    private RequestHandledException handleWebAppExc(
            WebApplicationException webAppExc, CallContext callContext,
            AbstractMethodWrapper resourceMethod)
            throws RequestHandledException {
        // the message of the Exception is not used in the
        // WebApplicationException
        jaxRsRespToRestletResp(webAppExc.getResponse(), callContext,
                resourceMethod);
        // MediaType rausfinden
        throw new RequestHandledException();
    }

    /**
     * @param resourceMethod
     * @param resourceObject
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     */
    private void invokeMethodAndHandleResult(ResourceMethod resourceMethod,
            ResourceObject resourceObject, CallContext callContext)
            throws RequestHandledException {
        Object result;
        try {
            result = resourceMethod.invoke(resourceObject, callContext, this);
        } catch (WebApplicationException e) {
            throw e;
        } catch (InstantiateParameterException e) {
            throw new WebApplicationException(e, 404);
        } catch (InvocationTargetException ite) {
            // LATER if RuntimeException, then propagate and not handle here?
            throw handleExecption(ite, resourceMethod, callContext,
                    "Exception in resource method");
        } catch (RequestHandledException e) {
            throw e;
        } catch (Exception e) {
            throw handleExecption(e, resourceMethod, callContext,
                    "Can not invoke the resource method");
        }
        Response restletResponse = callContext.getResponse();
        if (result == null) { // no representation
            restletResponse.setStatus(Status.SUCCESS_NO_CONTENT);
            restletResponse.setEntity(null);
            return;
        } else {
            restletResponse.setStatus(Status.SUCCESS_OK);
            if (result instanceof javax.ws.rs.core.Response) {
                jaxRsRespToRestletResp((javax.ws.rs.core.Response) result,
                        callContext, resourceMethod);
                // } else if(result instanceof URI) { // perhaps 201 or 303
            } else if (result instanceof javax.ws.rs.core.Response.ResponseBuilder) {
                javax.ws.rs.core.Response jaxRsResponse = ((javax.ws.rs.core.Response.ResponseBuilder) result)
                        .build();
                jaxRsRespToRestletResp(jaxRsResponse, callContext,
                        resourceMethod);
            } else {
                Representation entity = convertToRepresentation(result,
                        resourceMethod, callContext, null, null);
                restletResponse.setEntity(entity);
                // throw new NotYetImplementedException();
                // LATER perhaps another default as option (email 2008-01-29)
            }
        }
    }

    private void jaxRsRespToRestletResp(
            javax.ws.rs.core.Response jaxRsResponse, CallContext callContext,
            AbstractMethodWrapper resourceMethod)
            throws RequestHandledException {
        Response restletResponse = callContext.getResponse();
        restletResponse.setStatus(Status.valueOf(jaxRsResponse.getStatus()));
        Object mediaTypeStr = jaxRsResponse.getMetadata().getFirst(
                HttpHeaders.CONTENT_TYPE);
        MediaType respMediaType = null;
        if (mediaTypeStr != null)
            respMediaType = MediaType.valueOf(mediaTypeStr.toString());
        restletResponse.setEntity(convertToRepresentation(jaxRsResponse
                .getEntity(), resourceMethod, callContext, respMediaType,
                jaxRsResponse.getMetadata()));
        Util.copyResponseHeaders(jaxRsResponse.getMetadata(), restletResponse,
                getLogger());
    }

    /**
     * 
     * @param entity
     *                the entity to convert.
     * @param resourceMethod
     *                The {@link ResourceMethod} created the entity.
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param responseMediaType
     *                The MediaType of the JAX-RS response. May be null.
     * @param jaxRsRespHeaders
     *                The headers added to the {@link javax.ws.rs.core.Response}
     *                by the {@link ResponseBuilder}.
     * @return
     * @throws RequestHandledException
     */
    private Representation convertToRepresentation(Object entity,
            AbstractMethodWrapper resourceMethod, CallContext callContext,
            MediaType responseMediaType,
            MultivaluedMap<String, Object> jaxRsRespHeaders)
            throws RequestHandledException {
        if (entity instanceof Representation)
            return (Representation) entity;
        if (entity == null)
            return null;
        Class<? extends Object> entityClass = entity.getClass();
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        MessageBodyWriterSet mbws = this.messageBodyWriters.subSet(entityClass);
        List<MediaType> possMediaTypes;
        if (responseMediaType != null)
            possMediaTypes = Collections.singletonList(responseMediaType);
        else if (resourceMethod instanceof ResourceMethod)
            possMediaTypes = determineMediaType16(
                    (ResourceMethod) resourceMethod, mbws, callContext);
        else
            possMediaTypes = Collections.singletonList(MediaType.TEXT_PLAIN);
        mbws = mbws.subSet(possMediaTypes);
        MessageBodyWriter mbw = mbws.getBest(accMediaTypes);
        if (mbw == null)
            throw handleWebAppExc(new WebApplicationException(406),
                    callContext, resourceMethod);
        MediaType mediaType;
        if (responseMediaType != null)
            mediaType = responseMediaType;
        else
            mediaType = determineMediaType79(possMediaTypes, callContext);
        MultivaluedMap<String, Object> httpResponseHeaders = new WrappedRequestForHttpHeaders(
                callContext.getResponse(), jaxRsRespHeaders, getLogger());
        // TESTEN Response Headers for MessageBodyWriter is not null yet
        return new JaxRsOutputRepresentation(entity, mediaType, mbw,
                httpResponseHeaders);
    }

    /**
     * Determines the MediaType for a response. See JAX-RS-Spec, Section 2.6
     * "Determining the MediaType of Responses", Parts 1-6
     * 
     * @param resourceMethod
     *                The ResourceMethod that created the entity.
     * @param mbwsForEntityClass
     *                {@link MessageBodyWriter}s, that support the entity
     *                class.
     * @param accMediaTypes
     *                see {@link SortedMetadata}
     * @param restletResponse
     *                The Restlet {@link Response}; needed for a not acceptable
     *                return.
     * @return
     * @throws RequestHandledException
     */
    private List<MediaType> determineMediaType16(ResourceMethod resourceMethod,
            MessageBodyWriterSet mbwsForEntityClass, CallContext callContext)
            throws RequestHandledException {
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        // 1. Gather the set of producible media types P:
        // (a) + (b)
        List<MediaType> p = resourceMethod.getProducedMimes();
        // 1. (c)
        if (p.isEmpty()) {
            p = new ArrayList<MediaType>();
            for (MessageBodyWriter messageBodyWriter : mbwsForEntityClass)
                p.addAll(messageBodyWriter.getProducedMimes());
        }
        // 2.
        if (p.isEmpty())
            return Collections.singletonList(MediaType.ALL);
        // 3. Obtain the acceptable media types A. If A = {}, set A = {'*/*'}
        if (accMediaTypes.isEmpty())
            accMediaTypes = SortedMetadata.getMediaTypeAll();
        // 4. Sort P and A: a is already sorted.
        p = Util.sortByConcreteness(p);
        // 5.
        List<MediaType> m = new ArrayList<MediaType>();
        for (MediaType prod : p)
            for (MediaType acc : accMediaTypes)
                if (prod.isCompatibleTo(acc))
                    m.add(MediaType.getMostSpecific(prod, acc));
        // 6.
        if (m.isEmpty())
            throwNotAcceptableWhileDetermineMediaType(callContext.getRequest(),
                    callContext.getResponse());
        return m;
    }

    /**
     * Determines the MediaType for a response. See JAX-RS-Spec, Section 2.6
     * "Determining the MediaType of Responses", Part 7-9
     * 
     * @param m
     *                the possible {@link MediaType}s.
     * @param restletResponse
     *                The Restlet {@link Response}; needed for a not acceptable
     *                return.
     * @return the determined {@link MediaType}
     * @throws RequestHandledException
     */
    private MediaType determineMediaType79(List<MediaType> m,
            CallContext callContext) throws RequestHandledException {
        // 7.
        for (MediaType mediaType : m)
            if (mediaType.isConcrete())
                return mediaType;
        // 8.
        if (m.contains(MediaType.ALL) || m.contains(MediaType.APPLICATION_ALL))
            return MediaType.APPLICATION_OCTET_STREAM;
        // 9.
        throw throwNotAcceptableWhileDetermineMediaType(callContext
                .getRequest(), callContext.getResponse());
    }

    /**
     * Structure to return the identiied {@link RootResourceClass}, the
     * remaining path after identifying and the matched template parameters.
     * 
     * @author Stephan Koops
     */
    class RrcAndRemPath {
        private RootResourceClass rrc;

        private RemainingPath u;

        RrcAndRemPath(RootResourceClass rrc, RemainingPath u) {
            this.rrc = rrc;
            this.u = u;
        }
    }

    /**
     * Structure to return the obtained {@link ResourceObject}, the remaining
     * path after identifying the object and all matched template parameters.
     * 
     * @author Stephan Koops
     */
    class ResObjAndRemPath {

        private ResourceObject resourceObject;

        private RemainingPath u;

        ResObjAndRemPath(ResourceObject resourceObject, RemainingPath u) {
            this.resourceObject = resourceObject;
            this.u = u;
        }
    }

    /**
     * Structure to return the obtained {@link ResourceObject}, the
     * {@link ResourceMethod} identifying it and all matched template
     * parameters.
     * 
     * @author Stephan Koops
     */
    class ResObjAndMeth {

        private ResourceObject resourceObject;

        private ResourceMethod resourceMethod;

        ResObjAndMeth(ResourceObject resourceObject,
                ResourceMethod resourceMethod) {
            this.resourceObject = resourceObject;
            this.resourceMethod = resourceMethod;
        }
    }

    /**
     * @return the authenticator
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * @param authenticator
     *                the authenticator to set
     */
    public void setAuthenticator(Authenticator authenticator) {
        if (authenticator == null)
            throw new IllegalArgumentException(
                    "The authenticator must nit be null. You can use the "
                            + AllowAllAuthenticator.class.getName()
                            + " or the "
                            + ForbidAllAuthenticator.class.getName());
        this.authenticator = authenticator;
    }

    /**
     * Returns a set with the attached root resource classes.
     * 
     * @return
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        for (RootResourceClass rootResourceClass : this.rootResourceClasses)
            rrcs.add(rootResourceClass.getJaxRsClass());
        return Collections.unmodifiableSet(rrcs);
    }

    /**
     * for internal use only
     * 
     * @see org.restlet.ext.jaxrs.wrappers.HiddenJaxRsRouter#getMessageBodyReaders()
     */
    @Deprecated
    public MessageBodyReaderSet getMessageBodyReaders() {
        return this.messageBodyReaders;
    }

    /**
     * for internal use only
     * 
     * @see org.restlet.ext.jaxrs.wrappers.HiddenJaxRsRouter#getMessageBodyWriters()
     */
    @Deprecated
    public MessageBodyWriterSet getMessageBodyWriters() {
        return this.messageBodyWriters;
    }
}