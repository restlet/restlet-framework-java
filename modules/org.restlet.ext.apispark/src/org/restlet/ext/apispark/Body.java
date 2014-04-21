package org.restlet.ext.apispark;

public class Body {

	/**
	 * Reference of the representation in the body 
	 * of the message
	 */
	private String type;
	
	/**
	 * Indicates whether you should provide an array
	 * of [type] or just one [type]
	 */
	private boolean array;
	
	public String getRepresentation() {
		return type;
	}
	public void setRepresentation(String representation) {
		this.type = representation;
	}
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
}
