/*
 * Copyright 2005-2008 Noelios Consulting.
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

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * 
 * @author Davide Angelocola
 */
public class InteractiveClient extends Client {

    private final Shell shell;

    public InteractiveClient(Protocol protocol, ScriptEngine scriptEngine) {
        super(protocol);
        this.shell = new Shell(scriptEngine, "client> ");
        this.shell.put("client", this);
    }

    // TODO: other constructor

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        this.shell.put("request", request);
        this.shell.put("response", response);
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        this.shell.loop();
    }
}
