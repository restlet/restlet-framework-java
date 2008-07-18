package org.restlet.ext.shell.connectors;

import javax.script.ScriptEngine;

import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

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