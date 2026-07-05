/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.restlet.engine.util.DateUtils;

class DateWriterTestCase {

    private static Date date(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(year, month, day, hour, minute, second);
        return cal.getTime();
    }

    @Test
    void write_defaultFormat_matchesDateUtilsDefault() {
        Date date = date(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals(DateUtils.format(date), DateWriter.write(date));
    }

    @Test
    void write_cookieFalse_matchesDefaultFormat() {
        Date date = date(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals(DateWriter.write(date), DateWriter.write(date, false));
    }

    @Test
    void write_cookieTrue_matchesRfc1036Format() {
        Date date = date(2024, Calendar.MARCH, 5, 13, 7, 9);
        assertEquals(
                DateUtils.format(date, DateUtils.FORMAT_RFC_1036.getFirst()),
                DateWriter.write(date, true));
    }
}
