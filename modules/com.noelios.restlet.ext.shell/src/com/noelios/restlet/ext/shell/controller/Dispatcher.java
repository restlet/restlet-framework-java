package com.noelios.restlet.ext.shell.controller;

import com.noelios.restlet.ext.shell.controller.commands.Commands;
import com.noelios.restlet.ext.shell.controller.commands.CommandsSingleton;
import com.noelios.restlet.ext.shell.controller.commands.usage.UsageException;
import com.noelios.restlet.ext.shell.controller.commands.usage.UsageParserFacade;
import com.noelios.restlet.ext.shell.model.RESTShellClient;
import com.noelios.restlet.ext.shell.view.View;

import java.util.HashSet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Status;

public class Dispatcher {

    private Commands commands;
    private RESTShellClient model;
    private View view;

    public Dispatcher(RESTShellClient model, View view) {
        commands = CommandsSingleton.getInstance();

        for (Command command : new HashSet<Command>(commands.values())) {
            command.setModel(model);
            command.setView(view);
        }

        this.model = model;
        this.view = view;
    }

    public void dispatch(String line) {
        String[] args = DispatcherHelpers.tokenize(line);
        Command command = commands.get(args[0]);
        String[] realArgs;

        try {
            realArgs = UsageParserFacade.buildArguments(command.getUsage(), args);
        } catch (UsageException e) {
            view.output("usage: %s: %s", command.getUsage(), e.getMessage());
            return;
        }

        executeCommand(command, realArgs);

        if (model.getResponse().getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            ChallengeRequest challengeRequest = model.getResponse().getChallengeRequest();
            view.output("%s authentication for realm '%s'", challengeRequest.getScheme().getTechnicalName(), challengeRequest.getRealm());
            String username = view.input("username: ");
            String password = view.input("password: ");
            ChallengeResponse challengeResponse = new ChallengeResponse(challengeRequest.getScheme(), username, password);
            model.setChallengeResponse(challengeResponse);
            executeCommand(command, args);
        }
    }

    private void executeCommand(Command command, String[] args) {
        try {
            command.execute(args);
        } catch (IllegalArgumentException e) {
            view.output(e.getMessage());
        } catch (Exception e) {
        }
    }
}
