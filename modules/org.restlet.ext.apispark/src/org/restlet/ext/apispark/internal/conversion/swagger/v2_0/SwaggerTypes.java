package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;

/**
 * Created by manu on 15/10/2014.
 */
public class SwaggerTypes {

    /**
     * Converts Java types to Swagger types
     *
     * @param dataType
     *            The Java type
     * @return The corresponding Swagger type
     */
    public static SwaggerTypeFormat toSwaggerType(String dataType) {
        if ("string".equals(dataType)) {
            return new SwaggerTypeFormat("string");
        } else if ("byte".equals(dataType)) {
            return new SwaggerTypeFormat("string", "byte");
        } else if ("short".equals(dataType)) {
            return new SwaggerTypeFormat("integer", "int32");
        } else if ("integer".equals(dataType)) {
            return new SwaggerTypeFormat("integer", "int32");
        } else if ("long".equals(dataType)) {
            return new SwaggerTypeFormat("integer", "int64");
        } else if ("float".equals(dataType)) {
            return new SwaggerTypeFormat("number", "float");
        } else if ("double".equals(dataType)) {
            return new SwaggerTypeFormat("number", "double");
        } else if ("boolean".equals(dataType)) {
            return new SwaggerTypeFormat("boolean");
        } else if ("date".equals(dataType)) {
            return new SwaggerTypeFormat("string", "date");
        } else {
            return new SwaggerTypeFormat(dataType);
        }
    }

    /**
     * Converts Swagger types to Java types
     *
     * @param dataType
     *            The Swagger type
     * @return The corresponding Java type
     */
    public static String toDefinitionType(SwaggerTypeFormat dataType) {
        if ("string".equals(dataType.getType())) {
            if ("date".equals(dataType.getFormat())) {
                return "date";
            } else if ("byte".equals(dataType.getFormat())) {
                return "byte";
            } else {
                return "string";
            }
        } else if ("integer".equals(dataType.getType())) {
            if ("int64".equals(dataType.getFormat())) {
                return "long";
            } else {
                return "integer";
            }
        } else if ("number".equals(dataType.getType())) {
            if ("float".equals(dataType.getFormat())) {
                return "Float";
            } else {
                return "double";
            }
        } else if ("boolean".equals(dataType.getType())) {
            return "boolean";
        } else {
            return dataType.getType();
        }
    }
}
