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

            Set<Command> uniqueCommands = new HashSet<Command>(commands
                    .values());

            for (Command command : uniqueCommands) {
                if (command.getHelp() != null) {
                    view.output("%s : %s", command.getUsage(), command
                            .getHelp());
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
