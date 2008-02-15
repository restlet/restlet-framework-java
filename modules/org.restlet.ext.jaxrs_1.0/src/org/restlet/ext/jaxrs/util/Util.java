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

package org.restlet.ext.jaxrs.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Form;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.core.UnmodifiableMultivaluedMap;
import org.restlet.resource.Representation;
import org.restlet.util.DateUtils;
import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * This class contains utility methods.
 * 
 * @author Stephan Koops
 */
public class Util {

    /**
     * Name of the Header Principal in the request attributes.
     * 
     * @see Principal
     */
    public static final String JAVA_SECURITY_HEADER = "java.security.Principal";

    /**
     * This comparator sorts the concrete MediaTypes to the beginning and the
     * unconcrete to the end. The last is '*<!---->/*'
     */
    public static final Comparator<org.restlet.data.MediaType> MEDIA_TYPE_COMP = new Comparator<org.restlet.data.MediaType>() {
        public int compare(org.restlet.data.MediaType mediaType1,
                org.restlet.data.MediaType mediaType2) {
            if (mediaType1 == null)
                return mediaType2 == null ? 0 : 1;
            if (mediaType2 == null)
                return -1;
            if (mediaType1.equals(mediaType2, false))
                return 0;
            int specNess1 = specificness(mediaType1);
            int specNess2 = specificness(mediaType2);
            int rt = specNess1 - specNess2;
            if (rt != 0)
                return rt;
            // LATER optimizing possible here: do not use toString()
            return mediaType1.toString().compareToIgnoreCase(
                    mediaType2.toString());
        }
    };

    /**
     * The name of the header {@link MultivaluedMap}&lt;String, String&gt; in
     * the attribute map.
     * 
     */
    public static final String ORG_RESTLET_EXT_JAXRS_HTTP_HEADERS = "org.restlet.ext.jaxrs.http.headers";

    /**
     * The name of the header {@link Form} in the attribute map.
     * 
     * @see #getHttpHeaders(Request)
     * @see #getHttpHeaders(Response)
     */
    public static final String ORG_RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

    /**
     * appends the given String to the StringBuilder. If convertBraces is true,
     * all "{" and "}" are converted to "%7B" and "%7D"
     * 
     * @param stb
     *                the Appendable to append on
     * @param string
     *                the CharSequence to append
     * @param convertBraces
     *                if true, all braces are converted, if false then not.
     * @throws IOException
     *                 If the Appendable have a problem
     */
    public static void append(Appendable stb, CharSequence string,
            boolean convertBraces) throws IOException {
        if (!convertBraces) {
            stb.append(string);
            return;
        }
        int l = string.length();
        for (int i = 0; i < l; i++) {
            char c = string.charAt(i);
            if (c == '{')
                stb.append("%7B");
            else if (c == '}')
                stb.append("%7D");
            else
                stb.append(c);
        }
    }

