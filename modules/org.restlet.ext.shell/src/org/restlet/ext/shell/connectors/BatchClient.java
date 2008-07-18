package org.restlet.ext.shell.connectors;

import java.util.List;

import javax.script.ScriptEngine;

import org.restlet.Client;
import org.restlet.data.Protocol;

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
