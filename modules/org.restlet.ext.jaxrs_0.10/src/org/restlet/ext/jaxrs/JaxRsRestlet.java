/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs;

import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.addPathVarsToMap;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.getBestMethod;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.removeNotSupportedHttpMethod;
import static org.restlet.ext.jaxrs.internal.util.Util.copyResponseHeaders;
import static org.restlet.ext.jaxrs.internal.util.Util.getMediaType;
import static org.restlet.ext.jaxrs.internal.util.Util.getSupportedCharSet;
import static org.restlet.ext.jaxrs.internal.util.Util.sortByConcreteness;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.internal.provider.BufferedReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.ByteArrayProvider;
import org.restlet.ext.jaxrs.internal.provider.DataSourceProvider;
import org.restlet.ext.jaxrs.internal.provider.FileProvider;
import org.restlet.ext.jaxrs.internal.provider.FileUploadProvider;
import org.restlet.ext.jaxrs.internal.provider.InputStreamProvider;
import org.restlet.ext.jaxrs.internal.provider.JaxbElementProvider;
import org.restlet.ext.jaxrs.internal.provider.JaxbProvider;
import org.restlet.ext.jaxrs.internal.provider.JsonProvider;
import org.restlet.ext.jaxrs.internal.provider.MultipartProvider;
import org.restlet.ext.jaxrs.internal.provider.ReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.SourceProvider;
import org.restlet.ext.jaxrs.internal.provider.StreamingOutputProvider;
import org.restlet.ext.jaxrs.internal.provider.StringProvider;
import org.restlet.ext.jaxrs.internal.provider.WebAppExcMapper;
import org.restlet.ext.jaxrs.internal.provider.WwwFormFormProvider;
import org.restlet.ext.jaxrs.internal.provider.WwwFormMmapProvider;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.ExceptionHandler;
import org.restlet.ext.jaxrs.internal.util.JaxRsOutputRepresentation;
import org.restlet.ext.jaxrs.internal.util.MatchingResult;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.util.WrappedRequestForHttpHeaders;
import org.restlet.ext.jaxrs.internal.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClasses;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.SubResourceLocator;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriterSubSet;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;

/**
 * <p>
 * This class choose the JAX-RS resource class and method to use for a request
 * and handles the result from he resource method. Typcally you should
 * instantiate a {@link JaxRsApplication} to run JAX-RS resource classes.
 * </p>
 * <p>
 * <i>The JAX-RS extension as well as the JAX-RS specification are currently
 * under development. You should use this extension only for experimental
 * purpose.</i> <br>
 * For further information see <a href="https://jsr311.dev.java.net/">Java
 * Service Request 311</a>.
 * </p>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Stephan Koops
 */
public class JaxRsRestlet extends Restlet {

    /**
     * 
     */
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private volatile RoleChecker roleChecker;

    private final JaxRsProviders providers;

    private final ResourceClasses resourceClasses;

    /**
     * Contains and handles the exceptions occuring while in resource objects
     * and providers, and also for the other cases where the runtime environment
     * should throw {@link WebApplicationException}.
     */
    private final ExceptionHandler excHandler;

    /**
     * Contains the thread localized {@link CallContext}s.
     */
    private final ThreadLocalizedContext tlContext = new ThreadLocalizedContext();

    private volatile ObjectFactory objectFactory;

    /**
     * Creates a new JaxRsRestlet with the given Context. Only the default
     * providers are loaded. If a resource class later wants to check if a user
     * has a role, the request is returned with HTTP status 500 (Internal Server
     * Error), see {@link SecurityContext#isUserInRole(String)}. You may set a
     * {@link RoleChecker} by using the constructor
     * {@link #JaxRsRestlet(Context, RoleChecker, MetadataService)} or method
     * {@link #setRoleChecker(RoleChecker)}.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @param metadataService
     *                the metadata service of the {@link JaxRsApplication}.
     * @see #JaxRsRestlet(Context, RoleChecker, MetadataService)
     */
    public JaxRsRestlet(Context context, MetadataService metadataService) {
        this(context, null, metadataService);
    }

