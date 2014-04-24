package org.restlet.ext.apispark;

import java.util.List;

public class Parameter {

	/**
	 * Name of the parameter
	 */
	private String name;
	
	/**
	 * Textual description of this parameter
	 */
	private String description;
	
	/**
	 * Default value of the parameter
	 */
	private String defaultValue;
	
	/**
	 * List of possible values of the parameter if there
	 * is a limited number of possible values for it
	 */
	private List<String> possibleValues;
	
	/**
	 * Indicates whether the parameter is mandatory or not
	 */
	private boolean required;
	
	/**
	 * Indicates whether you can provide multiple values
	 * for this parameter or not
	 */
	private boolean allowMultiple;
	
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
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isAllowMultiple() {
		return allowMultiple;
	}
	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}
}
