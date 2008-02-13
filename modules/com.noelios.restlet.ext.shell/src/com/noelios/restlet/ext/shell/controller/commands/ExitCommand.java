package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;

public class ExitCommand extends Command {

    @Override
    public void execute(String... args) {
        System.exit(0);
    }

    @Override
    public String[] getAliases() {
        return aliases("quit", "q", "exit", "e");
    }

    @Override
    public String getHelp() {
        return "quits RESTShell";
    }

    @Override
    public String getUsage() {
        return "exit";
    }
}
