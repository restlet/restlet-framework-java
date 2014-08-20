package org.restlet.ext.swagger.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TypePropertyDeclaration {
    private String type;

    @JsonProperty("$ref")
    private String ref;

    private ItemsDeclaration items;

    private String description;

    private String format;

    private String minimum;

    private String maximum;

    private boolean uniqueItems;

    @JsonProperty("enum")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> enum_;

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

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public ItemsDeclaration getItems() {
        return items;
    }

    public void setItems(ItemsDeclaration items) {
        this.items = items;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

}
