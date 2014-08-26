package org.restlet.ext.apispark.internal.model.swagger;

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
