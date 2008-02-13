package com.noelios.restlet.ext.shell;

import com.noelios.restlet.ext.shell.view.View;
import com.noelios.restlet.ext.shell.view.textual.TextualView;

public class Main {

    public static void main(String[] args) throws Exception {
        View view = new TextualView();
        RESTShell RESTShell = new RESTShell(view);
        view.output("RESTShell version %s", RESTShell.getVersion());
        view.output("Type 'help' for more commands help.");
        RESTShell.start();
    }
}
