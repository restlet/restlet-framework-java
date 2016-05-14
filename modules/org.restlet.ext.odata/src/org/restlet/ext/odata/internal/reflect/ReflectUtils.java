/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.odata.internal.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.atom.Category;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.odata.internal.edm.Property;
import org.restlet.ext.odata.internal.edm.TypeUtils;

/**
 * Handles Java reflection operations.
 * 
 * @author Thierry Boileau
 */
public class ReflectUtils {

    /** The internal logger. */
    private final static Logger logger = Context.getCurrentLogger();

    /** List of reserved Java words. */
    private final static List<String> reservedWords = Arrays.asList("abstract",
            "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "double",
            "else", "enum", "extends", "final", "finally", "float", "for",
            "if", "goto", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "switch", "synchronized", "this", "throw",
            "transient", "try", "void", "volatile", "while");

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
                        result = Class.forName(TypeUtils
                                .getFullClassName(category.getTerm()));
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
                } else {
                    result = field.getType();
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't access to the following property "
                    + normPteName + " on " + entity.getClass() + ".", e);
        }

        return result;
    }

    /**
     * Returns the value of a property on an entity based on its name.
     * 
     * @param entity
     *            The entity.
     * @param propertyName
     *            The property name.
     * @return The value of a property for an entity.
     * @throws Exception
     */
    public static Object invokeGetter(Object entity, String propertyName)
            throws Exception {
        Object result = null;

        if (propertyName != null && entity != null) {
            propertyName = propertyName.replaceAll("/", ".");
            Object o = entity;
            String pty = propertyName;
            int index = propertyName.indexOf(".");
            if (index != -1) {
                o = invokeGetter(entity, propertyName.substring(0, index));
                pty = propertyName.substring(index + 1);

                result = invokeGetter(o, pty);
            } else {
                String getterName = null;
                char firstLetter = propertyName.charAt(0);
                if (Character.isLowerCase(firstLetter)) {
                    getterName = "get" + Character.toUpperCase(firstLetter)
                            + pty.substring(1);
                } else {
                    getterName = "get" + pty;
                }

                Method getter = null;
                Method method;
                for (int i = 0; (getter == null)
                        && (i < entity.getClass().getDeclaredMethods().length); i++) {
                    method = entity.getClass().getDeclaredMethods()[i];

                    if (method.getName().equals(getterName)) {
                        getter = method;
                    }
                }

                if (getter != null) {
                    result = getter.invoke(o);
                }
            }
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
        if (propertyName != null && entity != null) {
            propertyName = propertyName.replaceAll("/", ".");
            Object o = entity;
            String pty = propertyName;
            String[] strings = propertyName.split("\\.");
            if (strings.length > 1) {
                for (int i = 0; i < strings.length - 1; i++) {
                    String string = strings[i];
                    Object p = invokeGetter(o, string);
                    if (p == null) {
                        // Try to instantiate it
                        Field[] fields = o.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getName().equalsIgnoreCase(string)) {
                                p = field.getType().newInstance();
                                break;
                            }
                        }
                    }
                    if (p == null) {
                        // can't set a property on a null value
                        return;
                    }
                    o = p;
                }
                pty = strings[strings.length - 1];
            }

            String setterName = null;
            char firstLetter = pty.charAt(0);
            if (Character.isLowerCase(firstLetter)) {
                setterName = "set" + Character.toUpperCase(firstLetter) + pty.substring(1);
            } else {
                setterName = "set" + pty;
            }

            Method setter = null;
            Method method;
            for (int i = 0; (setter == null)
                    && (i < o.getClass().getDeclaredMethods().length); i++) {
                method = o.getClass().getDeclaredMethods()[i];

                if (method.getName().equals(setterName)) {
                    if ((method.getParameterTypes() != null)
                            && (method.getParameterTypes().length == 1)) {
                        setter = method;
                    }
                }
            }

            if (setter != null) {
                setter.invoke(o, propertyValue);
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
     * @param propertyType
     *            The property data type.
     * @throws Exception
     */
    public static void invokeSetter(Object entity, String propertyName,
            String propertyValue, String propertyType) throws Exception {

        if (propertyName != null) {
            propertyName = propertyName.replaceAll("/", ".");
            Object o = entity;
            String pty = propertyName;

            String[] strings = propertyName.split("\\.");
            if (strings.length > 1) {
                for (int i = 0; i < strings.length - 1; i++) {
                    String string = strings[i];
                    Object p = invokeGetter(o, string);
                    if (p == null) {
                        // Try to instantiate it
                        Field[] fields = o.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getName().equalsIgnoreCase(string)) {
                                p = field.getType().newInstance();
                                break;
                            }
                        }
                    }
                    o = p;
                }
                pty = strings[strings.length - 1];
            }

            String setterName = null;
            char firstLetter = propertyName.charAt(0);
            if (Character.isLowerCase(firstLetter)) {
                setterName = "set" + Character.toUpperCase(firstLetter)
                        + pty.substring(1);
            } else {
                setterName = "set" + pty;
            }

            Method setter = null;
            Object setterParameter = null;
            Method method;
            for (int i = 0; (setter == null)
                    && (i < o.getClass().getDeclaredMethods().length); i++) {
                method = o.getClass().getDeclaredMethods()[i];

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
                setter.invoke(o, setterParameter);
            }
        }
    }

    /**
     * Returns true if the given name is a Java reserved word.
     * 
     * @param name
     *            The name to test.
     * @return True if the given name is a Java reserved word.
     */
    public static boolean isReservedWord(String name) {
        return reservedWords.contains(name);
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
        if (property.getType() != null) {
            invokeSetter(entity, property.getNormalizedName(),
                    TypeUtils.fromEdm(propertyValue, property.getType()
                            .getName()));
        }

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
