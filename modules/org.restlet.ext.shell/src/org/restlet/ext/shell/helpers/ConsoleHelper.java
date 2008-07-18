package org.restlet.ext.shell.helpers;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

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
