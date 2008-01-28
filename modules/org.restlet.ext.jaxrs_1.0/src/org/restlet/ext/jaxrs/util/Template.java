package org.restlet.ext.jaxrs.util;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * Template sub class which allows different Variable Resolvers
 * 
 * @see org.restlet.util.Template
 * @author Stephan Koops
 */
public class Template extends org.restlet.util.Template {

    /**
     * Resolves variable values.
     * 
     * @see org.restlet.util.Template.VariableResolver
     */
    public interface VariableResolver
    // extends org.restlet.util.Template.VariableResolver
    {
        /**
         * Resolves a variable name into a value. Copy of
         * {@link org.restlet.util.Template.VariableResolver}.
         * 
         * @param variableName
         *                The variable name to resolve.
         * @return The resolved value.
         */
        public abstract String resolve(String variableName);
    }

    /**
     * 
     * @param pattern
     */
    public Template(String pattern) {
        super(pattern);
    }

    /**
     * 
     * @param pattern
     * @param matchingMode
     */
    public Template(String pattern, int matchingMode) {
        super(pattern, matchingMode);
    }

    /**
     * @param resolver
     *                The resolver to resolve the variables
     * @return the variable value.
     * @see org.restlet.util.Template#format(VariableResolver)
     */
    public String format(VariableResolver resolver) {
        return super.format(resolver);
    }
    
    protected String resolve(Object userObject, String variableName) {
        throw new NotYetImplementedException("waiting while Disussing a API change");
    }
}
