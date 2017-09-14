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

package org.restlet.ext.apispark.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a property of a Web API representation
 * 
 * @author Cyprien Quilici
 */
public class Property {

    /**
     * Default value if this property is of a primitive type<br>
     * Note: need to check casts for non-String primitive types
     */
    private String defaultValue;

    /** Textual description of this property. */
    private String description;

    /**
     * A list of possible values for this property if it has a limited number of
     * possible values.
     */
    private List<String> enumeration = new ArrayList<>();

    /**
     * An example of the property's value.
     */
    private String example;

    /**
     * Maximum value of this property if it is a number.<br>
     * Note: check casts.
     */
    private String max;

    /** Maximum number of occurences of the items of this property. */
    private Integer maxOccurs;

    /**
     * Minimum value of this property if it is a number.<br>
     * Note: check casts.
     */
    private String min;

    /** Minimum number of occurences of the items of this property. */
    private Integer minOccurs;

    /** Name of this property. */
    private String name;

    /** list of properties, in case of nested type. */
    private List<Property> properties = new ArrayList<>();

    /**
     * Type of this property, either a primitive type or a reference to a
     * representation.
     */
    private String type;

    /**
     * If maxOccurs > 1, indicates whether each item in this property is
     * supposed to be unique or not.
     */
    private boolean uniqueItems;

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<String> getEnumeration() {
        return enumeration;
    }

    public String getExample() {
        return example;
    }

    public String getMax() {
        return max;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public String getMin() {
        return min;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public String getName() {
        return name;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Property> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    @JsonIgnore
    public boolean isList() {
        if (this.maxOccurs == null) {
            return false;
        }
        return this.maxOccurs == -1 || this.maxOccurs > 1;
    }

    @JsonIgnore
    public boolean isRequired() {
        if (this.minOccurs == null) {
            return false;
        }
        return this.minOccurs == 1;
    }

    @JsonIgnore
    public void setList(boolean list) {
        this.maxOccurs = list ? -1 : 1;
    }

    @JsonIgnore
    public void setRequired(boolean required) {
        this.minOccurs = required ? 1 : 0;
    }
}
