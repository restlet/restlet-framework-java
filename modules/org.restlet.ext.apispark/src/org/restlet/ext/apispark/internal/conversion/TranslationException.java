package org.restlet.ext.apispark.internal.conversion;

@SuppressWarnings("serial")
public class TranslationException extends Exception {

    private String message;

    private String type;

    public TranslationException(String type, String message) {
        super();
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

}
