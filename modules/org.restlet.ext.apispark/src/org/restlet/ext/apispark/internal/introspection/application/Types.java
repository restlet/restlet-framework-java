package org.restlet.ext.apispark.internal.introspection.application;

import com.google.common.collect.Maps;
import org.restlet.ext.apispark.internal.introspection.application.TypeInfo;
import org.restlet.ext.apispark.internal.model.Representation;

import java.beans.BeanInfo;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Manuel Boillod
 */
public abstract class Types {

    /** TypeInfo cache. */
    private static final ConcurrentMap<TypeInfoKey, TypeInfo> cache = new ConcurrentHashMap<>();

    private static final List<String> primitivesTypes = Arrays.asList("byte",
            "short", "integer", "long", "float", "double", "boolean", "double",
            "string", "date", "file");

    private static final Map<Class<?>, String> primitiveTypesByClass;

    static {
        primitiveTypesByClass = new HashMap<Class<?>, String>();
        primitiveTypesByClass.put(Byte.TYPE, "byte");
        primitiveTypesByClass.put(Byte.class, "byte");
        primitiveTypesByClass.put(Short.TYPE, "short");
        primitiveTypesByClass.put(Short.class, "short");
        primitiveTypesByClass.put(Integer.TYPE, "integer");
        primitiveTypesByClass.put(Integer.class, "integer");
        primitiveTypesByClass.put(Long.TYPE, "long");
        primitiveTypesByClass.put(Long.class, "long");
        primitiveTypesByClass.put(Float.TYPE, "float");
        primitiveTypesByClass.put(Float.class, "float");
        primitiveTypesByClass.put(Double.TYPE, "double");
        primitiveTypesByClass.put(Double.class, "double");
        primitiveTypesByClass.put(Boolean.TYPE, "boolean");
        primitiveTypesByClass.put(Boolean.class, "boolean");
        primitiveTypesByClass.put(Double.TYPE, "double");
        primitiveTypesByClass.put(Double.class, "double");
        primitiveTypesByClass.put(String.class, "string"); // others types could
                                                           // be considered as
                                                           // string
        primitiveTypesByClass.put(Date.class, "date"); // others types could be
                                                       // considered as date
    }

    public static TypeInfo getTypeInfo(Class<?> clazz, Type type){
        TypeInfoKey key = new TypeInfoKey(clazz, type);
        TypeInfo typeInfo = cache.get(key);

        if (typeInfo == null) {
            typeInfo = new TypeInfo(clazz, type);
            cache.put(key, typeInfo);
        }
        return typeInfo;
    }

    /**
     * Returns simple type name for primitive type or full name otherwise
     */
    public static String convertPrimitiveType(Class<?> type) {
        String primitiveName = primitiveTypesByClass.get(type);
        if (primitiveName != null) {
            return primitiveName;
        }
        if (CharSequence.class.isAssignableFrom(type)) {
            return "string";
        }
        if (Date.class.isAssignableFrom(type)) {
            return "date";
        }
        if (Representation.class.isAssignableFrom(type) ||
                File.class.isAssignableFrom(type)) {
            return "file";
        }
        return type.getName();
    }

    public static boolean isPrimitiveType(Class<?> type) {
        return (primitiveTypesByClass.get(type) != null
                || CharSequence.class.isAssignableFrom(type) || Date.class
                    .isAssignableFrom(type));
    }

    public static boolean isPrimitiveType(String typename) {
        return primitivesTypes.contains(typename);
    }


    private static class TypeInfoKey {

        private final Class<?> clazz;
        private final Type type;

        public TypeInfoKey(Class<?> clazz, Type type) {
            this.clazz = clazz;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeInfoKey) {
                TypeInfoKey that = (TypeInfoKey) obj;
                return Objects.equals(this.clazz, that.clazz)
                        && Objects.equals(this.type, that.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, type);
        }
    }
}
