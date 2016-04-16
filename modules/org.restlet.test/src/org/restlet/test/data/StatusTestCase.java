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

package org.restlet.test.data;

import org.restlet.data.Status;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.Status}.
 * 
 * @author Jerome Louvel
 */
public class StatusTestCase extends RestletTestCase {

    public void testCustomDescription() {
        final String customDescription = "My custom description";
        final Status s = new Status(Status.CLIENT_ERROR_NOT_FOUND, customDescription);
        assertEquals(customDescription, s.getDescription());
    }

    /**
     * Equality tests.
     */
    public void testEquals() throws Exception {
        final Status s1 = new Status(201);
        final Status s2 = Status.SUCCESS_CREATED;

        assertTrue(s1.equals(s2));
        assertTrue(s1.getCode() == s2.getCode());
        assertEquals(s1, s2);

        assertTrue(s1.equals(s1));
        assertEquals(s1, s1);
    }

    /**
     * Tests for status classes.
     */
    public void testStatusClasses() {
        final Status s1 = new Status(287);
        assertTrue(s1.isSuccess());

        final Status s2 = Status.CLIENT_ERROR_BAD_REQUEST;
        assertTrue(s2.isClientError());
        assertTrue(s2.isError());
    }

    /**
     * Unequality tests.
     */
    public void testUnEquals() throws Exception {
        final Status s1 = new Status(200);
        final Status s2 = Status.SUCCESS_CREATED;

        assertFalse(s1.equals(s2));
        assertFalse(s1.getCode() == s2.getCode());
        assertFalse(s1.equals(null));
        assertFalse(s2.equals(null));
    }

}
