/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.representation;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.DigesterRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.engine.util.DateUtils}.
 * 
 * @author Thierry Boileau
 */
public class DigesterRepresentationTestCase extends RestletTestCase {

    /** Component used for the tests. */
    private Component component;

    /**
     * Internal class used for test purpose.
     * 
     */
    private static class TestDigestApplication extends Application {

        @Override
        public Restlet createInboundRoot() {
            Restlet restlet = new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    Representation rep = request.getEntity();
                    try {
                        // Such representation computes the digest while
                        // consuming the wrapped representation.
                        DigesterRepresentation digester = new DigesterRepresentation(
                                rep);
                        digester.exhaust();
                        if (digester.checkDigest()) {
                            response.setStatus(Status.SUCCESS_OK);
                            StringRepresentation f = new StringRepresentation(
                                    "9876543210");
                            digester = new DigesterRepresentation(f);
                            // Consume first
                            digester.exhaust();
                            // Set the digest
                            digester.setDigest(digester.computeDigest());
                            response.setEntity(digester);
                        } else {
                            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } catch (Exception e1) {
                    }
                }

            };
            return restlet;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        component = new Component();
        component.getServers().add(Protocol.HTTP, TEST_PORT);
        component.getDefaultHost().attach(new TestDigestApplication());
        component.start();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        super.tearDown();
    }

    /**
     * Tests partial Get requests.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testGet() throws IOException, NoSuchAlgorithmException {
        Client client = new Client(Protocol.HTTP);

        // Test partial Get.
        Request request = new Request(Method.PUT, "http://localhost:"
                + TEST_PORT + "/");
        StringRepresentation rep = new StringRepresentation("0123456789");
        try {
            DigesterRepresentation digester = new DigesterRepresentation(rep);
            // Such representation computes the digest while
            // consuming the wrapped representation.
            digester.exhaust();
            // Set the digest with the computed one
            digester.setDigest(digester.computeDigest());
            request.setEntity(digester);

            Response response = client.handle(request);

            assertEquals(Status.SUCCESS_OK, response.getStatus());
            digester = new DigesterRepresentation(response.getEntity());
            digester.exhaust();
            assertTrue(digester.checkDigest());

            client.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
