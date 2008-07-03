package org.restlet.ext.shell.connectors;

import org.restlet.ext.shell.helpers.ConsoleHelper;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.TreeSet;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.ScriptEngine;

class Shell {

    private ConsoleHelper console;
    private ScriptEngine scriptEngine;
    private String prompt;

    public Shell(ScriptEngine aScriptEngine) {
        this(aScriptEngine, "> ");
    }
    
    public Shell(ScriptEngine aScriptEngine, String aPrompt) {
        scriptEngine = aScriptEngine;
        prompt = aPrompt;
        console = new ConsoleHelper();
        scriptEngine.put("console", console);
    }

    public void loop() {
        for (;;) {
            // update completor
            console.setCandidates(new TreeSet<String>(scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).keySet()));

            String line = console.readLine(prompt);

            if (line == null) {
                break;
            }

            if (line.equals("")) {
                continue;
            }

            if (scriptEngine.get(line) != null) {
                System.out.println(scriptEngine.get(line));
            }

            try {
                scriptEngine.eval(line);
            } catch (ScriptException e) {
                System.err.println(e.getMessage());
            }
        }

        console.writeLine("");
    }

    public void executeScript(String script) {
        try {
            scriptEngine.eval(new InputStreamReader(new FileInputStream(script)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void put(String key, Object value) {
        scriptEngine.put(key, value);
    }

    public void writeLine(String line) {
        console.writeLine(line);
    }

    public void writeLine(String formatter, Object... args) {
        writeLine(String.format(formatter, args));
    }
}
