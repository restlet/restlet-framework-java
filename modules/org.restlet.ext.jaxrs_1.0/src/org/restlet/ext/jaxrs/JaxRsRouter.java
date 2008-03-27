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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
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
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.HttpHeaders;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateProviderException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.internal.provider.BufferedReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.ByteArrayProvider;
import org.restlet.ext.jaxrs.internal.provider.DataSourceProvider;
import org.restlet.ext.jaxrs.internal.provider.FileProvider;
import org.restlet.ext.jaxrs.internal.provider.InputStreamProvider;
import org.restlet.ext.jaxrs.internal.provider.JaxRsOutputRepresentation;
import org.restlet.ext.jaxrs.internal.provider.JaxbElementProvider;
import org.restlet.ext.jaxrs.internal.provider.JaxbProvider;
import org.restlet.ext.jaxrs.internal.provider.JsonProvider;
import org.restlet.ext.jaxrs.internal.provider.ReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.StreamingOutputProvider;
import org.restlet.ext.jaxrs.internal.provider.StringProvider;
import org.restlet.ext.jaxrs.internal.provider.WwwFormFormProvider;
import org.restlet.ext.jaxrs.internal.provider.WwwFormMmapProvider;
import org.restlet.ext.jaxrs.internal.provider.SourceProvider;
import org.restlet.ext.jaxrs.internal.util.MatchingResult;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.util.WrappedRequestForHttpHeaders;
import org.restlet.ext.jaxrs.internal.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.internal.wrappers.ContextResolver;
import org.restlet.ext.jaxrs.internal.wrappers.ContextResolverCollection;
import org.restlet.ext.jaxrs.internal.wrappers.MessageBodyReaderSet;
import org.restlet.ext.jaxrs.internal.wrappers.MessageBodyWriter;
import org.restlet.ext.jaxrs.internal.wrappers.MessageBodyWriterSet;
import org.restlet.ext.jaxrs.internal.wrappers.Provider;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.SubResourceLocator;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperFactory;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * <p>
 * The router choose the JAX-RS resource class and method to use for a request.
 * This class has methods {@link #attach(ApplicationConfig)} like the Restlet
 * {@link Router}.
 * </p>
 * <p>
 * Now some internal informations are following:
 * <ul>
 * <li>The variable names in this class are often the same as in the
 * JAX-RS-Definition.</li>
 * <li>This class is a subclass of {@link JaxRsRouterHelpMethods}. The methods
 * to handle exceptions while identifying the method that should handle the
 * request and in other situations are moved to that super class. So this class
 * contains only the real logic code and is more well arranged. </li>
 * </ul>
 * <p>
 * <!--LATER The class JaxRsRouter is not thread save while attach or detach
 * classes.-->
 * </p>
 * For further information see <a href="https://jsr311.dev.java.net/">Java
 * Service Request 311</a>. Because the specification is just under development
 * the link is not set to the PDF.
 * 
 * @author Stephan Koops
 */
public class JaxRsRouter extends JaxRsRouterHelpMethods {

    private static final String PRE_CONSTR_EXC_MESSAGE = "Exception while calling the pre construct method";

    /**
     * This set must only changed by adding a root resource class to this
     * JaxRsRouter.
     */
    private Set<RootResourceClass> rootResourceClasses = new HashSet<RootResourceClass>();

    private AccessControl accessControl;

    private MessageBodyReaderSet messageBodyReaders = new MessageBodyReaderSet();

    private MessageBodyWriterSet messageBodyWriters = new MessageBodyWriterSet();

    /**
     * This {@link Set} contains the available
     * {@link javax.ws.rs.ext.ContextResolver}s, each wrapped with an
     * {@link ContextResolver}.<br>
     * This field is final, because it is shared with other objects.
     */
    private final ContextResolverCollection contextResolvers = new ContextResolverCollection();

    private WrapperFactory wrapperFactory;

    /**
     * Creates a new JaxRsRouter with the {@link ApplicationConfig}. You can
     * add more {@link ApplicationConfig}s; use method
     * {@link #attach(ApplicationConfig)}.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @param appConfig
     *                Contains the classes to load as root resource classes and
     *                as providers. You could add more {@link ApplicationConfig}s;
     *                use method {@link #attach(ApplicationConfig)}.
     * @param accessControl
     *                The AccessControl to use. If you don't need the access
     *                control, you can use the {@link ForbidAllAccess}, the
     *                {@link AllowAllAccess} or the
     *                {@link ThrowExcAccessControl}. See also
     *                {@link #JaxRsRouter(Context, ApplicationConfig)}.
     * @throws IllegalArgumentException
     *                 if the {@link ApplicationConfig} contains invalid data;
     *                 see {@link #attach(ApplicationConfig)} for detailed
     *                 information.
     */
    public JaxRsRouter(Context context, ApplicationConfig appConfig,
            AccessControl accessControl) throws IllegalArgumentException {
        super(context);
        this.wrapperFactory = new WrapperFactory(getContext()
                .getLogger());
        this.loadDefaultProviders();
        if (appConfig != null)
            this.attach(appConfig);
        if (accessControl != null)
            this.setAccessControl(accessControl);
        else
            this.setAccessControl(ThrowExcAccessControl.getInstance());
    }

    /**
     * <p>
     * Creates a new JaxRsRouter with the {@link ApplicationConfig}. You can
     * add more {@link ApplicationConfig}s; use method
     * {@link #attach(ApplicationConfig)}.
     * </p>
     * <p>
     * If a resource class wants to check if a user has a role, the request is
     * returned with HTTP status 500 (Internal Server Error), see
     * {@link SecurityContext#isUserInRole(String)}. If you want to use the
     * access control, use constructor
     * {@link #JaxRsRouter(Context, ApplicationConfig, AccessControl)}.
     * </p>
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @param appConfig
     *                Contains the classes to load as root resource classes and
     *                as providers. You could add more {@link ApplicationConfig}s;
     *                use method {@link #attach(ApplicationConfig)}.
     * @throws IllegalArgumentException
     *                 if the {@link ApplicationConfig} contains invalid data;
     *                 see {@link #attach(ApplicationConfig)} for detailed
     *                 information.
     * @see #JaxRsRouter(Context, ApplicationConfig, AccessControl)
     */
    public JaxRsRouter(Context context, ApplicationConfig appConfig)
            throws IllegalArgumentException {
        this(context, appConfig, null);
    }

    /**
     * Creates a new JaxRsRouter with the given Context. Only the default
     * providers are loaded. No {@link ApplicationConfig} is loaded, use
     * {@link #attach(ApplicationConfig)} to attach some. If a resource class
     * later wants to check if a user has a role, the request is returned with
     * HTTP status 500 (Internal Server Error), see
     * {@link SecurityContext#isUserInRole(String)}.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @see #JaxRsRouter(Context, ApplicationConfig)
     * @see #JaxRsRouter(Context, ApplicationConfig, AccessControl)
     */
    public JaxRsRouter(Context context) {
        this(context, null, null);
    }

    /**
     * Creates a new JaxRsRouter with the given Context. Only the default
     * providers are loaded. No {@link ApplicationConfig} is loaded, use
     * {@link #attach(ApplicationConfig)} to attach some, or use constructor
     * {@link #JaxRsRouter(Context, ApplicationConfig, AccessControl)}.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @param accessControl
     *                The AccessControl to use. If you don't need the access
     *                control, you can use the {@link ForbidAllAccess}, the
     *                {@link AllowAllAccess} or the
     *                {@link ThrowExcAccessControl}.
     * @see #JaxRsRouter(Context, ApplicationConfig, AccessControl)
     */
    public JaxRsRouter(Context context, AccessControl accessControl) {
        this(context, null, accessControl);
    }

    /**
     * attaches the classes and providers to this JaxRsRouter. The providers are
     * available for all root resource classes provided to this JaxRsRouter. If
     * you won't mix them, instantiate another JaxRsRouter.
     * 
     * @param appConfig
     *                Contains the classes to load as root resource classes and
     *                as providers.
     * @throws IllegalArgumentException
     *                 if {@link ApplicationConfig} contains non-valid resource
     *                 classes or non-valid providers, or one of their
     *                 constructors throws an exception.
     * @throws NullPointerException
     *                 if the appConfig is null.
     */
    public void attach(ApplicationConfig appConfig)
            throws IllegalArgumentException {
        // TODO Interface comparable to other Restlet classes.
        Collection<Class<?>> rrcs = appConfig.getResourceClasses();
        Collection<Class<?>> providerClasses = appConfig.getProviderClasses();
        if (rrcs == null || rrcs.isEmpty())
            throw new IllegalArgumentException(
                    "The ApplicationConfig must return root resource classes");
        for (Class<?> rrc : rrcs) {
            try {
                this.addRootResourceClass(rrc);
            } catch (MissingAnnotationException e) {
                getLogger().warning(e.getMessage());
            }
        }
        if (providerClasses != null) {
            for (Class<?> providerClass : providerClasses) {
                try {
                    this.addProvider(providerClass);
                } catch (InstantiateProviderException ipe) {
                    String msg = "The provider " + providerClass.getName()
                            + " could not be instantiated";
                    throw new IllegalArgumentException(msg, ipe.getCause());
                } catch (InjectException ie) {
                    String msg = "The provider " + providerClass.getName()
                            + " could not be instantiated";
                    throw new IllegalArgumentException(msg, ie.getCause());
                }
            }
        }
        this.addExtensionMappings(appConfig.getExtensionMappings());
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void stop() throws Exception {
        try {
            Set<Provider<?>> allProviders = new HashSet<Provider<?>>(64);
            allProviders.addAll((Collection) this.messageBodyReaders);
            allProviders.addAll((Collection) this.messageBodyWriters);
            allProviders.addAll((Collection) this.contextResolvers);
            for (Provider<?> p : allProviders) {
                try {
                    p.preDestroy();
                } catch (MethodInvokeException e) {
                    getLogger().log(Level.INFO, PRE_CONSTR_EXC_MESSAGE, e);
                } catch (InvocationTargetException e) {
                    getLogger().log(Level.INFO, PRE_CONSTR_EXC_MESSAGE, e);
                }
            }
        } finally {
            super.stop();
        }
    }

    /**
     * Will use the given JAX-RS root resource class. Intended for internal use
     * only. Use the method {@link #attach(ApplicationConfig)}.
     * 
     * @param rootResourceClass
     *                the JAX-RS root resource class to add. If the root
     *                resource class is already available in this JaxRsRouter,
     *                it is ignored for later calls of this method.
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class, or if
     *                 there is already a root resource class with the given
     *                 name, which is not the same root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @see {@link #attach(ApplicationConfig)}
     */
    private void addRootResourceClass(Class<?> rootResourceClass)
            throws IllegalArgumentException, MissingAnnotationException {
        RootResourceClass newRrc;
        try {
            newRrc = wrapperFactory.getRootResourceClass(rootResourceClass);
        } catch (IllegalPathOnClassException e) {
            throw new IllegalArgumentException("The root resource class "
                    + rootResourceClass.getName()
                    + " is annotated with an illegal path: " + e.getPath()
                    + ". (" + e.getMessage() + ")", e);
        }
        // LATER use CopyOnWriteList
        PathRegExp uriTempl = newRrc.getPathRegExp();
        for (RootResourceClass rrc : this.rootResourceClasses) {
            if (rrc.getJaxRsClass().equals(rootResourceClass))
                return;
            if (rrc.getPathRegExp().equals(uriTempl))
                throw new IllegalArgumentException(
                        "There is already a root resource class with path "
                                + uriTempl.getPathPattern());
        }
        rootResourceClasses.add(newRrc);
    }

    private void loadDefaultProviders() {
        this.addDefaultProvider(BufferedReaderProvider.class);
        this.addDefaultProvider(ByteArrayProvider.class);
        this.addDefaultProvider(DataSourceProvider.class);
        this.addDefaultProvider(FileProvider.class);
        this.addDefaultProvider(InputStreamProvider.class);
        this.addDefaultProvider(JaxbElementProvider.class);
        this.addDefaultProvider(JaxbProvider.class);
        this.addDefaultProvider(JsonProvider.class);
        this.addDefaultProvider(ReaderProvider.class);
        this.addDefaultProvider(StreamingOutputProvider.class);
        this.addDefaultProvider(StringProvider.class);
        this.addDefaultProvider(WwwFormFormProvider.class);
        this.addDefaultProvider(WwwFormMmapProvider.class);
        this.addDefaultProvider(SourceProvider.class);
    }

    private void addDefaultProvider(Class<?> jaxRsProviderClass) {
        try {
            this.addProvider(jaxRsProviderClass);
        } catch (IllegalArgumentException e) {
            throw new ImplementationException(e);
        } catch (InstantiateProviderException e) {
            throw new ImplementationException(e);
        } catch (InjectException e) {
            throw new ImplementationException(e);
        }
    }

    /**
     * Adds the provider object to this JaxRsRouter.
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class. The class must implement at
     *                least one of the interfaces
     *                {@link javax.ws.rs.ext.MessageBodyWriter},
     *                {@link javax.ws.rs.ext.MessageBodyReader} or
     *                {@link javax.ws.rs.ext.ContextResolver}.
     * @throws IllegalArgumentException
     *                 if the provider is not a valid provider.
     * @throws InstantiateProviderException
     * @throws InjectException
     *                 If injection for &#64;{@link javax.ws.rs.core.Context}
     * @throws InvocationTargetException
     * @see {@link javax.ws.rs.ext.Provider}
     */
    private void addProvider(Class<?> jaxRsProviderClass)
            throws IllegalArgumentException, InstantiateProviderException,
            InjectException {
        if (jaxRsProviderClass == null)
            throw new IllegalArgumentException(
                    "The JAX-RS provider class must not be null");
        if (!jaxRsProviderClass
                .isAnnotationPresent(javax.ws.rs.ext.Provider.class)) {
            String message = "Officially a JAX-RS provider class must be annotated with @javax.ws.rs.ext.Provider";
            getLogger().config(message);
        }
        Provider<?> provider;
        try {
            provider = new Provider<Object>(jaxRsProviderClass);
        } catch (InvocationTargetException e) {
            InstantiateProviderException ipe = new InstantiateProviderException(
                    e.getMessage());
            ipe.setStackTrace(e.getCause().getStackTrace());
            throw ipe;
        }
        provider.init(this.contextResolvers);
        if (provider.isWriter())
            this.messageBodyWriters.add(provider);
        if (provider.isReader())
            this.messageBodyReaders.add(provider);
        if (provider.isContextResolver())
            this.contextResolvers.add(provider.getJaxRsContextResolver());
        // TODO hold all providers for preDestroy
    }

    /**
     * @see ApplicationConfig#getExtensionMappings()
     * @param extensionMappings
     */
    @SuppressWarnings("all")
    private void addExtensionMappings(
            Map<String, javax.ws.rs.core.MediaType> extensionMappings) {
        // TODO JaxRsRouter.extensionMappings.
        // https://jsr311.dev.java.net/servlets/ReadMsg?listName=users&msgNo=77
        // there is also a solution in Restlet
        // http://restlet.tigris.org/issues/show_bug.cgi?id=463
    }

    // methods for initialization ready
    // now methods for the daily work

    /**
     * Handles a call by looking for the resource metod to call, call it and
     * return the result.
     * 
     * @param request
     *                The {@link Request} to handle.
     * @param response
     *                The {@link Response} to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        ResourceObject resourceObject = null;
        try {
            CallContext callContext = new CallContext(request, response,
                    this.accessControl);
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
                resourceObject = resObjAndMeth.resourceObject;
                invokeMethodAndHandleResult(resourceMethod, resourceObject,
                        callContext);
            } catch (WebApplicationException e) {
                handleWebAppExc(e, callContext, null);
                // Exception was handled and data were set into the Response.
            }
        } catch (RequestHandledException e) {
            // Exception was handled and data were set into the Response.
        } finally {
            if (resourceObject != null) {
                try {
                    resourceObject.preDestroy();
                } catch (MethodInvokeException e) {
                    getLogger().log(Level.INFO, PRE_CONSTR_EXC_MESSAGE, e);
                } catch (InvocationTargetException e) {
                    getLogger().log(Level.INFO, PRE_CONSTR_EXC_MESSAGE, e);
                }
            }
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
        // Part 1
        RemainingPath u = new RemainingPath(restletRequest.getResourceRef()
                .getRemainingPart());
        RrcAndRemPath rcat = identifyRootResourceClass(u, callContext);
        // Part 2
        ResObjAndRemPath resourceObjectAndPath = obtainObjectThatHandleRequest(
                rcat, callContext);
        Representation entity = restletRequest.getEntity();
        // Part 3
        MediaType givenMediaType;
        if (entity != null)
            givenMediaType = entity.getMediaType();
        else
            givenMediaType = null;
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
            PathRegExp rrcPathRegExp = rootResourceClass.getPathRegExp();
            MatchingResult matchingResult = rrcPathRegExp.match(u);
            if (matchingResult == null)
                continue; // doesn't match
            if (matchingResult.getFinalCapturingGroup().isEmptyOrSlash())
                eAndCs.add(rootResourceClass);
            else if (rootResourceClass.hasSubResourceMethodsOrLocators())
                eAndCs.add(rootResourceClass);
        }
        // (d)
        if (eAndCs.isEmpty())
            handleRootResourceNotFound(u);
        // (e) and (f)
        RootResourceClass tClass = getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eAndCs);
        // (f)
        PathRegExp rMatch = tClass.getPathRegExp();
        MatchingResult matchResult = rMatch.match(u);
        u = matchResult.getFinalCapturingGroup();
        addPathVarsToMap(matchResult, callContext);
        return new RrcAndRemPath(tClass, matchResult.getMatched(), u);
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
        RootResourceClass rrc = rrcAndRemPath.rrc;
        PathRegExp rMatch = rrc.getPathRegExp();
        ResourceObject o;
        // LATER Do I use dynamic proxies, to inject instance variables?
        try {
            o = rrc.createInstance(callContext, contextResolvers,
                    this.messageBodyReaders, getLogger());
        } catch (WebApplicationException e) {
            throw e;
        } catch (NoMessageBodyReaderException e) {
            throw handleNoMessageBodyReader(callContext, e);
        } catch (RuntimeException e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        } catch (MissingAnnotationException e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        } catch (InstantiateRootRessourceException e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        } catch (MethodInvokeException e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        } catch (InvocationTargetException e) {
            throw handleExecption(e, null, callContext,
                    "Could not create new instance of root resource class");
        } catch (ConvertRepresentationException e) {
            throw handleConvertRepresentationExc(e);
        } catch (ConvertHeaderParamException e) {
            throw handleConvertHeaderParamExc(e);
        } catch (ConvertPathParamException e) {
            throw handleConvertPathParamExc(e);
        } catch (ConvertMatrixParamException e) {
            throw handleConvertMatrixParamExc(e);
        } catch (ConvertQueryParamException e) {
            throw handleConvertQueryParamExc(e);
        } catch (ConvertCookieParamException e) {
            throw handleConvertCookieParamExc(e);
        }
        Object jaxRsResObj1 = o.getJaxRsResourceObject();
        callContext.addForAncestor(jaxRsResObj1, rrcAndRemPath.matchedUriPath);
        ResourceClass resClass = rrc;
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
                PathRegExp pathRegExp = methodOrLocator.getPathRegExp();
                MatchingResult matchingResult = pathRegExp.match(u);
                if (matchingResult == null)
                    continue;
                if (matchingResult.getFinalCapturingGroup().isEmptyOrSlash())
                    eWithMethod.add(methodOrLocator);
                // the following is added by Stephan (is not in spec 2008-03-06)
                else if (methodOrLocator instanceof SubResourceLocator)
                    eWithMethod.add(methodOrLocator);
            }
            // (e) If E is empty -> HTTP 404
            if (eWithMethod.isEmpty())
                handleResourceNotFound(o, u);
            // (f) and (g) sort E, use first member of E
            ResourceMethodOrLocator firstMeth = getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eWithMethod);

            rMatch = firstMeth.getPathRegExp();
            MatchingResult matchingResult = rMatch.match(u);

            addPathVarsToMap(matchingResult, callContext);

            // (h) When Method is resource method
            if (firstMeth instanceof ResourceMethod)
                return new ResObjAndRemPath(o, u);
            String matchedUriPart = matchingResult.getMatched();
            Object jaxRsResObj2 = o.getJaxRsResourceObject();
            callContext.addForAncestor(jaxRsResObj2, matchedUriPart);

            // (g) and (i)
            u = matchingResult.getFinalCapturingGroup();
            SubResourceLocator subResourceLocator = (SubResourceLocator) firstMeth;
            try {
                o = subResourceLocator.createSubResource(o, callContext,
                        this.messageBodyReaders, wrapperFactory, contextResolvers, getLogger());
            } catch (WebApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            } catch (MissingAnnotationException e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            } catch (InstantiateRessourceException e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            } catch (MethodInvokeException e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            } catch (InvocationTargetException e) {
                throw handleExecption(e, subResourceLocator, callContext,
                        "Could not create new instance of root resource class");
            } catch (NoMessageBodyReaderException nmbre) {
                throw handleNoMessageBodyReader(callContext, nmbre);
            } catch (ConvertRepresentationException e) {
                throw handleConvertRepresentationExc(e);
            } catch (ConvertHeaderParamException e) {
                throw handleConvertHeaderParamExc(e);
            } catch (ConvertPathParamException e) {
                throw handleConvertPathParamExc(e);
            } catch (ConvertMatrixParamException e) {
                throw handleConvertMatrixParamExc(e);
            } catch (ConvertQueryParamException e) {
                throw handleConvertQueryParamExc(e);
            } catch (ConvertCookieParamException e) {
                throw handleConvertCookieParamExc(e);
            }
            resClass = o.getResourceClass();
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
            handleResourceMethodNotFound(resourceClass, u);
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
            handleMethodNotAllowed(httpMethod, resourceClass, u);
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
                handleUnsupportedMediaType(httpMethod, resourceClass, u,
                        givenMediaType);
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
            handleNoResourceMethodForAccMediaTypes(httpMethod, resourceClass, u);
        }
        // (b) and (c)
        ResourceMethod bestResourceMethod = getBestMethod(resourceMethods,
                givenMediaType, accMediaTypes, httpMethod);
        MatchingResult mr = bestResourceMethod.getPathRegExp().match(u);
        addPathVarsToMap(mr, callContext);
        String matchedUriPart = mr.getMatched();
        if (matchedUriPart.length() > 0) {
            Object jaxRsResObj = resObj.getJaxRsResourceObject();
            callContext.addForAncestor(jaxRsResObj, matchedUriPart);
        }
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
     * are also used such that x/y;q=1.0 < x/y;q=0.7. <br>
     * See JSR-311 Spec, section 2.6, Part 3b+c. <br>
     * Never returns null.
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
     */
    private ResourceMethod getBestMethod(
            Collection<ResourceMethod> resourceMethods,
            MediaType givenMediaType, SortedMetadata<MediaType> accMediaTypes,
            Method httpMethod) {
        SortedMetadata<MediaType> givenMediaTypes;
        if (givenMediaType != null)
            givenMediaTypes = SortedMetadata.singleton(givenMediaType);
        else
            givenMediaTypes = null;
        // mms = methods that support the given MediaType
        Map<ResourceMethod, List<MediaType>> mms1;
        mms1 = findMethodSupportsMime(resourceMethods,
                ConsOrProdMime.CONSUME_MIME, givenMediaTypes);
        if (mms1.isEmpty())
            return Util.getFirstElement(resourceMethods);
        if (mms1.size() == 1)
            return Util.getFirstKey(mms1);
        // check for method with best ProduceMime (secondary key)
        // mms = Methods support given MediaType and requested MediaType
        Map<ResourceMethod, List<MediaType>> mms2;
        mms2 = findMethodSupportsMime(mms1.keySet(),
                ConsOrProdMime.PRODUCE_MIME, accMediaTypes);
        if (mms2.isEmpty())
            return Util.getFirstKey(mms1);
        if (mms2.size() == 1)
            return Util.getFirstKey(mms2);
        for (MediaType accMediaType : accMediaTypes) {
            ResourceMethod bestMethod = null;
            for (Map.Entry<ResourceMethod, List<MediaType>> mm : mms2
                    .entrySet()) {
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
                                    // use one of the methods, e.g. the first
                                }
                            } else {
                                // use one of the methods, e.g. the first
                            }
                        }
                    }
                }
            }
            if (bestMethod != null)
                return bestMethod;
        }
        return Util.getFirstKey(mms2);
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

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2008-03-11, Section 3.6, Part 2f+2g
     * 
     * @param eWithMethod
     *                Collection of Sub-ResourceMethods and SubResourceLocators
     * @return the resource method or sub resource locator, or null, if the Map
     *         is null or empty.
     */
    private ResourceMethodOrLocator getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<ResourceMethodOrLocator> eWithMethod) {
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
                    if ((srml instanceof ResourceMethod)
                            && (bestSrml instanceof SubResourceLocator)) {
                        // prefare methods ahead locators
                        bestSrml = srml;
                        bestSrmlChars = srmlNoLitChars;
                        bestSrmlNoCaptGroups = srmlNoCaptGroups;
                        continue;
                    }
                    // use one the methods
                }
            }
        }
        return bestSrml;
    }

    /**
     * See JSR-311-Spec, Section 2.6 Matching Requests to Resource Methods, item
     * 1.e
     * 
     * @param rrcs
     *                Collection of root resource classes
     * @return null, if the Map is null or empty
     */
    private RootResourceClass getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<RootResourceClass> rrcs) {
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
                // use one of the classes
            }
        }
        return bestRrc;
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
            result = resourceMethod.invoke(resourceObject, callContext,
                    this.messageBodyReaders, getLogger());
        } catch (WebApplicationException e) {
            throw e;
        } catch (InvocationTargetException ite) {
            // LATER if RuntimeException, then propagate and not handle here?
            throw handleExecption(ite, resourceMethod, callContext,
                    "Exception in resource method");
        } catch (RuntimeException e) {
            throw handleExecption(e, resourceMethod, callContext,
                    "Can not invoke the resource method");
        } catch (MethodInvokeException e) {
            throw handleExecption(e, resourceMethod, callContext,
                    "Can not invoke the resource method");
        } catch (MissingAnnotationException e) {
            throw handleExecption(e, resourceMethod, callContext,
                    "Can not invoke the resource method");
        } catch (NoMessageBodyReaderException nmbre) {
            throw handleNoMessageBodyReader(callContext, nmbre);
        } catch (ConvertRepresentationException e) {
            throw handleConvertRepresentationExc(e);
        } catch (ConvertHeaderParamException e) {
            throw handleConvertHeaderParamExc(e);
        } catch (ConvertPathParamException e) {
            throw handleConvertPathParamExc(e);
        } catch (ConvertMatrixParamException e) {
            throw handleConvertMatrixParamExc(e);
        } catch (ConvertQueryParamException e) {
            throw handleConvertQueryParamExc(e);
        } catch (ConvertCookieParamException e) {
            throw handleConvertCookieParamExc(e);
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
                String warning = "the method "
                        + resourceMethod
                        + " returnef a ResponseBuilder. This is not allowed by default. Call responseBuilder.build() in the resource method";
                getLogger().warning(warning);
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

    /**
     * Converts the given JAX-RS {@link javax.ws.rs.core.Response} to a Restlet
     * {@link Response}.
     * 
     * @see JaxRsRouterHelpMethods#jaxRsRespToRestletResp(javax.ws.rs.core.Response,
     *      CallContext, AbstractMethodWrapper)
     */
    @Override
    void jaxRsRespToRestletResp(javax.ws.rs.core.Response jaxRsResponse,
            CallContext callContext, AbstractMethodWrapper resourceMethod)
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
     *                The {@link ResourceMethod} created the entity. Could be
     *                null, if an exception is handled, e.g. a
     *                {@link WebApplicationException}.
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
    @SuppressWarnings("unchecked")
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
        Type genericReturnType = null;
        Annotation[] methodAnnotations = null;
        if (resourceMethod != null) { // is default
            genericReturnType = resourceMethod.getGenericReturnType();
            methodAnnotations = resourceMethod.getAnnotations();
        }
        MessageBodyWriterSet mbws = this.messageBodyWriters.subSet(entityClass,
                genericReturnType, methodAnnotations);
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        List<MediaType> possMediaTypes;
        if (responseMediaType != null)
            possMediaTypes = Collections.singletonList(responseMediaType);
        else if (resourceMethod instanceof ResourceMethod)
            possMediaTypes = determineMediaType16(
                    (ResourceMethod) resourceMethod, mbws, callContext);
        else
            possMediaTypes = Collections.singletonList(MediaType.TEXT_PLAIN);
        mbws = mbws.subSet(possMediaTypes);
        MessageBodyWriter<?> mbw = mbws.getBest(accMediaTypes);
        if (mbw == null)
            handleNoMessageBodyWriter(callContext.getResponse(), accMediaTypes,
                    entityClass);
        MediaType mediaType;
        if (responseMediaType != null)
            mediaType = responseMediaType;
        else
            mediaType = determineMediaType79(possMediaTypes, callContext);
        MultivaluedMap<String, Object> httpResponseHeaders = new WrappedRequestForHttpHeaders(
                callContext.getResponse(), jaxRsRespHeaders, getLogger());
        return new JaxRsOutputRepresentation(entity, genericReturnType,
                mediaType, methodAnnotations, mbw, httpResponseHeaders);
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
            for (MessageBodyWriter<?> messageBodyWriter : mbwsForEntityClass)
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
                if (prod.isCompatible(acc))
                    m.add(MediaType.getMostSpecific(prod, acc));
        // 6.
        if (m.isEmpty())
            handleNotAcceptableWhileDetermineMediaType(
                    callContext.getRequest(), callContext.getResponse());
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
        throw handleNotAcceptableWhileDetermineMediaType(callContext
                .getRequest(), callContext.getResponse());
    }

    /**
     * Adds the matched template parameters to the {@link CallContext}.
     * 
     * @param matchResult
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     */
    private void addPathVarsToMap(MatchingResult matchResult,
            CallContext callContext) {
        Map<String, String> variables = matchResult.getVariables();
        for (Map.Entry<String, String> varEntry : variables.entrySet()) {
            String key = varEntry.getKey();
            String value = varEntry.getValue();
            callContext.addPathParamsEnc(key, value);
        }
    }

    /**
     * Structure to return the identiied {@link RootResourceClass}, the
     * remaining path after identifying and the matched template parameters.
     * 
     * @author Stephan Koops
     */
    class RrcAndRemPath {
        private RootResourceClass rrc;

        private String matchedUriPath;

        private RemainingPath u;

        RrcAndRemPath(RootResourceClass rrc, String matchedUriPath,
                RemainingPath u) {
            this.rrc = rrc;
            this.matchedUriPath = matchedUriPath;
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
     * Gets the currently used {@link AccessControl}.
     * 
     * @return the currently used AccessControl.
     * @see #setAccessControl(AccessControl)
     */
    public AccessControl getAccessControl() {
        return accessControl;
    }

    /**
     * Sets the {@link AccessControl} to use.
     * 
     * @param accessControl
     *                the accessControl to set.
     * @throws IllegalArgumentException
     *                 If the given accessControl is null, an
     *                 {@link IllegalArgumentException} is thrown.
     * @see AccessControl
     * @see #getAccessControl()
     */
    public void setAccessControl(AccessControl accessControl)
            throws IllegalArgumentException {
        if (accessControl == null)
            throw new IllegalArgumentException(
                    "The accessControl must not be null. You can use the "
                            + AllowAllAccess.class.getName() + ", the "
                            + ForbidAllAccess.class.getName() + " or the "
                            + ThrowExcAccessControl.class.getName());
        this.accessControl = accessControl;
    }

    /**
     * Returns an unmodifiable set with the attached root resource classes.
     * 
     * @return an unmodifiable set with the attached root resource classes.
     * @see #attach(ApplicationConfig)
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        for (RootResourceClass rootResourceClass : this.rootResourceClasses)
            rrcs.add(rootResourceClass.getJaxRsClass());
        return Collections.unmodifiableSet(rrcs);
    }

    /**
     * Returns a Collection with all root uris attached to this JaxRsRouter.
     * 
     * @return a Collection with all root uris attached to this JaxRsRouter.
     */
    public Collection<String> getRootUris() {
        List<String> uris = new ArrayList<String>();
        for (RootResourceClass rrc : this.rootResourceClasses)
            uris.add(rrc.getPathRegExp().getPathPattern());
        return Collections.unmodifiableCollection(uris);
    }
}