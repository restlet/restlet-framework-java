package org.restlet.ext.apispark;

import java.util.List;

public class Operation {

	/**
	 * HTTP method for this operation
	 */
	private Method method;
	
	/**
	 * Textual description of this operation
	 */
	private String description;
	
	/**
	 * Unique name for this operation
	 * Note: will be used for client SDK generation in
	 * the future
	 */
	private String name;
	
	/**
	 * Representation retrieved by this operation if any
	 */
	private Body inRepresentation;
	
	/**
	 * Representation to send in the body of your request
	 * for this operation if any
	 */
	private Body outRepresentation;
	
	/**
	 * Query parameters available for this operation
	 */
	private List<Parameter> queryParameters;
	
	/**
	 * Headers to use for this operation
	 */
	private List<Parameter> headers;
	
	/**
	 * Path variables you must provide for this operation
	 */
	private List<PathVariable> pathVariables;
	
	/**
	 * Possible response messages you could encounter
	 */
	private List<Response> responses;
	
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Body getInRepresentation() {
		return inRepresentation;
	}
	public void setInRepresentation(Body inRepresentation) {
		this.inRepresentation = inRepresentation;
	}
	public Body getOutRepresentation() {
		return outRepresentation;
	}
	public void setOutRepresentation(Body outRepresentation) {
		this.outRepresentation = outRepresentation;
	}
	public List<Parameter> getQueryParameters() {
		return queryParameters;
	}
	public void setQueryParameters(List<Parameter> queryParameters) {
		this.queryParameters = queryParameters;
	}
	public List<Parameter> getHeaders() {
		return headers;
	}
	public void setHeaders(List<Parameter> headers) {
		this.headers = headers;
	}
	public List<PathVariable> getPathVariables() {
		return pathVariables;
	}
	public void setPathVariables(List<PathVariable> pathVariables) {
		this.pathVariables = pathVariables;
	}
	public List<Response> getResponses() {
		return responses;
	}
	public void setResponses(List<Response> responses) {
		this.responses = responses;
	}
}
