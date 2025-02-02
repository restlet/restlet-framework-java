/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.routing;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link org.restlet.routing.Validator}.
 *
 * @author Jerome Louvel
 */
public class ValidatorTestCase {

    @Test
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

    @Test
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
