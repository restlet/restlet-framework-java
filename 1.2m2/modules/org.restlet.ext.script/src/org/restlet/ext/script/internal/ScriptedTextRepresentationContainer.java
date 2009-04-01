package org.restlet.ext.script.internal;

import org.restlet.ext.script.ScriptedTextRepresentation;
import org.restlet.representation.Representation;

/**
 * This is the type of the "container" variable exposed to the script. The name
 * is set according to
 * {@link ScriptedTextRepresentation#getContainerVariableName()}.
 * 
 * @author Tal Liron
 * @see ScriptedTextRepresentation
 */
public class ScriptedTextRepresentationContainer {
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
    public ScriptedTextRepresentationContainer(
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
