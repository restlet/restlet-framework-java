package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ScopeDeclaration {

    private String description;

    private String scope;

    public String getDescription() {
        return description;
    }

    public String getScope() {
        return scope;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