    /**
     * Creates a new JaxRsRestlet with the given Context. Only the default
     * providers are loaded.
     * 
     * @param context
     *                the context from the parent, see
     *                {@link Restlet#Restlet(Context)}.
     * @param roleChecker
     *                The RoleChecker to use. If you don't need the access
     *                control, you can use the {@link RoleChecker#FORBID_ALL},
     *                the {@link RoleChecker#ALLOW_ALL} or the
     *                {@link RoleChecker#REJECT_WITH_ERROR}.
     * @param metadataService
     *                the metadata service of the {@link JaxRsApplication}.
     * @see #JaxRsRestlet(Context, MetadataService)
     */
    public JaxRsRestlet(Context context, RoleChecker roleChecker,
            MetadataService metadataService) {
        super(context);
        final ExtensionBackwardMapping extensionBackwardMapping = new ExtensionBackwardMapping(
                metadataService);
        this.excHandler = new ExceptionHandler(getLogger());
        this.providers = new JaxRsProviders(this.objectFactory, this.tlContext,
                extensionBackwardMapping, getLogger());
        this.resourceClasses = new ResourceClasses(this.tlContext,
                this.providers, extensionBackwardMapping, getLogger());
        this.loadDefaultProviders();
        if (roleChecker != null)
            this.setRoleChecker(roleChecker);
        else
            this.setRoleChecker(RoleChecker.REJECT_WITH_ERROR);
    }

    private void loadDefaultProviders() {
        this.addSingleton(new BufferedReaderProvider(), true);
        this.addSingleton(new ByteArrayProvider(), true);
        this.addSingleton(new DataSourceProvider(), true);
        this.addSingleton(new FileUploadProvider(), true); // not yet tested
        this.addSingleton(new FileProvider(), true);
        this.addSingleton(new InputStreamProvider(), true);
        this.addSingleton(new JaxbElementProvider(), true);
        this.addSingleton(new JaxbProvider(), true);
        this.addSingleton(new JsonProvider(), true);
        this.addSingleton(new MultipartProvider(), true); // not yet tested
        this.addSingleton(new ReaderProvider(), true);
        this.addSingleton(new StreamingOutputProvider(), true);
        this.addSingleton(new StringProvider(), true);
        this.addSingleton(new WwwFormFormProvider(), true);
        this.addSingleton(new WwwFormMmapProvider(), true);
        this.addSingleton(new SourceProvider(), true);

        this.addSingleton(new WebAppExcMapper(), true);
    }

    /**
     * Will use the given JAX-RS root resource class.<br>
     * If the given class is not a valid root resource class, a warning is
     * logged and false is returned.
     * 
     * @param jaxRsClass
     *                A JAX-RS root resource class or provider class to add. If
     *                the root resource class is already available in this
     *                JaxRsRestlet, it is ignored for later calls of this
     *                method.
     * @return true if the class is added or was already included, or false if
     *         the given class is not a valid class (a warning was logged).
     * @throws IllegalArgumentException
     *                 if the root resource class is null.
     */
    public boolean addClass(Class<?> jaxRsClass)
            throws IllegalArgumentException {
        if (jaxRsClass == null)
            throw new IllegalArgumentException(
                    "The JAX-RS class to add must not be null");
        boolean used = false;
        if (Util.isRootResourceClass(jaxRsClass)) {
            used = resourceClasses.addRootClass(jaxRsClass);
        }
        if (Util.isProvider(jaxRsClass)) {
            if (providers.addClass(jaxRsClass))
                used = true;
        }
        // TODO log a message or what ever
        // @see Application#getClasses()
        // @see Application#getSingletons()
        if (!used) {
            final String warning = ("The class " + jaxRsClass + " is neither a povider nor a root resource class");
            getLogger().warning(warning);
        }
        return used;
    }

    /**
     * Adds the provider object to this JaxRsRestlet.
     * 
     * @param jaxRsProviderOrRootresourceObject
     *                the JAX-RS provider or root resource object
     * @return true, if the provider is ok and added, otherwise false.
     * @throws IllegalArgumentException
     *                 if null was given
     * @see javax.ws.rs.ext.Provider
     */
    public boolean addSingleton(Object jaxRsProviderOrRootresourceObject)
            throws IllegalArgumentException {
        return addSingleton(jaxRsProviderOrRootresourceObject, false);
    }

