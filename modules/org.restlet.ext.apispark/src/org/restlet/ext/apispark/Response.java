package org.restlet.ext.apispark;

public class Response {

	/**
	 * HTTP code for the response
	 * See: http://fr.wikipedia.org/wiki/Liste_des_codes_HTTP
	 */
	private Integer code;
	
	/**
	 * Textual message associated with code in RCF
	 * See: http://fr.wikipedia.org/wiki/Liste_des_codes_HTTP
	 */
	private String message;
	
	/**
	 * Custom content of the body if any
	 */
	private Body body;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
}
