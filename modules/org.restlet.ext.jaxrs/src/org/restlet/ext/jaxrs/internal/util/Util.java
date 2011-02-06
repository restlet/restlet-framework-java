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

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.restlet.data.CharacterSet.UTF_8;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.ContentType;
import org.restlet.engine.http.header.DimensionWriter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.jaxrs.internal.core.UnmodifiableMultivaluedMap;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.JaxRsRuntimeException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * This class contains utility methods.
 * 
 * @author Stephan Koops
 */
public class Util {

    /**
     * The default character set to be used, if no character set is given.
     */
    public static final CharacterSet JAX_RS_DEFAULT_CHARACTER_SET = UTF_8;

    /**
     * The default character set to be used, if no character set is given, as
     * String
     * 
     * @see #JAX_RS_DEFAULT_CHARACTER_SET
     */
    public static final String JAX_RS_DEFAULT_CHARACTER_SET_AS_STRING = JAX_RS_DEFAULT_CHARACTER_SET
            .toString();

    /**
     * This comparator sorts the concrete MediaTypes to the beginning and the
     * unconcrete to the end. The last is '*<!---->/*'
     */
    public static final Comparator<org.restlet.data.MediaType> MEDIA_TYPE_COMP = new Comparator<org.restlet.data.MediaType>() {
        public int compare(org.restlet.data.MediaType mediaType1,
                org.restlet.data.MediaType mediaType2) {
            if (mediaType1 == null) {
                return mediaType2 == null ? 0 : 1;
            }
            if (mediaType2 == null) {
                return -1;
            }
            if (mediaType1.equals(mediaType2, false)) {
                return 0;
            }
            final int specNess1 = specificness(mediaType1);
            final int specNess2 = specificness(mediaType2);
            final int rt = specNess1 - specNess2;
            if (rt != 0) {
                return rt;
            }
            return mediaType1.toString().compareToIgnoreCase(
                    mediaType2.toString());
        }
    };

    private static final byte NAME_READ = 2;

    private static final byte NAME_READ_READY = 3;

    private static final byte NAME_READ_START = 1;

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
     *            the Appendable to append on
     * @param string
     *            the CharSequence to append
     * @param convertBraces
     *            if true, all braces are converted, if false then not.
     * @throws IOException
     *             If the Appendable have a problem
     */
    public static void append(Appendable stb, CharSequence string,
            boolean convertBraces) throws IOException {
        append(stb, string, convertBraces, 0, string.length());
    }

    /**
     * appends the given String to the StringBuilder. If convertBraces is true,
     * all "{" and "}" are converted to "%7B" and "%7D"
     * 
     * @param stb
     *            the Appendable to append on
     * @param string
     *            the CharSequence to append
     * @param convertBraces
     *            if true, all braces are converted, if false then not.
     * @param startIndex
     * @throws IOException
     *             If the Appendable have a problem
     */
    public static void append(Appendable stb, CharSequence string,
            boolean convertBraces, int startIndex) throws IOException {
        append(stb, string, convertBraces, startIndex, string.length());
    }

    /**
     * appends the given String to the StringBuilder. If convertBraces is true,
     * all "{" and "}" are converted to "%7B" and "%7D"
     * 
     * @param stb
     *            the Appendable to append on
     * @param string
     *            the CharSequence to append
     * @param convertBraces
     *            if true, all braces are converted, if false then not.
     * @param startIndex
     * @param endIndex
     * @throws IOException
     *             If the Appendable have a problem
     */
    public static void append(Appendable stb, CharSequence string,
            boolean convertBraces, int startIndex, int endIndex)
            throws IOException {
        if (!convertBraces) {
            stb.append(string, startIndex, endIndex);
            return;
        }
        for (int i = startIndex; i < endIndex; i++) {
            final char c = string.charAt(i);
            if (c == '{') {
                stb.append("%7B");
            } else if (c == '}') {
                stb.append("%7D");
            } else {
                stb.append(c);
            }
        }
    }

