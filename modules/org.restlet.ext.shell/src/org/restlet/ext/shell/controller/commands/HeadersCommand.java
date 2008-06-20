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

package org.restlet.ext.shell.controller.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.restlet.data.Message;
import org.restlet.data.Parameter;
import org.restlet.ext.shell.controller.Command;

class HeadersCommand extends Command {

    Map<String, String> attributes;

    public HeadersCommand() {
        attributes = new HashMap<String, String>();
        attributes.put("http", "org.restlet.http.headers");
    }

    @SuppressWarnings("unchecked")
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

        List<Parameter> httpHeaders = (List<Parameter>) message.getAttributes()
                .get(headers);

        if (httpHeaders == null) {
            view.output("no headers");
        } else {
            for (Parameter key : httpHeaders) {
                view.output(key.toString());
            }
        }
    }

    @Override
    public String[] getAliases() {
        return aliases("headers", "H");
    }

    @Override
    public String getHelp() {
        return "prints the headers";
    }

    @Override
    public String getUsage() {
        return "headers [request|response] [headers]";
    }
}
