package org.restlet.ext.apispark;

import java.util.List;

public class Representation {

	/**
	 * Name of the representation
	 */
	private String name;
	
	/**
	 * Textual description of this representation
	 */
	private String description;
	
	/**
	 * Reference to its parent type if any
	 */
	private String parentType;
	
	/**
	 * List of variants available for this representation
	 */
	private List<Variant> variants;
	
	/**
	 * List of this representation's properties
	 */
	private List<Property> properties;
	
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
	public String getParentType() {
		return parentType;
	}
	public void setParentType(String parentType) {
		this.parentType = parentType;
	}
	public List<Variant> getVariants() {
		return variants;
	}
	public void setVariants(List<Variant> variants) {
		this.variants = variants;
	}
	public List<Property> getProperties() {
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
}
