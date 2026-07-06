/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ImmutableDateTestCase {

    @Test
    void constructor_copiesTimeFromSource() {
        Date source = new Date(123456789L);
        ImmutableDate immutable = new ImmutableDate(source);
        assertEquals(source.getTime(), immutable.getTime());
    }

    @Test
    void clone_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, immutable::clone);
    }

    @Test
    void setDate_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setDate(1));
    }

    @Test
    void setHours_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setHours(1));
    }

    @Test
    void setMinutes_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setMinutes(1));
    }

    @Test
    void setMonth_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(
                UnsupportedOperationException.class, () -> immutable.setMonth(Calendar.AUGUST));
    }

    @Test
    void setSeconds_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setSeconds(1));
    }

    @Test
    void setTime_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setTime(1));
    }

    @Test
    void setYear_throwsUnsupportedOperationException() {
        ImmutableDate immutable =
                new ImmutableDate(new Date(Instant.parse("2026-05-07T10:00:00Z").toEpochMilli()));
        assertThrows(UnsupportedOperationException.class, () -> immutable.setYear(1));
    }
}
