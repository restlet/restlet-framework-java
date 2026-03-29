/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * Test {@link org.restlet.routing.Validator}.
 *
 * @author Jerome Louvel
 */
class ValidatorTestCase {

    @Test
    void testRequired() {
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
    void testFormat() {
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
