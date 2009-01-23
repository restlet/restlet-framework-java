/*
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
