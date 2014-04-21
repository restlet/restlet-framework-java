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
	private String content;
	
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
