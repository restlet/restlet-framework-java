package org.restlet.ext.swagger.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceDeclaration {
    private String path;

    private String description;

    private List<ResourceOperationDeclaration> operations;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ResourceOperationDeclaration> getOperations() {
        if (operations == null) {
            operations = new ArrayList<ResourceOperationDeclaration>();
        }
        return operations;
    }

    public void setOperations(List<ResourceOperationDeclaration> operations) {
        this.operations = operations;
    }
}
