package org.restlet.ext.apispark;

import org.restlet.data.Status;

public class Response {

    /** Custom content of the body if any. */
    private Body body;

    /** Status code of the response */
    private int code;

    /** Textual description of this response */
    private String description;

    /** Status message of the response. */
    private String message;

    /** Name of this response */
    private String name;

    /**
     * Constructor. The default status code is {@link Status#SUCCESS_OK}.
     */
    public Response() {
        setCode(Status.SUCCESS_OK.getCode());
        setMessage(Status.SUCCESS_OK.getDescription());
    }

    public Body getBody() {
        return body;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }
}
