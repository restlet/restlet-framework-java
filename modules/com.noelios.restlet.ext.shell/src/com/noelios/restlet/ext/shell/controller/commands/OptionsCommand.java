package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;

class OptionsCommand extends Command {

    @Override
    public void execute(String... args) {
        final String uri = args[0];
        model.options(uri);
        view.output(model.getResponse().getStatus().toString());
    }

    @Override
    public String[] getAliases() {
        return aliases("options", "o");
    }

    @Override
    public String getHelp() {
        return "gets the options for the identified resource";
    }

    @Override
    public String getUsage() {
        return "options uri";
    }
}
