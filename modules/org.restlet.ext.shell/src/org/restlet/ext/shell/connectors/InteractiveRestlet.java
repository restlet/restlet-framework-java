package org.restlet.ext.shell.connectors;

import javax.script.ScriptEngine;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class InteractiveRestlet extends Restlet {

    private Shell shell;

    public InteractiveRestlet(ScriptEngine scriptEngine, String prompt) {
        shell = new Shell(scriptEngine, prompt);
    }

    @Override
    public void handle(Request request, Response response) {
        shell.put("request", request);
        shell.put("response", response);
        shell.writeLine("a request is available in a variable called 'request'");
        shell.writeLine("a response is available in a variable called 'response'");
        shell.loop();
        super.handle(request, response);
        shell.writeLine("\nlistening...");
    }
}