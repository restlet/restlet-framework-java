package org.restlet.ext.apispark.internal.conversion;

@SuppressWarnings("serial")
public class SwaggerConversionException extends Exception {

    private String type;

    private String message;

    public SwaggerConversionException(String type, String message) {
        super();
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
