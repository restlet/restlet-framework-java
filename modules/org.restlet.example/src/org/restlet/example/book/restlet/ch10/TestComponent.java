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

package org.restlet.example.book.restlet.ch10;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Creates a component.
 */
public class TestComponent {

    public static void main(String[] args) throws Exception {
        final Component component = new Component();
        // Add a new HTTP server connector
        component.getServers().add(Protocol.HTTP, 8182);
        // Add a new FILE client connector
        component.getClients().add(Protocol.FILE);

        // Attach the application to the component and start it
        component.getDefaultHost().attach("/dynamicApplication",
                new DynamicApplication());

        component.start();

        // Request the XML file
        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET,
                "http://localhost:8182/dynamicApplication/transformer");
        request.getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_HTML));
        final Response response = client.handle(request);
        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            response.getEntity().write(System.out);
        }

        component.stop();
    }

}
