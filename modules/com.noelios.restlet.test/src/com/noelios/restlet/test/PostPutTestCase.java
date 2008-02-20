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

package com.noelios.restlet.test;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Unit tests for the Grizzly connector class.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class PostPutTestCase extends BaseConnectorsTestCase {
    @Override
    protected Application createApplication(final Component component) {
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                Restlet trace = new Restlet(component.getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        Form inputForm = request.getEntityAsForm();
                        response.setEntity(inputForm.getWebRepresentation());
                    }
                };

                return trace;
            }
        };

        return application;
    }

    @Override
    protected void call(String uri) throws Exception {
        Client client = new Client(Protocol.HTTP);
        testCall(client, Method.POST, uri);
        testCall(client, Method.PUT, uri);
    }

    private void testCall(Client client, Method method, String uri)
            throws Exception {
        Form inputForm = new Form();
        inputForm.add("a", "a");
        inputForm.add("b", "b");

        Request request = new Request(method, uri);
        request.setEntity(inputForm.getWebRepresentation());

        Response response = client.handle(request);
        assertNotNull(response.getEntity());

        Form outputForm = response.getEntityAsForm();
        assertEquals(2, outputForm.size());
        assertEquals("a", outputForm.getFirstValue("a"));
        assertEquals("b", outputForm.getFirstValue("b"));
    }
}
