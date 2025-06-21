/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test {@link org.restlet.data.Status}.
 * 
 * @author Jerome Louvel
 */
public class StatusTestCase {

    @Test
    public void testCustomDescription() {
        final String customDescription = "My custom description";
        final Status s = new Status(Status.CLIENT_ERROR_NOT_FOUND, customDescription);
        assertEquals(customDescription, s.getDescription());
    }

    /**
     * Equality tests.
     */
    @Test
    public void testEquals() {
        final Status s1 = new Status(201);
        final Status s2 = Status.SUCCESS_CREATED;

        assertEquals(s1, s2);
        assertEquals(s1.getCode(), s2.getCode());
        assertEquals(s1, s2);
    }

    /**
     * Tests for status classes.
     */
    @Test
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
    @Test
    public void testUnEquals() {
        final Status s1 = new Status(200);
        final Status s2 = Status.SUCCESS_CREATED;

        assertNotEquals(s1, s2);
        assertNotEquals(s1.getCode(), s2.getCode());
        assertNotEquals(null, s1);
        assertNotEquals(null, s2);
    }

}
