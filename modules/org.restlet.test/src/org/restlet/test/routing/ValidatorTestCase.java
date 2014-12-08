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

package org.restlet.test.routing;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Validator;
import org.restlet.test.RestletTestCase;
import org.restlet.test.TraceRestlet;

/**
 * Test {@link org.restlet.routing.Validator}.
 * 
 * @author Jerome Louvel
 */
public class ValidatorTestCase extends RestletTestCase {

    public void testRequired() {
        // Create mock call
        Request rq = new Request();
        Response rs = new Response(rq);

        // Prepare the validator to test
        Validator validator = new Validator();
        validator.setNext(new TraceRestlet(null));
        validator.validatePresence("a");
        validator.handle(rq, rs);

        // Test if the absence of "a" is detected
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, rs.getStatus());

        // Test if the presence of "a" is ignored
        rq.getAttributes().put("a", "123");
        rs.setStatus(Status.SUCCESS_OK);
        validator.handle(rq, rs);
        assertEquals(Status.SUCCESS_OK, rs.getStatus());
    }

    public void testFormat() {
        // Create mock call
        Request rq = new Request();
        Response rs = new Response(rq);

        // Prepare the validator to test
        Validator validator = new Validator();
        validator.setNext(new TraceRestlet(null));
        validator.validateFormat("a", "\\d*");
        validator.handle(rq, rs);

        // Test if the absence of "a" is ignored
        assertEquals(Status.SUCCESS_OK, rs.getStatus());

        // Test if a wrong format of "a" is detected
        rq.getAttributes().put("a", "abc");
        rs.setStatus(Status.SUCCESS_OK);
        validator.handle(rq, rs);
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, rs.getStatus());

        // Test if a correct format of "a" is ignored
        rq.getAttributes().put("a", "123");
        rs.setStatus(Status.SUCCESS_OK);
        validator.handle(rq, rs);
        assertEquals(Status.SUCCESS_OK, rs.getStatus());
    }
}
