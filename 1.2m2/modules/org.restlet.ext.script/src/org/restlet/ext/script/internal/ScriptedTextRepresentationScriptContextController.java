package org.restlet.ext.script.internal;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.restlet.ext.script.ScriptedTextRepresentation;

import com.threecrickets.scripturian.ScriptContextController;

/**
 * @author Tal Liron
 * @see ScriptedTextRepresentationContainer
 */
public class ScriptedTextRepresentationScriptContextController implements
        ScriptContextController {
    /**
     * The representation.
     */
    private final ScriptedTextRepresentation representation;

    /**
     * The container.
     */
    private final ScriptedTextRepresentationContainer container;

    /**
     * The container variable name.
     */
    private final String containerVariableName;

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation
     * @param container
     *            The container
     * @param containerVariableName
     *            The container variable name
     */
    public ScriptedTextRepresentationScriptContextController(
            ScriptedTextRepresentation representation,
            ScriptedTextRepresentationContainer container,
            String containerVariableName) {
        this.container = container;
        this.representation = representation;
        this.containerVariableName = containerVariableName;
    }

    public void finalize(ScriptContext scriptContext) {
        ScriptContextController scriptContextController = this.representation
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.finalize(scriptContext);
        }
    }

    public void initialize(ScriptContext scriptContext) throws ScriptException {
        scriptContext.setAttribute(this.containerVariableName, this.container,
                ScriptContext.ENGINE_SCOPE);

        ScriptContextController scriptContextController = this.representation
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.initialize(scriptContext);
        }
    }
}
