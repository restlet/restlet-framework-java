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
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.shell;

import javax.script.ScriptEngine;

import org.restlet.Connector;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.shell.connectors.BatchClient;
import org.restlet.ext.shell.connectors.InteractiveClient;
import org.restlet.ext.shell.connectors.InteractiveRestlet;
import org.restlet.ext.shell.helpers.VersionHelper;
import org.restlet.ext.shell.script.ScriptEngineBuilder;

/**
 * 
 * @author Davide Angelocola
 */
public class Main {

    public static void main(String[] args) throws Exception {
        final CommandLineArguments commandLineArguments = new CommandLineArguments(
                args);

        if (commandLineArguments.haveError()) {
            System.out.println(commandLineArguments.getError());
            return;
        }

        if (commandLineArguments.haveHelp()) {
            System.out.println(commandLineArguments.getHelp());
            return;
        }

        if (commandLineArguments.haveVersion()) {
            System.out.println(commandLineArguments.getVersion());
            return;
        }

        Connector connector;
        final ScriptEngine javaScriptEngine = ScriptEngineBuilder.create();

        if (commandLineArguments.haveSourceFiles()) {
            connector = new BatchClient(Protocol.HTTP, javaScriptEngine,
                    commandLineArguments.getSourceFiles());
        } else {
            System.out.println("RESTShell " + VersionHelper.getVersion());
            System.out.println("Type CTRL-D to exit.");

            if (commandLineArguments.haveServer()) {
                connector = new Server(Protocol.HTTP, commandLineArguments
                        .getServerPort(), new InteractiveRestlet(
                        javaScriptEngine, "server> "));
            } else {
                connector = new InteractiveClient(Protocol.HTTP,
                        javaScriptEngine);
            }
        }

        connector.start();
    }
}
