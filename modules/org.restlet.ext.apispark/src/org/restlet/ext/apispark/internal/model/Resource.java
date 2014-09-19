/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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
import java.util.List;

import org.restlet.data.ChallengeScheme;

/**
 * Represents a Web API resource
 * 
 * @author Cyprien Quilici
 */
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
    
    /** Authentication protocol used for this resource */
    private ChallengeScheme authenticationProtocol;

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

    public List<Operation> getOperations() {
        if (operations == null) {
            operations = new ArrayList<Operation>();
        }
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

    public List<PathVariable> getPathVariables() {
        if (pathVariables == null) {
            pathVariables = new ArrayList<PathVariable>();
        }
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

    public ChallengeScheme getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public void setAuthenticationProtocol(ChallengeScheme authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }
}
