package org.restlet.ext.apispark.internal.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by manu on 13/10/2014.
 */
public abstract class Types {


    private static Map<Class<?>, String> primitiveTypes;
    static {
        primitiveTypes = new HashMap<Class<?>, String>();
        primitiveTypes.put(Byte.TYPE, "byte");
        primitiveTypes.put(Byte.class, "byte");
        primitiveTypes.put(Short.TYPE, "short");
        primitiveTypes.put(Short.class, "short");
        primitiveTypes.put(Integer.TYPE, "integer");
        primitiveTypes.put(Integer.class, "integer");
        primitiveTypes.put(Long.TYPE, "long");
        primitiveTypes.put(Long.class, "long");
        primitiveTypes.put(Float.TYPE, "float");
        primitiveTypes.put(Float.class, "float");
        primitiveTypes.put(Double.TYPE, "double");
        primitiveTypes.put(Double.class, "double");
        primitiveTypes.put(Boolean.TYPE, "boolean");
        primitiveTypes.put(Boolean.class, "boolean");
        primitiveTypes.put(Double.TYPE, "double");
        primitiveTypes.put(Double.class, "double");
        primitiveTypes.put(String.class, "string"); //others types could be considered as string
        primitiveTypes.put(Date.class, "date"); //others types could be considered as date
    }

    /**
     * Returns simple type name for primitive type or full name otherwise
     */
    public static String convertPrimtiveType(Class<?> type) {
        String primitiveName = primitiveTypes.get(type);
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
                primitiveTypes.get(type) != null ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type));
    }
}
