package org.restlet.ext.script.internal;

import java.io.Writer;

import javax.script.ScriptEngineManager;

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
    private final ScriptedTextRepresentation representation;

    private final Writer writer;

    private final Writer errorWriter;

    public ScriptedTextRepresentationContainer(
            ScriptedTextRepresentation representation, Writer writer,
            Writer errorWriter) {
        this.representation = representation;
        this.writer = writer;
        this.errorWriter = errorWriter;
    }

    /**
     * Same as {@link #getWriter()}, for standard error.
     * 
     * @return The error writer
     */
    public Writer getErrorWriter() {
        return this.errorWriter;
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

    /**
     * This is the {@link ScriptEngineManager} used to create the script engine.
     * Scripts may use it to get information about what other engines are
     * available.
     * 
     * @return The script engine manager
     */
    public ScriptEngineManager getScriptEngineManager() {
        return null;
        // return getRepresentation().getScriptEngineManager();
    }

    /**
     * Allows the script direct access to the {@link Writer}. This should rarely
     * be necessary, because by default the standard output for your scripting
     * engine would be directed to it, and the scripting platform's native
     * method for printing should be preferred. However, some scripting
     * platforms may not provide adequate access or may otherwise be broken.
     * 
     * @return The writer
     */
    public Writer getWriter() {
        return this.writer;
    }
}
