package com.noelios.restlet.ext.shell.view.textual;

import com.noelios.restlet.ext.shell.controller.Command;
import com.noelios.restlet.ext.shell.controller.commands.CommandsSingleton;
import com.noelios.restlet.ext.shell.view.View;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

public class TextualView extends View {

    private ConsoleReader consoleReader;
    private static final File historyFile = new File(System.getProperty("user.home") + File.separator + ".RESTSHell_history");
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

        consoleReader.addCompletor(new SimpleCompletor(candidateStringsList.toArray(new String[0])));
        
        // History
        try {
            history = new History(historyFile);
        } catch (IOException e) {
            
        }
        
        consoleReader.setHistory(history);
        consoleReader.setUseHistory(true);
    }

    @Override
    public void output(String format, Object... args) {
        System.out.printf(format + "\n", args);
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
}
