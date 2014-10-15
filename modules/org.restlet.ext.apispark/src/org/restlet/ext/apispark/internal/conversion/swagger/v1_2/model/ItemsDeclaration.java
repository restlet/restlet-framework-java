package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ItemsDeclaration {

    private String format;

    @JsonProperty("$ref")
    private String ref;

    private String type;

    public String getFormat() {
        return format;
    }

    public String getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setType(String type) {
        this.type = type;
    }
}
