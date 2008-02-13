package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import org.restlet.data.ClientInfo;

class ClientInfoCommand extends Command {

    @Override
    public void execute(String... args) {
        final ClientInfo clientInfo = model.getRequest().getClientInfo();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("agent   : ").append(clientInfo.getAgent()).append("\n");
        stringBuilder.append("address : ").append(clientInfo.getAddress()).append("\n");
        stringBuilder.append("port    : ").append(clientInfo.getPort());
        view.output("%s", stringBuilder.toString());
    }

    @Override
    public String[] getAliases() {
        return aliases("clientinfo", "ci");
    }

    @Override
    public String getHelp() {
        return "shows the client information about";
    }

    @Override
    public String getUsage() {
        return "clientinfo";
    }
}
