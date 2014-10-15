package org.restlet.ext.apispark.internal.conversion;

@SuppressWarnings("serial")
public class TranslationException extends Exception {

    private String type;

    public TranslationException(String type, String message) {
        super(message);
        this.type = type;
    }

    public TranslationException(String type, String message, Throwable throwable) {
        super(message, throwable);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
