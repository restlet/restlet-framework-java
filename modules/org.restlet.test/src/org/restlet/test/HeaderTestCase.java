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

package org.restlet.test;

import org.restlet.Client;
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
import org.restlet.resource.StringRepresentation;

public class HeaderTestCase extends RestletTestCase {

    /**
     * Restlet that returns as a new Representation the list of values of
     * "testHeader" header.
     * 
     */
    public static class TestHeaderRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            StringBuilder stb = new StringBuilder();
            Form headers = getHttpHeaders(request);
            for (Parameter header : headers) {
                if (header.getName().equals(TEST_HEADER)) {
                    stb.append(header.getValue());
                    stb.append('\n');
                }
            }
            response.setEntity(new StringRepresentation(stb,
                    MediaType.TEXT_PLAIN));
        }
    }

    private static final String HTTP_HEADERS = "org.restlet.http.headers";

    private static final int PORT = 8182;

    /**
     * Name of a test header field
     */
    private static final String TEST_HEADER = "testHeader";

    /**
     * Returns the list of http headers of a request as a Form.
     * 
     * @param request
     *                The request.
     * @return The list of headers as a Form object.
     */
    private static Form getHttpHeaders(Request request) {
        Form headers = (Form) request.getAttributes().get(HTTP_HEADERS);
        if (headers == null) {
            headers = new Form();
            request.getAttributes().put(HTTP_HEADERS, headers);
        }
        return headers;
    }

    private Component component;

    /**
     * Handle a new request built according to the parameters and return the
     * response object.
     * 
     * @param parameters
     *                The list of parameters used to build the request.
     * @return The response of the request.
     */
    private Response getWithParams(Parameter... parameters) {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.GET, "http://localhost:" + PORT);
        Form headers = getHttpHeaders(request);
        for (Parameter p : parameters)
            headers.add(p);

        return client.handle(request);
    }

    @Override
    public void setUp() throws Exception {
        if (this.component == null) {
            this.component = new Component();
            this.component.getServers().add(Protocol.HTTP, PORT);
            component.getDefaultHost().attachDefault(new TestHeaderRestlet());
        }
        if (!component.isStarted()) {
            component.start();
        }
    }

    @Override
    public void tearDown() throws Exception {
        component.stop();
    }

    /** test with no test header */
    public void test0() throws Exception {
        Response response = getWithParams();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(null, response.getEntity().getText());
    }

    /** test with one test header */
    public void test1() throws Exception {
        Response response = getWithParams(new Parameter(TEST_HEADER, "a"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\n", response.getEntity().getText());
    }

    /** test with two test headers */
    public void test2() throws Exception {
        Response response = getWithParams(new Parameter(TEST_HEADER, "a"),
                new Parameter(TEST_HEADER, "b"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\nb\n", response.getEntity().getText());
    }
}