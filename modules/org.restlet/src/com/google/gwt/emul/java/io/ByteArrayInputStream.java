package com.google.gwt.emul.java.io;

/**
 * Emulate the ByteArrayInputStream class, especially for the GWT module.
 * 
 * @author Jerome Louvel
 */
public class ByteArrayInputStream {
    /** The text to stream. */
    private String text;

    public ByteArrayInputStream() {
        super();
    }

    public ByteArrayInputStream(byte[] bytes) {
        super();
        this.text = new String(bytes);
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
