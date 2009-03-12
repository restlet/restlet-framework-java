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

package org.restlet.ext.shell.connectors;

import javax.script.ScriptEngine;

import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * 
 * @author Davide Angelocola
 */
public class InteractiveRestlet extends Restlet {

    private final Shell shell;

    public InteractiveRestlet(ScriptEngine scriptEngine, String prompt) {
        this.shell = new Shell(scriptEngine, prompt);
    }

    @Override
    public void handle(Request request, Response response) {
        this.shell.put("request", request);
        this.shell.put("response", response);
        this.shell
                .writeLine("a request is available in a variable called 'request'");
        this.shell
                .writeLine("a response is available in a variable called 'response'");
        this.shell.loop();
        super.handle(request, response);
        this.shell.writeLine("\nlistening...");
    }
}