    /**
     * Adds the provider object to this JaxRsRestlet.
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class.
     * @return true, if the provider is ok and added, otherwise false.
     * @throws IllegalArgumentException
     *                 if null was given
     * @see {@link javax.ws.rs.ext.Provider}
     */
    private boolean addSingleton(Object jaxRsObject, boolean defaultProvider)
            throws IllegalArgumentException {
        if (jaxRsObject == null)
            throw new IllegalArgumentException(
                    "The JAX-RS object to add must not be null");
        if (jaxRsObject instanceof Class)
            throw new IllegalArgumentException(
                    "The JAX-RS object to add must not be a java.lang.Class");
        boolean used = false;
        if (defaultProvider || Util.isProvider(jaxRsObject.getClass())) {
            used = providers.addSingleton(jaxRsObject, defaultProvider);
        }
        if (Util.isRootResourceClass(jaxRsObject.getClass())) {
            throw new NotYetImplementedException(
                    "only providers are allowed as singletons for now");
            // used = ...
        }
        if (!used) {
            final String warning = ("The class " + jaxRsObject.getClass() + " is neither a povider nor a root resource class");
            getLogger().warning(warning);
        }
        return used;
    }

    @Override
    public void start() throws Exception {
        providers.initAll();
        super.start();
    }

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
            CallContext callContext;
            callContext = new CallContext(request, response, this.roleChecker);
            tlContext.set(callContext);
            try {
                ResObjAndMeth resObjAndMeth;
                resObjAndMeth = requestMatching();
                callContext.setReadOnly();
                ResourceMethod resourceMethod = resObjAndMeth.resourceMethod;
                resourceObject = resObjAndMeth.resourceObject;
                Object result = invokeMethod(resourceMethod, resourceObject);
                handleResult(result, resourceMethod);
            } catch (WebApplicationException e) {
                // the message of the Exception is not used in the
                // WebApplicationException
                jaxRsRespToRestletResp(this.providers.convert(e), null);
                return;
            }
        } catch (RequestHandledException e) {
            // Exception was handled and data were set into the Response.
        } finally {
            Representation entity = request.getEntity();
            if (entity != null)
                entity.release();
        }
    }

    /**
     * Implementation of algorithm in JAX-RS-Spec (2008-04-16), Section 3.7.2
     * "Request Matching"
     * 
     * @return (Sub)Resource Method
     * @throws RequestHandledException
     * @throws WebApplicationException
     */
    private ResObjAndMeth requestMatching() throws RequestHandledException,
            WebApplicationException {
        Request restletRequest = tlContext.get().getRequest();
        // Part 1
        RemainingPath u = new RemainingPath(restletRequest.getResourceRef()
                .getRemainingPart());
        RroRemPathAndMatchedPath rrm = identifyRootResource(u);
        // Part 2
        ResObjAndRemPath resourceObjectAndPath = obtainObject(rrm);
        Representation entity = restletRequest.getEntity();
        // Part 3
        MediaType givenMediaType;
        if (entity != null)
            givenMediaType = entity.getMediaType();
        else
            givenMediaType = null;
        ResObjAndMeth method = identifyMethod(resourceObjectAndPath,
                givenMediaType);
        return method;
    }

    /**
     * Identifies the root resource class, see JAX-RS-Spec (2008-04-16), section
     * 3.7.2 "Request Matching", Part 1: "Identify the root resource class"
     * 
     * @param u
     *                the remaining path after the base ref
     * @return The identified root resource object, the remaining path after
     *         identifying and the matched template parameters; see
     *         {@link RroRemPathAndMatchedPath}.
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    private RroRemPathAndMatchedPath identifyRootResource(RemainingPath u)
            throws WebApplicationException, RequestHandledException {
        // 1. Identify the root resource class:
        // (a)
        // c: Set<Class>: root resource classes
        // e: Set<RegExp>
        // Map<UriTemplateRegExp, Class> eAndCs = new HashMap();
        Collection<RootResourceClass> eAndCs = new ArrayList<RootResourceClass>();
        // (a) and (b) and (c) Filter E
        for (RootResourceClass rootResourceClass : this.resourceClasses.roots()) {
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
            excHandler.rootResourceNotFound();
        // (e) and (f)
        RootResourceClass tClass = getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eAndCs);
        // (f)
        PathRegExp rMatch = tClass.getPathRegExp();
        MatchingResult matchResult = rMatch.match(u);
        u = matchResult.getFinalCapturingGroup();
        addPathVarsToMap(matchResult, tlContext.get());
        ResourceObject o = instantiateRrc(tClass);
        return new RroRemPathAndMatchedPath(o, u, matchResult.getMatched());
    }

    /**
     * Obtains the object that will handle the request, see JAX-RS-Spec
     * (2008-04-16), section 3.7.2 "Request Matching", Part 2: "Obtain the
     * object that will handle the request"
     * 
     * @param rroRemPathAndMatchedPath
     * @throws WebApplicationException
     * @throws RequestHandledException
     * @throws RuntimeException
     */
    private ResObjAndRemPath obtainObject(
            RroRemPathAndMatchedPath rroRemPathAndMatchedPath)
            throws WebApplicationException, RequestHandledException {
        ResourceObject o = rroRemPathAndMatchedPath.rootResObj;
        RemainingPath u = rroRemPathAndMatchedPath.u;
        ResourceClass resClass = o.getResourceClass();
        CallContext callContext = tlContext.get();
        callContext.addForMatched(o.getJaxRsResourceObject(),
                rroRemPathAndMatchedPath.matchedUriPath);
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
                    .getResourceMethodsAndLocators()) {
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
                excHandler.resourceNotFound();// NICE (o.getClass(), u);
            // (f) and (g) sort E, use first member of E
            ResourceMethodOrLocator firstMeth = getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eWithMethod);

            PathRegExp rMatch = firstMeth.getPathRegExp();
            MatchingResult matchingResult = rMatch.match(u);

            addPathVarsToMap(matchingResult, callContext);

            // (h) When Method is resource method
            if (firstMeth instanceof ResourceMethod)
                return new ResObjAndRemPath(o, u);
            String matchedUriPart = matchingResult.getMatched();
            Object jaxRsResObj = o.getJaxRsResourceObject();
            callContext.addForMatched(jaxRsResObj, matchedUriPart);

            // (g) and (i)
            u = matchingResult.getFinalCapturingGroup();
            SubResourceLocator subResourceLocator = (SubResourceLocator) firstMeth;
            o = createSubResource(o, subResourceLocator, callContext);
            resClass = o.getResourceClass();
            // (j) Go to step 2a (repeat for)
        }
    }

    /**
     * Identifies the method that will handle the request, see JAX-RS-Spec
     * (2008-04-16), section 3.7.2 "Request Matching", Part 3: Identify the
     * method that will handle the request:"
     * 
     * @return Resource Object and Method, that handle the request.
     * @throws RequestHandledException
     *                 for example if the method was OPTIONS, but no special
     *                 Resource Method for OPTIONS is available.
     * @throws ResourceMethodNotFoundException
     */
    private ResObjAndMeth identifyMethod(ResObjAndRemPath resObjAndRemPath,
            MediaType givenMediaType) throws RequestHandledException {
        CallContext callContext = tlContext.get();
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
            excHandler.resourceMethodNotFound();// NICE (resourceClass, u);
        // (a) 2: remove methods not support the given method
        boolean alsoGet = httpMethod.equals(Method.HEAD);
        removeNotSupportedHttpMethod(resourceMethods, httpMethod, alsoGet);
        if (resourceMethods.isEmpty()) {
            Set<Method> allowedMethods = resourceClass.getAllowedMethods(u);
            if (httpMethod.equals(Method.OPTIONS)) {
                callContext.getResponse().getAllowedMethods().addAll(
                        allowedMethods);
                throw new RequestHandledException();
            }
            excHandler.methodNotAllowed(allowedMethods);
        }
        // (a) 3
        if (givenMediaType != null) {
            Collection<ResourceMethod> supporting = resourceMethods;
            resourceMethods = new ArrayList<ResourceMethod>();
            for (ResourceMethod resourceMethod : supporting) {
                if (resourceMethod.isGivenMediaTypeSupported(givenMediaType))
                    resourceMethods.add(resourceMethod);
            }
            if (resourceMethods.isEmpty())
                excHandler.unsupportedMediaType(supporting);
        }
        // (a) 4
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        Collection<ResourceMethod> supporting = resourceMethods;
        resourceMethods = new ArrayList<ResourceMethod>();
        for (ResourceMethod resourceMethod : supporting) {
            if (resourceMethod.isAcceptedMediaTypeSupported(accMediaTypes))
                resourceMethods.add(resourceMethod);
        }
        if (resourceMethods.isEmpty()) {
            excHandler.noResourceMethodForAccMediaTypes(supporting);
        }
        // (b) and (c)
        ResourceMethod bestResourceMethod = getBestMethod(resourceMethods,
                givenMediaType, accMediaTypes, httpMethod);
        MatchingResult mr = bestResourceMethod.getPathRegExp().match(u);
        addPathVarsToMap(mr, callContext);
        String matchedUriPart = mr.getMatched();
        if (matchedUriPart.length() > 0) {
            Object jaxRsResObj = resObj.getJaxRsResourceObject();
            callContext.addForMatched(jaxRsResObj, matchedUriPart);
        }
        return new ResObjAndMeth(resObj, bestResourceMethod);
    }

    /**
     * Invokes the (sub) resource method. Handles / converts also occuring
     * exceptions.
     * 
     * @param resourceMethod
     *                the (sub) resource method to invoke
     * @param resourceObject
     *                the resource object to invoke the method on.
     * @return the object returned by the (sub) resource method.
     * @throws RequestHandledException
     *                 if the request is already handled
     * @throws WebApplicationException
     *                 if a JAX-RS class throws an WebApplicationException
     */
    private Object invokeMethod(ResourceMethod resourceMethod,
            ResourceObject resourceObject) throws WebApplicationException,
            RequestHandledException {
        Object result;
        try {
            result = resourceMethod.invoke(resourceObject);
        } catch (WebApplicationException e) {
            throw e;
        } catch (InvocationTargetException ite) {
            throw handleInvocationTargetExc(ite);
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, resourceMethod, tlContext
                    .get(), "Can not invoke the resource method");
        } catch (MethodInvokeException e) {
            throw excHandler.methodInvokeException(e, tlContext.get(),
                    "Can not invoke the resource method");
        } catch (ConvertRepresentationException e) {
            throw excHandler.convertRepresentationExc(e);
        }
        return result;
    }

    /**
     * Sets the result of the resource method invocation into the response. Do
     * necessary converting.
     * 
     * @param result
     *                the object returned by the resource method
     * @param resourceMethod
     *                the resource method; it is needed for the conversion.
     *                Could be null, if an exception is handled, e.g. a
     *                {@link WebApplicationException}.
     */
    private void handleResult(Object result, ResourceMethod resourceMethod) {
        Response restletResponse = tlContext.get().getResponse();
        if (result instanceof javax.ws.rs.core.Response) {
            jaxRsRespToRestletResp((javax.ws.rs.core.Response) result,
                    resourceMethod);
        } else if (result instanceof ResponseBuilder) {
            String warning = "the method " + resourceMethod
                    + " returnef a ResponseBuilder. You should "
                    + "call responseBuilder.build() in the resource method";
            getLogger().warning(warning);
            jaxRsRespToRestletResp(((ResponseBuilder) result).build(),
                    resourceMethod);
        } else {
            if (result == null) // no representation
                restletResponse.setStatus(Status.SUCCESS_NO_CONTENT);
            else
                restletResponse.setStatus(Status.SUCCESS_OK);
            SortedMetadata<MediaType> accMediaTypes;
            accMediaTypes = tlContext.get().getAccMediaTypes();
            restletResponse.setEntity(convertToRepresentation(result,
                    resourceMethod, null, null, accMediaTypes));
        }
    }

    /**
     * Converts the given JAX-RS {@link javax.ws.rs.core.Response} to a Restlet
     * {@link Response}.
     * 
     * @param jaxRsResponse
     *                The response returned by the resource method, perhaps as
     *                attribute of a {@link WebApplicationException}.
     * @param resourceMethod
     *                The resource method creating the response. Could be null,
     *                if an exception is handled, e.g. a
     *                {@link WebApplicationException}.
     */
    private void jaxRsRespToRestletResp(
            javax.ws.rs.core.Response jaxRsResponse,
            ResourceMethod resourceMethod) {
        Response restletResponse = tlContext.get().getResponse();
        restletResponse.setStatus(Status.valueOf(jaxRsResponse.getStatus()));
        MultivaluedMap<String, Object> httpHeaders = jaxRsResponse
                .getMetadata();
        MediaType respMediaType = getMediaType(httpHeaders);
        Object jaxRsEntity = jaxRsResponse.getEntity();
        SortedMetadata<MediaType> accMediaType;
        if (respMediaType != null)
            accMediaType = SortedMetadata.get(respMediaType);
        else
            accMediaType = tlContext.get().getAccMediaTypes();
        restletResponse.setEntity(convertToRepresentation(jaxRsEntity,
                resourceMethod, respMediaType, httpHeaders, accMediaType));
        copyResponseHeaders(httpHeaders, restletResponse);
    }

    /**
     * Converts the given entity - returned by the resource method - to a
     * Restlet {@link Representation}.
     * 
     * @param entity
     *                the entity to convert.
     * @param resourceMethod
     *                The resource method created the entity. Could be null, if
     *                an exception is handled, e.g. a
     *                {@link WebApplicationException}.
     * @param jaxRsResponseMediaType
     *                The MediaType of the JAX-RS
     *                {@link javax.ws.rs.core.Response}. May be null.
     * @param jaxRsRespHeaders
     *                The headers added to the {@link javax.ws.rs.core.Response}
     *                by the {@link ResponseBuilder}.
     * @param accMediaTypes
     *                the accepted media type from the current call context, or
     *                the returned of the JAX-RS
     *                {@link javax.ws.rs.core.Response}.
     * @return the corresponding Restlet Representation. Returns
     *         <code>null</code> only if null was given.
     * @throws WebApplicationException
     * @see AbstractMethodWrapper.EntityGetter
     */
    @SuppressWarnings("unchecked")
    private Representation convertToRepresentation(Object entity,
            ResourceMethod resourceMethod, MediaType jaxRsResponseMediaType,
            MultivaluedMap<String, Object> jaxRsRespHeaders,
            SortedMetadata<MediaType> accMediaTypes)
            throws ImplementationException {
        if (entity instanceof Representation) {
            Representation repr = (Representation) entity;
            // ensures that a supported character set is set
            repr.setCharacterSet(getSupportedCharSet(repr.getCharacterSet()));
            if (jaxRsResponseMediaType != null) {
                repr.setMediaType(Converter
                        .getMediaTypeWithoutParams(jaxRsResponseMediaType));
            }
            return repr;
        }
        Type genericReturnType;
        Class<? extends Object> entityClass;
        Annotation[] methodAnnotations;
        if (resourceMethod != null) // is default
            methodAnnotations = resourceMethod.getAnnotations();
        else
            methodAnnotations = EMPTY_ANNOTATION_ARRAY;

        if (entity instanceof GenericEntity) {
            GenericEntity<?> genericEntity = (GenericEntity) entity;
            genericReturnType = genericEntity.getType();
            entityClass = genericEntity.getRawType();
            entity = genericEntity.getEntity();
        } else {
            entityClass = (entity != null) ? entity.getClass() : null;
            if (resourceMethod != null) // is default
                genericReturnType = resourceMethod.getGenericReturnType();
            else
                genericReturnType = null;
            if (genericReturnType instanceof Class
                    && ((Class) genericReturnType)
                            .isAssignableFrom(javax.ws.rs.core.Response.class)) {
                genericReturnType = entityClass;
            }
        }

        MessageBodyWriterSubSet mbws;
        if (entity != null) {
            mbws = providers.writerSubSet(entityClass, genericReturnType,
                    methodAnnotations);
            if (mbws.isEmpty())
                throw excHandler.noMessageBodyWriter(entityClass,
                        genericReturnType, methodAnnotations, null, null);
        } else {
            mbws = MessageBodyWriterSubSet.empty();
        }
        final MediaType respMediaType;
        if (jaxRsResponseMediaType != null)
            respMediaType = jaxRsResponseMediaType;
        else if (resourceMethod != null)
            respMediaType = determineMediaType(resourceMethod, mbws);
        else
            respMediaType = MediaType.TEXT_PLAIN;
        final Response response = tlContext.get().getResponse();
        MultivaluedMap<String, Object> httpResponseHeaders = new WrappedRequestForHttpHeaders(
                response, jaxRsRespHeaders);
        final Representation repr;
        if (entity == null) {
            repr = Representation.createEmpty();
            repr.setMediaType(respMediaType);
        } else {
            final MessageBodyWriter mbw;
            mbw = mbws.getBestWriter(respMediaType, accMediaTypes);
            if (mbw == null)
                throw excHandler.noMessageBodyWriter(entityClass,
                        genericReturnType, methodAnnotations, respMediaType,
                        accMediaTypes);
            repr = new JaxRsOutputRepresentation(entity, genericReturnType,
                    respMediaType, methodAnnotations, mbw, httpResponseHeaders);
        }
        repr.setCharacterSet(getSupportedCharSet(httpResponseHeaders));
        return repr;
    }

    /**
     * Determines the MediaType for a response, see JAX-RS-Spec (2008-04-18),
     * section 3.8 "Determining the MediaType of Responses"
     * 
     * @param resourceMethod
     *                The ResourceMethod that created the entity.
     * @param mbws
     *                The {@link MessageBodyWriter}s, that support the class of
     *                the returned entity object.
     * @return the determined {@link MediaType}
     * @throws RequestHandledException
     * @throws WebApplicationException
     */
    private MediaType determineMediaType(ResourceMethod resourceMethod,
            MessageBodyWriterSubSet mbws) throws WebApplicationException {
        CallContext callContext = tlContext.get();
        // 1. Gather the set of producible media types P:
        // (a) + (b)
        Collection<MediaType> p = resourceMethod.getProducedMimes();
        // 1. (c)
        if (p.isEmpty()) {
            p = mbws.getAllProducibleMediaTypes();
            // 2.
            if (p.isEmpty())
                // '*/*', in conjunction with 8.:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
        // 3. Obtain the acceptable media types A.
        SortedMetadata<MediaType> a = callContext.getAccMediaTypes();
        // 3. If A = {}, set A = {'*/*'}
        if (a.isEmpty())
            a = SortedMetadata.getMediaTypeAll();
        // 4. Sort P and A (A is already sorted)
        List<MediaType> pSorted = sortByConcreteness(p);
        // 5.
        List<MediaType> m = new ArrayList<MediaType>();
        for (MediaType prod : pSorted)
            for (MediaType acc : a)
                if (prod.isCompatible(acc))
                    m.add(MediaType.getMostSpecific(prod, acc));
        // 6.
        if (m.isEmpty())
            excHandler.notAcceptableWhileDetermineMediaType();
        // 7.
        for (MediaType mediaType : m)
            if (mediaType.isConcrete())
                return mediaType;
        // 8.
        if (m.contains(MediaType.ALL) || m.contains(MediaType.APPLICATION_ALL))
            return MediaType.APPLICATION_OCTET_STREAM;
        // 9.
        throw excHandler.notAcceptableWhileDetermineMediaType();
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param ite
     * @param methodName
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     * @throws RequestHandledException
     */
    private RequestHandledException handleInvocationTargetExc(
            InvocationTargetException ite) throws RequestHandledException {
        Throwable cause = ite.getCause();
        if (cause instanceof ResourceException) {
            // avoid mapping to a JAX-RS Response and back to a Restlet Response
            Status status = ((ResourceException) cause).getStatus();
            Response restletResponse = tlContext.get().getResponse();
            restletResponse.setStatus(status);
        } else {
            javax.ws.rs.core.Response jaxRsResp = this.providers.convert(cause);
            jaxRsRespToRestletResp(jaxRsResp, null);
        }
        throw new RequestHandledException();
    }

    /**
     * Instantiates the root resource class and handles thrown exceptions.
     * 
     * @param rrc
     *                the root resource class to instantiate
     * @return the instance of the root resource
     * @throws WebApplicationException
     *                 if a WebApplicationException was thrown while creating
     *                 the instance.
     * @throws RequestHandledException
     *                 If an Exception was thrown and the request is already
     *                 handeled.
     */
    private ResourceObject instantiateRrc(RootResourceClass rrc)
            throws WebApplicationException, RequestHandledException {
        ResourceObject o;
        try {
            o = rrc.getInstance(this.objectFactory);
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, null, tlContext.get(),
                    "Could not create new instance of root resource class");
        } catch (InstantiateException e) {
            throw excHandler.instantiateExecption(e, tlContext.get(),
                    "Could not create new instance of root resource class");
        } catch (InvocationTargetException e) {
            throw handleInvocationTargetExc(e);
        }
        return o;
    }

    /**
     * @param o
     * @param subResourceLocator
     * @param callContext
     * @return
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    private ResourceObject createSubResource(ResourceObject o,
            SubResourceLocator subResourceLocator, CallContext callContext)
            throws WebApplicationException, RequestHandledException {
        try {
            o = subResourceLocator.createSubResource(o, resourceClasses,
                    getLogger());
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, subResourceLocator,
                    callContext,
                    "Could not create new instance of resource class");
        } catch (MissingAnnotationException e) {
            throw excHandler.missingAnnotation(e, callContext,
                    "Could not create new instance of resource class");
        } catch (InstantiateException e) {
            throw excHandler.instantiateExecption(e, callContext,
                    "Could not create new instance of resource class");
        } catch (InvocationTargetException e) {
            throw handleInvocationTargetExc(e);
        } catch (ConvertRepresentationException e) {
            throw excHandler.convertRepresentationExc(e);
        }
        return o;
    }

    /**
     * Structure to return an instance of the identified
     * {@link RootResourceClass}, the matched URI path and the remaining path
     * after identifying the root resource class.
     * 
     * @author Stephan Koops
     */
    class RroRemPathAndMatchedPath {

        private ResourceObject rootResObj;

        private RemainingPath u;

        private String matchedUriPath;

        RroRemPathAndMatchedPath(ResourceObject rootResObj, RemainingPath u,
                String matchedUriPath) {
            this.rootResObj = rootResObj;
            this.u = u;
            this.matchedUriPath = matchedUriPath;
        }
    }

    /**
     * Structure to return the obtained {@link ResourceObject} and the remaining
     * path after identifying the object.
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
     * Structure to return the obtained {@link ResourceObject} and the
     * identified {@link ResourceMethod}.
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
     * Gets the currently used {@link RoleChecker}.
     * 
     * @return the currently used RoleChecker.
     * @see #setRoleChecker(RoleChecker)
     */
    public RoleChecker getRoleChecker() {
        return roleChecker;
    }

    /**
     * Sets the {@link RoleChecker} to use.
     * 
     * @param roleChecker
     *                the roleChecker to set. Must not be null. Take a look at
     *                {@link RoleChecker#ALLOW_ALL},
     *                {@link RoleChecker#FORBID_ALL} and
     *                {@link RoleChecker#REJECT_WITH_ERROR}.
     * @throws IllegalArgumentException
     *                 If the given roleChecker is null.
     * @see RoleChecker
     * @see #getRoleChecker()
     */
    public void setRoleChecker(RoleChecker roleChecker)
            throws IllegalArgumentException {
        if (roleChecker == null)
            throw new IllegalArgumentException(
                    "The roleChecker must not be null. You can use the "
                            + "RoleChecker.ALLOW_ALL constant, the "
                            + "RoleChecker.FORBID_ALL constant or the "
                            + "RoleChecker.REJECT_WITH_ERROR constant");
        this.roleChecker = roleChecker;
    }

    /**
     * Returns an unmodifiable set with the attached root resource classes.
     * 
     * @return an unmodifiable set with the attached root resource classes.
     * @see RootResourceClass
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        for (RootResourceClass rootResourceClass : this.resourceClasses.roots())
            rrcs.add(rootResourceClass.getJaxRsClass());
        return Collections.unmodifiableSet(rrcs);
    }

    /**
     * Returns a Collection with all root uris attached to this JaxRsRestlet.
     * 
     * @return a Collection with all root uris attached to this JaxRsRestlet.
     */
    public Collection<String> getRootUris() {
        List<String> uris = new ArrayList<String>();
        for (RootResourceClass rrc : this.resourceClasses.roots())
            uris.add(rrc.getPathRegExp().getPathPattern());
        return Collections.unmodifiableCollection(uris);
    }

    /**
     * Returns the ObjectFactory for root resource class and provider
     * instantiation, if given.
     * 
     * @return the ObjectFactory for root resource class and provider
     *         instantiation, if given.
     */
    public ObjectFactory getObjectFactory() {
        return this.objectFactory;
    }

    /**
     * Sets the ObjectFactory for root resource class and provider
     * instantiation.
     * 
     * @param objectFactory
     *                the ObjectFactory for root resource class and provider
     *                instantiation.
     */
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.providers.setObjectFactory(objectFactory);
    }
}