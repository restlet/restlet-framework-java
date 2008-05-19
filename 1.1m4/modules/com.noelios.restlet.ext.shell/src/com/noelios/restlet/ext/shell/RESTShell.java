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

import org.restlet.Application;
import org.restlet.Restlet;

import com.noelios.restlet.ext.shell.controller.Dispatcher;
import com.noelios.restlet.ext.shell.model.RESTShellClient;
import com.noelios.restlet.ext.shell.view.View;

public class RESTShell extends Application {

    private Dispatcher commandDispatcher;

    private RESTShellClient RESTShellClient;

    private String version;

    private View view;

    public RESTShell(View concreteView) {
        super();
        view = concreteView;
        getContext().setLogger(null);
        RESTShellClient = new RESTShellClient(getContext());
        commandDispatcher = new Dispatcher(RESTShellClient, concreteView);
    }

    @Override
    public Restlet createRoot() {
        return null;
    }

    public String getVersion() {
        if (version == null) {
            version = "1.0-SNAPSHOT";
            // TODO: need a restshell.properties generated with maven
            // InputStream is =
            // getClass().getResourceAsStream("/restshell.properties");
            // Properties properties = new Properties();
            //            
            // try {
            // properties.load(is);
            // } catch (IOException e) {
            // version = "unknown";
            // }
            //
            // version = properties.getProperty("version");
        }

        return version;
    }

    @Override
    public void start() throws Exception {
        super.start();
        String input;

        for (;;) {
            input = view.input("RESTShell> ");

            if (input == null) {
                break;
            }

            commandDispatcher.dispatch(input);
        }
    }
}
