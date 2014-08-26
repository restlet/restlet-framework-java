package org.restlet.ext.apispark.internal.model.swagger;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceDeclaration {
    private String description;

    private List<ResourceOperationDeclaration> operations;

    private String path;

    public String getDescription() {
        return description;
    }

    public List<ResourceOperationDeclaration> getOperations() {
        if (operations == null) {
            operations = new ArrayList<ResourceOperationDeclaration>();
        }
        return operations;
    }

    public String getPath() {
        return path;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperations(List<ResourceOperationDeclaration> operations) {
        this.operations = operations;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
