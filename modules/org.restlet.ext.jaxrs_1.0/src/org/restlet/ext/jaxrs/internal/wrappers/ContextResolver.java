package org.restlet.ext.jaxrs.internal.wrappers;

/**
 * Wraps a {@link javax.ws.rs.ext.ContextResolver}.
 * 
 * @author Stephan Koops
 * @param <T> LATER javadoc
 * @see javax.ws.rs.ext.ContextResolver
 */
public interface ContextResolver<T> {
    /**
     * Get a context of type <code>T</code> that is applicable to the supplied
     * type.
     * 
     * @param type
     *                the class of object for which a context is desired
     * @return a context for the supplied type or <code>null<code> if a 
     * context for the supplied type is not available from this provider.
     * @see javax.ws.rs.ext.ContextResolver#getContext(Class)
     */
    public Object getContext(Class<T> type);
}