/*
 * Copyright 2005-2007 Noelios Technologies.
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

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import static org.restlet.example.tutorial.Constants.*;

/**
 * Guard access to a Restlet.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part09a {
    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        // Create an application
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                // Create a Guard
                Guard guard = new Guard(getContext(),
                        ChallengeScheme.HTTP_BASIC, "Tutorial");
                guard.getSecrets().put("scott", "tiger".toCharArray());

                // Create a Directory able to return a deep hierarchy of files
                Directory directory = new Directory(getContext(), ROOT_URI);
                guard.setNext(directory);
                return guard;
            }
        };

        // Attach the application to the component and start it
        component.getDefaultHost().attach("", application);
        component.start();
    }

}
