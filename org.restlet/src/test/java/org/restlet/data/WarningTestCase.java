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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Warning}. */
class WarningTestCase {

    @Test
    void defaultConstructor_hasNullFields() {
        Warning warning = new Warning();
        assertNull(warning.getAgent());
        assertNull(warning.getDate());
        assertNull(warning.getStatus());
        assertNull(warning.getText());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        Warning warning = new Warning();

        warning.setAgent("agent");
        assertEquals("agent", warning.getAgent());

        Date date = new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli());
        warning.setDate(date);
        assertEquals(date, warning.getDate());

        warning.setStatus(Status.SUCCESS_OK);
        assertEquals(Status.SUCCESS_OK, warning.getStatus());

        warning.setText("text");
        assertEquals("text", warning.getText());
    }
}
