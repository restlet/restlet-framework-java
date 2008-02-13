package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;


class NullCommand extends Command {

    @Override
    public void execute(String... args) {
        // do nothing
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String[] getAliases() {
        return aliases("");
    }

    @Override
    public String getHelp() {
        return null;
    }
}
