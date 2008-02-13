package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;


class DeleteCommand extends Command {

    @Override
    public void execute(String... args) {
        final String uri = args[0];
        model.delete(uri);
        view.output(model.getResponse().getStatus().toString());
    }

    @Override
    public String[] getAliases() {
        return aliases("delete", "d");
    }

    @Override
    public String getHelp() {
        return "deletes the identified resource";
    }

    @Override
    public String getUsage() {
        return "delete uri";
    }
}
