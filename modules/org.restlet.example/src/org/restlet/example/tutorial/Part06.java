/*
 * Copyright 2005-2008 Noelios Technologies.
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
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * Server static files using an application.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part06 {
    public static void main(String[] args) throws Exception {
        // Create a component
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        // Create an application
        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                return new Directory(getContext(), ROOT_URI);
            }
        };

        // Attach the application to the component and start it
        component.getDefaultHost().attach(application);
        component.start();
    }

}
