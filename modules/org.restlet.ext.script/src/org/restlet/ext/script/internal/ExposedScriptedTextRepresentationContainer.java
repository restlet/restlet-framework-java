package org.restlet.ext.script.internal;

import org.restlet.ext.script.ScriptedTextRepresentation;
import org.restlet.representation.Representation;

/**
 * This is the type of the <code>script.container</code> variable exposed to the
 * script.
 * 
 * @author Tal Liron
 * @see ScriptedTextRepresentation
 */
public class ExposedScriptedTextRepresentationContainer {
    /**
     * Access to the representation itself.
     */
    private final ScriptedTextRepresentation representation;

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation
     */
    public ExposedScriptedTextRepresentationContainer(
            ScriptedTextRepresentation representation) {
        this.representation = representation;
    }

    /**
     * Access to the representation itself. This can be useful for generating
     * text according to set characteristics. For example, calling
     * {@link Representation#getLanguages()} and generating the appropriate
     * text.
     * 
     * @return The representation
     */
    public ScriptedTextRepresentation getRepresentation() {
        return this.representation;
    }
}
