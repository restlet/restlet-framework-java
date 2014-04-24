package org.restlet.ext.apispark;

import java.util.List;

public class Representation {

    /** Textual description of this representation. */
    private String description;

    /** Name of the representation. */
    private String name;

    /** Reference to its parent type if any. */
    private String parentType;

    /** List of this representation's properties. */
    private List<Property> properties;

    /** Indicates if the representation is structured or not. */
    private boolean raw;
    
    /** List of variants available for this representation. */
    private List<Variant> variants;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getParentType() {
        return parentType;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }
}