    /**
     * appends the array elements to the {@link StringBuilder}, separated by ", ".
     * 
     * @param stb
     *                The {@link StringBuilder} to append the array elements.
     * @param array
     *                The array to append to the {@link StringBuilder}.
     */
    public static void append(StringBuilder stb, Object[] array) {
        if (array == null || array.length == 0)
            return;
        stb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            stb.append(", ");
            stb.append(array[i]);
        }
    }

    /**
     * Checks, if the String is a valid URI scheme
     * 
     * @param scheme
     *                the String to check.
     * @throws IllegalArgumentException
     *                 If the string is not a valid URI scheme.
     */
    public static void checkValidScheme(String scheme)
            throws IllegalArgumentException {
        if (scheme == null)
            throw new IllegalArgumentException("The scheme must not be null");
        int schemeLength = scheme.length();
        if (schemeLength == 0)
            throw new IllegalArgumentException(
                    "The scheme must not be an empty String");
        char c = scheme.charAt(0);
        if (!((c > 64 && c <= 90) || (c > 92 && c <= 118)))
            throw new IllegalArgumentException(
                    "The first character of a scheme must be an alphabetic character");
        for (int i = 1; i < schemeLength; i++) {
            c = scheme.charAt(i);
            if (!((c > 64 && c <= 90) || (c > 92 && c <= 118) || (c == '+')
                    || (c == '-') || (c == '.'))) {
                String message = "The "
                        + i
                        + ". character of a scheme must be an alphabetic character, a number, a '+', a '-' or a '.'";
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * Copies headers into a response.
     * 
     * @param jaxRsHeaders
     *                Headers of an JAX-RS-Response.
     * @param restletResponse
     *                Restlet Response to copy the headers in.
     * @param logger
     *                The logger to use
     * @see javax.ws.rs.core.Response#getMetadata()
     */
    public static void copyResponseHeaders(
            final MultivaluedMap<String, Object> jaxRsHeaders,
            Response restletResponse, Logger logger) {
        Collection<Parameter> headers = new ArrayList<Parameter>();
        for (Map.Entry<String, List<Object>> m : jaxRsHeaders.entrySet()) {
            String headerName = m.getKey();
            for (Object headerValue : m.getValue()) {
                String hValue;
                if (headerValue == null)
                    hValue = null;
                else if (headerValue instanceof Date)
                    hValue = formatDate((Date) headerValue, false);
                // TODO temporarily constant not as cookie;
                else
                    hValue = headerValue.toString();
                headers.add(new Parameter(headerName, hValue));
            }
        }
        if (restletResponse.getEntity() == null) {
            restletResponse.setEntity(Representation.createEmpty());
        }
        Engine.getInstance().copyResponseHeaders(headers, restletResponse,
                logger);
    }

    /**
     * Copies the headers of the given {@link Response} into the given
     * {@link Series}.
     * 
     * @param restletResponse
     *                The response to update. Should contain a
     *                {@link Representation} to copy the representation headers
     *                from it.
     * @param logger
     *                The logger to use.
     * @return The copied headers.
     */
    public static Series<Parameter> copyResponseHeaders(
            Response restletResponse, Logger logger) {
        Series<Parameter> headers = new Form();
        Engine engine = Engine.getInstance();
        if (true) // TODO waiting for engine fix: copyResponseHeaders.
            throw new UnsupportedOperationException("Waiting for enfine fix");
        // engine.copyResponseHeaders(restletResponse, headers, logger);
        return headers;
    }

    /**
     * Copies the non-null components of the supplied URI to the Reference
     * replacing any existing values for those components.
     * 
     * @param uri
     *                the URI to copy components from.
     * @param reference
     *                The Reference to copy the URI data in.
     * @throws IllegalArgumentException
     *                 if uri is null
     * @see javax.ws.rs.core.UriBuilder#uri(URI)
     */
    public static void copyUriToReference(URI uri, Reference reference)
            throws IllegalArgumentException {
        if (uri == null)
            throw new IllegalArgumentException("The URI must not be null");
        if (uri.getScheme() != null)
            reference.setScheme(uri.getScheme());
        if (uri.getAuthority() != null)
            reference.setAuthority(uri.getAuthority());
        if (uri.getHost() != null)
            reference.setHostDomain(uri.getHost());
        if (uri.getUserInfo() != null)
            reference.setUserInfo(uri.getUserInfo());
        if (uri.getPort() >= 0)
            reference.setHostPort(uri.getPort());
        if (uri.getPath() != null)
            reference.setPath(uri.getPath());
        if (uri.getQuery() != null)
            reference.setQuery(uri.getQuery());
        if (uri.getFragment() != null)
            reference.setFragment(uri.getFragment());
    }

    /**
     * Creates an modifiable Collection with the given Objects in it, and no
     * other objects. nulls will be ignored.
     * 
     * @param objects
     * @param <A>
     * @return Returns the created list with the given objects in it.
     */
    public static <A> Collection<A> createColl(A... objects) {
        return createList(objects);
    }

    /**
     * Creates an modifiable List with the given Object in it, and no other
     * objects. If the given object is null, than an empty List will returned
     * 
     * @param objects
     * @param <A>
     * @return Returns the created list with the given object in it or an empty
     *         list, if the given object is null.
     */
    public static <A> List<A> createList(A... objects) {
        List<A> list = new ArrayList<A>();
        int l = objects.length;
        for (int i = 0; i < l; i++) {
            A o = objects[i];
            if (o != null)
                list.add(o);
        }
        return list;
    }

    /**
     * Creates a map with the given keys and values.
     * 
     * @param keysAndValues
     *                first element is key1, second element value1, third
     *                element key2, forth element value2 and so on.
     * @return
     */
    public static Map<String, String> createMap(String... keysAndValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keysAndValues.length; i += 2)
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        return map;
    }

    /**
     * Creates a JAX-RS-MediaType.
     * 
     * @param type
     *                main type of the MediaType
     * @param subtype
     *                subtype of the MediaType
     * @param keysAndValues
     *                parameters (optional)
     * @return the created MediaType
     */
    public static MediaType createMediaType(String type, String subtype,
            String... keysAndValues) {
        return new MediaType(type, subtype, Util.createMap(keysAndValues));
    }

    /**
     * Creates an modifiable Set with the given Object in it, and no other
     * objects. If the given object is null, than an empty Set will returned.
     * 
     * @param <A>
     * @param objects
     * @return the created Set
     */
    public static <A> Set<A> createSet(A... objects) {
        Set<A> set = new HashSet<A>();
        int l = objects.length;
        for (int i = 0; i < l; i++) {
            A o = objects[i];
            if (o != null)
                set.add(o);
        }
        return set;
    }

    /**
     * @param <A>
     * @param collection
     * @param comparator
     * @return
     */
    public static <A> Collection<A> createTreeSet(Collection<A> collection,
            Comparator<A> comparator) {
        Collection<A> coll2 = new TreeSet<A>(comparator);
        coll2.addAll(collection);
        return coll2;
    }

    /**
     * Ensures that the path starts wirh a string. if not, a slash will be added
     * at the beginning.
     * 
     * @param path
     * @return
     */
    public static String ensureStartSlash(String path) {
        if (path.startsWith("/"))
            return path;
        return "/" + path;
    }

    /**
     * Check if the given objects are equal. Can deal with null references. if
     * both elements are null, than the result is true.
     * 
     * @param object1
     * @param object2
     * @return
     */
    public static boolean equals(Object object1, Object object2) {
        if (object1 == null)
            return object2 == null;
        return object1.equals(object2);
    }

    /**
     * Converte the given Date into a String. Copied from
     * {@link com.noelios.restlet.HttpCall}.
     * 
     * @param date
     *                Date to format
     * @param cookie
     *                if true, using RFC 1036 format, otherwise RFC 1123 format.
     * @return
     */
    public static String formatDate(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.get(0));
        } else {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1123.get(0));
        }
    }

    /**
     * @param coll
     * @param <A>
     * @return Returns the first Element of the collection
     * @throws IndexOutOfBoundsException
     *                 If the list is empty
     */
    public static <A> A getFirstElement(Collection<A> coll)
            throws IndexOutOfBoundsException {
        if (coll.isEmpty())
            throw new IndexOutOfBoundsException(
                    "The Collection is empty; you can't get the first element of it.");
        if (coll instanceof LinkedList)
            return ((LinkedList<A>) coll).getFirst();
        if (coll instanceof List)
            return ((List<A>) coll).get(0);
        return coll.iterator().next();
    }

    /**
     * @param list
     * @param <A>
     * @return Returns the first Element of the collection
     * @throws IndexOutOfBoundsException
     *                 If the list is empty
     */
    public static <A> A getFirstElement(List<A> list)
            throws IndexOutOfBoundsException {
        if (list.isEmpty())
            throw new IndexOutOfBoundsException(
                    "The Collection is empty; you can't get the first element of it.");
        if (list instanceof LinkedList)
            return ((LinkedList<A>) list).getFirst();
        return list.get(0);
    }

    /**
     * @param map
     * @param <K>
     * @param <V>
     * @return Returns the first element, returned by the iterator over the
     *         map.entrySet()
     * 
     * @throws NoSuchElementException
     *                 If the map is empty.
     */
    public static <K, V> Map.Entry<K, V> getFirstEntry(Map<K, V> map)
            throws NoSuchElementException {
        return map.entrySet().iterator().next();
    }

    /**
     * @return Returns the first element, returned by the iterator over the
     *         map.keySet()
     * 
     * @param map
     * @param <K>
     * @param <V>
     * @throws NoSuchElementException
     *                 If the map is empty.
     */
    public static <K, V> K getFirstKey(Map<K, V> map)
            throws NoSuchElementException {
        return map.keySet().iterator().next();
    }

    /**
     * @return Returns the first element, returned by the iterator over the
     *         map.values()
     * @param map
     * @param <K>
     * @param <V>
     * @throws NoSuchElementException
     *                 If the map is empty.
     */
    public static <K, V> V getFirstValue(Map<K, V> map)
            throws NoSuchElementException {
        return map.values().iterator().next();
    }

    /**
     * @param request
     * @return Returns the HTTP-Headers-Form from the Request.
     */
    public static Form getHttpHeaders(Request request) {
        Form headers = (Form) request.getAttributes().get(
                ORG_RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Form();
            request.getAttributes().put(ORG_RESTLET_HTTP_HEADERS, headers);
        }
        return headers;
    }

    /**
     * @param response
     *                a Restlet response
     * @return Returns the HTTP-Headers-Form from the Response.
     */
    public static Form getHttpHeaders(Response response) {
        Form headers = (Form) response.getAttributes().get(
                ORG_RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Form();
            response.getAttributes().put(ORG_RESTLET_HTTP_HEADERS, headers);
        }
        return headers;
    }

    /**
     * Returns the request headers as {@link MultivaluedMap}.
     * 
     * @param request
     * @return
     */
    public static MultivaluedMap<String, String> getJaxRsHttpHeaders(
            Request request) {
        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> headers = (MultivaluedMap) request
                .getAttributes().get(ORG_RESTLET_EXT_JAXRS_HTTP_HEADERS);
        if (headers == null) {
            headers = UnmodifiableMultivaluedMap.getFromForm(
                    getHttpHeaders(request), false);
            request.getAttributes().put(ORG_RESTLET_EXT_JAXRS_HTTP_HEADERS,
                    headers);
        }
        return headers;
    }

    /**
     * @param list
     * @param <A>
     * @return Returns the last Element of the list
     * @throws IndexOutOfBoundsException
     *                 If the list is empty
     */
    public static <A> A getLastElement(List<A> list)
            throws IndexOutOfBoundsException {
        if (list instanceof LinkedList)
            return ((LinkedList<A>) list).getLast();
        return list.get(list.size() - 1);
    }

    /**
     * Returns all public {@link Method}s of the class with the given name
     * (case-sensitive)
     * 
     * @param clazz
     *                The {@link Class} to search the {@link Method}s.
     * @param methodName
     *                The name of the {@link Method} to search.
     * @return Returns a {@link Collection} all of {@link Method}s with the
     *         given name. Never returns null. If no methods are found an empty
     *         Collection will be returned. The method {@link Iterator#remove()}
     *         of this collection is supported.
     * @throws IllegalArgumentException
     *                 if the clazz or the method name is null.
     */
    public static Collection<Method> getMethodsByName(Class<?> clazz,
            String methodName) throws IllegalArgumentException {
        if (clazz == null)
            throw new IllegalArgumentException("The class must not be null");
        if (methodName == null)
            throw new IllegalArgumentException(
                    "The method name must not be null");
        Collection<Method> methods = new ArrayList<Method>(2);
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName))
                methods.add(method);
        }
        return methods;
    }

    /**
     * Returns the only element of the list, or null, if the List is null or
     * empty.
     * 
     * @param <A>
     * @param list
     *                a List with at most one element
     * @return The element of the List, or null, if there is no element.
     * @throws IllegalArgumentException
     *                 if the list contains more than one element.
     */
    public static <A> A getOnlyElement(Collection<A> list)
            throws IllegalArgumentException {
        if (list == null)
            return null;
        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new IllegalArgumentException(
                    "The list must have exactly one element");
        if (list instanceof List)
            return ((List<A>) list).get(0);
        return list.iterator().next();
    }

    /**
     * Returns the Name of the only element of the list of the given Metadata.
     * Returns null, if the list is empty or null.
     * 
     * @param metadatas
     * @return the name of the Metadata
     * @see #getOnlyElement(List)
     */
    public static String getOnlyMetadataName(List<? extends Metadata> metadatas) {
        Metadata metadata = getOnlyElement(metadatas);
        if (metadata == null)
            return null;
        return metadata.getName();
    }

    /**
     * Gets the logged in user.
     * 
     * @param request
     *                The Restlet {@link Request}
     * @return The {@link Principal} of the logged in user.
     * @see #setPrincipal(Principal, Request)
     */
    public static Principal getPrincipal(Request request) {
        return (Principal) request.getAttributes().get(JAVA_SECURITY_HEADER);
    }

    /**
     * This method throws an {@link WebApplicationException} for Exceptions
     * where is no planned handling. Logs the exception (warn {@link Level}).
     * 
     * @param e
     *                the catched Exception
     * @param logger
     *                the logger to log the messade
     * @param logMessage
     *                the message to log.
     * @return Will never return anyithing, because the generated exceptions
     *         will be thrown. You an formally thro the returned exception (e.g.
     *         in a catch block). So the compiler is sure, that the method will
     *         be left here.
     */
    public static RuntimeException handleException(Exception e, Logger logger,
            String logMessage) {
        logger.log(Level.WARNING, logMessage, e);
        throw new WebApplicationException(e, Status.SERVER_ERROR_INTERNAL
                .getCode());
    }

    /**
     * Checks if the {@link org.restlet.data.MediaType}s are compatible.
     * 
     * @param mediaType1
     * @param mediaType2
     * @return
     */
    public static boolean isCompatible(org.restlet.data.MediaType mediaType1,
            org.restlet.data.MediaType mediaType2) {
        // TODO Jerome: move to Restlet API?
        return mediaType1.includes(mediaType2)
                || mediaType2.includes(mediaType1);
    }

    /**
     * Checks, if the given MediaType is concrete
     * 
     * @param mediaType
     * @return
     * @see #specificness(org.restlet.data.MediaType)
     */
    public static boolean isConcrete(org.restlet.data.MediaType mediaType) {
        // TODO Jerome: move to Restlet API?
        if (mediaType.getSubType().equals("*"))
            return false;
        if (mediaType.getMainType().equals("*"))
            return false;
        return true;
    }

    /**
     * Checks, if the list is empty.
     * 
     * @param list
     * @return true, if the list is empty or null, or false, if the list
     *         contains elements.
     * @see #isEmpty(Object[])
     */
    public static boolean isEmpty(List<?> list) {
        return (list == null || list.isEmpty());
    }

    /**
     * Tests, if the given array is empty. Will not throw a
     * NullPointerException.
     * 
     * @param array
     * @return Returns true, if the given array ist null or has zero elements,
     *         otherwise false.
     * @see #isEmpty(List)
     */
    public static boolean isEmpty(Object[] array) {
        if (array == null || array.length == 0)
            return true;
        return false;
    }

    /**
     * Tests, if the given String is empty or "/". Will not throw a
     * NullPointerException.
     * 
     * @param string
     * @return Returns true, if the given string ist null, empty or equals "/"
     */
    public static boolean isEmptyOrSlash(String string) {
        return string == null || string.length() == 0 || string.equals("/");
    }

    /**
     * Checks, if the list contains elements.
     * 
     * @param list
     * @return true, if the list contains elements, or false, if the list is
     *         empty or null.
     */
    public static boolean isNotEmpty(List<?> list) {
        return (list != null && !list.isEmpty());
    }

    /**
     * Returns one of the most specific {@link MediaType}s of the given
     * MediaTypes. See JSR-311 specification, section 2.6. 'Determining the
     * MediaType of Responses', part 5.
     * 
     * @param mediaTypes
     *                an array of {@link org.restlet.data.MediaType}s.
     * @return the most concrete {@link MediaType}.
     * @throws IllegalArgumentException
     *                 if the array is null or empty.
     */
    public static org.restlet.data.MediaType mostSpecific(
            org.restlet.data.MediaType... mediaTypes)
            throws IllegalArgumentException {
        // TODO Jerome: move to Restlet-API? static MediaType.mostSpecific(...)
        if (mediaTypes == null || mediaTypes.length == 0)
            throw new IllegalArgumentException(
                    "You must give at least one MediaType");
        if (mediaTypes.length == 1)
            return mediaTypes[0];
        org.restlet.data.MediaType mostSpecific = mediaTypes[0];
        for (int i = 1; i < mediaTypes.length; i++) {
            org.restlet.data.MediaType mediaType = mediaTypes[i];
            if (mediaType.getMainType().equals("*"))
                continue;
            if (mostSpecific.getMainType().equals("*")) {
                mostSpecific = mediaType;
                continue;
            }
            if (mostSpecific.getSubType().equals("*")) {
                mostSpecific = mediaType;
                continue;
            }
        }
        return mostSpecific;
    }

    /**
     * Sets the logged in user.
     * 
     * @param principal
     *                The Principal of the logged in user.
     * @param request
     *                The Restlet request
     * @see #getPrincipal(Request)
     */
    public static void setPrincipal(Principal principal, Request request) {
        request.getAttributes().put(JAVA_SECURITY_HEADER, principal);
    }

    /**
     * Returns a new {@link List}, which contains all
     * {@link org.restlet.data.MediaType}s of the given List, sorted by it's
     * concreteness, the concrete {@link org.restlet.data.MediaType} at the
     * beginning.
     * 
     * @param mediaTypes
     * @return
     * @see Util#specificness(org.restlet.data.MediaType)
     */
    public static List<org.restlet.data.MediaType> sortByConcreteness(
            Collection<org.restlet.data.MediaType> mediaTypes) {
        List<org.restlet.data.MediaType> newList = new ArrayList<org.restlet.data.MediaType>(
                mediaTypes.size());
        for (org.restlet.data.MediaType mediaType : mediaTypes)
            if (specificness(mediaType) > 0)
                newList.add(mediaType);
        for (org.restlet.data.MediaType mediaType : mediaTypes)
            if (specificness(mediaType) == 0)
                newList.add(mediaType);
        for (org.restlet.data.MediaType mediaType : mediaTypes)
            if (specificness(mediaType) < 0)
                newList.add(mediaType);
        return newList;
    }

    /**
     * Returns the specificness of the given {@link org.restlet.data.MediaType}:
     * <ul>
     * <li>1 for any concrete type (contains no star)</li>
     * <li>0 for the types (anything/*)</li>
     * <li>-1 for '*<!---->/*</li>
     * </ul>
     * 
     * @param mediaType
     * @return 1, 0 or -1
     * @see #isConcrete(org.restlet.data.MediaType)
     * @see #sortByConcreteness(Collection)
     */
    public static int specificness(org.restlet.data.MediaType mediaType) {
        if (mediaType.equals(org.restlet.data.MediaType.ALL, true))
            return -1;
        if (mediaType.getSubType().equals("*"))
            return 0;
        return 1;
    }

    /**
     * Returns the given object as String. If null was given, null is returned.
     * 
     * @param object
     *                the object to convert to String.
     * @return the object as String, or null, if null was given
     * @see Object#toString()
     */
    public static String toString(Object object) {
        if (object == null)
            return null;
        return object.toString();
    }
}
