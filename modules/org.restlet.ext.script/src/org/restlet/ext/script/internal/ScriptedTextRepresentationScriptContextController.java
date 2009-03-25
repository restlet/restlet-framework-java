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
    private final ScriptedTextRepresentation representation;

    private final ScriptedTextRepresentationContainer container;

    public ScriptedTextRepresentationScriptContextController(
            ScriptedTextRepresentation representation,
            ScriptedTextRepresentationContainer container) {
        this.container = container;
        this.representation = representation;
    }

    public void finalize(ScriptContext scriptContext) {
        ScriptContextController scriptContextController = this.representation
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.finalize(scriptContext);
        }
    }

    public void initialize(ScriptContext scriptContext) throws ScriptException {
        scriptContext.setAttribute(this.representation
                .getContainerVariableName(), this.container,
                ScriptContext.ENGINE_SCOPE);

        ScriptContextController scriptContextController = this.representation
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.initialize(scriptContext);
        }
    }
}
