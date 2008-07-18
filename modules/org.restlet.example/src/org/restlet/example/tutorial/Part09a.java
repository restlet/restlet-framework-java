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

package org.restlet.example.tutorial;

import static org.restlet.example.tutorial.Constants.ROOT_URI;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;

/**
 * Guard access to a Restlet.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part09a extends Application {

    /**
     * Run the example as a standalone component.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a component
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        // Create an application
        final Application application = new Part09a(component.getContext());

        // Attach the application to the component and start it
        component.getDefaultHost().attachDefault(application);
        component.start();
    }

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The component's context.
     */
    public Part09a(Context parentContext) {
        super(parentContext);
    }

    @Override
    public Restlet createRoot() {
        // Create a Guard
        final Guard guard = new Guard(getContext(), ChallengeScheme.HTTP_BASIC,
                "Tutorial");
        guard.getSecrets().put("scott", "tiger".toCharArray());

        // Create a Directory able to return a deep hierarchy of files
        final Directory directory = new Directory(getContext(), ROOT_URI);
        guard.setNext(directory);
        return guard;
    }

}
