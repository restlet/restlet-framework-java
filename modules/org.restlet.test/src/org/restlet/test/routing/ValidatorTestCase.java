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
