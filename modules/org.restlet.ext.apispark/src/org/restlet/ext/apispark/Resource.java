package org.restlet.ext.apispark;

import java.util.List;

public class Resource {

	/**
	 * Relative path from the endpoint to this resource
	 */
	private String resourcePath;
	
	/**
	 * List of the APIs this resource provides
	 */
	private List<SubResource> subResources;
	
	/**
	 * Textual description of this resource
	 */
	private String description;
	
	public String getResourcePath() {
		return resourcePath;
	}
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	public List<SubResource> getSubResources() {
		return subResources;
	}
	public void setSubResources(List<SubResource> subResources) {
		this.subResources = subResources;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
