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

    private SimpleCompletor completor;

    public ConsoleHelper() {
        // / ConsoleHelper
        try {
            consoleReader = new ConsoleReader();
        } catch (IOException e) {
            throw new RuntimeException("cannot initialize jline", e);
        }

        // History
        try {
            history = new History(historyFile);
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                    "cannot initialize history file %s", historyFile), e);
        }

        consoleReader.setHistory(history);
        consoleReader.setUseHistory(true);

        // Completition
        completor = new SimpleCompletor(new String[] { "help", "version" });
        consoleReader.addCompletor(completor);
    }

    public String readLine(String aPrompt) {
        String line = null;

        try {
            line = consoleReader.readLine(aPrompt);
        } catch (IOException e) {
            // do nothing
        }

        return line;
    }

    public String readPassword(String aPrompt) {
        String password = null;

        try {
            password = consoleReader.readLine(aPrompt);
        } catch (IOException e) {
            // do nothing
        }

        return password;
    }

    public void writeLine(String line) {
        try {
            consoleReader.printString(line + "\n");
            consoleReader.flushConsole();
        } catch (IOException ex) {
            // do nothing
        }
    }

    public void setCandidates(SortedSet<String> canditates) {
        completor.setCandidates(canditates);
    }
}
