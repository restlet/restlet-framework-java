/*
 * Copyright 2005-2008 Noelios Technologies.
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

import org.restlet.data.Status;

/**
 * Test {@link org.restlet.data.Status}.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StatusTestCase extends RestletTestCase {

    public void testCustomDescription() {
        final String customDescription = "My custom description";
        final Status s = new Status(Status.CLIENT_ERROR_NOT_FOUND,
                customDescription);
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
