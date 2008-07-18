package org.restlet.ext.shell.connectors;

import javax.script.ScriptEngine;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

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
