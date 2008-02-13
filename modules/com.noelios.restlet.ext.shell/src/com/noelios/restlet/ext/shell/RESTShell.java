package com.noelios.restlet.ext.shell;

import com.noelios.restlet.ext.shell.controller.Dispatcher;
import com.noelios.restlet.ext.shell.model.RESTShellClient;
import com.noelios.restlet.ext.shell.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.restlet.Application;
import org.restlet.Restlet;

public class RESTShell extends Application {

    private String version;
    private Dispatcher commandDispatcher;
    private RESTShellClient RESTShellClient;
    private View view;

    public RESTShell(View concreteView) {
        super();
        view = concreteView;
        getContext().setLogger(null);
        RESTShellClient = new RESTShellClient(getContext());
        commandDispatcher = new Dispatcher(RESTShellClient, concreteView);
    }

    @Override
    public Restlet createRoot() {
        return null;
    }

    @Override
    public void start() throws Exception {
        super.start();
        String input;

        for (;;) {
            input = view.input("RESTShell> ");

            if (input == null) {
                break;
            }

            commandDispatcher.dispatch(input);
        }
    }

    public String getVersion() {
        if (version == null) {
            version = "1.0-SNAPSHOT";
// TODO: need a restshell.properties generated with maven
//            InputStream is = getClass().getResourceAsStream("/restshell.properties");
//            Properties properties = new Properties();
//            
//            try {
//                properties.load(is);
//            } catch (IOException e) {
//                version = "unknown";
//            }
//
//            version = properties.getProperty("version");
        }

        return version;
    }
}
