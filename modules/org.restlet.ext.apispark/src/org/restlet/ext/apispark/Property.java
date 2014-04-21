package org.restlet.ext.apispark;

import java.util.List;

public class Property {

	/**
	 * Name ot this property
	 */
	private String name;
	
	/**
	 * Textual description of this property
	 */
	private String description;
	
	/**
	 * Type of this property, either a primitive type or
	 * a reference to a representation
	 */
	private String type;
	
	/**
	 * Default value if this property is of a primitive type
	 * Note: need to check casts for non-String primitive
	 * types
	 */
	private String defaultValue;
	
	/**
	 * A list of possible values for this property if it has 
	 * a limited number of possible values
	 */
	private List<String> possibleValues;
	
	/**
	 * Minimum value of this property if it is a number
	 * Note: check casts
	 */
	private String min;
	
	/**
	 * Maximum value of this property if it is a number
	 * Note: check casts
	 */
	private String max;
	
	/**
	 * If maxOccurs > 1, indicates whether each item in
	 * this property is supposed to be unique or not
	 */
	private boolean uniqueItems;
	
	/**
	 * Minimum number of occurences of the items of this 
	 * property
	 */
	private Integer minOccurs;
	
	/**
	 * Maximum number of occurences of the items of this 
	 * property
	 */
	private Integer maxOccurs;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public List<String> getPossibleValues() {
		return possibleValues;
	}
	public void setPossibleValues(List<String> possibleValues) {
		this.possibleValues = possibleValues;
	}
	public String getMin() {
		return min;
	}
	public void setMin(String min) {
		this.min = min;
	}
	public String getMax() {
		return max;
	}
	public void setMax(String max) {
		this.max = max;
	}
	public boolean isUniqueItems() {
		return uniqueItems;
	}
	public void setUniqueItems(boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}
	public Integer getMinOccurs() {
		return minOccurs;
	}
	public void setMinOccurs(Integer minOccurs) {
		this.minOccurs = minOccurs;
	}
	public Integer getMaxOccurs() {
		return maxOccurs;
	}
	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
}
