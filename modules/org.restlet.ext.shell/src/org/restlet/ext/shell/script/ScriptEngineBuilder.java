/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.shell.script;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 
 * @author Davide Angelocola
 */
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