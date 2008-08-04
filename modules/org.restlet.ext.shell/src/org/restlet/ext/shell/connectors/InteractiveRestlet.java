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