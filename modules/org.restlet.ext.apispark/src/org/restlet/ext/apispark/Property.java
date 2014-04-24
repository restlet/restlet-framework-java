package org.restlet.ext.apispark;

import java.util.List;

public class Property {

    /**
     * Type of this property, either a primitive type or a reference to a
     * representation.
     */
    private String dataType;

    // TODO review comment
    /**
     * Default value if this property is of a primitive type<br>
     * Note: need to check casts for non-String primitive types
     */
    private String defaultValue;

    /** Textual description of this property. */
    private String description;

    // TODO review comment
    /**
     * Maximum value of this property if it is a number Note: check casts
     */
    private String max;

    // TODO review comment
    /** Maximum number of occurences of the items of this property. */
    private Integer maxOccurs;

    // TODO review comment
    /**
     * Minimum value of this property if it is a number Note: check casts
     */
    private String min;

    // TODO review comment
    /** Minimum number of occurences of the items of this property. */
    private Integer minOccurs;

    /** Name of this property. */
    private String name;

    // TODO review comment
    /**
     * A list of possible values for this property if it has a limited number of
     * possible values.
     */
    private List<String> possibleValues;

    // TODO review comment
    /**
     * If maxOccurs > 1, indicates whether each item in this property is
     * supposed to be unique or not
     */
    private boolean uniqueItems;

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getMax() {
        return max;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public String getMin() {
        return min;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public String getName() {
        return name;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public String getType() {
        return dataType;
    }

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public void setType(String type) {
        this.dataType = type;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }
}
