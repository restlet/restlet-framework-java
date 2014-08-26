package org.restlet.ext.apispark.internal.model.swagger;

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
