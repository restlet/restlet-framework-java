package org.restlet.ext.shell.connectors;

import javax.script.ScriptEngine;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class InteractiveClient extends Client {

    private Shell shell;

    public InteractiveClient(Protocol protocol, ScriptEngine scriptEngine) {
        super(protocol);
        shell = new Shell(scriptEngine, "client> ");
        shell.put("client", this);
    }

    // TODO: other constructor
    
    @Override
    public synchronized void start() throws Exception {
        super.start();
        shell.loop();
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        shell.put("request", request);
        shell.put("response", response);
    }
}
