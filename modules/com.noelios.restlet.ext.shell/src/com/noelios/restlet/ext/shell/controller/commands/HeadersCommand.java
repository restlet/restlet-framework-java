package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.restlet.data.Message;
import org.restlet.data.Parameter;

class HeadersCommand extends Command {

    Map<String, String> attributes;

    public HeadersCommand() {
        attributes = new HashMap<String, String>();
        attributes.put("http", "org.restlet.http.headers");
    }

    @Override
    public void execute(String... args) {
        final Message message;
        final String headersOf = args[0];
        String headers = args[1];

        if (headersOf.equals("response")) {
            message = model.getResponse();
        } else if (headersOf.equals("request")) {
            message = model.getRequest();
        } else {
            message = model.getResponse();
        }

        if (headers.equals("")) {
            headers = attributes.get("http");
        }

        List<Parameter> httpHeaders = (List<Parameter>) message.getAttributes().get(headers);

        if (httpHeaders == null) {
            view.output("no headers");
        } else {
            for (Parameter key : httpHeaders) {
                view.output(key.toString());
            }
        }
    }

    @Override
    public String getUsage() {
        return "headers [request|response] [headers]";
    }

    @Override
    public String getHelp() {
        return "prints the headers";
    }

    @Override
    public String[] getAliases() {
        return aliases("headers", "H");
    }
}
