/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Digest;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Test {@link org.restlet.engine.util.DateUtils}.
 * 
 * @author Thierry Boileau
 */
public class DigestTestCase extends RestletTestCase {

    /** Component used for the tests. */
    private Component component;

    /**
     * Internal class used for test purpose.
     * 
     */
    private static class TestDigestApplication extends Application {

        @Override
        public Restlet createRoot() {
            Restlet restlet = new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    Representation rep = request.getEntity();
                    StringRepresentation f = new StringRepresentation(
                            "9876543210");
                    f.setDigest(f.computeDigest(Digest.ALGORITHM_MD5));
                    response.setEntity(f);
                    if (rep.checkDigest(Digest.ALGORITHM_MD5)) {
                        response.setStatus(Status.SUCCESS_OK);
                    } else {
                        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }

            };
            return restlet;
        }
    }

    @Override
    protected void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, TEST_PORT);
        component.getDefaultHost().attach(new TestDigestApplication());
        component.start();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
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
        rep.setDigest(rep.computeDigest(Digest.ALGORITHM_MD5));
        request.setEntity(rep);
        Response response = client.handle(request);

        assertTrue(response.getEntity().checkDigest(Digest.ALGORITHM_MD5));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }
}
