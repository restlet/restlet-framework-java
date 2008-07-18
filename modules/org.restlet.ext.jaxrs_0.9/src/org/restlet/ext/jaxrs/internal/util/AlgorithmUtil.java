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
package org.restlet.ext.jaxrs.internal.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.SubResourceLocator;

/**
 * This class contains helper methods for the algorithm in
 * {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
 * 
 * @author Stephan Koops
 */
public class AlgorithmUtil {

    private static enum ConsOrProdMime {
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
     * Adds the matched template parameters to the {@link CallContext}.
     * 
     * @param matchResult
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link Request} and the Restlet
     *            {@link Response}.
     */
    public static void addPathVarsToMap(MatchingResult matchResult,
            CallContext callContext) {
        final Map<String, String> variables = matchResult.getVariables();
        for (final Map.Entry<String, String> varEntry : variables.entrySet()) {
            final String key = varEntry.getKey();
            final String value = varEntry.getValue();
            callContext.addPathParamsEnc(key, value);
        }
    }

    private static Map<ResourceMethod, List<MediaType>> findMethodsSupportAllTypes(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut) {
        final Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (final ResourceMethod resourceMethod : resourceMethods) {
            final List<MediaType> mimes = getConsOrProdMimes(resourceMethod,
                    inOut);
            for (final MediaType resMethMediaType : mimes) {
                if (resMethMediaType.equals(MediaType.ALL)) {
                    returnMethods.put(resourceMethod, mimes);
                }
            }
        }
        return returnMethods;
    }

