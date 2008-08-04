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

import java.util.List;

import javax.script.ScriptEngine;

import org.restlet.Client;
import org.restlet.data.Protocol;

/**
 * 
 * @author Davide Angelocola
 */
public class BatchClient extends Client {

    private final Shell shell;

    private final String[] scripts;

    public BatchClient(List<Protocol> protocols, ScriptEngine scriptEngine,
            String... args) {
        super(protocols);
        this.shell = new Shell(scriptEngine);
        this.scripts = args;
    }

    public BatchClient(Protocol protocol, ScriptEngine scriptEngine,
            String... args) {
        super(protocol);
        this.shell = new Shell(scriptEngine);
        this.scripts = args;
    }

    // TODO: other constructors

    @Override
    public synchronized void start() throws Exception {
        super.start();

        for (final String script : this.scripts) {
            this.shell.executeScript(script);
        }
    }
}
