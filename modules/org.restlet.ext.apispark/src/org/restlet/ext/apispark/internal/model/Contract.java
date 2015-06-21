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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the contract of a Web API. Contains the representations and
 * resources sorted in sections.
 * 
 * @author Cyprien Quilici
 */
public class Contract {

    /** Textual description of the API. */
    private String description;

    /** Name of the API. */
    private String name;

    /**
     * Representations available with this API Note: their "name" is used as a
     * reference further in this description.
     */
    private List<Representation> representations = new ArrayList<>();

    /** Resources provided by the API. */
    private List<Resource> resources = new ArrayList<>();

    /** Sections referenced by the API's Representations and Resources. */
    private List<Section> sections = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Representation getRepresentation(String name) {
        for (Representation result : getRepresentations()) {
            if (name.equals(result.getName())) {
                return result;
            }
        }
        return null;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Representation> getRepresentations() {
        return representations;
    }

    public Resource getResource(String path) {
        for (Resource result : getResources()) {
            if (path.equals(result.getResourcePath())) {
                return result;
            }
        }
        return null;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Resource> getResources() {
        return resources;
    }

    public Section getSection(String name) {
        for (Section section : sections) {
            if (name.equals(section.getName())) {
                return section;
            }
        }
        return null;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Section> getSections() {
        return sections;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepresentations(List<Representation> representations) {
        this.representations = representations;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
