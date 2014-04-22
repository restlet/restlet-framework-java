package org.restlet.ext.apispark;

import java.util.List;

public class Api {
	
	/**
	 * Name of the API
	 */
	private String name;
	
	/**
	 * Textual description of the API
	 */
	private String description;
	
	/**
	 * Representations available with this API
	 * Note: their "name" is used as a reference further in 
	 * this description
	 */
	private List<Representation> Representations;
	
	/**
	 * Resources provided by the API
	 */
	private List<Resource> resources;

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

	public List<Representation> getRepresentations() {
		return Representations;
	}

	public void setRepresentations(List<Representation> representations) {
		Representations = representations;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
}
