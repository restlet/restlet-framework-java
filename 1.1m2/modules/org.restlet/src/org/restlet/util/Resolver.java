package org.restlet.util;

/**
 * Resolves a variable name into a value.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public interface Resolver {

    /**
     * Resolves a variable name into a value.
     * 
     * @param variableName
     *                The variable name to resolve.
     * @return The resolved value.
     */
    public String resolve(String variableName);

}
