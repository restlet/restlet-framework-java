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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a Web API representation.
 * 
 * @author Cyprien Quilici
 */
public class Representation {

    /** Textual description of this representation. */
    private String description;

    /** Reference to its parent type if any. */
    private String extendedType;

    /** Name of the representation. */
    private String name;

    /** List of this representation's properties. */
    private List<Property> properties = new ArrayList<>();

    /** Indicates if the representation is structured or not. */
    private boolean raw;

    /** The list of Sections this Representation belongs to. */
    private List<String> sections = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public String getExtendedType() {
        return extendedType;
    }

    public String getName() {
        return name;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<Property> getProperties() {
        return properties;
    }

    public Property getProperty(String name) {
        for (Property result : getProperties()) {
            if (name.equals(result.getName())) {
                return result;
            }
        }
        return null;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<String> getSections() {
        return sections;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExtendedType(String extendedType) {
        this.extendedType = extendedType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
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

    public void addSectionsToProperties(Contract contract) {
        Set<String> processedRepresentations = new HashSet<String>(Arrays.asList(name));
        for (Property property : properties) {
            Representation representation = contract.getRepresentation(property.getType());
            if (representation != null
                    && !processedRepresentations.contains(representation.getName())) {
                representation.addSections(sections);
                processedRepresentations.add(representation.getName());
                representation.addSectionsToProperties(processedRepresentations, contract);
            }
        }
    }

    private void addSectionsToProperties(Set<String> processedRepresentations, Contract contract) {
        for (Property property : properties) {
            Representation representation = contract.getRepresentation(property.getType());
            if (representation != null
                    && !processedRepresentations.contains(representation.getName())) {
                representation.addSections(sections);
                processedRepresentations.add(representation.getName());
                representation.addSectionsToProperties(processedRepresentations, contract);
            }
        }
    }
}
