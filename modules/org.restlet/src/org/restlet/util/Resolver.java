package org.restlet.util;

/**
 * Resolves a name into a value.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public interface Resolver<T> {

    /**
     * Resolves a name into a value.
     * 
     * @param name
     *                The name to resolve.
     * @return The resolved value.
     */
    public T resolve(String name);

}
