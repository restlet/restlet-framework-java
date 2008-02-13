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
