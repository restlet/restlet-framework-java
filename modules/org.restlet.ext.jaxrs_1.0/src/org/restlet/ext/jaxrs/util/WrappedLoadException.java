package org.restlet.ext.jaxrs.util;

/**
 * RuntimeException indicating, that something could not be loaded.
 * @see #getClassName()
 * @see #getCause()
 * 
 * @author Stephan Koops
 */
public class WrappedLoadException extends RuntimeException
{
    private static final long serialVersionUID = 8448320871361699569L;

    /**
     * Creates a new WrappedClassLoadException
     * @param message
     * @param exc
     */
    public WrappedLoadException(String message, Throwable exc)
    {
        super(message, exc);
    }
}