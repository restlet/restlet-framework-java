package org.restlet.ext.jaxrs.util;

/**
 * RuntimeException indicating, that the class could not be loaded.
 * @see #getClassName()
 * @see #getCause()
 * 
 * @author Stephan Koops
 */
public class WrappedClassLoadException extends RuntimeException
{
    private static final long serialVersionUID = 8448320871361699569L;

    private String className;
    
    /**
     * Creates a new WrappedClassLoadException
     * @param className
     * @param exc
     */
    public WrappedClassLoadException(String className, Throwable exc)
    {
        super("Could not load class with name "+className, exc);
        this.className = className;
    }

    /**
     * @return the name of the class that could not be loaded.
     */
    public String getClassName() {
        return className;
    }
}