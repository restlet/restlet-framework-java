package org.restlet.ext.odata;

import java.util.Date;

/**
 * Handles the conversion from Edm type to Java class.
 */
public class JavaTypeHandler {
    
    /**
     * Returns the corresponding Java class or scalar type.
     * 
     * @param edmTypeName
     *            The type name.
     * @return The corresponding Java class or scalar type.
     */
    public Class<?> toJavaClass(String edmTypeName) {
        Class<?> result = Object.class;
        if (edmTypeName.endsWith("Binary")) {
            result = byte[].class;
        } else if (edmTypeName.endsWith("Boolean")) {
            result = Boolean.class;
        } else if (edmTypeName.endsWith("DateTime")) {
            result = Date.class;
        } else if (edmTypeName.endsWith("DateTimeOffset")) {
            result = Date.class;
        } else if (edmTypeName.endsWith("Time")) {
            result = Long.class;
        } else if (edmTypeName.endsWith("Decimal")) {
            result = Double.class;
        } else if (edmTypeName.endsWith("Single")) {
            result = Double.class;
        } else if (edmTypeName.endsWith("Double")) {
            result = Double.class;
        } else if (edmTypeName.endsWith("Guid")) {
            result = String.class;
        } else if (edmTypeName.endsWith("Int16")) {
            result = Short.class;
        } else if (edmTypeName.endsWith("Int32")) {
            result = Integer.class;
        } else if (edmTypeName.endsWith("Int64")) {
            result = Long.class;
        } else if (edmTypeName.endsWith("Byte")) {
            result = Byte.class;
        } else if (edmTypeName.endsWith("String")) {
            result = String.class;
        }

        return result;
    }
    /**
     * Returns the name of the corresponding Java class or scalar type.
     * 
     * @param edmTypeName
     *            The type name.
     * @return The name of the corresponding Java class or scalar type.
     */
    public String toJavaTypeName(String edmTypeName) {
        String result = "Object";
        if (edmTypeName.endsWith("Binary")) {
            result = "byte[]";
        } else if (edmTypeName.endsWith("Boolean")) {
            result = "boolean";
        } else if (edmTypeName.endsWith("DateTime")) {
            result = "Date";
        } else if (edmTypeName.endsWith("DateTimeOffset")) {
            result = "Date";
        } else if (edmTypeName.endsWith("Time")) {
            result = "long";
        } else if (edmTypeName.endsWith("Decimal")) {
            result = "double";
        } else if (edmTypeName.endsWith("Single")) {
            result = "double";
        } else if (edmTypeName.endsWith("Double")) {
            result = "double";
        } else if (edmTypeName.endsWith("Guid")) {
            result = "String";
        } else if (edmTypeName.endsWith("Int16")) {
            result = "short";
        } else if (edmTypeName.endsWith("Int32")) {
            result = "int";
        } else if (edmTypeName.endsWith("Int64")) {
            result = "long";
        } else if (edmTypeName.endsWith("Byte")) {
            result = "byte";
        } else if (edmTypeName.endsWith("String")) {
            result = "String";
        }

        return result;
    }

}
