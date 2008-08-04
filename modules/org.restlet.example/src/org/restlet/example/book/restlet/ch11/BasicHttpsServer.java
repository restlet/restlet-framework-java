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
