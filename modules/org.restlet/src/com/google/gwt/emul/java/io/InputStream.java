package com.google.gwt.emul.java.io;

/**
 * Emulate the InputStream class, especially for the GWT module.
 * 
 * @author Jerome Louvel
 */
public class InputStream {
    /** The text to stream. */
    private String text;

    public InputStream() {
        super();
    }

    public InputStream(String text) {
        super();
        this.text = text;
    }

    public int available() {
        if (text != null) {
            return text.length();
        }

        return 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void close() {

    }
}
