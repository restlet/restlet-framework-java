/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.ext.shell.helpers;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

/**
 * 
 * @author Davide Angelocola
 */
public class ConsoleHelper {

    private static final File historyFile = new File(System
            .getProperty("user.home")
            + File.separator + ".RESTSHell_history");

    private ConsoleReader consoleReader;

    private History history;

    private final SimpleCompletor completor;

    public ConsoleHelper() {
        // / ConsoleHelper
        try {
            this.consoleReader = new ConsoleReader();
        } catch (final IOException e) {
            throw new RuntimeException("cannot initialize jline", e);
        }

        // History
        try {
            this.history = new History(historyFile);
        } catch (final IOException e) {
            throw new RuntimeException(String.format(
                    "cannot initialize history file %s", historyFile), e);
        }

        this.consoleReader.setHistory(this.history);
        this.consoleReader.setUseHistory(true);

        // Completition
        this.completor = new SimpleCompletor(new String[] { "help", "version" });
        this.consoleReader.addCompletor(this.completor);
    }

    public String readLine(String aPrompt) {
        String line = null;

        try {
            line = this.consoleReader.readLine(aPrompt);
        } catch (final IOException e) {
            // do nothing
        }

        return line;
    }

    public String readPassword(String aPrompt) {
        String password = null;

        try {
            password = this.consoleReader.readLine(aPrompt);
        } catch (final IOException e) {
            // do nothing
        }

        return password;
    }

    public void setCandidates(SortedSet<String> canditates) {
        this.completor.setCandidates(canditates);
    }

    public void writeLine(String line) {
        try {
            this.consoleReader.printString(line + "\n");
            this.consoleReader.flushConsole();
        } catch (final IOException ex) {
            // do nothing
        }
    }
}
