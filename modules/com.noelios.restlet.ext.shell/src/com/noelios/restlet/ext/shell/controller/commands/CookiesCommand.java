package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import org.restlet.data.CookieSetting;

class CookiesCommand extends Command {

    @Override
    public void execute(String... args) {
        for (CookieSetting cookieSetting : model.getResponse().getCookieSettings()) {
            view.output("%s", cookieSetting);
        }
    }

    @Override
    public String[] getAliases() {
        return aliases("cookies", "c");
    }

    @Override
    public String getHelp() {
        return "shows the cookies";
    }

    @Override
    public String getUsage() {
        return "cookies";
    }
}
