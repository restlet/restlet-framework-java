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
    private final ScriptedTextRepresentationContainer container;

    private final ScriptContextController scriptContextController;

    public ScriptedTextRepresentationScriptContextController(
            ScriptedTextRepresentationContainer container,
            ScriptContextController scriptContextController) {
        this.container = container;
        this.scriptContextController = scriptContextController;
    }

    public void finalize(ScriptContext scriptContext) {
        if (this.scriptContextController != null) {
            this.scriptContextController.finalize(scriptContext);
        }
    }

    public void initialize(ScriptContext scriptContext) throws ScriptException {
        scriptContext.setAttribute(
                ScriptedTextRepresentation.containerVariableName,
                this.container, ScriptContext.ENGINE_SCOPE);

        if (this.scriptContextController != null) {
            this.scriptContextController.initialize(scriptContext);
        }
    }
}
