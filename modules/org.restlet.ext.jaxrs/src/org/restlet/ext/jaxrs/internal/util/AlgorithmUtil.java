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

package org.restlet.ext.jaxrs.internal.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.RrcOrRml;
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
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
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

    private static OrderedMap<ResourceMethod, List<MediaType>> findMethodsSupportAllTypes(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut) {
        final OrderedMap<ResourceMethod, List<MediaType>> returnMethods = new OrderedMap<ResourceMethod, List<MediaType>>();
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

    private static OrderedMap<ResourceMethod, List<MediaType>> findMethodsSupportType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        final OrderedMap<ResourceMethod, List<MediaType>> returnMethods = new OrderedMap<ResourceMethod, List<MediaType>>();
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
    private static OrderedMap<ResourceMethod, List<MediaType>> findMethodsSupportTypeAndSubType(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        final OrderedMap<ResourceMethod, List<MediaType>> returnMethods = new OrderedMap<ResourceMethod, List<MediaType>>();
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
     * @param inOut
     * @param mediaType
     * @return
     */
    private static OrderedMap<ResourceMethod, List<MediaType>> findMethodSupportsMime(
            Collection<ResourceMethod> resourceMethods, ConsOrProdMime inOut,
            SortedMetadata<MediaType> mediaTypes) {
        if ((mediaTypes == null) || mediaTypes.isEmpty()) {
            return findMethodsSupportAllTypes(resourceMethods, inOut);
        }
        OrderedMap<ResourceMethod, List<MediaType>> mms;
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
     * Sorts the ResourceMethods by it's number of non default regular
     * expressions
     */
    private static Comparator<ResourceMethod> COMP = new Comparator<ResourceMethod>() {
        public int compare(ResourceMethod rm1, ResourceMethod rm2) {
            int nndre1 = rm1.getPathRegExp().getNoNonDefCaprGroups();
            int nndre2 = rm2.getPathRegExp().getNoNonDefCaprGroups();
            return nndre2 - nndre1;
        }
    };

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
     * @param unsortedResourceMethods
     *                the resourceMethods that provide the required mediaType
     * @param givenMediaType
     *                The MediaType of the given entity.
     * @param accMediaTypes
     *                The accepted MediaTypes
     * @param requHttpMethod
     *                The HTTP method of the request.
     * @return Returns the method who best matches the given and accepted media
     *         type in the request, or null
     */
    public static ResourceMethod getBestMethod(
            Collection<ResourceMethod> unsortedResourceMethods,
            MediaType givenMediaType, SortedMetadata<MediaType> accMediaTypes,
            Method requHttpMethod) {
        final Collection<ResourceMethod> resourceMethods;
        resourceMethods = new SortedOrderedBag<ResourceMethod>(COMP, 
                unsortedResourceMethods);
        // 3 b+c
        SortedMetadata<MediaType> givenMediaTypes;
        if (givenMediaType != null) {
            givenMediaTypes = SortedMetadata.singleton(givenMediaType);
        } else {
            givenMediaTypes = null;
        }
        // mms = methods that support the given MediaType
        OrderedMap<ResourceMethod, List<MediaType>> mms1;
        mms1 = findMethodSupportsMime(resourceMethods,
                ConsOrProdMime.CONSUME_MIME, givenMediaTypes);
        if (mms1.size() == 1) {
            return Util.getFirstKey(mms1);
        }
        if (mms1.isEmpty()) {
            return Util.getFirstElement(resourceMethods);
        }
        // check for method with best Produces (secondary key)
        // mms = Methods support given MediaType and requested MediaType
        Map<ResourceMethod, List<MediaType>> mms2;
        mms2 = findMethodSupportsMime(mms1.keySet(),
                ConsOrProdMime.PRODUCE_MIME, accMediaTypes);
        if (mms2.size() == 1) {
            return Util.getFirstKey(mms2);
        }
        if (mms2.isEmpty()) {
            return Util.getFirstKey(mms1);
        }
        for (final MediaType accMediaType : accMediaTypes) {
            ResourceMethod bestResMethod = null;
            for (final Map.Entry<ResourceMethod, List<MediaType>> mm : mms2
                    .entrySet()) {
                for (final MediaType methodMediaType : mm.getValue()) {
                    if (accMediaType.includes(methodMediaType)) {
                        final ResourceMethod currentResMethod = mm.getKey();
                        if (bestResMethod == null) {
                            bestResMethod = currentResMethod;
                        } else {
                            if (requHttpMethod.equals(Method.HEAD)) {
                                // special handling for HEAD
                                final Method bestMethodHttp;
                                bestMethodHttp = bestResMethod.getHttpMethod();
                                if (bestMethodHttp.equals(Method.GET)
                                        && currentResMethod.getHttpMethod()
                                                .equals(Method.HEAD)) {
                                    // ignore HEAD method
                                } else if (bestMethodHttp.equals(Method.HEAD)
                                        && currentResMethod.getHttpMethod()
                                                .equals(Method.GET)) {
                                    bestResMethod = currentResMethod;
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
            if (bestResMethod != null) {
                return bestResMethod;
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
     * 2008-08-27, Section 3.7.2, Part 1.e and nearly the same part 2f+2g.<br>
     * Sort E using
     * <ol>
     * <li>the number of literal characters in each member as the primary key
     * (descending order),</li>
     * <li>the number of capturing groups as a secondary key (descending
     * order),</li>
     * <li>the number of capturing groups with non-default regular expressions
     * (i.e. not "([^/]+?)") as the tertiary key (descending order), and</li>
     * <li>the source of each member as quaternary key sorting those derived
     * from T<sub>method</sub> ahead of those derived from T<sub>locator</sub>.</li>
     * </ol>
     * 
     * @param <R>
     * 
     * @param rrcOrRmls
     *                Collection of Sub-ResourceMethods and SubResourceLocators
     *                or root resource class wrappers.
     * @return the resource method or sub resource locator or root resource
     *         class, or null, if the Map is null or empty.
     */
    public static <R extends RrcOrRml> R getFirstByNoOfLiteralCharsNoOfCapturingGroups(
            Collection<R> rrcOrRmls) {
        if ((rrcOrRmls == null) || rrcOrRmls.isEmpty()) {
            return null;
        }
        final Iterator<R> srmlIter = rrcOrRmls.iterator();
        R bestSrml = srmlIter.next();
        if (rrcOrRmls.size() == 1) {
            return bestSrml;
        }
        int bestSrmlChars = Integer.MIN_VALUE;
        int bestSrmlNoCaptGroups = Integer.MIN_VALUE;
        int bestSrmlNoNonDefCaptGroups = Integer.MIN_VALUE;
        for (final R srml : rrcOrRmls) {
            final PathRegExp srmlRegExp = srml.getPathRegExp();
            final int srmlNoLitChars = srmlRegExp.getNoOfLiteralChars();
            final int srmlNoCaptGroups = srmlRegExp.getNoOfCapturingGroups();
            final int srmlNoNonDefCaptGroups = srmlRegExp
                    .getNoNonDefCaprGroups();
            if (srmlNoLitChars > bestSrmlChars) {
                bestSrml = srml;
                bestSrmlChars = srmlNoLitChars;
                bestSrmlNoCaptGroups = srmlNoCaptGroups;
                bestSrmlNoNonDefCaptGroups = srmlNoNonDefCaptGroups;
                continue;
            }
            if (srmlNoLitChars == bestSrmlChars) {
                if (srmlNoCaptGroups > bestSrmlNoCaptGroups) {
                    bestSrml = srml;
                    bestSrmlChars = srmlNoLitChars;
                    bestSrmlNoCaptGroups = srmlNoCaptGroups;
                    bestSrmlNoNonDefCaptGroups = srmlNoNonDefCaptGroups;
                    continue;
                }
                if (srmlNoCaptGroups == bestSrmlNoCaptGroups) {
                    if (srmlNoNonDefCaptGroups > bestSrmlNoNonDefCaptGroups) {
                        bestSrml = srml;
                        bestSrmlChars = srmlNoLitChars;
                        bestSrmlNoCaptGroups = srmlNoCaptGroups;
                        bestSrmlNoNonDefCaptGroups = srmlNoNonDefCaptGroups;
                        continue;
                    }
                    if (srmlNoCaptGroups == bestSrmlNoCaptGroups) {
                        if ((srml instanceof ResourceMethod)
                                && (bestSrml instanceof SubResourceLocator)) {
                            // prefare methods ahead locators
                            bestSrml = srml;
                            bestSrmlChars = srmlNoLitChars;
                            bestSrmlNoCaptGroups = srmlNoCaptGroups;
                            bestSrmlNoNonDefCaptGroups = srmlNoNonDefCaptGroups;
                            continue;
                        }
                    }
                }
            }
        }
        return bestSrml;
    }

    /**
     * Removes the {@link ResourceMethod}s from the collection, that do not
     * support the given HTTP method.
     * 
     * @param resourceMethods
     *                the collection of {@link ResourceMethod}s.
     * @param httpMethod
     *                the HTTP {@link Method}
     * @param alsoGet
     *                if true, also methods suporting GET are included, also if
     *                another HTTP method is required. It is intended to be used
     *                for HEAD requests.
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