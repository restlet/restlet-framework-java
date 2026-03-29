/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.engine.Engine;

/**
 * Test {@link org.restlet.data.Status}.
 *
 * @author Jerome Louvel
 */
class StatusTestCase {

    @Test
    void testCustomDescription() {
        final String customDescription = "My custom description";
        final Status s = new Status(Status.CLIENT_ERROR_NOT_FOUND, customDescription);
        assertEquals(customDescription, s.getDescription());
    }

    /** Equality tests. */
    @Test
    void testEquals() {
        final Status s1 = new Status(201);
        final Status s2 = Status.SUCCESS_CREATED;

        assertEquals(s1, s2);
        assertEquals(s1.getCode(), s2.getCode());
        assertEquals(s1, s2);
    }

    /** Tests for status classes. */
    @Test
    void testStatusClasses() {
        final Status s1 = new Status(287);
        assertTrue(s1.isSuccess());

        final Status s2 = Status.CLIENT_ERROR_BAD_REQUEST;
        assertTrue(s2.isClientError());
        assertTrue(s2.isError());
    }

    /** Unequality tests. */
    @Test
    void testUnEquals() {
        final Status s1 = new Status(200);
        final Status s2 = Status.SUCCESS_CREATED;

        assertNotEquals(s1, s2);
        assertNotEquals(s1.getCode(), s2.getCode());
        assertNotEquals(null, s1);
        assertNotEquals(null, s2);
    }

    @Test
    void testValueOfForKnownStatusCodes() {
        assertEquals(Status.INFO_CONTINUE, Status.valueOf(100));
        assertEquals(Status.SUCCESS_OK, Status.valueOf(200));
        assertEquals(Status.CONNECTOR_ERROR_INTERNAL, Status.valueOf(1002));
    }

    @Test
    void testDescriptionForKnownStatusCodes() {
        assertEquals(
                "The client should continue with its request",
                Status.INFO_CONTINUE.getDescription());
        assertEquals(
                "The client should continue with its request",
                Status.valueOf(100).getDescription());
        assertEquals("The request has succeeded", Status.valueOf(200).getDescription());
        assertEquals(
                "The connector encountered an unexpected condition which prevented it from fulfilling the request",
                Status.CONNECTOR_ERROR_INTERNAL.getDescription());
    }

    @Test
    void testReasonPhraseForKnownStatusCodes() {
        assertEquals("Continue", Status.INFO_CONTINUE.getReasonPhrase());
        assertEquals("Continue", Status.valueOf(100).getReasonPhrase());
        assertEquals("OK", Status.valueOf(200).getReasonPhrase());
        assertEquals("Internal Connector Error", Status.CONNECTOR_ERROR_INTERNAL.getReasonPhrase());
    }

    @Test
    void testUriForKnownStatusCodes() {
        assertEquals(
                "https://www.rfc-editor.org/rfc/rfc9110.html#name-100-continue",
                Status.INFO_CONTINUE.getUri());
        assertEquals(
                "https://www.rfc-editor.org/rfc/rfc9110.html#name-100-continue",
                Status.valueOf(100).getUri());
        assertEquals(
                "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes",
                Status.valueOf(299).getUri());
        assertEquals(
                "https://javadoc.io/static/org.restlet/org.restlet/"
                        + Engine.VERSION
                        + "/org/restlet/data/Status.html#CONNECTOR_ERROR_INTERNAL",
                Status.CONNECTOR_ERROR_INTERNAL.getUri());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 50, 1000, -10})
    void testValueOfForUnknownStatusCodes(final int statusCode) {
        final Status actual = Status.valueOf(statusCode);
        assertNotNull(actual);
        assertEquals(new Status(statusCode), actual);
    }
}
