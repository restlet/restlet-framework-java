package org.restlet.ext.apispark;

public class Variant {

	/**
	 * Textual description of this variant
	 */
	private String description;
	
	/**
	 * Must be a MIME type 
	 */
	private String dataType;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
