/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.shell.connectors;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.restlet.ext.shell.helpers.ConsoleHelper;

/**
 * 
 * @author Davide Angelocola
 */
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
        } catch (Exception e) {
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
            } catch (ScriptException e) {
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
