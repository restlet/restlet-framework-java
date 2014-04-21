package org.restlet.ext.apispark;

import java.util.List;

public class SubResource {

	/**
	 * Relative path to this sub-resource from its parent
	 * resource
	 */
	private String path;
	
	/**
	 * Textual description of this sub-resource
	 */
	private String description;
	
	/**
	 * List of operations available on this sub-resource
	 */
	private List<Operation> operations;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Operation> getOperations() {
		return operations;
	}
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}
}
