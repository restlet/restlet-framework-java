package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import java.util.HashMap;

public class Commands extends HashMap<String, Command> {

    private Command[] commands = {
        new ClientInfoCommand(),
        new ContentsCommand(),
        new CookiesCommand(),
        new DeleteCommand(),
        new ExitCommand(),
        new GetCommand(),
        new HeadCommand(),
        new HeadersCommand(),
        new HelpCommand(),
        new NullCommand(),
        new OptionsCommand(),
        new PostCommand(),
        new PutCommand(),
        new ServerInfoCommand(),
        new UnknownCommand()
    };

    protected Commands() {
        for (Command command : commands) {
            for (String alias : command.getAliases()) {
                if (containsKey(alias)) {
                    throw new IllegalArgumentException(String.format("%s already registred for %s", alias, get(alias).getClass()));
                } else {
                    put(alias, command);
                }
            }
        }
    }

    @Override
    public Command get(Object key) {
        Command command = super.get(key);

        // returns UnknownCommand
        if (command == null) {
            command = commands[commands.length - 1];
        }

        return command;
    }
}
