package org.restlet.ext.apispark.internal.introspection.util;

/**
 * @author Manuel Boillod
 */
public class UnsupportedTypeException extends RuntimeException {

    public UnsupportedTypeException() {
    }

    public UnsupportedTypeException(String message) {
        super(message);
    }

    public UnsupportedTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedTypeException(Throwable cause) {
        super(cause);
    }
}
