package org.restlet.ext.apispark.internal.model;

import java.util.*;

/**
 * Created by manu on 13/10/2014.
 */
public abstract class Types {


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
        primitiveTypesByClass.put(String.class, "string"); //others types could be considered as string
        primitiveTypesByClass.put(Date.class, "date"); //others types could be considered as date
    }

    private static final List<String> primitivesTypes = Arrays.asList(
            "byte", "short", "integer", "long", "float",
            "double", "boolean", "double", "string", "date",
            "file"
    );

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
        if(Date.class.isAssignableFrom(type)) {
            return "date";
        }
        return type.getName();
    }


    public static boolean isPrimitiveType(Class<?> type) {
        return (
                primitiveTypesByClass.get(type) != null ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type));
    }

    public static boolean isPrimitiveType(String typename) {
        return primitivesTypes.contains(typename);
    }
}
