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

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ModelDeclaration {
    private String description;

    private String id;

    private Map<String, TypePropertyDeclaration> properties;

    private List<String> required;

    private List<String> subTypes;

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public Map<String, TypePropertyDeclaration> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<String, TypePropertyDeclaration>();
        }
        return properties;
    }

    public List<String> getRequired() {
        if (required == null) {
            required = new ArrayList<String>();
        }
        return required;
    }

    public List<String> getSubTypes() {
        if (subTypes == null) {
            subTypes = new ArrayList<String>();
        }
        return subTypes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProperties(Map<String, TypePropertyDeclaration> properties) {
        this.properties = properties;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public void setSubTypes(List<String> subTypes) {
        this.subTypes = subTypes;
    }
}
