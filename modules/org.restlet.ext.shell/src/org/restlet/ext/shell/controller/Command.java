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

package org.restlet.ext.shell.controller;

import org.restlet.ext.shell.model.RESTShellClient;
import org.restlet.ext.shell.view.View;

public abstract class Command implements Comparable<Command> {

    protected RESTShellClient model;

    protected View view;

    // helper
    protected final String[] aliases(String... aliases) {
        return aliases;
    }

    public int compareTo(Command otherCommand) {
        if (otherCommand == this) {
            return 0;
        } else {
            return 1;
        }
    }

    public abstract void execute(String... args);

    public abstract String[] getAliases();

    public abstract String getHelp();

    public abstract String getUsage();

    public final void setModel(RESTShellClient model) {
        this.model = model;
    }

    public final void setView(View view) {
        this.view = view;
    }
}
