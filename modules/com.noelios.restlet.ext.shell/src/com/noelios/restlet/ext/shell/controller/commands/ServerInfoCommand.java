package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import org.restlet.data.ServerInfo;

class ServerInfoCommand extends Command {

    @Override
    public void execute(String... args) {
        final ServerInfo serverInfo = model.getResponse().getServerInfo();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("agent   : ").append(serverInfo.getAgent()).append("\n");
        stringBuilder.append("address : ").append(serverInfo.getAddress()).append("\n");
        stringBuilder.append("port    : ").append(serverInfo.getPort());
        view.output("%s", stringBuilder.toString());
    }

    @Override
    public String[] getAliases() {
        return aliases("serverinfo", "si");
    }

    @Override
    public String getHelp() {
        return "shows the server information";
    }

    @Override
    public String getUsage() {
        return "serverinfo";
    }
}
