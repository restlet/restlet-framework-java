package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import java.util.HashSet;
import java.util.Set;

class HelpCommand extends Command {

    @Override
    public void execute(String... args) {
        final String commandName = args[0];
        final Commands commands = CommandsSingleton.getInstance();

        if (commandName.equals("")) {

            Set<Command> uniqueCommands = new HashSet<Command>(commands.values());

            for (Command command : uniqueCommands) {
                if (command.getHelp() != null) {
                    view.output("%s : %s", command.getUsage(), command.getHelp());
                }
            }
        } else {
            Command command = commands.get(commandName);
            view.output("usage : %s", command.getUsage());
            view.output("help  : %s", command.getHelp());
        }
    }

    @Override
    public String[] getAliases() {
        return aliases("help", "?");
    }

    @Override
    public String getHelp() {
        return "show the help";
    }

    @Override
    public String getUsage() {
        return "help [help]";
    }
}
