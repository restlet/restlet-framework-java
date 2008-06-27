package org.restlet.ext.shell.script;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineBuilder {

    private ScriptEngineBuilder() {
    }

    public static ScriptEngine create(String scriptEngineName) {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(scriptEngineName);

        if (scriptEngine == null) {
            throw new RuntimeException(String.format("script engine '%s' not found", scriptEngineName));
        }

        return scriptEngine;
    }

    // FIXME: for convenience: to be removed
    public static ScriptEngine create() {
        ScriptEngine scriptEngine = create("js");
        URL builtins = ClassLoader.getSystemResource("javascript/builtins.js");

        try {
            scriptEngine.eval(new InputStreamReader(builtins.openStream()));
        } catch (ScriptException e) {
            throw new RuntimeException("cannot load builtins.js: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("cannot load builtins.js: " + e.getMessage(), e);
        }

        return scriptEngine;
    }
}