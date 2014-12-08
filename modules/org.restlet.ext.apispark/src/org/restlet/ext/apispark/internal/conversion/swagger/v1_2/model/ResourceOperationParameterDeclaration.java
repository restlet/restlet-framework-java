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
public class ResourceOperationParameterDeclaration {
    public static final String PARAM_TYPE_BODY = "body";

    public static final String PARAM_TYPE_FORM = "form";

    public static final String PARAM_TYPE_HEADER = "header";

    public static final String PARAM_TYPE_PATH = "path";

    public static final String PARAM_TYPE_QUERY = "query";

    private boolean allowMultiple;

    private String defaultValue;

    private String description;

    @JsonProperty("enum")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> enum_;

    private String format;

    private ItemsDeclaration items;

    private String maximum;

    private String minimum;

    private String name;

    private String paramType;

    private boolean required;

    private String type;

    public String getDefaultValue() {
        return defaultValue;
    }

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

    public String getName() {
        return name;
    }

    public String getParamType() {
        return paramType;
    }

    public String getType() {
        return type;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public boolean isRequired() {
        return required;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setType(String type) {
        this.type = type;
    }
}
