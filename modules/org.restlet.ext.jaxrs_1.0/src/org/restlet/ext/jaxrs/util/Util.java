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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Variant;

import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.StringRepresentation;
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
     * Paramater in the JAX-RS MediaType for the charset. See MIME
     * specification.
     */
    private static final String CHARSET_PARAM = "charset";

    /**
     * The header in the attribute map to return the HTTP headers.
     * 
     * @see #getHttpHeaders(Request)
     * @see #getHttpHeaders(Response)
     */
    public static final String ORG_RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

    /**
     * Converts a JAX-RS Cookie to a Restlet Cookie
     * 
     * @param jaxRsCookie
     *                the JAX-RS Cookie
     * @return the Restlet Cookie
     */
    public static org.restlet.data.Cookie convertCookie(Cookie jaxRsCookie) {
        if (jaxRsCookie == null)
            return null;
        return new org.restlet.data.Cookie(jaxRsCookie.getVersion(),
                jaxRsCookie.getName(), jaxRsCookie.getValue(), jaxRsCookie
                        .getPath(), jaxRsCookie.getDomain());
    }

    /**
     * Converts a Restlet Cookie to a JAX-RS Cookie
     * 
     * @param restletCookie
     *                the Restlet Cookie
     * @return the JAX-RS Cookie
     */
    public static Cookie convertCookie(org.restlet.data.Cookie restletCookie) {
        if (restletCookie == null)
            return null;
        return new Cookie(restletCookie.getName(), restletCookie.getValue(),
                restletCookie.getPath(), restletCookie.getDomain(),
                restletCookie.getVersion());
    }

    /**
     * Converts a JAX-RS-EntityTag to a Restlet-EntityTag
     * 
     * @param jaxRsEntityTag
     *                the JAX-RS-EntityTag to convert.
     * @return The corresponding Restlet-Entity-Tag
     */
    public static Tag convertEntityTag(EntityTag jaxRsEntityTag) {
        if (jaxRsEntityTag == null)
            return null;
        return new Tag(jaxRsEntityTag.getValue(), jaxRsEntityTag.isWeak());
    }

    /**
     * Converts a Restlet-EntityTag to a JAX-RS-EntityTag
     * 
     * @param restletEntityTag
     *                the Restlet-EntityTag to convert.
     * @return The corresponding JAX-RS-Entity-Tag
     */
    public static EntityTag convertEntityTag(Tag restletEntityTag) {
        if (restletEntityTag == null)
            return null;
        return new EntityTag(restletEntityTag.getName(), restletEntityTag
                .isWeak());
    }

    /**
     * Convert a JAX-RS MediaType to a Restlet MediaType.
     * 
     * @param jaxRsMediaType
     * @return the converted MediaType
     */
    public static org.restlet.data.MediaType convertMediaType(
            MediaType jaxRsMediaType) {
        if (jaxRsMediaType == null)
            return null;
        Series<Parameter> parameters = convertToSeries(jaxRsMediaType
                .getParameters());
        String name = jaxRsMediaType.getType() + "/"
                + jaxRsMediaType.getSubtype();
        return new org.restlet.data.MediaType(name, parameters);
    }

    /**
     * Convert a Restlet MediaType to a JAX-RS MediaType.
     * 
     * @param restletMediaType
     *                the MediaType to convert.
     * @param restletCharacterSet
     *                the CharacterSet for the MediaType; may be null.
     * @return the converted MediaType
     */
    public static MediaType convertMediaType(
            org.restlet.data.MediaType restletMediaType,
            org.restlet.data.CharacterSet restletCharacterSet) {
        if (restletMediaType == null)
            return null;
        Map<String, String> parameters = convertSeries(restletMediaType
                .getParameters());
        if (restletCharacterSet != null)
            parameters.put(CHARSET_PARAM, restletCharacterSet.getName());
        return new MediaType(restletMediaType.getMainType(), restletMediaType
                .getSubType(), parameters);
    }

    /**
     * @param parameters
     * @return
     */
    public static Map<String, String> convertSeries(Series<Parameter> parameters) {
        if (parameters == null)
            return null;
        Map<String, String> map = new HashMap<String, String>();
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }

    /**
     * Converts the Restlet JAX-RS NewCookie to a CookieSettings.
     * 
     * @param newCookie
     * @return
     * @throws IllegalArgumentException
     */
    public static CookieSetting convertToCookieSetting(NewCookie newCookie)
            throws IllegalArgumentException {
        if (newCookie == null)
            return null;
        return new CookieSetting(newCookie.getVersion(), newCookie.getName(),
                newCookie.getValue(), newCookie.getPath(), newCookie
                        .getDomain(), newCookie.getComment(), newCookie
                        .getMaxAge(), newCookie.isSecure());
    }

    /**
     * Converts the Restlet CookieSettings to a JAX-RS NewCookie.
     * 
     * @param cookieSetting
     * @return
     * @throws IllegalArgumentException
     */
    public static NewCookie convertToNewCookie(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        if (cookieSetting == null)
            return null;
        return new NewCookie(cookieSetting.getName(), cookieSetting.getValue(),
                cookieSetting.getPath(), cookieSetting.getDomain(),
                cookieSetting.getVersion(), cookieSetting.getComment(),
                cookieSetting.getMaxAge(), cookieSetting.isSecure());
    }

    /**
     * @param parameters
     * @return a form with the given parameters. Will never return null.
     */
    public static Form convertToSeries(Map<String, String> parameters) {
        Form form = new Form();
        if (parameters == null)
            return form;
        for (Map.Entry<String, String> parameter : parameters.entrySet())
            form.add(parameter.getKey(), parameter.getValue());
        return form;
    }

    /**
     * Converts the given Restlet Variant to a JAX-RS Variant TODO javadoc
     * 
     * @param restletVariant
     * @return
     * @throws IllegalArgumentException
     *                 If the given Variant does not contain exactly one
     *                 language and one
     */
    public static Variant convertVariant(
            org.restlet.resource.Variant restletVariant)
            throws IllegalArgumentException {
        MediaType mediaType = convertMediaType(restletVariant.getMediaType(),
                restletVariant.getCharacterSet());
        String language = Util.getOnlyMetadataName(restletVariant.getLanguages());
        String encoding = Util.getOnlyMetadataName(restletVariant.getEncodings());
        return new Variant(mediaType, language, encoding);
    }

    /**
     * Converts the given JAX-RS Variants to Restlet Variants.
     * 
     * @param jaxRsVariants
     * @return
     */
    public static List<org.restlet.resource.Variant> convertVariants(
            List<Variant> jaxRsVariants) {
        List<org.restlet.resource.Variant> restletVariants = new ArrayList<org.restlet.resource.Variant>();
        for (Variant jaxRsVariant : jaxRsVariants) {
            org.restlet.resource.Variant restletVariant = new org.restlet.resource.Variant();
            restletVariant.setCharacterSet(getCharacterSet(jaxRsVariant
                    .getMediaType()));
            restletVariant.setEncodings(Util.createList(Encoding
                    .valueOf(jaxRsVariant.getEncoding())));
            restletVariant.setLanguages(Util.createList(Language
                    .valueOf(jaxRsVariant.getLanguage())));
            restletVariant.setMediaType(convertMediaType(jaxRsVariant
                    .getMediaType()));
            restletVariants.add(restletVariant);
        }
        return restletVariants;
    }

    /**
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
            // TODO Jerome: wie bekommt man am elegantesten ne leere
            // Repräsentation?
            restletResponse.setEntity(new StringRepresentation(""));
        }
        Engine.getInstance().copyResponseHeaders(headers, restletResponse,
                logger);
    }

    /**
     * Creates an modifiable Collection with the given Objects in it, and no
     * other objects. nulls will be ignored.
     * 
     * @param object1
     * @param object2
     * @param <A>
     * @return Returns the created list with the given objects in it.
     */
    public static <A> Collection<A> createColl(A object1, A object2) {
        Collection<A> coll = new ArrayList<A>();
        if (object1 != null)
            coll.add(object1);
        if (object2 != null)
            coll.add(object2);
        return coll;
    }

    /**
     * Creates an modifiable List with the given Object in it, and no other
     * objects. If the given object is null, than an empty List will returned
     * 
     * @param object
     * @param <A>
     * @return Returns the created list with the given object in it or an empty
     *         list, if the given object is null.
     */
    public static <A> List<A> createList(A object) {
        List<A> list = new ArrayList<A>();
        if (object != null)
            list.add(object);
        return list;
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

    private static CharacterSet getCharacterSet(MediaType mediaType) {
        String charset = mediaType.getParameters().get(CHARSET_PARAM);
        return CharacterSet.valueOf(charset);
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
        if (coll instanceof List)
            return ((List<A>) coll).get(0);
        return coll.iterator().next();
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
        return (Form) request.getAttributes().get(ORG_RESTLET_HTTP_HEADERS);
    }

    /**
     * @param response
     *                a Restlet response
     * @return Returns the HTTP-Headers-Form from the Response.
     */
    public static Form getHttpHeaders(Response response) {
        return (Form) response.getAttributes().get(ORG_RESTLET_HTTP_HEADERS);
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
    public static <A> A getOnlyElement(List<A> list)
            throws IllegalArgumentException {
        if (list == null)
            return null;
        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new IllegalArgumentException(
                    "The list must have exactly one element");
        return list.get(0);
    }
    
    /**
     * Returns the Name of the only element of the list of the given Metadata.
     * Returns null, if the list is empty or null.
     * @param metadatas
     * @return the name of the Metadata
     * @see #getOnlyElement(List)
     */
    public static String getOnlyMetadataName(List<? extends Metadata> metadatas)
    {
        Metadata metadata = getOnlyElement(metadatas);
        if(metadata == null)
            return null;
        return metadata.getName();
    }

    /**
     * This method throws an WebApplicationException for Exceptions where is no
     * planned handling.
     * 
     * @param e
     * @return Will never return anyithing, because the generated exceptions
     *         will be thrown. You an formally thro the returned exception (e.g.
     *         in a catch block). So the compiler is sure, that the method will
     *         be left here.
     */
    public static RuntimeException handleException(Exception e) {
        // TODO irgendwie irgendwas loggen
        throw new WebApplicationException(e, Status.SERVER_ERROR_INTERNAL
                .getCode());
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
     * Sorts the Metadata by it's quality into the Collections. The list is
     * ordered by the qualities, most wanted Metadata at first.
     * 
     * @param preferences
     * 
     * @return Returns a List of collections of Metadata
     */
    public static List<Collection<? extends Metadata>> sortMetadataList(
            Collection<Preference<Metadata>> preferences) {
        SortedMap<Float, Collection<Metadata>> map = new TreeMap<Float, Collection<Metadata>>(
                Collections.reverseOrder());
        for (Preference<Metadata> preference : preferences) {
            Float quality = preference.getQuality();
            Collection<Metadata> metadatas = map.get(quality);
            if (metadatas == null) {
                metadatas = new ArrayList<Metadata>(2);
                map.put(quality, metadatas);
            }
            metadatas.add(preference.getMetadata());
        }
        return new ArrayList<Collection<? extends Metadata>>(map.values());
    }

    /**
     * @see Arrays#toList(Object[])
     * @param <E>
     * @param elements
     * @return
     */
    public static <E> List<E> toList(E[] elements) {
        List<E> list = new ArrayList<E>(elements.length);
        for (E element : elements)
            list.add(element);
        return list;
    }
}