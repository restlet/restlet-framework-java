package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;

class PutCommand extends Command {

    @Override
    public void execute(String... args) {
        // TODO
    }

    @Override
    public String[] getAliases() {
        return aliases("put");
    }

    @Override
    public String getHelp() {
        return "puts a representation in the identified resource";
    }

    @Override
    public String getUsage() {
        return "put uri";   
    }
}
