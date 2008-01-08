/*
 * Copyright 2005-2007 Noelios Consulting.
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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.ext.jaxrs.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.wrappers.SubResourceLocator;
import org.restlet.ext.jaxrs.wrappers.SubResourceMethod;
import org.restlet.ext.jaxrs.wrappers.SubResourceMethodOrLocator;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * The router choose the JAX-RS resource class and method to use for a request.
 * This class has methods {@link #attach(Class)} and {@link #detach(Class)} like
 * the Restlet {@link Router}. The variable names in this class are often the
 * same as in the JAX-RS-Definition.<br />
 * 
 * TODO The class JaxRsRouter is not thread save while attach or detach classes.
 * 
 * @see <a href="https://jsr311.dev.java.net/"> Java Service Request 311</a>
 *      Because the specification is just under development the link is not set
 *      to the PDF.
 * 
 * @author Stephan Koops
 */
public class JaxRsRouter extends Restlet {
    /**
     * This set must only changed by adding or removing a root resource class to
     * this JaxRsRouter.
     * 
     * @see #attach(Class)
     * @see #detach(Class)
     */
    private Set<RootResourceClass> rootResourceClasses = new HashSet<RootResourceClass>();

    /**
     * The default Restlet used when a root resource can not be found.
     * 
     * @see #errorRestletRootResourceNotFound
     */
    public static final ReturnStatusRestlet DEFAULT_ROOT_RESOURCE_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Root resource class not found"));

    /**
     * The default Restlet used when a (sub) resource can not be found.
     * 
     * @see #errorRestletResourceNotFound
     */
    public static final ReturnStatusRestlet DEFAULT_RESOURCE_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Resource class not found"));

    /**
     * The default Restlet used when a (sub) resource method can not be found.
     * 
     * @see #errorRestletResourceMethodNotFound
     */
    public static final ReturnStatusRestlet DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Resource method not found or it is not public"));

    /**
     * The default Restlet used when multiple possible resource methods was
     * found.
     * 
     * @see #errorRestletMultipleResourceMethods
     */
    public static final ReturnStatusRestlet DEFAULT_ON_MULTIPLE_RESOURCE_METHODS = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Multiple possible resource methods found"));

    /**
     * The default Restlet used when multiple root resource were found.
     * 
     * @see #errorRestletMultipleRootResourceClasses
     */
    public static final ReturnStatusRestlet DEFAULT_ON_MULTIPLE_ROOT_RESOURCE_CLASSES = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Multiple possible root resource classes found"));

    /**
     * The default Restlet used when the method is not allwed on the resource.
     * 
     * @see #errorRestletMethodNotAllowed
     */
    public static final ReturnStatusRestlet DEFAULT_METHOD_NOT_ALLOWED_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

