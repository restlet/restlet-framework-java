/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
