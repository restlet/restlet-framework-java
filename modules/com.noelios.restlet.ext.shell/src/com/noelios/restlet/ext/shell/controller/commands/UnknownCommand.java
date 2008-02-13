package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;


class UnknownCommand extends Command {

    @Override
    public void execute(String... args) {
        view.output("unknown command, type 'help'");
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String[] getAliases() {
        return aliases("__dummy__");
    }

    @Override
    public String getHelp() {
        return null;
    }
}
