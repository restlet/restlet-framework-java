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

package com.noelios.restlet.ext.shell.view.textual;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

import com.noelios.restlet.ext.shell.controller.Command;
import com.noelios.restlet.ext.shell.controller.commands.CommandsSingleton;
import com.noelios.restlet.ext.shell.view.View;

public class TextualView extends View {

    private static final File historyFile = new File(System
            .getProperty("user.home")
            + File.separator + ".RESTSHell_history");

    private ConsoleReader consoleReader;

    private History history;

    public TextualView() {
        try {
            consoleReader = new ConsoleReader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Command completer
        List<String> candidateStringsList = new LinkedList<String>();

        for (Command command : CommandsSingleton.getInstance().values()) {
            candidateStringsList.addAll(Arrays.asList(command.getAliases()));
        }

        consoleReader.addCompletor(new SimpleCompletor(candidateStringsList
                .toArray(new String[0])));

        // History
        try {
            history = new History(historyFile);
        } catch (IOException e) {

        }

        consoleReader.setHistory(history);
        consoleReader.setUseHistory(true);
    }

    @Override
    public String input(String prompt) {
        String input;

        try {
            input = consoleReader.readLine(prompt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return input;
    }

    @Override
    public void output(String format, Object... args) {
        System.out.printf(format + "\n", args);
    }
}
