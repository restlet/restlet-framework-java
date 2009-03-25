/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.ext.script.internal;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.restlet.ext.script.ScriptedResource;

import com.threecrickets.scripturian.ScriptContextController;

/**
 * @author Tal Liron
 * @see ScriptedResourceContainer
 */
class ScriptedResourceScriptContextController implements
        ScriptContextController {
    private final ScriptedResource resource;

    private final ScriptedResourceContainer container;

    /**
     * Constructor.
     * 
     * @param resource
     *            The resource
     */
    ScriptedResourceScriptContextController(ScriptedResource resource,
            ScriptedResourceContainer container) {
        this.resource = resource;
        this.container = container;
    }

    public void finalize(ScriptContext scriptContext) {
        this.resource.getScriptSource().finalize(scriptContext);

        ScriptContextController scriptContextController = this.resource
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.finalize(scriptContext);
        }
    }

    public void initialize(ScriptContext scriptContext) throws ScriptException {
        scriptContext.setAttribute(this.resource.getContainerVariableName(),
                this.container, ScriptContext.ENGINE_SCOPE);

        ScriptContextController scriptContextController = this.resource
                .getScriptContextController();
        if (scriptContextController != null) {
            scriptContextController.initialize(scriptContext);
        }

        this.resource.getScriptSource().initialize(scriptContext);
    }
}