    /**
     * appends the array elements to the {@link StringBuilder}, separated by
     * ", ".
     * 
     * @param stb
     *            The {@link StringBuilder} to append the array elements.
     * @param array
     *            The array to append to the {@link StringBuilder}.
     */
    public static void append(StringBuilder stb, Object[] array) {
        if ((array == null) || (array.length == 0)) {
            return;
        }
        stb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            stb.append(", ");
            stb.append(array[i]);
        }
    }

    /**
     * Checks, if the class is concrete.
     * 
     * @param jaxRsClass
     *            JAX-RS root resource class or JAX-RS provider.
     * @param typeName
     *            for the exception message "root resource class" or "provider"
     * @throws IllegalArgumentException
     *             if the class is not concrete.
     */
    public static void checkClassConcrete(Class<?> jaxRsClass, String typeName)
            throws IllegalArgumentException {
        final int modifiers = jaxRsClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("The " + typeName + " "
                    + jaxRsClass.getName() + " is not concrete");
        }
    }

    /**
     * Copies headers into a response.
     * 
     * @param jaxRsHeaders
     *            Headers of an JAX-RS-Response.
     * @param restletResponse
     *            Restlet Response to copy the headers in.
     * @see javax.ws.rs.core.Response#getMetadata()
     */
    public static void copyResponseHeaders(
            final MultivaluedMap<String, Object> jaxRsHeaders,
            Response restletResponse) {
        Series<Parameter> headers = new Form();

        for (Map.Entry<String, List<Object>> m : jaxRsHeaders.entrySet()) {
            String headerName = m.getKey();

            for (Object headerValue : m.getValue()) {
                String hValue;

                if (headerValue == null) {
                    hValue = null;
                } else if (headerValue instanceof Date) {
                    hValue = formatDate((Date) headerValue, false);
                } else {
                    hValue = headerValue.toString();
                }

                headers.add(new Parameter(headerName, hValue));
            }
        }

        if (restletResponse.getEntity() == null) {
            restletResponse.setEntity(new EmptyRepresentation());
        }

        HeaderUtils.copyResponseTransportHeaders(headers, restletResponse);
        HeaderUtils.extractEntityHeaders(headers, restletResponse
                .getEntity());

        // Copy extension headers
        @SuppressWarnings("unchecked")
        Series<Parameter> extensionHeaders = (Series<Parameter>) jaxRsHeaders
                .getFirst(HeaderConstants.ATTRIBUTE_HEADERS);

        if (extensionHeaders != null) {
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put(HeaderConstants.ATTRIBUTE_HEADERS, extensionHeaders);
            restletResponse.setAttributes(attributes);
        }
    }

    /**
     * Copies the headers of the given {@link Response} into the given
     * {@link Series}.
     * 
     * @param restletResponse
     *            The response to update. Should contain a
     *            {@link Representation} to copy the representation headers from
     *            it.
     * @return The copied headers.
     */
    public static Series<Parameter> copyResponseHeaders(Response restletResponse) {
        final Series<Parameter> headers = new Form();
        HeaderUtils.addResponseHeaders(restletResponse, headers);
        HeaderUtils.addEntityHeaders(restletResponse.getEntity(), headers);
        return headers;
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
        final List<A> list = new ArrayList<A>();
        final int l = objects.length;
        for (int i = 0; i < l; i++) {
            final A o = objects[i];
            if (o != null) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Creates a map with the given keys and values.
     * 
     * @param keysAndValues
     *            first element is key1, second element value1, third element
     *            key2, forth element value2 and so on.
     * @return the created Map
     */
    public static Map<String, String> createMap(String... keysAndValues) {
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
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
        final Set<A> set = new HashSet<A>();
        final int l = objects.length;
        for (int i = 0; i < l; i++) {
            final A o = objects[i];
            if (o != null) {
                set.add(o);
            }
        }
        return set;
    }

    /**
     * Checks, if the given class implements the given interface.
     * 
     * @param clazz
     * @param interfaze
     * @return true, if the class implements the given interface, otherwise
     *         false. if the clazz or interfaze is null, false is returned
     */
    public static boolean doesImplement(Class<?> clazz, Class<?> interfaze) {
        if (clazz == null || interfaze == null)
            return false;
        if (doesImplement(clazz.getSuperclass(), interfaze))
            return true;
        for (Class<?> interf : clazz.getInterfaces()) {
            if (interf.equals(interfaze))
                return true;
            if (doesImplement(interf, interfaze))
                return true;
        }
        return false;
    }

    /**
     * Checks, if the given CharSequence ends with the given character.
     * 
     * @param charSequence
     * @param character
     * @return true, if the given charSequence ends with the given character,
     *         otherwise false.
     * @see #notEndsWith(CharSequence, char)
     * @see #startsWith(CharSequence, char)
     */
    public static boolean endsWith(CharSequence charSequence, char character) {
        if (charSequence == null)
            return false;
        if (charSequence.length() == 0)
            return false;
        return charSequence.charAt(charSequence.length() - 1) == character;
    }

    /**
     * Check if the given objects are equal. Can deal with null references. if
     * both elements are null, than the result is true.
     * 
     * @param object1
     * @param object2
     * @return true, if the objects are equal.
     */
    public static boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        }
        return object1.equals(object2);
    }

    /**
     * Converts the given Date into a String. Copied from
     * {@link org.restlet.engine.Call}.
     * 
     * @param date
     *            Date to format
     * @param cookie
     *            if true, using RFC 1036 format, otherwise RFC 1123 format.
     * @return The formated date
     */
    public static String formatDate(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.get(0));
        }

        return DateUtils.format(date, DateUtils.FORMAT_RFC_1123.get(0));
    }

    /**
     * Formats the given {@link Set} of {@link Dimension}s to a String for the
     * HTTP Vary header.
     * 
     * @param dimensions
     *            the dimensions to format.
     * @return the Vary header or null, if dimensions is null or empty.
     */
    public static String formatDimensions(Set<Dimension> dimensions) {
        return DimensionWriter.write(dimensions);
    }

    /**
     * @param genCompType
     * @param forMessage
     * @throws NegativeArraySizeException
     * @throws ImplementationException
     */
    private static Class<?> getArrayClass(Type genCompType, Type forMessage)
            throws NegativeArraySizeException, ImplementationException {
        if (genCompType.equals(Byte.TYPE)) {
            return (new byte[0]).getClass();
        }
        if (genCompType.equals(Short.TYPE)) {
            return (new short[0]).getClass();
        }
        if (genCompType.equals(Integer.TYPE)) {
            return (new int[0]).getClass();
        }
        if (genCompType.equals(Long.TYPE)) {
            return (new long[0]).getClass();
        }
        if (genCompType.equals(Float.TYPE)) {
            return (new float[0]).getClass();
        }
        if (genCompType.equals(Double.TYPE)) {
            return (new double[0]).getClass();
        }
        if (genCompType.equals(Character.TYPE)) {
            return (new char[0]).getClass();
        }
        if (genCompType.equals(Boolean.TYPE)) {
            return (new boolean[0]).getClass();
        }
        if (genCompType instanceof Class<?>) {
            return Array.newInstance((Class<?>) genCompType, 0).getClass();
        }
        throw new ImplementationException("Sorry, could not handle a "
                + forMessage.getClass());
        // LATER could not handle all classes
    }

    /**
     * Returns the character set as String of the given http headers (e.g. from
     * a JAX-RS {@link javax.ws.rs.core.Response}).
     * 
     * @param httpHeaders
     *            the JAX-RS {@link javax.ws.rs.core.Response}
     * @param defaultCs
     *            the character set to return, if no one could be found.
     * @return the Restlet {@link CharacterSet} of the given http headers, or
     *         the given defaultCs as String if the header "Content-Type" is not
     *         available or has no parameter "charset". Returns only null, if
     *         the given defaultCs is null.
     * @see #getSupportedCharSet(MultivaluedMap)
     */
    public static String getCharsetName(
            MultivaluedMap<String, Object> httpHeaders, CharacterSet defaultCs) {
        String result = null;
        CharacterSet charSet = null;
        final Object contentType = httpHeaders.getFirst(CONTENT_TYPE);

        if (contentType == null) {
            charSet = defaultCs;
        } else {
            charSet = ContentType.readCharacterSet(contentType.toString());

            if (charSet == null) {
                charSet = defaultCs;
            }
        }

        if (charSet != null) {
            result = charSet.getName();
        }

        return result;
    }

    /**
     * Returns the first element of the given collection. Throws an exception if
     * the collection is empty.
     * 
     * @param coll
     * @param <A>
     * @return Returns the first Element of the collection
     * @throws NoSuchElementException
     *             If the collection is empty.
     */
    public static <A> A getFirstElement(Collection<A> coll)
            throws NoSuchElementException {
        if (coll.isEmpty()) {
            throw new NoSuchElementException(
                    "The Collection is empty; you can't get the first element of it.");
        }

        if (coll instanceof LinkedList<?>) {
            return ((LinkedList<A>) coll).getFirst();
        }

        if (coll instanceof List<?>) {
            return ((List<A>) coll).get(0);
        }

        return coll.iterator().next();
    }

    /**
     * Returns the first element of the given {@link Iterable}. Throws an
     * exception if the {@link Iterable} is empty.
     * 
     * @param coll
     * @param <A>
     * @return Returns the first Element of the collection
     * @throws NoSuchElementException
     *             If the collection is empty
     */
    public static <A> A getFirstElement(Iterable<A> coll)
            throws NoSuchElementException {
        if (coll instanceof LinkedList<?>) {
            return ((LinkedList<A>) coll).getFirst();
        }

        if (coll instanceof List<?>) {
            return ((List<A>) coll).get(0);
        }

        return coll.iterator().next();
    }

    /**
     * Returns the first element of the {@link List}. Throws an exception if the
     * list is empty.
     * 
     * @param list
     * @param <A>
     * @return Returns the first Element of the collection
     * @throws IndexOutOfBoundsException
     *             If the list is empty
     */
    public static <A> A getFirstElement(List<A> list)
            throws IndexOutOfBoundsException {
        if (list.isEmpty()) {
            throw new IndexOutOfBoundsException(
                    "The Collection is empty; you can't get the first element of it.");
        }

        if (list instanceof LinkedList<?>) {
            return ((LinkedList<A>) list).getFirst();
        }

        return list.get(0);
    }

    /**
     * Returns the first element of the given {@link Iterable}. Returns null, if
     * the {@link Iterable} is empty.
     * 
     * @param coll
     * @param <A>
     * @return the first element of the collection, or null if the iterable is
     *         empty.
     */
    public static <A> A getFirstElementOrNull(Iterable<A> coll) {
        if (coll instanceof LinkedList<?>) {
            final LinkedList<A> linkedList = ((LinkedList<A>) coll);
            if (linkedList.isEmpty()) {
                return null;
            }
            return linkedList.getFirst();
        }

        if (coll instanceof List<?>) {
            final List<A> list = ((List<A>) coll);
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        }

        if (coll instanceof Collection<?>) {
            if (((Collection<A>) coll).isEmpty()) {
                return null;
            }
        }

        return coll.iterator().next();
    }

    /**
     * Returns the first entry of the given {@link Map}. Throws an exception if
     * the Map is empty.
     * 
     * @param map
     * @param <K>
     * @param <V>
     * @return the first entry of the given {@link Map}.
     * @throws NoSuchElementException
     *             If the map is empty.
     */
    public static <K, V> Map.Entry<K, V> getFirstEntry(Map<K, V> map)
            throws NoSuchElementException {
        return map.entrySet().iterator().next();
    }

    /**
     * Returns the key of the first entry of the given {@link Map}. Throws an
     * exception if the Map is empty.
     * 
     * @param map
     * @param <K>
     * @param <V>
     * @return the key of the first entry of the given {@link Map}.
     * @throws NoSuchElementException
     *             If the map is empty.
     */
    public static <K, V> K getFirstKey(Map<K, V> map)
            throws NoSuchElementException {
        return map.keySet().iterator().next();
    }

    /**
     * Returns the value of the first entry of the given {@link Map}. Throws an
     * exception if the Map is empty.
     * 
     * @param map
     * @param <K>
     * @param <V>
     * @return the value of the first entry of the given {@link Map}.
     * @throws NoSuchElementException
     *             If the map is empty.
     */
    public static <K, V> V getFirstValue(Map<K, V> map)
            throws NoSuchElementException {
        return map.values().iterator().next();
    }

    /**
     * 
     * @param clazz
     *            The class which implemented interface should be checked.
     * @param implInterface
     *            the interface from which the generic type should be returned.
     * @return the type parameter of the given class.
     */
    public static Class<?> getGenericClass(Class<?> clazz,
            Class<?> implInterface) {
        if (clazz == null)
            throw new IllegalArgumentException("The class must not be null");
        if (implInterface == null)
            throw new IllegalArgumentException(
                    "The interface to b eimplemented must not be null");
        return getGenericClass(clazz, implInterface, null);
    }

    private static Class<?> getGenericClass(Class<?> clazz,
            Class<?> implInterface, Type[] gsatp) {
        for (Type ifGenericType : clazz.getGenericInterfaces()) {
            if (!(ifGenericType instanceof ParameterizedType)) {
                continue;
            }
            final ParameterizedType pt = (ParameterizedType) ifGenericType;
            Type ptRawType = pt.getRawType();
            if (ptRawType == null)
                continue;
            if (!ptRawType.equals(implInterface))
                continue;
            final Type[] atps = pt.getActualTypeArguments();
            if (atps == null || atps.length == 0)
                continue;
            final Type atp = atps[0];
            if (atp instanceof Class<?>) {
                return (Class<?>) atp;
            }
            if (atp instanceof ParameterizedType) {
                final Type rawType = ((ParameterizedType) atp).getRawType();
                if (rawType instanceof Class<?>) {
                    return (Class<?>) rawType;
                }
            }
            if (atp instanceof TypeVariable<?>) {
                TypeVariable<?> tv = (TypeVariable<?>) atp;
                String name = tv.getName();
                if (name == null)
                    continue;
                // clazz = AbstractProvider
                // implInterface = MessageBodyReader
                // name = "T"
                // pt = MessageBodyReader<T>
                for (int i = 0; i < atps.length; i++) {
                    TypeVariable<?> tv2 = (TypeVariable<?>) atps[i];
                    String tv2Name = tv2.getName();
                    if (tv2Name == null)
                        continue;
                    if (tv2Name.equals(name)) {
                        Type gsatpn = gsatp[i];
                        if (gsatpn instanceof Class<?>) {
                            return (Class<?>) gsatpn;
                        }
                        if (gsatpn instanceof ParameterizedType) {
                            final Type rawType = ((ParameterizedType) gsatpn)
                                    .getRawType();
                            if (rawType instanceof Class<?>)
                                return (Class<?>) rawType;
                            throw new ImplementationException(
                                    "Sorry, don't know how to return the class here");
                        }
                        if (gsatpn instanceof GenericArrayType) {
                            Type genCompType = ((GenericArrayType) gsatpn)
                                    .getGenericComponentType();
                            return getArrayClass(genCompType, gsatpn);
                        }
                        // if(gsatpn instanceof TypeVariable) {
                        // TypeVariable<Class<?>> tvn = (TypeVariable)gsatpn;
                        // Class<?> cl = tvn.getGenericDeclaration();
                        // Type[] boulds = tvn.getBounds();
                        // cl.toString();
                        // }
                        // throw new
                        // ImplementationException("Sorry, could not handle a "
                        // +gsatpn.getClass());
                    }
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        Type genericSuperClass = clazz.getGenericSuperclass();

        if ((genericSuperClass instanceof Class<?>)) {
            if (!implInterface.isAssignableFrom((Class<?>) genericSuperClass)) {
                // if the superclass doesn't implemented the
                // required interface, give up here ...
                return null;
            }
        }

        // ... otherwise, obtain type arguments from
        // superclass if it's a parameterized type ...
        if ((gsatp == null) && (genericSuperClass instanceof ParameterizedType)) {
            // LATER this is a hack
            gsatp = ((ParameterizedType) genericSuperClass)
                    .getActualTypeArguments();
        }

        // ... or make a recursion for a non-parameterized superclass.
        if (superClass != null)
            return getGenericClass(superClass, implInterface, gsatp);

        return null;
    }

    /**
     * Example: in List&lt;String&gt; -&gt; out: String.class
     * 
     * @param genericType
     * @return otherwise null
     */
    public static Class<?> getGenericClass(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }
        final ParameterizedType pt = (ParameterizedType) genericType;
        Type[] actualTypeArguments = pt.getActualTypeArguments();
        if (actualTypeArguments == null || actualTypeArguments.length == 0)
            return null;
        final Type atp = actualTypeArguments[0];
        if (atp instanceof Class<?>) {
            return (Class<?>) atp;
        }
        if (atp instanceof ParameterizedType) {
            final Type rawType = ((ParameterizedType) atp).getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            }

            return null;
        }
        return null;
    }

    /**
     * Returns the HTTP headers of the Restlet {@link Request} as {@link Form}.
     * 
     * @param request
     * @return Returns the HTTP headers of the Request.
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
     * Returns the HTTP headers of the Restlet {@link Response} as {@link Form}.
     * 
     * @param response
     * @return Returns the HTTP headers of the Response.
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
     * @return the request headers as {@link MultivaluedMap}.
     */
    public static MultivaluedMap<String, String> getJaxRsHttpHeaders(
            Request request) {
        final Map<String, Object> attrsOfRequ = request.getAttributes();
        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> headers = (MultivaluedMap<String, String>) attrsOfRequ
                .get(ORG_RESTLET_EXT_JAXRS_HTTP_HEADERS);
        if (headers == null) {
            headers = UnmodifiableMultivaluedMap.getFromForm(
                    getHttpHeaders(request), false);
            attrsOfRequ.put(ORG_RESTLET_EXT_JAXRS_HTTP_HEADERS, headers);
        }
        return headers;
    }

    /**
     * Returns the last element of the given {@link Iterable}. Throws an
     * exception if the given iterable is empty.
     * 
     * @param iterable
     * @param <A>
     * @return Returns the last element of the {@link Iterable}
     * @throws IndexOutOfBoundsException
     *             If the {@link Iterable} is a {@link List} and its is empty.
     * @throws NoSuchElementException
     *             If the {@link Iterable} is empty and the {@link Iterable} is
     *             not a {@link List}.
     */
    public static <A> A getLastElement(Iterable<A> iterable)
            throws IndexOutOfBoundsException, NoSuchElementException {
        if (iterable instanceof LinkedList<?>) {
            return ((LinkedList<A>) iterable).getLast();
        }

        if (iterable instanceof List<?>) {
            final List<A> list = ((List<A>) iterable);
            return list.get(list.size() - 1);
        }

        return getLastElement(iterable.iterator());
    }

    /**
     * Returns the last element of the given {@link Iterator}. Throws an
     * exception if the given iterator is empty.
     * 
     * @param iter
     * @param <A>
     * @return Returns the last element of the {@link Iterator}.
     * @throws NoSuchElementException
     *             If the {@link Iterator} is empty.
     */
    public static <A> A getLastElement(Iterator<A> iter)
            throws NoSuchElementException {
        A e = iter.next();
        while (iter.hasNext()) {
            e = iter.next();
        }
        return e;
    }

    /**
     * Returns the last element of the given {@link List}. Throws an exception
     * if the given list is empty.
     * 
     * @param list
     * @param <A>
     * @return Returns the last element of the list
     * @throws IndexOutOfBoundsException
     *             If the list is empty
     */
    public static <A> A getLastElement(List<A> list)
            throws IndexOutOfBoundsException {
        if (list instanceof LinkedList<?>) {
            return ((LinkedList<A>) list).getLast();
        }

        return list.get(list.size() - 1);
    }

    /**
     * Returns the last element of the given {@link Iterable}, or null, if the
     * iterable is empty. Returns null, if the iterable is empty.
     * 
     * @param iterable
     * @param <A>
     * @return Returns the last Element of the {@link Iterable}, or null if it
     *         is empty.
     */
    public static <A> A getLastElementOrNull(Iterable<A> iterable) {
        if (iterable instanceof LinkedList<?>) {
            final LinkedList<A> linkedList = ((LinkedList<A>) iterable);
            if (linkedList.isEmpty()) {
                return null;
            }
            return linkedList.getLast();
        }

        if (iterable instanceof List<?>) {
            final List<A> list = ((List<A>) iterable);
            if (list.isEmpty()) {
                return null;
            }
            return list.get(list.size() - 1);
        }

        if (iterable instanceof Collection<?>) {
            if (((Collection<A>) iterable).isEmpty()) {
                return null;
            }
        }

        return getLastElementOrNull(iterable.iterator());
    }

    /**
     * Returns the last element of the given {@link Iterator}, or null, if the
     * iterator is empty. Returns null, if the iterator is empty.
     * 
     * @param iter
     * @param <A>
     * @return Returns the last Element of the {@link Iterator}.
     */
    public static <A> A getLastElementOrNull(Iterator<A> iter) {
        A e = null;
        while (iter.hasNext()) {
            e = iter.next();
        }
        return e;
    }

    /**
     * Returns the Restlet {@link MediaType} of the given http headers (e.g.
     * from a JAX-RS {@link javax.ws.rs.core.Response}).
     * 
     * @param httpHeaders
     *            the JAX-RS {@link javax.ws.rs.core.Response}
     * @return the Restlet {@link MediaType} of the given http headers, or null,
     *         if the header "Content-Type" is not available.
     */
    public static MediaType getMediaType(
            MultivaluedMap<String, Object> httpHeaders) {
        final Object contentType = httpHeaders.getFirst(CONTENT_TYPE);
        if (contentType == null) {
            return null;
        }
        if (contentType instanceof MediaType) {
            return (MediaType) contentType;
        }
        if (contentType instanceof javax.ws.rs.core.MediaType) {
            return Converter
                    .toRestletMediaType((javax.ws.rs.core.MediaType) contentType);
        }
        return ContentType.readMediaType(contentType.toString());
    }

    /**
     * Returns all public {@link Method}s of the class with the given name
     * (case-sensitive)
     * 
     * @param clazz
     *            The {@link Class} to search the {@link Method}s.
     * @param methodName
     *            The name of the {@link Method} to search.
     * @return Returns a {@link Collection} all of {@link Method}s with the
     *         given name. Never returns null. If no methods are found an empty
     *         Collection will be returned. The method {@link Iterator#remove()}
     *         of this collection is supported.
     * @throws IllegalArgumentException
     *             if the clazz or the method name is null.
     */
    public static Collection<Method> getMethodsByName(Class<?> clazz,
            String methodName) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("The class must not be null");
        }
        if (methodName == null) {
            throw new IllegalArgumentException(
                    "The method name must not be null");
        }
        final Collection<Method> methods = new ArrayList<Method>(2);
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * Returns the only element of the list, or null, if the List is null or
     * empty.
     * 
     * @param <A>
     * @param list
     *            a List with at most one element
     * @return The element of the List, or null, if there is no element.
     * @throws IllegalArgumentException
     *             if the list contains more than one element.
     */
    public static <A> A getOnlyElement(Collection<A> list)
            throws IllegalArgumentException {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return null;
        }

        if (list.size() > 1) {
            throw new IllegalArgumentException(
                    "The list must have exactly one element");
        }

        if (list instanceof List<?>) {
            return ((List<A>) list).get(0);
        }

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
        final Metadata metadata = getOnlyElement(metadatas);
        if (metadata == null) {
            return null;
        }
        return metadata.getName();
    }

    /**
     * Returns the &#64;{@link Path} annotation of the given root resource
     * class.
     * 
     * @param jaxRsClass
     *            the root resource class.
     * @return the &#64;{@link Path} annotation of the given root resource
     *         class.
     * @throws MissingAnnotationException
     *             if the path annotation is missing
     * @throws IllegalArgumentException
     *             if the jaxRsClass is null.
     */
    public static Path getPathAnnotation(Class<?> jaxRsClass)
            throws MissingAnnotationException, IllegalArgumentException {
        if (jaxRsClass == null) {
            throw new IllegalArgumentException(
                    "The jaxRsClass must not be null");
        }
        final Path path = jaxRsClass.getAnnotation(Path.class);
        if (path == null) {
            throw new MissingAnnotationException(
                    "The root resource class does not have a @Path annotation");
        }
        return path;
    }

    /**
     * Returns the &#64;{@link Path} annotation of the given sub resource
     * locator. Throws an exception if no &#64;{@link Path} annotation is
     * available.
     * 
     * @param method
     *            the java method to get the &#64;Path from
     * @return the &#64;Path annotation.
     * @throws IllegalArgumentException
     *             if null was given.
     * @throws MissingAnnotationException
     *             if the annotation is not present.
     */
    public static Path getPathAnnotation(Method method)
            throws IllegalArgumentException, MissingAnnotationException {
        if (method == null) {
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        }
        final Path path = method.getAnnotation(Path.class);
        if (path == null) {
            throw new MissingAnnotationException("The method "
                    + method.getName() + " does not have an annotation @Path");
        }
        return path;
    }

    /**
     * Returns the &#64;{@link Path} annotation of the given sub resource
     * locator. Returns null if no &#64;{@link Path} annotation is available.
     * 
     * @param method
     *            the java method to get the &#64;Path from
     * @return the &#64;Path annotation or null, if not present.
     * @throws IllegalArgumentException
     *             if the method is null.
     */
    public static Path getPathAnnotationOrNull(Method method)
            throws IllegalArgumentException {
        if (method == null) {
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        }
        return method.getAnnotation(Path.class);
    }

    /**
     * Returns the perhaps decoded template of the path annotation.
     * 
     * @param resource
     * @return Returns the path template as String. Never returns null.
     * @throws IllegalPathOnClassException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     */
    public static String getPathTemplateWithoutRegExps(Class<?> resource)
            throws IllegalPathOnClassException, MissingAnnotationException,
            IllegalArgumentException {
        try {
            return getPathTemplateWithoutRegExps(Util
                    .getPathAnnotation(resource));
        } catch (IllegalPathException e) {
            throw new IllegalPathOnClassException(e);
        }
    }

    /**
     * Returns the path template of the given sub resource locator or sub
     * resource method. It is encoded (if necessary) and valid.
     * 
     * @param method
     *            the java method
     * @return the path template
     * @throws IllegalPathOnMethodException
     * @throws IllegalArgumentException
     * @throws MissingAnnotationException
     */
    public static String getPathTemplateWithoutRegExps(Method method)
            throws IllegalArgumentException, IllegalPathOnMethodException,
            MissingAnnotationException {
        final Path path = getPathAnnotation(method);
        try {
            return getPathTemplateWithoutRegExps(path);
        } catch (IllegalPathException e) {
            throw new IllegalPathOnMethodException(e);
        }
    }

    /**
     * Returns the path from the annotation. It will be encoded if necessary. If
     * it should not be encoded, this method checks, if all characters are
     * valid.
     * 
     * @param path
     *            The {@link Path} annotation. Must not be null.
     * @return the encoded path template
     * @throws IllegalPathException
     * @see Path#encode()
     */
    public static String getPathTemplateWithoutRegExps(final Path path)
            throws IllegalPathException {
        return getPathTemplateWithoutRegExps(path.value(), path);
    }

    /**
     * @param pathTemplate
     * @param pathForExcMess
     * @return &nbsp;
     * @throws IllegalPathException
     */
    public static String getPathTemplateWithoutRegExps(
            final String pathTemplate, final Path pathForExcMess)
            throws IllegalPathException {
        final StringBuilder stb = new StringBuilder();
        final int l = pathTemplate.length();
        for (int i = 0; i < l; i++) {
            final char c = pathTemplate.charAt(i);
            if (c == '{') {
                i = processTemplVarname(pathTemplate, i, stb, pathForExcMess);
            } else if (c == '%') {
                try {
                    EncodeOrCheck.processPercent(i, true, pathTemplate, stb);
                } catch (IllegalArgumentException e) {
                    throw new IllegalPathException(pathForExcMess, e);
                }
            } else if (c == '}') {
                throw new IllegalPathException(pathForExcMess,
                        "'}' is only allowed as "
                                + "end of a variable name in \"" + pathTemplate
                                + "\"");
            } else if (c == ';') {
                throw new IllegalPathException(pathForExcMess,
                        "A semicolon is not allowed in a path");
            } else if (c == '/') {
                stb.append(c);
            } else {
                EncodeOrCheck.encode(c, stb);
            }
        }
        return stb.toString();
    }

    /**
     * Returns the given character set, if it is supported on this system.
     * Returns UTF-8 otherwise.
     * 
     * @param characterSet
     *            the wished {@link CharacterSet}
     * @return a supported {@link CharacterSet}, never null.
     * @see #getCharsetName(MultivaluedMap, CharacterSet)
     */
    public static CharacterSet getSupportedCharSet(CharacterSet characterSet) {
        if (characterSet == null) {
            return JAX_RS_DEFAULT_CHARACTER_SET;
        }
        if (Charset.isSupported(characterSet.toString())) {
            return characterSet;
        }
        return logUnsupportedCharSet(characterSet.toString());
    }

    /**
     * Returns the character set of the http response headers. If no character
     * set is available or it is not supported, UFT-8 is returned.
     * 
     * @param httpResponseHeaders
     * @return a supported {@link CharacterSet}, never null.
     * @see #getCharsetName(MultivaluedMap, CharacterSet)
     */
    public static CharacterSet getSupportedCharSet(
            MultivaluedMap<String, Object> httpResponseHeaders) {
        final String csn = getCharsetName(httpResponseHeaders,
                JAX_RS_DEFAULT_CHARACTER_SET);
        if (Charset.isSupported(csn)) {
            return CharacterSet.valueOf(csn);
        }
        return logUnsupportedCharSet(csn);
    }

    /**
     * Returns the index of the first ";" in the last path segment.
     * 
     * @param path
     *            the path, without a query.
     * @return the index of the first ";" in the last path segment. Returns -1,
     *         if no ';' is available.
     */
    public static int indexBeginMatrixOfLastSegment(CharSequence path) {
        // NICE optimize for speed; avoid conversion to String, if it is faster.
        String pathStr = path.toString();
        return pathStr.indexOf(';', pathStr.lastIndexOf('/'));
    }

    /**
     * Returns the index of the first occurrence of the given character between
     * the given indexes.
     * 
     * @param charSequence
     *            the char sequence to look in
     * @param c
     *            the character to look for.
     * @param beginIndex
     * @param endIndex
     * @return the index of the given character between the given indexes, or -1
     *         if the character could not be found in the given range.
     * @see String#indexOf(String, int)
     * @see String#substring(int, int)
     */
    public static int indexOfBetween(CharSequence charSequence, char c,
            int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex < 0) {
            endIndex = 0;
        }
        for (int i = beginIndex; i < endIndex; i++) {
            char csc = charSequence.charAt(i);
            if (csc == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Injects the given toInject in the resource field or the given bean
     * setter.
     * 
     * @param resource
     * @param fieldOrBeanSetter
     * @param toInject
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws InjectException
     */
    public static void inject(Object resource,
            AccessibleObject fieldOrBeanSetter, Object toInject)
            throws InvocationTargetException, IllegalArgumentException,
            InjectException {
        if (fieldOrBeanSetter instanceof Field) {
            inject(resource, (Field) fieldOrBeanSetter, toInject);
        } else if (fieldOrBeanSetter instanceof Method) {
            inject(resource, (Method) fieldOrBeanSetter, toInject);
        } else {
            throw new IllegalArgumentException(
                    "The fieldOrBeanSetter must be a java.lang.reflect.Field or a java.lang.reflect.Method");
        }
    }

    /**
     * Inject the given toInject into the given field in the given resource (or
     * whatever)
     * 
     * @param resource
     *            the concrete Object to inject the other object in. If the
     *            field is static, thsi object may be null.
     * @param field
     *            the field to inject the third parameter in.
     * @param toInject
     *            the object to inject in the first parameter object.
     * @throws InjectException
     *             if the injection was not possible. See
     *             {@link InjectException#getCause()} for the reason.
     */
    public static void inject(final Object resource, final Field field,
            final Object toInject) throws InjectException {
        try {
            final IllegalAccessException iae = AccessController
                    .doPrivileged(new PrivilegedAction<IllegalAccessException>() {
                        public IllegalAccessException run() {
                            try {
                                field.set(resource, toInject);
                                return null;
                            } catch (IllegalAccessException e) {
                                return e;
                            }
                        }
                    });
            if (iae != null) {
                throw new InjectException("Could not inject the "
                        + toInject.getClass() + " into field " + field
                        + " of object " + resource, iae);
            }
        } catch (RuntimeException e) {
            throw new InjectException("Could not inject the "
                    + toInject.getClass() + " into field " + field
                    + " of object " + resource, e);
        }
    }

    /**
     * Injects the given toInject in the resource with the given bean setter.
     * 
     * @param resource
     * @param beanSetter
     * @param toInject
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws InjectException
     */
    public static void inject(Object resource, Method beanSetter,
            Object toInject) throws InvocationTargetException,
            IllegalArgumentException, InjectException {
        try {
            invokeMethod(resource, beanSetter, toInject);
        } catch (MethodInvokeException mie) {
            throw new InjectException(mie);
        }
    }

    /**
     * Invokes the given method without parameters. This constraint is not
     * checked; but the method could also be called, if access is normally not
     * allowed.<br>
     * If no javaMethod is given, nothing happens.
     * 
     * @param object
     *            the object to call the method on.
     * @param javaMethod
     *            the method to call
     * @param args
     *            the arguments of the method
     * @throws MethodInvokeException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     *             if at least one argument does not match the required method
     *             parameters.
     * @see #inject(Object, Field, Object)
     * @see #findPostConstructMethod(Class)
     * @see #findPreDestroyMethod(Class)
     */
    public static void invokeMethod(final Object object,
            final Method javaMethod, final Object... args)
            throws MethodInvokeException, InvocationTargetException,
            IllegalArgumentException {
        if (javaMethod == null) {
            return;
        }
        try {
            AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            javaMethod.invoke(object, args);
                            return null;
                        }
                    });
        } catch (PrivilegedActionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IllegalAccessException) {
                throw new MethodInvokeException(
                        "Not allowed to invoke post construct method "
                                + javaMethod, cause);
            }
            if (cause instanceof InvocationTargetException) {
                throw (InvocationTargetException) cause;
            }
            if (cause instanceof ExceptionInInitializerError) {
                throw new MethodInvokeException(
                        "Could not invoke post construct method " + javaMethod,
                        cause);
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new JaxRsRuntimeException(
                    "Error while invoking post construct method " + javaMethod,
                    cause);
        }
    }

    /**
     * Checks, if the list is empty or null.
     * 
     * @param list
     * @return true, if the list is empty or null, or false, if the list
     *         contains elements.
     * @see #isEmpty(Object[])
     */
    public static boolean isEmpty(List<?> list) {
        return ((list == null) || list.isEmpty());
    }

    /**
     * Tests, if the given array is empty or null. Will not throw a
     * NullPointerException.
     * 
     * @param array
     * @return Returns true, if the given array ist null or has zero elements,
     *         otherwise false.
     * @see #isEmpty(List)
     */
    public static boolean isEmpty(Object[] array) {
        if ((array == null) || (array.length == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Tests, if the given String is null, empty or "/". Will not throw a
     * NullPointerException.
     * 
     * @param string
     * @return Returns true, if the given string ist null, empty or equals "/",
     *         otherwise false.
     */
    public static boolean isEmptyOrSlash(String string) {
        return (string == null) || (string.length() == 0) || string.equals("/");
    }

    /**
     * Checks, if the list contains elements.
     * 
     * @param list
     * @return true, if the list contains elements, or false, if the list is
     *         empty or null.
     */
    public static boolean isNotEmpty(List<?> list) {
        return ((list != null) && !list.isEmpty());
    }

    /**
     * Checks, if the given class is a JAX-RS provider.
     * 
     * @param jaxRsClass
     *            the class to check
     * @return true, if the class is a JAX-RS provider, otherwise false.
     */
    public static boolean isProvider(Class<?> jaxRsClass) {
        return jaxRsClass.isAnnotationPresent(javax.ws.rs.ext.Provider.class);
    }

    /**
     * Checks, if the given class is a JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *            the class to check
     * @return true, if the class is a JAX-RS root resource class, otherwise
     *         false.
     */
    public static boolean isRootResourceClass(Class<?> jaxRsClass) {
        return jaxRsClass.isAnnotationPresent(javax.ws.rs.Path.class);
    }

    /**
     * Logs a message that the wished character set is not supported and UTF-8
     * is used.
     * 
     * @param csn
     * @return UFT-8
     */
    private static CharacterSet logUnsupportedCharSet(String charsetName) {
        Context.getCurrentLogger().warning(
                "The character set " + charsetName + " is not "
                        + "available. Will use "
                        + JAX_RS_DEFAULT_CHARACTER_SET_AS_STRING);
        return JAX_RS_DEFAULT_CHARACTER_SET;
    }

    /**
     * Checks, if the given CharSequence ends with the given character.
     * 
     * @param charSequence
     * @param character
     * @return true, if the given charSequence ends with the given character,
     *         otherwise false.
     * @see #endsWith(CharSequence, char)
     * @see #notStartsWith(CharSequence, char)
     */
    public static boolean notEndsWith(CharSequence charSequence, char character) {
        if (charSequence == null)
            return true;
        if (charSequence.length() == 0)
            return true;
        return charSequence.charAt(charSequence.length() - 1) != character;
    }

    /**
     * Checks, if the given CharSequence starts with the given character.
     * 
     * @param charSequence
     * @param character
     * @return true, if the given charSequence starts with the given character,
     *         otherwise false.
     * @see #startsWith(CharSequence, char)
     * @see #notEndsWith(CharSequence, char)
     */
    public static boolean notStartsWith(CharSequence charSequence,
            char character) {
        if (charSequence == null)
            return true;
        if (charSequence.length() == 0)
            return true;
        return charSequence.charAt(0) != character;
    }

    /**
     * @param pathTemplate
     * @param braceIndex
     * @param stb
     * @param pathForExcMess
     * @throws IllegalPathException
     */
    private static int processTemplVarname(final String pathTemplate,
            final int braceIndex, final StringBuilder stb,
            final Path pathForExcMess) throws IllegalPathException {
        final int l = pathTemplate.length();
        stb.append('{');
        int state = NAME_READ_START;
        for (int i = braceIndex + 1; i < l; i++) {
            final char c = pathTemplate.charAt(i);
            if (c == '{') {
                throw new IllegalPathException(pathForExcMess,
                        "A variable must not " + "contain an extra '{' in \""
                                + pathTemplate + "\"");
            } else if (c == ' ' || c == '\t') {
                if (state == NAME_READ)
                    state = NAME_READ_READY;
                continue;
            } else if (c == ':') {
                if (state == NAME_READ_START) {
                    throw new IllegalPathException(pathForExcMess,
                            "The variable name at position must not be null at "
                                    + braceIndex + " of \"" + pathTemplate
                                    + "\"");
                }
                if (state == NAME_READ || state == NAME_READ_READY) {
                    for (int j = i; j < l; j++) {
                        if (pathTemplate.charAt(j) == '}') {
                            stb.append('}');
                            return j;
                        }
                    }
                    throw new IllegalPathException(pathForExcMess,
                            "No '}' found after '{' at position " + braceIndex
                                    + " of \"" + pathTemplate + "\"");
                }
            } else if (c == '}') {
                if (state == NAME_READ_START) {
                    throw new IllegalPathException(pathForExcMess,
                            "The template variable name '{}' is not allowed in "
                                    + "\"" + pathTemplate + "\"");
                }
                stb.append('}');
                return i;
            }

            if (state == NAME_READ_START) {
                state = NAME_READ;
                stb.append(c);
            } else if (state == NAME_READ) {
                stb.append(c);
            } else {
                throw new IllegalPathException(pathForExcMess,
                        "Invalid character found at position " + i + " of \""
                                + pathTemplate + "\"");
            }
        }
        throw new IllegalPathException(pathForExcMess,
                "No '}' found after '{' " + "at position " + braceIndex
                        + " of \"" + pathTemplate + "\"");
    }

    /**
     * Returns a new {@link List}, which contains all
     * {@link org.restlet.data.MediaType}s of the given List, sorted by it's
     * concreteness, the concrete {@link org.restlet.data.MediaType} at the
     * beginning.
     * 
     * @param mediaTypes
     * @return the sorted media types
     * @see Util#specificness(org.restlet.data.MediaType)
     */
    public static List<org.restlet.data.MediaType> sortByConcreteness(
            Collection<org.restlet.data.MediaType> mediaTypes) {
        final List<org.restlet.data.MediaType> newList = new ArrayList<org.restlet.data.MediaType>(
                mediaTypes.size());
        for (final org.restlet.data.MediaType mediaType : mediaTypes) {
            if (specificness(mediaType) > 0) {
                newList.add(mediaType);
            }
        }
        for (final org.restlet.data.MediaType mediaType : mediaTypes) {
            if (specificness(mediaType) == 0) {
                newList.add(mediaType);
            }
        }
        for (final org.restlet.data.MediaType mediaType : mediaTypes) {
            if (specificness(mediaType) < 0) {
                newList.add(mediaType);
            }
        }
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
        if (mediaType.equals(org.restlet.data.MediaType.ALL, true)) {
            return -1;
        }
        if (mediaType.getSubType().equals("*")) {
            return 0;
        }
        return 1;
    }

    /**
     * Checks, if the given CharSequence starts with the given character.
     * 
     * @param charSequence
     * @param character
     * @return true, if the given charSequence starts with the given character,
     *         otherwise false.
     * @see #notStartsWith(CharSequence, char)
     * @see #endsWith(CharSequence, char)
     */
    public static boolean startsWith(CharSequence charSequence, char character) {
        if (charSequence == null)
            return false;
        if (charSequence.length() == 0)
            return false;
        return charSequence.charAt(0) == character;
    }

    /**
     * Creates an Array of the given type.
     * 
     * @param coll
     * @param arrayType
     * @return the object array
     * @throws NegativeArraySizeException
     */
    public static Object[] toArray(Collection<?> coll, Class<?> arrayType) {
        final int collSize = coll.size();
        final Object[] array = (Object[]) Array
                .newInstance(arrayType, collSize);
        return coll.toArray(array);
    }

    /**
     * Concatenate the members of the Set to a String, separated with the given
     * delimiter.
     * 
     * @param collection
     * @param delimiter
     * @return the concatenated
     */
    public static String toString(Collection<?> collection, String delimiter) {
        if ((collection == null) || collection.isEmpty()) {
            return "";
        }
        final Iterator<?> iterator = collection.iterator();
        if (collection.size() == 1) {
            return String.valueOf(iterator.next());
        }
        final StringBuilder stb = new StringBuilder();
        stb.append(iterator.next());
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            stb.append(delimiter);
            stb.append(object);
        }
        return stb.toString();
    }
}