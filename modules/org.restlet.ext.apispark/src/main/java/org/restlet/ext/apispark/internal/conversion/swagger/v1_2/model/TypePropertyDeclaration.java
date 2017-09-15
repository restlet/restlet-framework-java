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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TypePropertyDeclaration {
    private String description;

    @JsonProperty("enum")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> enum_;

    private String format;

    private ItemsDeclaration items;

    private String maximum;

    private String minimum;

    @JsonProperty("$ref")
    private String ref;

    private String type;

    private boolean uniqueItems;

    public String getDescription() {
        return description;
    }

    public List<String> getEnum_() {
        if (enum_ == null) {
            enum_ = new ArrayList<String>();
        }
        return enum_;
    }

    public String getFormat() {
        return format;
    }

    public ItemsDeclaration getItems() {
        return items;
    }

    public String getMaximum() {
        return maximum;
    }

    public String getMinimum() {
        return minimum;
    }

    public String getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnum_(List<String> enum_) {
        this.enum_ = enum_;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setItems(ItemsDeclaration items) {
        this.items = items;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

}
