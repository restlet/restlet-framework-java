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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class InternetDateFormatTestCase {

    @Test
    void toString_calendarUtc_formatsWithZSuffix() {
        Calendar cal = new GregorianCalendar(InternetDateFormat.UTC);
        cal.clear();
        cal.set(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals("2024-03-05T13:07:09Z", InternetDateFormat.toString(cal));
    }

    @Test
    void toString_calendarWithMilliseconds_includesFraction() {
        Calendar cal = new GregorianCalendar(InternetDateFormat.UTC);
        cal.clear();
        cal.set(2024, Calendar.MARCH, 5, 13, 7, 9);
        cal.set(Calendar.MILLISECOND, 500);
        assertEquals("2024-03-05T13:07:09.50Z", InternetDateFormat.toString(cal));
    }

    @Test
    void toString_calendarWithPositiveOffset_formatsSignedOffset() {
        TimeZone plusTwo = TimeZone.getTimeZone("GMT+02:00");
        Calendar cal = new GregorianCalendar(plusTwo);
        cal.clear();
        cal.set(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals("2024-03-05T13:07:09+02:00", InternetDateFormat.toString(cal));
    }

    @Test
    void toString_calendarWithNegativeOffset_formatsSignedOffset() {
        TimeZone minusFive = TimeZone.getTimeZone("GMT-05:00");
        Calendar cal = new GregorianCalendar(minusFive);
        cal.clear();
        cal.set(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals("2024-03-05T13:07:09-05:00", InternetDateFormat.toString(cal));
    }

    @Test
    void toString_date_usesUtcByDefault() {
        Date date = InternetDateFormat.parseDate("2024-03-05T13:07:09Z");
        assertEquals("2024-03-05T13:07:09Z", InternetDateFormat.toString(date));
    }

    @Test
    void toString_time_usesUtcByDefault() {
        long time = InternetDateFormat.parseTime("2024-03-05T13:07:09Z");
        assertEquals("2024-03-05T13:07:09Z", InternetDateFormat.toString(time));
    }

    @Test
    void now_returnsParseableString() {
        String now = InternetDateFormat.now();
        assertNotNull(InternetDateFormat.parseDate(now));
    }

    @Test
    void now_withZone_returnsParseableString() {
        String now = InternetDateFormat.now(TimeZone.getTimeZone("GMT+01:00"));
        assertNotNull(InternetDateFormat.parseDate(now));
    }

    @Test
    void parse_zuluTimeZone_parsesAsUtc() {
        Calendar cal = InternetDateFormat.parseCalendar("2024-03-05T13:07:09z");
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.getTimeZone().getRawOffset());
    }

    @Test
    void parse_explicitOffset_parsesTimeZone() {
        Calendar cal = InternetDateFormat.parseCalendar("2024-03-05T13:07:09+02:30");
        assertEquals(2 * 60 * 60000 + 30 * 60000, cal.getTimeZone().getRawOffset());
    }

    @Test
    void parse_negativeOffset_parsesTimeZone() {
        Calendar cal = InternetDateFormat.parseCalendar("2024-03-05T13:07:09-02:30");
        assertEquals(-(2 * 60 * 60000 + 30 * 60000), cal.getTimeZone().getRawOffset());
    }

    @Test
    void parse_withFractionalSeconds_setsMilliseconds() {
        Calendar cal = InternetDateFormat.parseCalendar("2024-03-05T13:07:09.5Z");
        assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void parse_spaceSeparator_isAccepted() {
        Calendar cal = InternetDateFormat.parseCalendar("2024-03-05 13:07:09Z");
        assertEquals(2024, cal.get(Calendar.YEAR));
    }

    @Test
    void parse_invalidString_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class, () -> InternetDateFormat.parseDate("not-a-date"));
    }

    @Test
    void valueOf_date_roundTripsToSameInstant() {
        Date date = new Date(1_700_000_000_000L);
        InternetDateFormat format = InternetDateFormat.valueOf(date);
        assertEquals(date, format.getDate());
        assertEquals(date.getTime(), format.getTime());
    }

    @Test
    void valueOf_dateWithZone_roundTripsToSameInstant() {
        Date date = new Date(1_700_000_000_000L);
        InternetDateFormat format =
                InternetDateFormat.valueOf(date, TimeZone.getTimeZone("GMT+01:00"));
        assertEquals(date, format.getDate());
    }

    @Test
    void valueOf_time_roundTripsToSameInstant() {
        long time = 1_700_000_000_000L;
        InternetDateFormat format = InternetDateFormat.valueOf(time);
        assertEquals(time, format.getTime());
    }

    @Test
    void valueOf_timeWithZone_roundTripsToSameInstant() {
        long time = 1_700_000_000_000L;
        InternetDateFormat format =
                InternetDateFormat.valueOf(time, TimeZone.getTimeZone("GMT+01:00"));
        assertEquals(time, format.getTime());
    }

    @Test
    void valueOf_string_parsesToMatchingCalendar() {
        InternetDateFormat format = InternetDateFormat.valueOf("2024-03-05T13:07:09Z");
        assertEquals("2024-03-05T13:07:09Z", format.toString());
    }

    @Test
    void constructor_fromCalendar_clonesInput() {
        Calendar cal = new GregorianCalendar(InternetDateFormat.UTC);
        cal.clear();
        cal.set(2024, Calendar.MARCH, 5, 13, 7, 9);
        InternetDateFormat format = new InternetDateFormat(cal);

        cal.set(Calendar.YEAR, 1999);

        assertEquals(2024, format.getCalendar().get(Calendar.YEAR));
    }

    @Test
    void constructor_defaultZone_usesUtc() {
        InternetDateFormat format = new InternetDateFormat();
        assertEquals(0, format.getCalendar().getTimeZone().getRawOffset());
    }

    @Test
    void constructor_withZoneOnly_setsCurrentTimeInThatZone() {
        InternetDateFormat format = new InternetDateFormat(TimeZone.getTimeZone("GMT+01:00"));
        assertEquals(60 * 60000, format.getCalendar().getTimeZone().getRawOffset());
    }

    @Test
    void format_appendsRfc3339RepresentationToBuffer() {
        Date date = InternetDateFormat.parseDate("2024-03-05T13:07:09Z");
        StringBuffer buffer = new StringBuffer("prefix-");
        InternetDateFormat format = new InternetDateFormat();
        format.format(date, buffer, null);
        assertEquals("prefix-2024-03-05T13:07:09Z", buffer.toString());
    }

    @Test
    void parse_instanceMethod_delegatesToStaticParseDate() throws Exception {
        InternetDateFormat format = new InternetDateFormat();
        Date date = format.parse("2024-03-05T13:07:09Z");
        assertEquals(InternetDateFormat.parseDate("2024-03-05T13:07:09Z"), date);
    }

    @Test
    void parse_instanceMethodWithPosition_delegatesToStaticParseDate() {
        InternetDateFormat format = new InternetDateFormat();
        Date date = format.parse("2024-03-05T13:07:09Z", new ParsePosition(0));
        assertEquals(InternetDateFormat.parseDate("2024-03-05T13:07:09Z"), date);
    }

    @Test
    void utc_hasZeroOffsetAndNoDst() {
        assertEquals(0, InternetDateFormat.UTC.getRawOffset());
        assertEquals(0, InternetDateFormat.UTC.getDSTSavings());
    }
}
