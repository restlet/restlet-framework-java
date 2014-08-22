package org.restlet.ext.swagger.internal.model.swagger;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ResourceOperationParameterDeclaration {
    public static final String PARAM_TYPE_PATH = "path";

    public static final String PARAM_TYPE_QUERY = "query";

    public static final String PARAM_TYPE_BODY = "body";

    public static final String PARAM_TYPE_HEADER = "header";

    public static final String PARAM_TYPE_FORM = "form";

    private String paramType;

    private String name;

    private String description;

    private String type;

    private ItemsDeclaration items;

    private String format;

    private boolean required;

    private String minimum;

    private String maximum;

    private String defaultValue;

    @JsonProperty("enum")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> enum_;

    private boolean allowMultiple;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getEnum_() {
        if (enum_ == null) {
            enum_ = new ArrayList<String>();
        }
        return enum_;
    }

    public void setEnum_(List<String> enum_) {
        this.enum_ = enum_;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public ItemsDeclaration getItems() {
        return items;
    }

    public void setItems(ItemsDeclaration items) {
        this.items = items;
    }
}
