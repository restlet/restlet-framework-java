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
