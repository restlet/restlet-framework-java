package org.restlet.ext.apispark;

import java.util.List;

public class Operation {

    /** Textual description of this operation. */
    private String description;

    /** Headers to use for this operation. */
    private List<Parameter> headers;

    /** Representation retrieved by this operation if any. */
    private Body inRepresentation;

    /** HTTP method for this operation. */
    private Method method;

    /**
     * Unique name for this operation<br>
     * Note: will be used for client SDK generation in the future.
     */
    private String name;

    /**
     * Representation to send in the body of your request for this operation if
     * any.
     */
    private Body outRepresentation;

    /** Query parameters available for this operation. */
    private List<Parameter> queryParameters;

    /** Possible response messages you could encounter. */
    private List<Response> responses;

    public String getDescription() {
        return description;
    }

    public List<Parameter> getHeaders() {
        return headers;
    }

    public Body getInRepresentation() {
        return inRepresentation;
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Body getOutRepresentation() {
        return outRepresentation;
    }

    public List<Parameter> getQueryParameters() {
        return queryParameters;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHeaders(List<Parameter> headers) {
        this.headers = headers;
    }

    public void setInRepresentation(Body inRepresentation) {
        this.inRepresentation = inRepresentation;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOutRepresentation(Body outRepresentation) {
        this.outRepresentation = outRepresentation;
    }

    public void setQueryParameters(List<Parameter> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }
}
