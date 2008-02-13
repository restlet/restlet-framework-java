package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import java.io.IOException;
import org.restlet.data.Message;

class ContentsCommand extends Command {

    @Override
    public void execute(String... args) {
        Message message;
        final String contentsOf = args[0];

        if (contentsOf.equals("response")) {
            message = model.getResponse();
        } else if (contentsOf.equals("request")) {
            message = model.getRequest();
        } else {
            message = model.getResponse();
        }

        if (message.getEntity() != null) {
            try {
                view.output("%s", message.getEntity().getText());
            } catch (IOException e) {

            }
        } else {
            view.output("no contents");
        }
    }

    @Override
    public String getUsage() {
        return "contents [request|response]";
    }

    @Override
    public String getHelp() {
        return "show the content of a message";
    }

    @Override
    public String[] getAliases() {
        return aliases("contents", "C");
    }
}