    /**
     * The default Restlet used when the media type is not supported
     * 
     * @see #errorRestletUnsupportedMediaType
     */
    public static final ReturnStatusRestlet DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);

    /**
     * The default Restlet used when the request is not acceptable.
     * 
     * @see #errorRestletRootResourceNotFound
     */
    public static final ReturnStatusRestlet DEFAULT_NOT_ACCEPTABLE_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_NOT_ACCEPTABLE);

    /**
     * This Restlet will be used to handle the request if no root resource class
     * can be found.
     */
    private Restlet errorRestletRootResourceNotFound = DEFAULT_ROOT_RESOURCE_NOT_FOUND_RESTLET;

    private Restlet errorRestletResourceNotFound = DEFAULT_RESOURCE_NOT_FOUND_RESTLET;

    /** When no Method for the give path is found */
    private Restlet errorRestletResourceMethodNotFound = DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET;

    private Restlet errorRestletMethodNotAllowed = DEFAULT_METHOD_NOT_ALLOWED_RESTLET;

    private Restlet errorRestletUnsupportedMediaType = DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET;

    private Restlet errorRestletNotAcceptable = DEFAULT_NOT_ACCEPTABLE_RESTLET;

    private Restlet errorRestletMultipleResourceMethods = DEFAULT_ON_MULTIPLE_RESOURCE_METHODS;

    private Restlet errorRestletMultipleRootResourceClasses = DEFAULT_ON_MULTIPLE_ROOT_RESOURCE_CLASSES;

    /**
     * Creates a new JaxRsRouter. You should use the other Constructor.
     * 
     * @see #JaxRsRouter(Context)
     * @see Restlet#Restlet()
     */
    public JaxRsRouter() {
        super();
    }

    /**
     * Creates a new JaxRsRouter with the given Context
     * 
     * @param context
     */
    public JaxRsRouter(Context context) {
        super(context);
    }

    /**
     * Will use the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     * @see #detach(Class)
     * @see #getRootResourceClasses()
     */
    public void attach(Class<?> jaxRsClass) {
        RootResourceClass newRrc = new RootResourceClass(jaxRsClass);
        UriTemplateRegExp uriTempl = newRrc.getUriTemplateRegExp();
        for(RootResourceClass rootResourceClass : this.rootResourceClasses)
        {
            if(rootResourceClass.getJaxRsClass().equals(jaxRsClass))
                return;
            if(rootResourceClass.getUriTemplateRegExp().equals(uriTempl))
                throw new IllegalArgumentException("There is already a root resource class with path "+uriTempl.getPathPattern());
        }
        rootResourceClasses.add(newRrc);
    }

    /**
     * Detaches the JAX-RS root resource class from this router.
     * 
     * @param jaxRsClass
     *                The JAX-RS root resource class to detach.
     * @see #attach(Class)
     */
    public void detach(Class<?> jaxRsClass) {
        this.rootResourceClasses.remove(jaxRsClass);
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
        ResObjAndMeth resObjAndMeth;
        try {
            resObjAndMeth = matchingRequestToResourceMethod(request, response);
        } catch (CouldNotFindMethodException e) {
            e.errorRestlet.handle(request, response);
            return;
        } catch (MethodInvokeException e) {
            // Exception was handled and data were set into the Response.
            return;
        }
        ResourceMethod resourceMethod = resObjAndMeth.resourceMethod;
        ResourceObject resourceObject = resObjAndMeth.resourceObject;
        MatchingResult matchingResult = resObjAndMeth.matchingResult;
        MultivaluedMap<String, String> allTemplParamsEnc = resObjAndMeth.allTemplParamsEnc;
        try {
            invokeMethodAndHandleResult(resourceMethod, resourceObject,
                    matchingResult, allTemplParamsEnc, request, response);
        } catch (MethodInvokeException e) {
            // Exception was handled and data were set into the Response.
            return; // not explicit necessary, but to document that here is
            // finish, also when more code comes later.
        }
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods.
     * 
     * @return (Sub)Resource Method
     * @throws CouldNotFindMethodException
     * @throws MethodInvokeException
     */
    private ResObjAndMeth matchingRequestToResourceMethod(
            Request restletRequest, Response restletResponse)
            throws CouldNotFindMethodException, MethodInvokeException {
        String uriRemainingPart = restletRequest.getResourceRef()
                .getRemainingPart();
        org.restlet.data.Method requMethod = restletRequest.getMethod();
        ResClAndTemplate rcat = identifyRootResourceClass(uriRemainingPart);
        ResObjAndPath resourceObjectAndPath = obtainObjectThatHandleRequest(
                rcat, restletRequest, restletResponse);
        MatchingResult matchingResult = resourceObjectAndPath.matchingResult;
        @SuppressWarnings("unchecked")
        List<Collection<MediaType>> accMediaTypes = (List) Util
                .sortMetadataList((Collection) restletRequest.getClientInfo()
                        .getAcceptedMediaTypes());
        MediaType givenMediaType = restletRequest.getEntity().getMediaType();
        ResObjAndMeth method = idenifyMethodThatHandleRequest(
                resourceObjectAndPath, requMethod, givenMediaType,
                accMediaTypes);
        method.matchingResult = matchingResult;
        return method;
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods, Part 1.
     * 
     * @return .. or null, wenn no root resource class could be found
     * @throws MethodInvokeException
     */
    private ResClAndTemplate identifyRootResourceClass(String uriRemainingPart)
            throws CouldNotFindMethodException {
        // 1. Identify the root resource class:
        // (a)
        String u = uriRemainingPart;
        // c: Set<Class>: root resource classes
        // e: Set<RegExp>
        // Map<UriTemplateRegExp, Class> eAndCs = new HashMap();
        Collection<RootResourceClass> eAndCs = new ArrayList<RootResourceClass>();
        // (a) and (b) and (c) Filter E
        for (RootResourceClass rootResourceClass : this.rootResourceClasses) {
            // Map.Entry<UriTemplateRegExp, Class> eAndC = eAndCIter.next();
            // UriTemplateRegExp regExp = eAndC.getKey();
            // Class clazz = eAndC.getValue();
            MatchingResult matchingResult = rootResourceClass
                    .getUriTemplateRegExp().match(u);
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
            throw new CouldNotFindMethodException(
                    errorRestletRootResourceNotFound);
        // (e) and (f)
        RootResourceClass tClass = getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eAndCs);
        // (f)
        UriTemplateRegExp rMatch = tClass.getUriTemplateRegExp();
        MatchingResult matchResult = rMatch.match(u);
        u = matchResult.getFinalCapturingGroup();
        MultivaluedMap<String, String> allTemplParamsEnc = new MultivaluedMapImpl<String, String>();
        for (Map.Entry<String, String> varEntry : matchResult.getVariables()
                .entrySet())
            allTemplParamsEnc.add(varEntry.getKey(), varEntry.getValue());
        return new ResClAndTemplate(u, tClass, matchResult, allTemplParamsEnc);
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5, Part 2
     * 
     * @param resClAndTemplate
     * @param restletRequest
     *                The Restlet request
     * @return Resource Object
     * @throws MethodInvokeException
     */
    private ResObjAndPath obtainObjectThatHandleRequest(
            ResClAndTemplate resClAndTemplate, Request restletRequest,
            Response restletResponse) throws CouldNotFindMethodException,
            MethodInvokeException {
        String u = resClAndTemplate.u;
        RootResourceClass resClass = resClAndTemplate.rrc;
        MultivaluedMap<String, String> allTemplParamsEnc = resClAndTemplate.allTemplParamsEnc;
        UriTemplateRegExp rMatch = resClass.getUriTemplateRegExp();
        ResourceObject o;
        // LATER benötige ich dynamische Proxies, um Instanzvariablen füllen zu
        // können?
        try {
            o = resClass.createInstance(resClAndTemplate.matchingResult,
                    allTemplParamsEnc, restletRequest);
        } catch (Exception e) {
            throw handleInvokeException(e, restletResponse, "createInstance",
                    "Could not create new instance of root resource class");
        }
        // Part 2
        for (;;) // (j)
        {
            // (a)
            if (Util.isEmptyOrSlash(u)) {
                @SuppressWarnings("unchecked")
                Map<String, String> variables = Collections.EMPTY_MAP;
                return new ResObjAndPath(o, u, new MatchingResult(variables,
                        "", "", 0), allTemplParamsEnc);
            }
            // (b)
            Collection<SubResourceMethodOrLocator> eWithMethod = new ArrayList<SubResourceMethodOrLocator>();
            // (c) and (d) Filter E
            for (SubResourceMethodOrLocator methodOrLocator : resClass
                    .getSubResourceMethodsAndLocators()) {
                MatchingResult matchResult = methodOrLocator
                        .getUriTemplateRegExp().match(u);
                if (matchResult == null)
                    continue;
                if (!Util.isEmptyOrSlash(matchResult.getFinalMatchingGroup()))
                    continue;
                eWithMethod.add(methodOrLocator);
            }
            // (e)
            if (eWithMethod.isEmpty())
                throw new CouldNotFindMethodException(
                        errorRestletResourceNotFound);
            // (f)
            SubResourceMethodOrLocator firstMeth = getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(eWithMethod);
            // (g)
            rMatch = firstMeth.getUriTemplateRegExp();
            MatchingResult matchingResult = rMatch.match(u);
            // (h) When Method is resource method
            if (firstMeth instanceof SubResourceMethod)
                return new ResObjAndPath(o, u, matchingResult,
                        allTemplParamsEnc);
            // TODO warum wird hier nicht die ausgewählte Methode mit
            // zurückgegeben?
            // (g) and (i)
            u = matchingResult.getFinalCapturingGroup();
            SubResourceLocator subResourceLocator = (SubResourceLocator) firstMeth;
            try {
                o = subResourceLocator.createSubResource(o, matchingResult,
                        allTemplParamsEnc, restletRequest);
            } catch (Exception e) {
                throw handleInvokeException(e, restletResponse,
                        "createSubResource",
                        "Could not create new instance of root resource class");
            }
            // (j) Go to step 2a (repeat for)
        }
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2007-12-07, Section 2.5 Matching Requests to Resource Methods, Part 3.
     * 
     * @param accMediaTypes
     *                sorted by its qualities.
     * @return Resource Object and Method, that handle the request.
     * @throws ResourceMethodNotFoundException
     */
    private ResObjAndMeth idenifyMethodThatHandleRequest(
            ResObjAndPath resObjAndPath, org.restlet.data.Method httpMethod,
            MediaType givenMediaType, List<Collection<MediaType>> accMediaTypes)
            throws CouldNotFindMethodException {
        // 3. Identify the method that will handle the request:
        // (a)
        ResourceObject resourceObject = resObjAndPath.resourceObject;
        String remainingPath = resObjAndPath.remainingPath;
        MultivaluedMap<String, String> allTemplParamsEnc = resObjAndPath.allTemplParamsEnc;
        List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        // (a) 1
        for (SubResourceMethod method : resourceObject.getResourceClass()
                .getSubResourceMethods()) {
            UriTemplateRegExp methodPath = method.getUriTemplateRegExp();// 
            if (Util.isEmptyOrSlash(remainingPath)) {
                if (methodPath.isEmptyOrSlash())
                    resourceMethods.add(method);
            } else {
                if (methodPath.matchesWithEmpty(remainingPath))
                    resourceMethods.add(method);
            }
        }
        if (resourceMethods.isEmpty())
            throw new CouldNotFindMethodException(
                    errorRestletResourceMethodNotFound);
        // (a) 2
        Iterator<ResourceMethod> methodIter = resourceMethods.iterator();
        // for (SubResourceMethod resourceMethod : allResourceMethods) {
        while (methodIter.hasNext()) {
            ResourceMethod resourceMethod = methodIter.next();
            if (!resourceMethod.isHttpMethodSupported(httpMethod))
                // resourceMethods.add(resourceMethod);
                methodIter.remove();
        }
        if (resourceMethods.isEmpty()) {
            // LATER Spezialitäten für HEAD und OPTIONS
            throw new CouldNotFindMethodException(errorRestletMethodNotAllowed);
        }
        // (a) 3
        if (givenMediaType != null) {
            methodIter = resourceMethods.iterator();
            while (methodIter.hasNext()) {
                ResourceMethod resourceMethod = methodIter.next();
                if (!resourceMethod.isGivenMediaTypeSupported(givenMediaType))
                    methodIter.remove();
            }
            if (resourceMethods.isEmpty())
                throw new CouldNotFindMethodException(
                        errorRestletUnsupportedMediaType);
        }
        // (a) 4
        methodIter = resourceMethods.iterator();
        while (methodIter.hasNext()) {
            ResourceMethod resourceMethod = methodIter.next();
            if (!resourceMethod.isAcceptedMediaTypeSupported(accMediaTypes))
                methodIter.remove();
        }
        if (resourceMethods.isEmpty()) {
            // LATER zurückgeben, welche MediaTypes unterstützt werden.
            throw new CouldNotFindMethodException(errorRestletNotAcceptable);
        }
        // (b) and (c)
        ResourceMethod bestResourceMethod = getBestMethod(resourceMethods,
                givenMediaType, accMediaTypes);
        if (bestResourceMethod == null) {
            // LATER keine Methode gefunden.
            throw new RuntimeException();
        }
        return new ResObjAndMeth(resourceObject, bestResourceMethod,
                allTemplParamsEnc);
    }

    /**
     * Sort by using the media type of input data as the primary key and the
     * media type of output data as the secondary key.<br>
     * Sorting of media types follows the general rule: x/y < x/* < *<!---->/*,
     * i.e. a method that explicitly lists one of the requested media types is
     * sorted before a method that lists *<!---->/*. Quality parameter values
     * are also used such that x/y;q=1.0 < x/y;q=0.7.
     * 
     * @param resourceMethods
     *                the resourceMethods that provide the requiredmediaType
     * @return Returns the method who best matches the given and accepted media
     *         type in the request, or null
     * @throws CouldNotFindMethodException
     */
    private ResourceMethod getBestMethod(
            Collection<ResourceMethod> resourceMethods,
            MediaType givenMediaType, List<Collection<MediaType>> accMediaTypess)
            throws CouldNotFindMethodException {
        List<Collection<MediaType>> givenMediaTypes;
        if (givenMediaType != null)
            givenMediaTypes = Collections
                    .singletonList((Collection<MediaType>) Collections
                            .singletonList(givenMediaType));
        else
            givenMediaTypes = null;
        // mms = methods that support the given MediaType
        Map<ResourceMethod, List<MediaType>> mms = findMethodSupportsMime(
                resourceMethods, ConsOrProdMime.CONSUME_MIME, givenMediaTypes);
        if (mms.isEmpty())
            return null;
        if (mms.size() == 1)
            return Util.getFirstKey(mms);
        // check for method with best ProduceMime (secondary key)
        // mms = Methods support given MediaType and requested MediaType
        mms = findMethodSupportsMime(mms.keySet(), ConsOrProdMime.PRODUCE_MIME,
                accMediaTypess);
        if (mms.isEmpty())
            return null;
        if (mms.size() == 1)
            return Util.getFirstKey(mms);
        for (Collection<MediaType> accMediaTypes : accMediaTypess) {
            for (MediaType accMediaType : accMediaTypes) {
                ResourceMethod bestMethod = null;
                for (Map.Entry<ResourceMethod, List<MediaType>> mm : mms
                        .entrySet()) {
                    for (MediaType methodMediaType : mm.getValue()) {
                        if (accMediaType.includes(methodMediaType)) {
                            if (bestMethod != null)
                                // TODO JSR311: What happens, if multiple are =?
                                throw new CouldNotFindMethodException(
                                        this.errorRestletMultipleResourceMethods);
                            bestMethod = mm.getKey();
                        }
                    }
                }
                if (bestMethod != null)
                    return bestMethod;
            }
        }
        return null;
    }

    /**
     * @param resourceMethods
     * @param consumeOrPr_mime
     * @param mediaType
     * @return
     */
    private Map<ResourceMethod, List<MediaType>> findMethodSupportsMime(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            List<Collection<MediaType>> mediaTypess) {
        if (mediaTypess == null || mediaTypess.isEmpty())
            return findMethodsSupportAllTypes(resourceMethods, inOut);
        Map<ResourceMethod, List<MediaType>> mms;
        mms = findMethodsSupportTypeAndSubType(resourceMethods, inOut,
                mediaTypess);
        if (mms.isEmpty()) {
            mms = findMethodsSupportType(resourceMethods, inOut, mediaTypess);
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
            List<Collection<MediaType>> mediaTypess) {
        Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (ResourceMethod resourceMethod : resourceMethods) {
            List<MediaType> mimes = getConsOrProdMimes(resourceMethod, inOut);
            for (MediaType resMethMediaType : mimes) {
                for (Collection<MediaType> mediaTypes : mediaTypess)
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
            List<Collection<MediaType>> mediaTypess) {
        Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (ResourceMethod resourceMethod : resourceMethods) {
            List<MediaType> mimes = getConsOrProdMimes(resourceMethod, inOut);
            for (MediaType resMethMediaType : mimes) {
                for (Collection<MediaType> mediaTypes : mediaTypess)
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
    private SubResourceMethodOrLocator getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<SubResourceMethodOrLocator> eWithMethod)
            throws CouldNotFindMethodException {
        if (eWithMethod == null || eWithMethod.isEmpty())
            return null;
        Iterator<SubResourceMethodOrLocator> srmlIter = eWithMethod.iterator();
        SubResourceMethodOrLocator bestSrml = srmlIter.next();
        if (eWithMethod.size() == 1)
            return bestSrml;
        int bestSrmlChars = Integer.MIN_VALUE;
        int bestSrmlNoCaptGroups = Integer.MIN_VALUE;
        for (SubResourceMethodOrLocator srml : eWithMethod) {
            int srmlNoLitChars = srml.getUriTemplateRegExp()
                    .getNumberOfLiteralChars();
            int srmlNoCaptGroups = srml.getUriTemplateRegExp()
                    .getNumberOfCapturingGroups();
            // TODO JSR311: ist es nicht sinnvoller, die mit den meisten festen
            // Zeichen zurückzugeben? auch beim Initialisierungswert anpassen
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
                    // TODO JSR311: What happens, if both are equals?
                    throw new CouldNotFindMethodException(
                            this.errorRestletMultipleResourceMethods);
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
            int rrcNoLitChars = rrc.getUriTemplateRegExp()
                    .getNumberOfLiteralChars();
            int rrcNoCaptGroups = rrc.getUriTemplateRegExp()
            .getNumberOfCapturingGroups();
            // TODO ist es nicht sinnvoller, die mit den meisten festen Zeichen
            // zurückzugeben? auch beim Initialisierungswert anpassen
            if (rrcNoLitChars > bestRrcChars) {
                bestRrc = rrc;
                bestRrcChars = rrcNoLitChars;
                bestRrcNoCaptGroups =  rrcNoCaptGroups;
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
                    throw new CouldNotFindMethodException(
                            this.errorRestletMultipleRootResourceClasses);
                }
            }
        }
        return bestRrc;
    }

    // TODO JSR311: What about an injectable Interface "AuthenticatedUser" or
    // something like this?

    /**
     * @param e
     * @param restletResponse
     * @param methodName
     * @param logMessage
     * @throws MethodInvokeException
     */
    private MethodInvokeException handleInvokeException(Exception e,
            Response restletResponse, String methodName, String logMessage)
            throws MethodInvokeException {
        if (e.getCause() instanceof WebApplicationException) {
            WebApplicationException webAppExc = (WebApplicationException) e
                    .getCause();
            // the message of the Exception is not used in the
            // WebApplicationException
            jaxRsRespToRestletResp(webAppExc.getResponse(), restletResponse,
                    null); // LATER handleInvokeException: MediaType rausfinden
            throw new MethodInvokeException();
        }
        restletResponse.setStatus(Status.SERVER_ERROR_INTERNAL);
        getLogger().logp(Level.WARNING, this.getClass().getName(), methodName,
                logMessage, e);
        throw new MethodInvokeException();
    }

    /**
     * Is thrown when an method invokation has an error (exception or other) The
     * Exception or whatever was handled and the necessary data in
     * org.restlet.data.Response were set.
     * 
     * @author Stephan Koops
     */
    class MethodInvokeException extends Exception {
        private static final long serialVersionUID = 2765454873472711005L;
    }

    /**
     * @param resourceMethod
     * @param resourceObject
     * @param matchingResult
     *                The matching result
     * @param allTemplParamsEnc
     *                Contains all Parameters, that are read from the called
     *                URI.
     * @param restletRequest
     *                The Restlet request
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void invokeMethodAndHandleResult(ResourceMethod resourceMethod,
            ResourceObject resourceObject, MatchingResult matchingResult,
            MultivaluedMap<String, String> allTemplParamsEnc,
            Request restletRequest, Response restletResponse)
            throws MethodInvokeException {
        Object result;
        try {
            result = resourceMethod.invoke(resourceObject, matchingResult,
                    allTemplParamsEnc, restletRequest);
        } catch (Exception e) {
            throw handleInvokeException(e, restletResponse, "invoke",
                    "Can not invoke the resource method");
        }
        if (result == null) { // no representation
            restletResponse.setStatus(Status.SUCCESS_OK);
            restletResponse.setEntity(null);
            return;
        } else {
            restletResponse.setStatus(Status.SUCCESS_OK); // default
            MediaType mediaType = resourceMethod.getProducedMediaType(result);
            if (result instanceof CharSequence) {
                Representation restletRepresentation = new StringRepresentation(
                        (CharSequence) result);
                restletRepresentation.setMediaType(mediaType);
                restletResponse.setEntity(restletRepresentation);
            } else if (result instanceof Response) {
                jaxRsRespToRestletResp((javax.ws.rs.core.Response) result,
                        restletResponse, mediaType);
            } else {
                throw new NotYetImplementedException();
            }
        }
    }

    private void jaxRsRespToRestletResp(
            javax.ws.rs.core.Response jaxRsResponse, Response restletResponse,
            MediaType mediaType) {
        restletResponse.setStatus(Status.valueOf(jaxRsResponse.getStatus()));
        restletResponse.setEntity(convertToRepresentation(jaxRsResponse
                .getEntity(), mediaType, null, null));
        Util.copyResponseHeaders(jaxRsResponse.getMetadata(), restletResponse,
                getLogger());
        // TODO Hier müssen noch anderes übertragen werden.
    }

    private Representation convertToRepresentation(Object entity,
            MediaType mediaType, Language language, CharacterSet characterSet) {
        if (entity instanceof CharSequence)
            return new StringRepresentation((CharSequence) entity, mediaType,
                    language, characterSet);
        // TODO hier muss noch die Konvertierung von Object zu Representation
        // erfolgen, z.B. mit JAXB
        throw new NotYetImplementedException();
    }

    /**
     * Restlet, is used on HTTP-Error 404, when no Root Resource class could be
     * found.
     * 
     * @param rootResourceNotFoundRestlet
     *                The Restlet to use when no root resource class could be
     *                found. This Restlet must return status 404.
     * @throws IllegalArgumentException
     *                 If the Restlet is null.
     */
    public void setErrorRestletRootResourceNotFound(
            Restlet rootResourceNotFoundRestlet)
            throws IllegalArgumentException {
        if (rootResourceNotFoundRestlet == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletRootResourceNotFound = rootResourceNotFoundRestlet;
    }

    /**
     * @return Returns the Restlet, that is actually if no Root Resource class
     *         could be found.
     */
    public Restlet getErrorRestletRootResourceNotFound() {
        return this.errorRestletRootResourceNotFound;
    }

    /**
     * Restlet, is used on HTTP-Error 404, when no Resource class could be
     * found.
     * 
     * @param resourceNotFoundRestlet
     *                The Restlet to use when no resource class could be found.
     *                This Restlet must return status 404.
     * @throws IllegalArgumentException
     *                 If the Restlet is null.
     */
    public void setErrorRestletResourceNotFound(Restlet resourceNotFoundRestlet)
            throws IllegalArgumentException {
        if (resourceNotFoundRestlet == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletResourceNotFound = resourceNotFoundRestlet;
    }

    /**
     * @return Returns the Restlet, that is actually if no resource class could
     *         be found.
     */
    public Restlet getErrorRestletResourceNotFound() {
        return this.errorRestletResourceNotFound;
    }

    /**
     * Sets the Restlet that handles the request if no Resource class could be
     * found.
     * 
     * @param resourceMethodNotFoundRestlet
     *                The Restlet to use when no resource class could be found.
     *                This Restlet must return status 404.
     * @throws IllegalArgumentException
     *                 If the given Restlet is null.
     * @see #DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET
     */
    public void setErrorRestletResourceMethodNotFound(
            Restlet resourceMethodNotFoundRestlet)
            throws IllegalArgumentException {
        if (resourceMethodNotFoundRestlet == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletResourceMethodNotFound = resourceMethodNotFoundRestlet;
    }

    /**
     * @return Returns the Restlet, is used on HTTP-Error 404, when no Resource
     *         class could be found.
     */
    public Restlet getErrorRestletResourceMethodNotFound() {
        return this.errorRestletResourceNotFound;
    }

    /**
     * @return Returns the Restlet, that is actually if no resource method could
     *         be found.
     */
    public Restlet getErrorRestletMethodNotAllowed() {
        return errorRestletMethodNotAllowed;
    }

    /**
     * Set the Restlet to be used if the method is not allowed for the resource.
     * It must return status 405.
     * 
     * @param errorRestletMethodNotAllowed
     *                The Restlet to use.
     * @throws IllegalArgumentException
     *                 If the given restlet is null.
     */
    public void setErrorRestletMethodNotAllowed(
            Restlet errorRestletMethodNotAllowed)
            throws IllegalArgumentException {
        if (errorRestletMethodNotAllowed == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletMethodNotAllowed = errorRestletMethodNotAllowed;
    }

    /**
     * @return Returns the Restlet that handles the request, if the method is
     *         not allowed on the resource.
     */
    public Restlet getErrorRestletUnsupportedMediaType() {
        return errorRestletUnsupportedMediaType;
    }

    /**
     * Sets the Restlet that handles the request if the given media type is not
     * supported.
     * 
     * @param errorRestletUnsupportedMediaType
     *                The Restlet to use.
     * @throws IllegalArgumentException
     *                 If the given restlet is null.
     */
    public void setErrorRestletUnsupportedMediaType(
            Restlet errorRestletUnsupportedMediaType)
            throws IllegalArgumentException {
        if (errorRestletUnsupportedMediaType == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletUnsupportedMediaType = errorRestletUnsupportedMediaType;
    }

    /**
     * @return Returns the Restlet that hanndles the request if the accepted
     *         media type is not supported.
     */
    public Restlet getErrorRestletNotAcceptable() {
        return errorRestletNotAcceptable;
    }

    /**
     * Sets the Restlet that should handle the request, if the accpeted media
     * type is not supported. Must return status 406.
     * 
     * @param errorRestletNotAcceptable
     *                The Restlet to use
     * @throws IllegalArgumentException
     *                 If the given restlet is null.
     */
    public void setErrorRestletNotAcceptable(Restlet errorRestletNotAcceptable)
            throws IllegalArgumentException {
        if (errorRestletNotAcceptable == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletNotAcceptable = errorRestletNotAcceptable;
    }

    /**
     * @return Returns the Restlet that handles the request if multiple resource
     *         methods for a request were found.
     */
    public Restlet getErrorRestletMultipleResourceMethods() {
        return errorRestletMultipleResourceMethods;
    }

    /**
     * 
     * @param errorRestletMultipleResourceMethods
     * @throws IllegalArgumentException
     *                 If the given restlet is null.
     */
    public void setErrorRestletMultipleResourceMethods(
            Restlet errorRestletMultipleResourceMethods)
            throws IllegalArgumentException {
        if (errorRestletMultipleResourceMethods == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletMultipleResourceMethods = errorRestletMultipleResourceMethods;
    }

    /**
     * @return Returns the request if multiple root resource classes were found.
     */
    public Restlet getErrorRestletMultipleRootResourceClasses() {
        return errorRestletMultipleRootResourceClasses;
    }

    /**
     * 
     * @param errorRestletMultipleRootResourceClasses
     * @throws IllegalArgumentException
     *                 If the given restlet is null.
     */
    public void setErrorRestletMultipleRootResourceClasses(
            Restlet errorRestletMultipleRootResourceClasses)
            throws IllegalArgumentException {
        if (errorRestletMultipleRootResourceClasses == null)
            throw new IllegalArgumentException(
                    "The Error Restlet must not be null");
        this.errorRestletMultipleRootResourceClasses = errorRestletMultipleRootResourceClasses;
    }

    /**
     * Returns a set with the attached root resource classes.
     * 
     * @return
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> jaxRsClasses = new HashSet<Class<?>>();
        for (RootResourceClass rootResourceClass : this.rootResourceClasses)
            jaxRsClasses.add(rootResourceClass.getJaxRsClass());
        return Collections.unmodifiableSet(jaxRsClasses);
    }

    class ResObjAndPath {

        private ResourceObject resourceObject;

        private String remainingPath;

        private MatchingResult matchingResult;

        private MultivaluedMap<String, String> allTemplParamsEnc;

        ResObjAndPath(ResourceObject resourceObject, String remainingPath,
                MatchingResult matchingResult,
                MultivaluedMap<String, String> allTemplParamsEnc) {
            this.resourceObject = resourceObject;
            this.remainingPath = remainingPath;
            this.matchingResult = matchingResult;
            this.allTemplParamsEnc = allTemplParamsEnc;
        }
    }

    class ResObjAndMeth {

        private ResourceObject resourceObject;

        private ResourceMethod resourceMethod;

        private MatchingResult matchingResult; // is added not on creation

        private MultivaluedMap<String, String> allTemplParamsEnc;

        ResObjAndMeth(ResourceObject resourceObject,
                ResourceMethod resourceMethod,
                MultivaluedMap<String, String> allTemplParamsEnc) {
            this.resourceObject = resourceObject;
            this.resourceMethod = resourceMethod;
            this.allTemplParamsEnc = allTemplParamsEnc;
        }
    }

    class ResClAndTemplate {

        private String u;

        private RootResourceClass rrc;

        MultivaluedMap<String, String> allTemplParamsEnc;

        private MatchingResult matchingResult;

        ResClAndTemplate(String u, RootResourceClass rrc,
                MatchingResult matchingResult,
                MultivaluedMap<String, String> allTemplParamsEnc) {
            this.u = u;
            this.rrc = rrc;
            this.matchingResult = matchingResult;
            this.allTemplParamsEnc = allTemplParamsEnc;
        }
    }

    /**
     * This exception is thrown, when the algorithm "Matching Requests to
     * Resource Methods" in Section 2.5 of JSR-311-Spec could not find a method.
     * 
     * @author Stephan Koops
     */
    private class CouldNotFindMethodException extends Exception {
        private static final long serialVersionUID = -8436314060905405146L;

        private Restlet errorRestlet;

        CouldNotFindMethodException(Restlet errorRestlet) {
            this.errorRestlet = errorRestlet;
        }
    }

    private static class ReturnStatusRestlet extends Restlet {
        private Status status;

        ReturnStatusRestlet(Status status) {
            this.status = status;
        }

        @Override
        public void handle(Request request, Response response) {
            super.handle(request, response);
            response.setStatus(status);
        }
    }
}