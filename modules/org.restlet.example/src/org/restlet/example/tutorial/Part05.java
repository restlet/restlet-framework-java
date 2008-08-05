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
 * @author Jerome Louvel
 */
public class Part05 {
    public static void main(String[] args) throws Exception {
        // Create a new Restlet component and add a HTTP server connector to it
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        // Create a new tracing Restlet
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                // Print the requested URI path
                final String message = "Resource URI  : "
                        + request.getResourceRef() + '\n' + "Root URI      : "
                        + request.getRootRef() + '\n' + "Routed part   : "
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
