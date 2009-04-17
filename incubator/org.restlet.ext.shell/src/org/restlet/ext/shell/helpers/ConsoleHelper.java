/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        } catch (IOException e) {
            throw new RuntimeException("cannot initialize jline", e);
        }

        // History
        try {
            this.history = new History(historyFile);
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                    "cannot initialize history file %s", historyFile), e);
        }

        this.consoleReader.setHistory(this.history);
        this.consoleReader.setUseHistory(true);

        // Completion
        this.completor = new SimpleCompletor(new String[] { "help", "version" });
        this.consoleReader.addCompletor(this.completor);
    }

    public String readLine(String aPrompt) {
        String line = null;

        try {
            line = this.consoleReader.readLine(aPrompt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return line;
    }

    public String readPassword(String aPrompt) {
        String password = null;

        try {
            password = this.consoleReader.readLine(aPrompt);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
