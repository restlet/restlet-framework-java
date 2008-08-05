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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch11;

import java.io.File;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 */
public class BasicHttpsServer {
    public static void main(String[] args) {
        // Creates a Restlet whose response to each request is "hello, world".
        final Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };

        final File keystoreFile = new File("d:\\temp\\certificats",
                "myServerKeystore");
        // Component declaring only one HTTPS server connector.
        final Component component = new Component();
        component.getServers().add(Protocol.HTTPS, 8182);
        component.getDefaultHost().attach("/helloWorld", restlet);

        // Update component's context with keystore parameters.
        component.getContext().getParameters().add("keystorePath",
                keystoreFile.getAbsolutePath());
        component.getContext().getParameters().add("keystorePassword",
                "storepass");
        component.getContext().getParameters().add("keyPassword", "keypass");

        try {
            component.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
