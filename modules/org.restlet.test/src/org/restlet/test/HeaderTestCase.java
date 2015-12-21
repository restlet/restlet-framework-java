/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

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
            Series<Header> headers = getHttpHeaders(request);

            for (Header header : headers) {
                if (TEST_HEADER.equalsIgnoreCase(header.getName())) {
                    stb.append(header.getValue());
                    stb.append('\n');
                }
            }

            response.setEntity(new StringRepresentation(stb,
                    MediaType.TEXT_PLAIN));
        }
    }

    /**
     * Name of a test header field
     */
    private static final String TEST_HEADER = "testHeader";

    /**
     * Returns the list of HTTP headers of a request as a Form.
     * 
     * @param request
     *            The request.
     * @return The list of headers as a Form object.
     */
    private static Series<Header> getHttpHeaders(Request request) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) request.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);

        if (headers == null) {
            headers = new Series<Header>(Header.class);
            request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    headers);
        }

        return headers;
    }

    private Client client;

    private Component component;

    /**
     * Handle a new request built according to the parameters and return the
     * response object.
     * 
     * @param additionalHeaders
     *            The list of header used to build the request.
     * @return The response of the request.
     * @throws Exception
     */
    private Response getWithParams(Header... additionalHeaders)
            throws Exception {
        Request request = new Request(Method.GET, "http://localhost:"
                + TEST_PORT);
        Series<Header> headers = getHttpHeaders(request);

        for (Header header : additionalHeaders) {
            headers.add(header);
        }

        Response result = client.handle(request);
        return result;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.client = new Client(Protocol.HTTP);

        if (this.component == null) {
            this.component = new Component();
            this.component.getServers().add(Protocol.HTTP, TEST_PORT);
            this.component.getDefaultHost().attachDefault(
                    new TestHeaderRestlet());
        }

        if (!this.component.isStarted()) {
            this.component.start();
        }
    }

    @Override
    public void tearDown() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
        super.tearDown();
    }

    /** test with no test header */
    public void test0() throws Exception {
        Response response = getWithParams();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(null, response.getEntity().getText());
    }

    /** test with one test header */
    public void test1() throws Exception {
        Response response = getWithParams(new Header(TEST_HEADER, "a"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\n", response.getEntity().getText());
    }

    /** test with two test headers */
    public void test2() throws Exception {
        Response response = getWithParams(new Header(TEST_HEADER, "a"),
                new Header(TEST_HEADER, "b"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a\nb\n", response.getEntity().getText());
    }
}
