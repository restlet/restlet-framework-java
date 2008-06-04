/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.shell.controller;

import java.util.HashSet;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Status;

import com.noelios.restlet.ext.shell.controller.commands.Commands;
import com.noelios.restlet.ext.shell.controller.commands.CommandsSingleton;
import com.noelios.restlet.ext.shell.controller.commands.usage.UsageException;
import com.noelios.restlet.ext.shell.controller.commands.usage.UsageParserFacade;
import com.noelios.restlet.ext.shell.model.RESTShellClient;
import com.noelios.restlet.ext.shell.view.View;

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
            realArgs = UsageParserFacade.buildArguments(command.getUsage(),
                    args);
        } catch (UsageException e) {
            view.output("usage: %s: %s", command.getUsage(), e.getMessage());
            return;
        }

        executeCommand(command, realArgs);

        if (model.getResponse().getStatus().equals(
                Status.CLIENT_ERROR_UNAUTHORIZED)) {
            // TODO: check for support of several challenge requests
            ChallengeRequest challengeRequest = model.getResponse()
                    .getChallengeRequests().get(0);
            view.output("%s authentication for realm '%s'", challengeRequest
                    .getScheme().getTechnicalName(), challengeRequest
                    .getRealm());
            String username = view.input("username: ");
            String password = view.input("password: ");
            ChallengeResponse challengeResponse = new ChallengeResponse(
                    challengeRequest.getScheme(), username, password);
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
