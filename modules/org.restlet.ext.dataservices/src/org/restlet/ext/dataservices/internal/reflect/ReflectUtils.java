/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.dataservices.internal.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.ext.atom.Category;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.dataservices.internal.edm.Property;
import org.restlet.ext.dataservices.internal.edm.Type;

/**
 * Handles Java reflection operations.
 * 
 * @author Thierry Boileau
 */
public class ReflectUtils {

    /** The internal logger. */
    private final static Logger logger = Logger.getLogger(ReflectUtils.class
            .getCanonicalName());

    /**
     * Returns the Java class of a set of entries contained inside a Feed.
     * 
     * @param feed
     *            The feed to analyze.
     * @return The Java class of a set of entries contained inside a Feed or
     *         null if it has not been found.
     */
    public static Class<?> getEntryClass(Feed feed) {
        Class<?> result = null;
        if (feed != null && feed.getEntries() != null
                && !feed.getEntries().isEmpty()) {
            for (Entry entry : feed.getEntries()) {
                if (entry.getCategories() != null
                        && !entry.getCategories().isEmpty()) {
                    Category category = entry.getCategories().get(0);
                    try {
                        result = Class.forName(Type.getFullClassName(category
                                .getTerm()));
                        break;
                    } catch (ClassNotFoundException e) {
                        continue;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the class of this entity's attribute, or if it is a Collection
     * (array, generic list, set), it returns the generic type.
     * 
     * @param entity
     *            The entity.
     * @param propertyName
     *            The property name.
     * @return The simple class of this entity's attribute.
     */
    public static Class<?> getSimpleClass(Object entity, String propertyName) {
        Class<?> result = null;
        String normPteName = normalize(propertyName);
        try {
            Field field = entity.getClass().getDeclaredField(normPteName);
            if (field.getType().isArray()) {
                result = field.getType().getComponentType();
            } else {
                java.lang.reflect.Type genericFieldType = field
                        .getGenericType();

                if (genericFieldType instanceof ParameterizedType) {
                    ParameterizedType aType = (ParameterizedType) genericFieldType;
                    java.lang.reflect.Type[] fieldArgTypes = aType
                            .getActualTypeArguments();
                    if (fieldArgTypes.length == 1) {
                        result = (Class<?>) fieldArgTypes[0];
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't access to the following property "
                    + normPteName + " on " + entity.getClass() + ".", e);
        }

        return result;
    }

    /**
     * Sets a property on an entity based on its name.
     * 
     * @param entity
     *            The entity to update.
     * @param propertyName
     *            The property name.
     * @param propertyValue
     *            The property value.
     * @throws Exception
     */
    public static void invokeSetter(Object entity, String propertyName,
            Object propertyValue) throws Exception {
        String setterName = null;
        char firstLetter = propertyName.charAt(0);
        if (Character.isLowerCase(firstLetter)) {
            setterName = "set" + Character.toUpperCase(firstLetter)
                    + propertyName.substring(1);
        } else {
            setterName = "set" + propertyName;
        }

        Method setter = null;
        Method method;
        for (int i = 0; (setter == null)
                && (i < entity.getClass().getDeclaredMethods().length); i++) {
            method = entity.getClass().getDeclaredMethods()[i];

            if (method.getName().equals(setterName)) {
                if ((method.getParameterTypes() != null)
                        && (method.getParameterTypes().length == 1)) {
                    setter = method;
                }
            }
        }

        if (setter != null) {
            setter.invoke(entity, propertyValue);
        }
    }

    /**
     * Sets a property on an entity based on its name.
     * 
     * @param entity
     *            The entity to update.
     * @param propertyName
     *            The property name.
     * @param propertyValue
     *            The property value.
     * @param propertyType
     *            The property data type.
     * @throws Exception
     */
    public static void invokeSetter(Object entity, String propertyName,
            String propertyValue, String propertyType) throws Exception {
        String setterName = null;
        char firstLetter = propertyName.charAt(0);
        if (Character.isLowerCase(firstLetter)) {
            setterName = "set" + Character.toUpperCase(firstLetter)
                    + propertyName.substring(1);
        } else {
            setterName = "set" + propertyName;
        }

        Method setter = null;
        Object setterParameter = null;
        Method method;
        for (int i = 0; (setter == null)
                && (i < entity.getClass().getDeclaredMethods().length); i++) {
            method = entity.getClass().getDeclaredMethods()[i];

            if (method.getName().equals(setterName)) {
                if ((method.getParameterTypes() != null)
                        && (method.getParameterTypes().length == 1)) {
                    Class<?> parameterType = method.getParameterTypes()[0];

                    if (String.class.equals(parameterType)) {
                        setterParameter = propertyValue;
                        setter = method;
                    } else if (Integer.class.equals(parameterType)) {
                        setterParameter = Integer.valueOf(propertyValue);
                        setter = method;
                    } else if (int.class.equals(parameterType)) {
                        setterParameter = Integer.valueOf(propertyValue);
                        setter = method;
                    }
                }
            }
        }

        if (setter != null) {
            setter.invoke(entity, setterParameter);
        }
    }

    /**
     * Returns the name following the the java naming rules.
     * 
     * @see <a
     *      href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#40625">
     *      Identifiers</a>
     * @param name
     *            The name to convert.
     * @return The name following the the java naming rules.
     */
    public static String normalize(String name) {
        String result = null;
        if (name != null) {
            // Build the normalized name according to the java naming rules
            StringBuilder b = new StringBuilder();
            boolean upperCase = false;
            char oldChar = 0;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (Character.isDigit(c)) {
                    b.append(c);
                    oldChar = c;
                } else if (c >= 'a' && c <= 'z') {
                    if (upperCase) {
                        b.append(Character.toUpperCase(c));
                        upperCase = false;
                    } else {
                        b.append(c);
                    }
                    oldChar = c;
                } else if (c >= 'A' && c <= 'Z') {
                    if (upperCase) {
                        b.append(c);
                        upperCase = false;
                    } else if (oldChar != 0 && Character.isLowerCase(oldChar)) {
                        b.append(c);
                    } else {
                        b.append(Character.toLowerCase(c));
                    }
                    oldChar = c;
                } else if (c == '.') {
                    upperCase = true;
                } else if (Character.isJavaIdentifierPart(c)) {
                    b.append(c);
                    oldChar = c;
                } else {
                    upperCase = true;
                }
            }
            result = b.toString();
        }

        return result;
    }

    /**
     * Sets a property on an entity based on its name.
     * 
     * @param entity
     *            The entity to update.
     * @param property
     *            The property.
     * @param propertyValue
     *            The property value.
     * @throws Exception
     */
    public static void setProperty(Object entity, Property property,
            String propertyValue) throws Exception {
        invokeSetter(entity, property.getNormalizedName(), Type.fromEdm(
                propertyValue, property.getType().getAdoNetType()));
    }

    /**
     * Sets a property on an entity based on its name.
     * 
     * @param entity
     *            The entity to update.
     * @param propertyName
     *            The property name.
     * @param isCollection
     *            Should this property be a collection.
     * @param iterator
     *            The collection of values to set.
     * @param propertyClass
     *            The kind of objects stored by this property.
     * @throws Exception
     */
    public static void setProperty(Object entity, String propertyName,
            boolean isCollection, Iterator<?> iterator, Class<?> propertyClass)
            throws Exception {
        String normPteName = normalize(propertyName);
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        boolean isGeneric = false;
        boolean isArray = false;
        Field field = entity.getClass().getDeclaredField(normPteName);
        if (field.getType().isArray()) {
            isArray = true;
        } else {
            java.lang.reflect.Type genericFieldType = field.getGenericType();

            if (genericFieldType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                java.lang.reflect.Type[] fieldArgTypes = aType
                        .getActualTypeArguments();
                if (fieldArgTypes.length == 1) {
                    isGeneric = true;
                }
            }
        }

        if (isCollection) {
            if (isArray) {
                List<Object> list = new ArrayList<Object>();
                for (; iterator.hasNext();) {
                    list.add(iterator.next());
                }
                ReflectUtils.invokeSetter(entity, normPteName, list.toArray());
            } else if (isGeneric) {
                if (List.class.isAssignableFrom(field.getType())) {
                    List<Object> list = new ArrayList<Object>();
                    for (; iterator.hasNext();) {
                        list.add(iterator.next());
                    }
                    ReflectUtils.invokeSetter(entity, normPteName, list);
                } else if (Set.class.isAssignableFrom(field.getType())) {
                    Set<Object> set = new TreeSet<Object>();
                    for (; iterator.hasNext();) {
                        set.add(iterator.next());
                    }

                    ReflectUtils.invokeSetter(entity, normPteName, set);
                }
            }
        } else {
            for (; iterator.hasNext();) {
                Object property = iterator.next();
                ReflectUtils.invokeSetter(entity, normPteName, property);
            }
        }

    }

    /**
     * Sets a property on an entity based on its name.
     * 
     * @param entity
     *            The entity to update.
     * @param propertyName
     *            The property name.
     * @param propertyValue
     *            The property value.
     * @throws Exception
     */
    public static void setProperty(Object entity, String propertyName,
            String propertyValue) throws Exception {
        int colonIndex = propertyName.indexOf(':');

        if (colonIndex != -1) {
            propertyName = propertyName.substring(colonIndex + 1);
        }

        invokeSetter(entity, propertyName, propertyValue, null);
    }
}
