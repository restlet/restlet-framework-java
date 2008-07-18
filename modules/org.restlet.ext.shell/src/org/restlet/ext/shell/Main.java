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
