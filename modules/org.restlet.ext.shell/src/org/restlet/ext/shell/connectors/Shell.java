package org.restlet.ext.shell.connectors;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.restlet.ext.shell.helpers.ConsoleHelper;

class Shell {

    private final ConsoleHelper console;

    private final ScriptEngine scriptEngine;

    private final String prompt;

    public Shell(ScriptEngine aScriptEngine) {
        this(aScriptEngine, "> ");
    }

    public Shell(ScriptEngine aScriptEngine, String aPrompt) {
        this.scriptEngine = aScriptEngine;
        this.prompt = aPrompt;
        this.console = new ConsoleHelper();
        this.scriptEngine.put("console", this.console);
    }

    public void executeScript(String script) {
        try {
            this.scriptEngine.eval(new InputStreamReader(new FileInputStream(
                    script)));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void loop() {
        for (;;) {
            // update completor
            this.console.setCandidates(new TreeSet<String>(this.scriptEngine
                    .getBindings(ScriptContext.ENGINE_SCOPE).keySet()));

            final String line = this.console.readLine(this.prompt);

            if (line == null) {
                break;
            }

            if (line.equals("")) {
                continue;
            }

            if (this.scriptEngine.get(line) != null) {
                System.out.println(this.scriptEngine.get(line));
            }

            try {
                this.scriptEngine.eval(line);
            } catch (final ScriptException e) {
                System.err.println(e.getMessage());
            }
        }

        this.console.writeLine("");
    }

    public void put(String key, Object value) {
        this.scriptEngine.put(key, value);
    }

    public void writeLine(String line) {
        this.console.writeLine(line);
    }

    public void writeLine(String formatter, Object... args) {
        writeLine(String.format(formatter, args));
    }
}
