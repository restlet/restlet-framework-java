package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;

class HeadCommand extends Command {

    @Override
    public void execute(String... args) {
        final String uri = args[0];
        model.head(uri);
        view.output(model.getResponse().getStatus().toString());
    }

    @Override
    public String getUsage() {
        return "head url";
    }

    @Override
    public String getHelp() {
        return "gets the identified resource without its representation's content";
    }

    @Override
    public String[] getAliases() {
        return aliases("head", "h");
    }
}
