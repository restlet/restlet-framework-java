/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.data;

import org.junit.jupiter.api.Test;
import org.restlet.data.Status;
import org.restlet.test.RestletTestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link org.restlet.data.Status}.
 * 
 * @author Jerome Louvel
 */
public class StatusTestCase extends RestletTestCase {

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
