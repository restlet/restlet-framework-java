package com.noelios.restlet.ext.shell.controller.commands;

public class CommandsSingleton {

    private static Commands commands = new Commands();

    public static Commands getInstance() {
        return commands;
    }

    private CommandsSingleton() {
    }
}
