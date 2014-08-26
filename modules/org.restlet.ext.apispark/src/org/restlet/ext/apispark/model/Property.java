/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Cyprien Quilici
 */
public class Property {

    // TODO review comment
    /**
     * Default value if this property is of a primitive type<br>
     * Note: need to check casts for non-String primitive types
     */
    private String defaultValue;

    /** Textual description of this property. */
    private String description;

    // TODO review comment
    /**
     * A list of possible values for this property if it has a limited number of
     * possible values.
     */
    private List<String> enumeration;

    // TODO review comment
    /**
     * Maximum value of this property if it is a number Note: check casts
     */
    private String max;

    // TODO review comment
    /** Maximum number of occurences of the items of this property. */
    private Integer maxOccurs;

    // TODO review comment
    /**
     * Minimum value of this property if it is a number Note: check casts
     */
    private String min;

    // TODO review comment
    /** Minimum number of occurences of the items of this property. */
    private Integer minOccurs;

    /** Name of this property. */
    private String name;

    /**
     * Type of this property, either a primitive type or a reference to a
     * representation.
     */
    private String type;

    // TODO review comment
    /**
     * If maxOccurs > 1, indicates whether each item in this property is
     * supposed to be unique or not
     */
    private boolean uniqueItems;

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getEnumeration() {
        if (enumeration == null) {
            enumeration = new ArrayList<String>();
        }
        return enumeration;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }
}
