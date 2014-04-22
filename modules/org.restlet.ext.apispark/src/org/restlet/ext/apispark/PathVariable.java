package org.restlet.ext.apispark;

public class PathVariable {

	/** Indicates whether you can provide a list of values or just a single one. */
	private boolean array;

	/** Textual description of this variable. */
	private String description;

	/** Name of this variable. */
	private String name;

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}
}
