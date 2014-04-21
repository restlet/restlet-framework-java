package org.restlet.ext.apispark;

import java.util.List;

public class Api {
	
	/**
	 * Current version of the API
	 */
	private String version;
	
	/**
	 * Name of the API
	 */
	private String name;
	
	/**
	 * Textual description of the API
	 */
	private String description;
	
	/**
	 * E-mail of the person to contact for further information
	 * or user acces on the API
	 */
	private String contact;
	
	/**
	 * URL of the description of the license used by the API
	 */
	private String license;
	
	/**
	 * Base URL on which you can access the API
	 * Note: will enable multiple endpoints and protocols in 
	 * the future (use class Endpoint in a list)
	 */
	private String endpoint;
	
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
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
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
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
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
