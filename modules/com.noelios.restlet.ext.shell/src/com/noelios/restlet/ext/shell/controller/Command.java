package com.noelios.restlet.ext.shell.controller;

import com.noelios.restlet.ext.shell.model.RESTShellClient;
import com.noelios.restlet.ext.shell.view.View;

public abstract class Command implements Comparable<Command> {

    protected View view;
    protected RESTShellClient model;

    public final void setModel(RESTShellClient model) {
        this.model = model;
    }

    public final void setView(View view) {
        this.view = view;
    }

    public abstract void execute(String... args);

    public abstract String[] getAliases();

    public abstract String getHelp();

    public abstract String getUsage();

    // helper
    protected final String[] aliases(String... aliases) {
        return aliases;
    }

    public int compareTo(Command otherCommand) {
        if (otherCommand == this) {
            return 0;
        } else {
            return 1;
        }
    }
}
