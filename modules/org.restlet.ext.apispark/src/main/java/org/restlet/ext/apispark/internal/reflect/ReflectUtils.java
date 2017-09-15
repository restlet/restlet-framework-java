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

package org.restlet.ext.apispark.internal.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.restlet.ext.apispark.internal.introspection.util.UnsupportedTypeException;

/**
 * Handles Java reflection operations.
 * 
 * @author Thierry Boileau
 */
public class ReflectUtils {

    @SuppressWarnings("rawtypes")
    public static Field[] getAllDeclaredFields(Class type) {
        List<Field> fields = new ArrayList<Field>();
        Class currentType = type;

        while (currentType != null) {
            Field[] currentFields = currentType.getDeclaredFields();
            Collections.addAll(fields, currentFields);
            currentType = currentType.getSuperclass();
            if (currentType != null && currentType.equals(Object.class)) {
                currentType = null;
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * Extracts the first segment of a path. Will retrieve "/pet" from
     * "/pet/{petId}" for example.
     * 
     * @param path
     *            The path of which the segment will be extracted.
     * @return The first segment of the given path.
     */
    public static String getFirstSegment(String path) {
        String segment = null;
        if (path != null) {
            int start = (path.startsWith("/")) ? 1 : 0;
            int index = path.indexOf("/", start);
            if (index != -1) {
                segment = "/" + path.substring(start, index);
            } else {
                segment = "/" + path.substring(start);
            }
        }
        return segment;
    }

    public static Class<?> getComponentClass(java.lang.reflect.Type type) {
        if (type instanceof Class<?>) {
            Class<?> c = (Class<?>) type;
            if (c.isArray()) {
                return c.getComponentType();
            } else if (Collection.class.isAssignableFrom(c)) {
                // Simple class that extends Collection<E>. Should inspect
                // superclass.
                return getComponentClass(c.getGenericSuperclass());
            } else {
                return c;
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            return getComponentClass(gat.getGenericComponentType());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            if (t.getActualTypeArguments().length != 1) {
                throw new UnsupportedTypeException("Type " + type + " is a generic type with zero or several arguments. This is not supported.");
            }
            if (t.getActualTypeArguments()[0] instanceof TypeVariable) {
                    throw new UnsupportedTypeException("Type " + type + " is a generic type with unkwnown type. This is not supported.");
            }
            return getComponentClass(t.getActualTypeArguments()[0]);
        }
        return (type != null) ? type.getClass() : null;
    }

    /**
     * TODO: need Javadocs
     * 
     * @param clazz
     * @return
     */
    public static boolean isJdkClass(Class<?> clazz) {
        if (clazz != null) {
            if (clazz.isPrimitive()) {
                return true;
            } else if (clazz.getPackage() != null) {
                return (clazz.getPackage().getName().startsWith("java.") || clazz
                        .getPackage().getName().startsWith("javax."));

            }
        }
        return false;
    }

    /**
     * TODO: need Javadocs
     * 
     * @param type
     * @return
     */
    public static boolean isListType(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    /**
     * Returns a new instance of classname and check that it's assignable from
     * expected class
     * 
     * @param className
     *            The class Name
     * @param instanceClazz
     *            The expected class
     * @param <T>
     *            The expected class
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className,
            Class<? extends T> instanceClazz) {
        if (className == null) {
            return null;
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (instanceClazz.isAssignableFrom(clazz)) {
                return (T) clazz.getConstructor().newInstance();
            } else {
                throw new RuntimeException(className
                        + " does not seem to be a valid subclass of "
                        + instanceClazz.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot locate the class " + className
                    + " in the classpath.", e);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate the class. "
                    + "Check that the class has an empty constructor.", e);
        }
    }

    /**
     * Generates the name of the given parameter's type.
     * 
     * @param parameterType
     *            the Java parameter's type.
     * @param genericParameterType
     *            I'ts
     * @param parameterizedType
     * @return
     */
    public String buildParameterTypeName(Class<?> parameterType,
            java.lang.reflect.Type genericParameterType,
            ParameterizedType parameterizedType) {
        java.lang.reflect.Type type;
        if (parameterizedType != null) {
            type = parameterizedType.getActualTypeArguments()[0];
        } else {
            type = null;
        }
        StringBuilder sb = new StringBuilder();
        if (type == null) {
            buildTypeName(parameterType, sb);
        } else {
            buildTypeName(genericParameterType, sb);
        }
        return sb.toString();
    }

    /**
     * Generates the name of the given type into the given StringBuilder.
     * 
     * @param type
     *            The type.
     * @param sb
     *            The stringBuilder to complete.
     */
    private void buildTypeName(java.lang.reflect.Type type, StringBuilder sb) {
        if (type instanceof Class<?>) {
            if (((Class<?>) type).isArray()) {
                buildTypeName(((Class<?>) type).getComponentType(), sb);
                sb.append("[]");
            } else {
                sb.append(((Class<?>) type).getName());
            }
        } else if (type instanceof GenericArrayType) {
            buildTypeName(((GenericArrayType) type).getGenericComponentType(),
                    sb);
            sb.append("[]");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            buildTypeName(t.getRawType(), sb);
            sb.append("<");
            if (t.getActualTypeArguments().length >= 1) {
                buildTypeName(t.getActualTypeArguments()[0], sb);
                for (int i = 1; i < t.getActualTypeArguments().length; i++) {
                    sb.append(", ");
                    buildTypeName(t.getActualTypeArguments()[i], sb);
                }
            }

            sb.append(">");
        } else {
            sb.append(type.toString());
        }
    }

}
