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

/**
 * Represents a section of a Web API. A section is a logical part of the Web API
 * and should be used to tidy it.
 * 
 * @author Cyprien Quilici
 * 
 */
public class Section {

    /**
     * Name of the default section
     */
    public static final String DEFAULT = "default";

    /** Textual description of the API. */
    private String description;

    /** Name of the API. */
    private String name;

    /**
     * Representations available with this API Note: their "name" is used as a
     * reference further in this description.
     */
    private List<Representation> representations;

    /** Resources provided by the API. */
    private List<Resource> resources;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Representation> getRepresentations() {
        if (representations == null) {
            representations = new ArrayList<Representation>();
        }
        return representations;
    }

    public void setRepresentations(List<Representation> representations) {
        this.representations = representations;
    }

    public List<Resource> getResources() {
        if (resources == null) {
            resources = new ArrayList<Resource>();
        }
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Resource getResource(String path) {
        for (Resource result : getResources()) {
            if (path.equals(result.getResourcePath())) {
                return result;
            }
        }
        return null;
    }

    public Representation getRepresentation(String name) {
        for (Representation result : getRepresentations()) {
            if (name.equals(result.getName())) {
                return result;
            }
        }
        return null;
    }

}
