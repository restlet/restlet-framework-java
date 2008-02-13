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

package com.noelios.restlet.ext.shell;

import com.noelios.restlet.ext.shell.view.View;
import com.noelios.restlet.ext.shell.view.textual.TextualView;

public class Main {

    public static void main(String[] args) throws Exception {
        View view = new TextualView();
        RESTShell RESTShell = new RESTShell(view);
        view.output("RESTShell version %s", RESTShell.getVersion());
        view.output("Type 'help' for more commands help.");
        RESTShell.start();
    }
}
