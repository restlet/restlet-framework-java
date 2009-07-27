package com.google.gwt.emul.java.io;

import java.io.IOException;

/**
 * Emulate the UnknownHostException class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 */
@SuppressWarnings("serial")
public class UnknownHostException extends IOException {

    public UnknownHostException() {
        super();
    }

    public UnknownHostException(String message) {
        super(message);
    }

}
