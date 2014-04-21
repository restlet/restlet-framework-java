package org.restlet.ext.apispark;

public class PathVariable {

	/**
	 * Name of this variable
	 */
	private String name;
	
	/**
	 * Textual description of this variable
	 */
	private String description;
	
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
}
