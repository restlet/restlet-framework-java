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

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Restlets components.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part05 {
    public static void main(String[] args) throws Exception {
        // Create a new Restlet component and add a HTTP server connector to it
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        // Create a new tracing Restlet
        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                // Print the requested URI path
                String message = "Resource URI  : " + request.getResourceRef()
                        + '\n' + "Root URI      : " + request.getRootRef()
                        + '\n' + "Routed part   : "
                        + request.getResourceRef().getBaseRef() + '\n'
                        + "Remaining part: "
                        + request.getResourceRef().getRemainingPart();
                response.setEntity(message, MediaType.TEXT_PLAIN);
            }
        };

        // Then attach it to the local host
        component.getDefaultHost().attach("/trace", restlet);

        // Now, let's start the component!
        // Note that the HTTP server connector is also automatically started.
        component.start();
    }

}
