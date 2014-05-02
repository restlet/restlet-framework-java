package org.restlet.ext.apispark;

import java.util.List;

public class Parameter {

    /**
     * Indicates whether you can provide multiple values for this parameter or
     * not.
     */
    private boolean allowMultiple;

    /** Default value of the parameter. */
    private String defaultValue;

    /** Textual description of this parameter. */
    private String description;

    /** Name of the parameter. */
    private String name;

    /**
     * List of possible values of the parameter if there is a limited number of
     * possible values for it.
     */
    private List<String> possibleValues;

    /** Indicates whether the parameter is mandatory or not. */
    private boolean required;

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public boolean isRequired() {
        return required;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
