/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.shell.controller.commands;

import java.util.HashMap;

import org.restlet.ext.shell.controller.Command;


public class Commands extends HashMap<String, Command> {

    private static final long serialVersionUID = 1L;

    private Command[] commands = { new ClientInfoCommand(),
            new ContentsCommand(), new CookiesCommand(), new DeleteCommand(),
            new ExitCommand(), new GetCommand(), new HeadCommand(),
            new HeadersCommand(), new HelpCommand(), new NullCommand(),
            new OptionsCommand(), new PostCommand(), new PutCommand(),
            new ServerInfoCommand(), new UnknownCommand() };

    protected Commands() {
        for (Command command : commands) {
            for (String alias : command.getAliases()) {
                if (containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                            "%s already registred for %s", alias, get(alias)
                                    .getClass()));
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
