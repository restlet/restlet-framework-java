package com.noelios.restlet.ext.shell.controller.commands;

import com.noelios.restlet.ext.shell.controller.Command;
import org.restlet.data.Form;

class PostCommand extends Command {

    @Override
    public void execute(String... args) {
        Form form = new Form();
        view.output("Asking form parameters. Type C-d to quit.");

        for (;;) {

            String name = view.input("  name => ");

            if (name == null) {
                break;
            }

            String value = view.input("  value => ");

            if (value == null) {
                break;
            }

            form.add(name, value);
        }
        final String uri = args[0];

        model.post(uri, form.getWebRepresentation());
        view.output(model.getResponse().getStatus().toString());
    }

    @Override
    public String[] getAliases() {
        return aliases("post", "p");
    }

    @Override
    public String getHelp() {
        return "posts a representation to the identified resource";
    }

    @Override
    public String getUsage() {
        return "post uri";
    }
    }