    private static Map<ResourceMethod, List<MediaType>> findMethodsSupportType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        final Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (final ResourceMethod resourceMethod : resourceMethods) {
            final List<MediaType> mimes = getConsOrProdMimes(resourceMethod,
                    inOut);
            for (final MediaType resMethMediaType : mimes) {
                for (final MediaType mediaType : mediaTypes) {
                    final String resMethMainType = resMethMediaType
                            .getMainType();
                    final String wishedMainType = mediaType.getMainType();
                    if (resMethMainType.equals(wishedMainType)) {
                        returnMethods.put(resourceMethod, mimes);
                    }
                }
            }
        }
        return returnMethods;
    }

    /**
     * @param resourceMethods
     * @param inOut
     * @param mediaType
     * @return Never returns null.
     */
    private static Map<ResourceMethod, List<MediaType>> findMethodsSupportTypeAndSubType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        final Map<ResourceMethod, List<MediaType>> returnMethods = new HashMap<ResourceMethod, List<MediaType>>();
        for (final ResourceMethod resourceMethod : resourceMethods) {
            final List<MediaType> mimes = getConsOrProdMimes(resourceMethod,
                    inOut);
            for (final MediaType resMethMediaType : mimes) {
                for (final MediaType mediaType : mediaTypes) {
                    if (resMethMediaType.equals(mediaType, true)) {
                        returnMethods.put(resourceMethod, mimes);
                    }
                }
            }
        }
        return returnMethods;
    }

    /**
     * @param resourceMethods
     * @param consumeOrPr_mime
     * @param mediaType
     * @return
     */
    private static Map<ResourceMethod, List<MediaType>> findMethodSupportsMime(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        if ((mediaTypes == null) || mediaTypes.isEmpty()) {
            return findMethodsSupportAllTypes(resourceMethods, inOut);
        }
        Map<ResourceMethod, List<MediaType>> mms;
        mms = findMethodsSupportTypeAndSubType(resourceMethods, inOut,
                mediaTypes);
        if (mms.isEmpty()) {
            mms = findMethodsSupportType(resourceMethods, inOut, mediaTypes);
            if (mms.isEmpty()) {
                mms = findMethodsSupportAllTypes(resourceMethods, inOut);
            }
        }
        return mms;
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
     *            the resourceMethods that provide the required mediaType
     * @param givenMediaType
     *            The MediaType of the given entity.
     * @param accMediaTypes
     *            The accepted MediaTypes
     * @param httpMethod
     *            The HTTP method of the request.
     * @return Returns the method who best matches the given and accepted media
     *         type in the request, or null
     */
    public static ResourceMethod getBestMethod(
            Collection<ResourceMethod> resourceMethods,
            MediaType givenMediaType, SortedMetadata<MediaType> accMediaTypes,
            Method httpMethod) {
        SortedMetadata<MediaType> givenMediaTypes;
        if (givenMediaType != null) {
            givenMediaTypes = SortedMetadata.singleton(givenMediaType);
        } else {
            givenMediaTypes = null;
        }
        // mms = methods that support the given MediaType
        Map<ResourceMethod, List<MediaType>> mms1;
        mms1 = findMethodSupportsMime(resourceMethods,
                ConsOrProdMime.CONSUME_MIME, givenMediaTypes);
        if (mms1.isEmpty()) {
            return Util.getFirstElement(resourceMethods);
        }
        if (mms1.size() == 1) {
            return Util.getFirstKey(mms1);
        }
        // check for method with best Produces (secondary key)
        // mms = Methods support given MediaType and requested MediaType
        Map<ResourceMethod, List<MediaType>> mms2;
        mms2 = findMethodSupportsMime(mms1.keySet(),
                ConsOrProdMime.PRODUCE_MIME, accMediaTypes);
        if (mms2.isEmpty()) {
            return Util.getFirstKey(mms1);
        }
        if (mms2.size() == 1) {
            return Util.getFirstKey(mms2);
        }
        for (final MediaType accMediaType : accMediaTypes) {
            ResourceMethod bestMethod = null;
            for (final Map.Entry<ResourceMethod, List<MediaType>> mm : mms2
                    .entrySet()) {
                for (final MediaType methodMediaType : mm.getValue()) {
                    if (accMediaType.includes(methodMediaType)) {
                        final ResourceMethod currentMethod = mm.getKey();
                        if (bestMethod == null) {
                            bestMethod = currentMethod;
                        } else {
                            if (httpMethod.equals(Method.HEAD)) {
                                // special handling for HEAD
                                final Method bestMethodHttp = bestMethod
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
            if (bestMethod != null) {
                return bestMethod;
            }
        }
        return Util.getFirstKey(mms2);
    }

    /**
     * @param resourceMethod
     * @param inOut
     * @return
     */
    private static List<MediaType> getConsOrProdMimes(
            ResourceMethod resourceMethod, ConsOrProdMime inOut) {
        if (inOut.equals(ConsOrProdMime.CONSUME_MIME)) {
            return resourceMethod.getConsumedMimes();
        }
        final List<MediaType> producedMimes = resourceMethod.getProducedMimes();
        if (producedMimes.isEmpty()) {
            return Util.createList(MediaType.ALL);
        }
        return producedMimes;
    }

    /**
     * Implementation of algorithm in JSR-311-Spec, Revision 151, Version
     * 2008-03-11, Section 3.6, Part 2f+2g
     * 
     * @param eWithMethod
     *            Collection of Sub-ResourceMethods and SubResourceLocators
     * @return the resource method or sub resource locator, or null, if the Map
     *         is null or empty.
     */
    public static ResourceMethodOrLocator getFirstMethOrLocByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<ResourceMethodOrLocator> eWithMethod) {
        if ((eWithMethod == null) || eWithMethod.isEmpty()) {
            return null;
        }
        final Iterator<ResourceMethodOrLocator> srmlIter = eWithMethod
                .iterator();
        ResourceMethodOrLocator bestSrml = srmlIter.next();
        if (eWithMethod.size() == 1) {
            return bestSrml;
        }
        int bestSrmlChars = Integer.MIN_VALUE;
        int bestSrmlNoCaptGroups = Integer.MIN_VALUE;
        for (final ResourceMethodOrLocator srml : eWithMethod) {
            final int srmlNoLitChars = srml.getPathRegExp()
                    .getNumberOfLiteralChars();
            final int srmlNoCaptGroups = srml.getPathRegExp()
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
     *            Collection of root resource classes
     * @return null, if the Map is null or empty
     */
    public static RootResourceClass getFirstRrcByNumberOfLiteralCharactersAndByNumberOfCapturingGroups(
            Collection<RootResourceClass> rrcs) {
        if ((rrcs == null) || rrcs.isEmpty()) {
            return null;
        }
        final Iterator<RootResourceClass> rrcIter = rrcs.iterator();
        RootResourceClass bestRrc = rrcIter.next();
        if (rrcs.size() == 1) {
            return bestRrc;
        }
        int bestRrcChars = Integer.MIN_VALUE;
        int bestRrcNoCaptGroups = Integer.MIN_VALUE;
        for (final RootResourceClass rrc : rrcs) {
            final int rrcNoLitChars = rrc.getPathRegExp()
                    .getNumberOfLiteralChars();
            final int rrcNoCaptGroups = rrc.getPathRegExp()
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
     * Removes the {@link ResourceMethod}s from the collection, that do not
     * support the given HTTP method.
     * 
     * @param resourceMethods
     *            the collection of {@link ResourceMethod}s.
     * @param httpMethod
     *            the HTTP {@link Method}
     * @param alsoGet
     *            if true, also methods suporting GET are included, also if
     *            another HTTP method is required. It is intended to be used for
     *            HEAD requests.
     */
    public static void removeNotSupportedHttpMethod(
            Collection<ResourceMethod> resourceMethods,
            org.restlet.data.Method httpMethod, boolean alsoGet) {
        final Iterator<ResourceMethod> methodIter = resourceMethods.iterator();
        while (methodIter.hasNext()) {
            final ResourceMethod resourceMethod = methodIter.next();
            if (!resourceMethod.isHttpMethodSupported(httpMethod, alsoGet)) {
                methodIter.remove();
            }
        }
    }
}