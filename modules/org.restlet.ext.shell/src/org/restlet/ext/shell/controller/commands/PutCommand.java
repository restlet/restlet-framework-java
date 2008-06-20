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

import org.restlet.ext.shell.controller.Command;

class PutCommand extends Command {

    @Override
    public void execute(String... args) {
        // TODO
    }

    @Override
    public String[] getAliases() {
        return aliases("put");
    }

    @Override
    public String getHelp() {
        return "puts a representation in the identified resource";
    }

    @Override
    public String getUsage() {
        return "put uri";
    }
}
