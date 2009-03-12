/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        } catch (ScriptException e) {
            throw new RuntimeException("cannot load builtins.js: "
                    + e.getMessage(), e);
        } catch (IOException e) {
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