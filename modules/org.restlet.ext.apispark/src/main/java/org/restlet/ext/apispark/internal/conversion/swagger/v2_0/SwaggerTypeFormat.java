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

import com.wordnik.swagger.models.properties.Property;

/**
 * Internal class representing a Swagger type
 */
public class SwaggerTypeFormat {
    private String format;

    private String type;

    private Property items;

    public SwaggerTypeFormat(String type) {
        this(type, null, null);
    }

    public SwaggerTypeFormat(String type, String format) {
        this(type, format, null);
    }

    public SwaggerTypeFormat(String type, String format, Property items) {
        this.format = format;
        this.type = type;
        this.items = items;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public Property getItems() {
        return items;
    }
}