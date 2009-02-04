/*
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
            final StringBuilder stb = new StringBuilder();
            final Form headers = getHttpHeaders(request);
            for (final Parameter header : headers) {
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

    /**
     * Name of a test header field
     */
    private static final String TEST_HEADER = "testHeader";

    /**
     * Returns the list of http headers of a request as a Form.
     * 
     * @param request
     *            The request.
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
     *            The list of parameters used to build the request.
     * @return The response of the request.
     */
    private Response getWithParams(Parameter... parameters) {
        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, "http://localhost:"
                + getTestPort());
        final Form headers = getHttpHeaders(request);
        for (final Parameter p : parameters) {
            headers.add(p);
        }

        Response result = client.handle(request);
        return result;
    }

    @Override
    public void setUp() throws Exception {
        if (this.component == null) {
            this.component = new Component();
            this.component.getServers().add(Protocol.HTTP, getTestPort());
            this.component.getDefaultHost().attachDefault(
                    new TestHeaderRestlet());
        }
        if (!this.component.isStarted()) {
            this.component.start();
        }
    }

    @Override
    public void tearDown() throws Exception {
        this.component.stop();
    }

    /** test with no test header */
    public void test0() throws Exception {
        final Response response = getWithParams();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(null, response.getEntity().getText());
    }

    /** test with one test header */
    public void test1() throws Exception {
        final Response response = getWithParams(new Parameter(TEST_HEADER, "a"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\n", response.getEntity().getText());
    }

    /** test with two test headers */
    public void test2() throws Exception {
        final Response response = getWithParams(
                new Parameter(TEST_HEADER, "a"),
                new Parameter(TEST_HEADER, "b"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\nb\n", response.getEntity().getText());
    }
}
