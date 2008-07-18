package org.restlet.ext.shell.script;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineBuilder {

    // FIXME: for convenience: to be removed
    public static ScriptEngine create() {
        final ScriptEngine scriptEngine = create("js");
        final URL builtins = ClassLoader
                .getSystemResource("javascript/builtins.js");

        try {
            scriptEngine.eval(new InputStreamReader(builtins.openStream()));
        } catch (final ScriptException e) {
            throw new RuntimeException("cannot load builtins.js: "
                    + e.getMessage(), e);
        } catch (final IOException e) {
            throw new RuntimeException("cannot load builtins.js: "
                    + e.getMessage(), e);
        }

        return scriptEngine;
    }

    public static ScriptEngine create(String scriptEngineName) {
        final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        final ScriptEngine scriptEngine = scriptEngineManager
                .getEngineByName(scriptEngineName);

        if (scriptEngine == null) {
            throw new RuntimeException(String.format(
                    "script engine '%s' not found", scriptEngineName));
        }

        return scriptEngine;
    }

    private ScriptEngineBuilder() {
    }
}