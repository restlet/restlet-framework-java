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

package org.restlet.test.engine;

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
 * Unit tests for POST and PUT requests.
 * 
 * @author Jerome Louvel
 */
public class PostPutTestCase extends BaseConnectorsTestCase {
    @Override
    protected void call(String uri) throws Exception {
        final Client client = new Client(Protocol.HTTP);
        testCall(client, Method.POST, uri);
        testCall(client, Method.PUT, uri);
    }

    @Override
    protected Application createApplication(final Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                final Restlet trace = new Restlet(getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        final Form inputForm = request.getEntityAsForm();
                        response.setEntity(inputForm.getWebRepresentation());
                    }
                };

                return trace;
            }
        };

        return application;
    }

    private void testCall(Client client, Method method, String uri)
            throws Exception {
        final Form inputForm = new Form();
        inputForm.add("a", "a");
        inputForm.add("b", "b");

        final Request request = new Request(method, uri);
        request.setEntity(inputForm.getWebRepresentation());

        final Response response = client.handle(request);
        assertNotNull(response.getEntity());

        final Form outputForm = response.getEntityAsForm();
        assertEquals(2, outputForm.size());
        assertEquals("a", outputForm.getFirstValue("a"));
        assertEquals("b", outputForm.getFirstValue("b"));
    }
}
