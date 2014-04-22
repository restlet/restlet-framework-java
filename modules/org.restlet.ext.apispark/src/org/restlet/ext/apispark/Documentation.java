package org.restlet.ext.apispark;

public class Documentation {

	/**
	 * Current version of the API
	 */
	private String version;
	
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
	 * E-mail of the person to contact for further information
	 * or user acces on the API
	 */
	private String contact;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
}
