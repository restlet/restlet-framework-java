package com.google.gwt.emul.java.io;

import java.io.IOException;

/**
 * Emulate the UnsupportedEncodingException class, especially for the GWT
 * module.
 * 
 * @author Thierry Boileau
 */
@SuppressWarnings("serial")
public class UnsupportedEncodingException extends IOException {

    public UnsupportedEncodingException() {
        super();
    }

    public UnsupportedEncodingException(String message) {
        super(message);
    }

}
