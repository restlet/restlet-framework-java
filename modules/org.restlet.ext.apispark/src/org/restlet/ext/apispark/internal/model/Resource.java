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

package org.restlet.ext.apispark.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a Web API resource
 * 
 * @author Cyprien Quilici
 */
public class Resource {

    /** Authentication protocol used for this resource */
    private String authenticationProtocol;

    /** Textual description of this resource */
    private String description;

    /** Name of this resource */
    private String name;

    /** List of the APIs this resource provides */
    private List<Operation> operations = new ArrayList<>();

    /** The variables you must provide for this operation. */
    private List<PathVariable> pathVariables = new ArrayList<>();

    /** Relative path from the endpoint to this resource */
    private String resourcePath;

    /** The list of Sections this Resource belongs to */
    private List<String> sections = new ArrayList<>();

    public String getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Operation getOperation(String name) {
        for (Operation result : getOperations()) {
            if (name.equals(result.getName())) {
                return result;
            }
        }
        return null;
    }

    public void addPathVariable(PathVariable pathVariable) {
        if (getPathVariable(pathVariable.getName()) == null) {
            pathVariables.add(pathVariable);
        }
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Operation> getOperations() {
        return operations;
    }

    public PathVariable getPathVariable(String name) {
        for (PathVariable result : getPathVariables()) {
            if (name.equals(result.getName())) {
                return result;
            }
        }
        return null;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<PathVariable> getPathVariables() {
        return pathVariables;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<String> getSections() {
        return sections;
    }

    public void setAuthenticationProtocol(String authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
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

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public void addSection(String section) {
        if (!this.sections.contains(section)) {
            this.sections.add(section);
        }
    }

    public void addSections(Collection<String> sections) {
        if (sections == null) {
            return;
        }

        for (String section : sections) {
            addSection(section);
        }
    }
}
