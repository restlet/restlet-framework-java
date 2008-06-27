package org.restlet.ext.shell.connectors;

import java.util.List;
import javax.script.ScriptEngine;
import org.restlet.Client;
import org.restlet.data.Protocol;

public class BatchClient extends Client {

    private Shell shell;
    private String[] scripts;

    public BatchClient(Protocol protocol, ScriptEngine scriptEngine, String... args) {
        super(protocol);
        shell = new Shell(scriptEngine);
        scripts = args;
    }

    public BatchClient(List<Protocol> protocols, ScriptEngine scriptEngine, String...args) {
        super(protocols);
        shell = new Shell(scriptEngine);
        scripts = args;
    }

    // TODO: other constructors
    
    @Override
    public synchronized void start() throws Exception {
        super.start();
        
        for (String script : scripts) {
            shell.executeScript(script);
        }
    }
}
