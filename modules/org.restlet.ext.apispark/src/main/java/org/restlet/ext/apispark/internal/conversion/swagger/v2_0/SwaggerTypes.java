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

package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;

import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;

/**
 * @author Manuel Boillod
 */
public class SwaggerTypes {

    /**
     * Converts Swagger primitive types to Java types. To be used only for query parameters,
     * headers and path variables.
     * 
     * @param dataType
     *            The Swagger type
     * @return The corresponding Java type
     */
    public static String toDefinitionPrimitiveType(SwaggerTypeFormat dataType) {
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
            if ("array".equals(dataType.getType())) {
                return dataType.getItems().getType();
            }
            return dataType.getType();
        }
    }
    
    /**
     * Returns the RWADef type corresponding to the given Swagger Property.
     * 
     * @param property
     *            The Swagger property.
     * @return
     *         The RWADef type.
     */
    public static String toDefinitionType(Property property) {
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            return refProperty.getSimpleRef();
        } else if (property instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) property;
            return toDefinitionType(arrayProperty.getItems());
        }
        
        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(property.getType(),
                property.getFormat());
        return SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat);
    }

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
}
