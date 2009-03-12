/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.misc;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Simple HTTP server invoked by the simple client.
 * 
 * @author Jerome Louvel
 */
public class SimpleServer {
    public static void main(String[] args) {
        try {
            // Create a new Restlet component
            final Component component = new Component();

            // Create the HTTP server connector, then add it as a server
            // connector to the Restlet component. Note that the component
            // is the call restlet.
            component.getServers().add(Protocol.HTTP, 9876);

            // Prepare and attach a test Handler
            final Restlet handler = new Restlet(component.getContext()
                    .createChildContext()) {
                @Override
                public void handle(Request request, Response response) {
                    if (request.getMethod().equals(Method.PUT)) {
                        System.out.println("Handling the call...");
                        System.out
                                .println("Trying to get the entity as a form...");
                        final Form form = request.getEntityAsForm();

                        System.out.println("Trying to getParameters...");
                        final StringBuffer sb = new StringBuffer("foo");
                        for (final Parameter p : form) {
                            System.out.println(p);

                            sb.append("field name = ");
                            sb.append(p.getName());
                            sb.append("value = ");
                            sb.append(p.getValue());
                            sb.append("\n");
                            System.out.println(sb.toString());
                        }

                        response.setEntity(sb.toString(), MediaType.TEXT_PLAIN);
                        System.out.println("Done!");
                    } else {
                        response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                    }
                }
            };

            component.getDefaultHost().attach("/test", handler);

            // Now, start the component
            component.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
