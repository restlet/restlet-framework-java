/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.test;

import junit.framework.TestCase;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.Engine;
import com.noelios.restlet.ext.grizzly.HttpServerHelper;
import com.noelios.restlet.ext.net.HttpClientHelper;

/**
 * Unit tests for the Grizzly connector class.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class GrizzlyTestCase extends TestCase {
    /**
     * Tests the cookies parsing.
     * 
     * @throws Exception
     */
    public void testConnector() throws Exception {
        // Manually registers the Grizzly server and the JDK HTTP client
        Engine nre = new Engine(false);
        nre.getRegisteredServers().add(new HttpServerHelper(null));
        nre.getRegisteredClients().add(new HttpClientHelper(null));
        org.restlet.util.Engine.setInstance(nre);

        // Create components
        Component clientComponent = new Component();
        Component originComponent = new Component();

        // Create the client connectors
        clientComponent.getClients().add(Protocol.HTTP);

        // Create a new Restlet that return the input form after parsing it
        Restlet trace = new Restlet(originComponent.getContext()) {
            public void handle(Request request, Response response) {
                Form inputForm = request.getEntityAsForm();
                response.setEntity(inputForm.getWebRepresentation());
            }
        };

        // Set the component roots
        originComponent.getDefaultHost().attach("", trace);

        // Create the server connectors
        originComponent.getServers().add(Protocol.HTTP, 9090);

        // Now, let's start the components!
        originComponent.start();
        clientComponent.start();

        // Tests
        Context context = clientComponent.getContext();
        String uri = "http://localhost:9090/?foo=bar";
        testCall(context, Method.POST, uri);
        testCall(context, Method.PUT, uri);

        // Stop the components
        clientComponent.stop();
        originComponent.stop();
    }

    private void testCall(Context context, Method method, String uri)
            throws Exception {
        Form inputForm = new Form();
        inputForm.add("a", "a");
        inputForm.add("b", "b");

        Request request = new Request(method, uri);
        request.setEntity(inputForm.getWebRepresentation());

        Response response = context.getClientDispatcher().handle(request);
        assertNotNull(response.getEntity());

        Form outputForm = response.getEntityAsForm();
        assertEquals(2, outputForm.size());
        assertEquals("a", outputForm.getFirst("a"));
        assertEquals("b", outputForm.getFirst("b"));

        response.getEntity().write(System.out);
    }
}
