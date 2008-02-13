package com.noelios.restlet.ext.shell.view;

public abstract class View {

    public abstract void output(String format, Object... args);

    public abstract String input(String prompt);
}
