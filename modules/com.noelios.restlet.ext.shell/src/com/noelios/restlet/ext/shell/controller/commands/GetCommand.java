package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;

class GetCommand extends Command {

    @Override
    public void execute(String... args) {
        final String uri = args[0];
        model.get(uri);
        view.output(model.getResponse().getStatus().toString());
    }

    @Override
    public String getUsage() {
        return "get uri";
    }

    @Override
    public String getHelp() {
        return "gets the identified resource";
    }

    @Override
    public String[] getAliases() {
        return aliases("get", "g");
    }
}
