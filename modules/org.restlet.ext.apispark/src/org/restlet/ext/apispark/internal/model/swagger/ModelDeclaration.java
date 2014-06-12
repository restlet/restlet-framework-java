package org.restlet.ext.apispark.internal.model.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ModelDeclaration {
    private String description;

    private String id;

    private Map<String, TypePropertyDeclaration> properties;

    private List<String> required;

    private List<String> subTypes;

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public Map<String, TypePropertyDeclaration> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, TypePropertyDeclaration>();
        }
        return properties;
    }

    public List<String> getRequired() {
        if (required == null) {
            required = new ArrayList<String>();
        }
        return required;
    }

    public List<String> getSubTypes() {
        if (subTypes == null) {
            subTypes = new ArrayList<String>();
        }
        return subTypes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProperties(Map<String, TypePropertyDeclaration> properties) {
        this.properties = properties;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public void setSubTypes(List<String> subTypes) {
        this.subTypes = subTypes;
    }
}
