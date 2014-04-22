package org.restlet.ext.apispark;

import java.util.List;

public class Contract {

	/** Textual description of the API. */
	private String description;

	/** Name of the API. */
	private String name;

	/**
	 * Representations available with this API Note: their "name" is used as a
	 * reference further in this description.
	 */
	private List<Representation> Representations;

	/** Resources provided by the API. */
	private List<Resource> resources;

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public List<Representation> getRepresentations() {
		return Representations;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRepresentations(List<Representation> representations) {
		Representations = representations;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
}
