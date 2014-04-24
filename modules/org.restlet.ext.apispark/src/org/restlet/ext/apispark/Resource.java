package org.restlet.ext.apispark;

import java.util.List;

public class Resource {

    /** Textual description of this resource */
    private String description;

    /** Name of this resource */
    private String name;

    /** List of the APIs this resource provides */
    private List<Operation> operations;

    /** The variables you must provide for this operation. */
    private List<PathVariable> pathVariables;

    /** Relative path from the endpoint to this resource */
    private String resourcePath;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public List<PathVariable> getPathVariables() {
        return pathVariables;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public void setPathVariables(List<PathVariable> pathVariables) {
        this.pathVariables = pathVariables;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
