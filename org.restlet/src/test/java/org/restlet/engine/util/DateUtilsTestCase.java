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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.Test;

/**
 * Test {@link DateUtils}
 *
 * @author Jerome Louvel
 */
class DateUtilsTestCase {

    private static final String DATE_RFC3339_1 = "1985-04-12T23:20:50.52Z";
    private static final String DATE_RFC3339_2 = "1996-12-19T16:39:57-08:00";
    private static final String DATE_RFC3339_3 = "1990-12-31T23:59:60Z";
    private static final String DATE_RFC3339_4 = "1990-12-31T15:59:60-08:00";
    private static final String DATE_RFC3339_5 = "1937-01-01T12:00:27.87+00:20";
    private static final String DATE_ASC_1 = "Fri Apr 12 23:20:50 1985";
    private static final String DATE_RFC1036_1 = "Friday, 12-Apr-85 23:20:50 GMT";
    private static final String DATE_RFC1123_1 = "Fri, 12 Apr 1985 23:20:50 GMT";
    private static final String DATE_RFC822_1 = "Fri, 12 Apr 85 23:20:50 GMT";

    /** Tests for dates in the RFC 822 format. */
    @Test
    void testRfc822() {
        Date date1 = DateUtils.parse(DATE_RFC822_1, DateUtils.FORMAT_RFC_822);

        String dateFormat1 = DateUtils.format(date1, DateUtils.FORMAT_RFC_822.getFirst());

        assertEquals(DATE_RFC822_1, dateFormat1);
    }

    /** Tests for dates in the RFC 1123 format. */
    @Test
    void testRfc1123() {
        Date date1 = DateUtils.parse(DATE_RFC1123_1, DateUtils.FORMAT_RFC_1123);

        String dateFormat1 = DateUtils.format(date1, DateUtils.FORMAT_RFC_1123.getFirst());

        assertEquals(DATE_RFC1123_1, dateFormat1);
    }

    /** Tests for dates in the RFC 1036 format. */
    @Test
    void testRfc1036() {
        Date date1 = DateUtils.parse(DATE_RFC1036_1, DateUtils.FORMAT_RFC_1036);

        String dateFormat1 = DateUtils.format(date1, DateUtils.FORMAT_RFC_1036.getFirst());

        assertEquals(DATE_RFC1036_1, dateFormat1);
    }

    /** Tests for dates in the RFC 3339 format. */
    @Test
    void testAsc() {
        Date date1 = DateUtils.parse(DATE_ASC_1, DateUtils.FORMAT_ASC_TIME);

        String dateFormat1 = DateUtils.format(date1, DateUtils.FORMAT_ASC_TIME.getFirst());

        assertEquals(DATE_ASC_1, dateFormat1);
    }

    /** Tests for dates in the RFC 3339 format. */
    @Test
    void testRfc3339() {
        Date date1 = DateUtils.parse(DATE_RFC3339_1, DateUtils.FORMAT_RFC_3339);
        Date date2 = DateUtils.parse(DATE_RFC3339_2, DateUtils.FORMAT_RFC_3339);
        Date date3 = DateUtils.parse(DATE_RFC3339_3, DateUtils.FORMAT_RFC_3339);
        Date date4 = DateUtils.parse(DATE_RFC3339_4, DateUtils.FORMAT_RFC_3339);
        Date date5 = DateUtils.parse(DATE_RFC3339_5, DateUtils.FORMAT_RFC_3339);

        String dateFormat1 = DateUtils.format(date1, DateUtils.FORMAT_RFC_3339.getFirst());
        String dateFormat2 = DateUtils.format(date2, DateUtils.FORMAT_RFC_3339.getFirst());
        String dateFormat3 = DateUtils.format(date3, DateUtils.FORMAT_RFC_3339.getFirst());
        String dateFormat4 = DateUtils.format(date4, DateUtils.FORMAT_RFC_3339.getFirst());
        String dateFormat5 = DateUtils.format(date5, DateUtils.FORMAT_RFC_3339.getFirst());

        assertEquals(DATE_RFC3339_1, dateFormat1);
        assertEquals("1996-12-20T00:39:57Z", dateFormat2);
        assertEquals("1991-01-01T00:00:00Z", dateFormat3);
        assertEquals("1991-01-01T00:00:00Z", dateFormat4);
        assertEquals("1937-01-01T11:40:27.87Z", dateFormat5);
    }

    @Test
    void unmodifiableDates() {
        Date now = new Date();
        Calendar yesterdayCal = new GregorianCalendar();
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);

        Date yesterday = yesterdayCal.getTime();

        assertTrue(now.after(yesterday));
        assertTrue(now.after(DateUtils.unmodifiable(yesterday)));
        assertTrue(DateUtils.unmodifiable(now).after(yesterday));
        assertTrue(DateUtils.unmodifiable(now).after(DateUtils.unmodifiable(yesterday)));

        assertTrue(yesterday.before(DateUtils.unmodifiable(now)));
        assertTrue(DateUtils.unmodifiable(yesterday).before(DateUtils.unmodifiable(now)));
        assertTrue(DateUtils.unmodifiable(yesterday).before(now));
    }
}
