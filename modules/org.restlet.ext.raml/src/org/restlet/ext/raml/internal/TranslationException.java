package org.restlet.ext.raml.internal;

@SuppressWarnings("serial")
public class TranslationException extends Exception {

    private String type;

    private String message;

    public TranslationException(String type, String message) {
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
