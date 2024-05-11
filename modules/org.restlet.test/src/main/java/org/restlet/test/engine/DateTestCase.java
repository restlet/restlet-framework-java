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

package org.restlet.test.engine;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.restlet.engine.util.DateUtils;
import org.restlet.test.RestletTestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link DateUtils}
 * 
 * @author Jerome Louvel
 */
public class DateTestCase extends RestletTestCase {

    private final String DATE_RFC3339_1 = "1985-04-12T23:20:50.52Z";

    private final String DATE_RFC3339_2 = "1996-12-19T16:39:57-08:00";

    private final String DATE_RFC3339_3 = "1990-12-31T23:59:60Z";

    private final String DATE_RFC3339_4 = "1990-12-31T15:59:60-08:00";

    private final String DATE_RFC3339_5 = "1937-01-01T12:00:27.87+00:20";

    private final String DATE_ASC_1 = "Fri Apr 12 23:20:50 1985";

    private final String DATE_RFC1036_1 = "Friday, 12-Apr-85 23:20:50 GMT";

    private final String DATE_RFC1123_1 = "Fri, 12 Apr 1985 23:20:50 GMT";

    private final String DATE_RFC822_1 = "Fri, 12 Apr 85 23:20:50 GMT";

    /**
     * Tests for dates in the RFC 822 format.
     */
    @Test
    public void testRfc822() {
        Date date1 = DateUtils.parse(DATE_RFC822_1, DateUtils.FORMAT_RFC_822);

        String dateFormat1 = DateUtils.format(date1,
                DateUtils.FORMAT_RFC_822.get(0));

        assertEquals(DATE_RFC822_1, dateFormat1);
    }

    /**
     * Tests for dates in the RFC 1123 format.
     */
    @Test
    public void testRfc1123() {
        Date date1 = DateUtils.parse(DATE_RFC1123_1, DateUtils.FORMAT_RFC_1123);

        String dateFormat1 = DateUtils.format(date1,
                DateUtils.FORMAT_RFC_1123.get(0));

        assertEquals(DATE_RFC1123_1, dateFormat1);
    }

    /**
     * Tests for dates in the RFC 1036 format.
     */
    @Test
    public void testRfc1036() {
        Date date1 = DateUtils.parse(DATE_RFC1036_1, DateUtils.FORMAT_RFC_1036);

        String dateFormat1 = DateUtils.format(date1,
                DateUtils.FORMAT_RFC_1036.get(0));

        assertEquals(DATE_RFC1036_1, dateFormat1);
    }

    /**
     * Tests for dates in the RFC 3339 format.
     */
    @Test
    public void testAsc() {
        Date date1 = DateUtils.parse(DATE_ASC_1, DateUtils.FORMAT_ASC_TIME);

        String dateFormat1 = DateUtils.format(date1,
                DateUtils.FORMAT_ASC_TIME.get(0));

        assertEquals(DATE_ASC_1, dateFormat1);
    }

    /**
     * Tests for dates in the RFC 3339 format.
     */
    @Test
    public void testRfc3339() {
        Date date1 = DateUtils.parse(DATE_RFC3339_1, DateUtils.FORMAT_RFC_3339);
        Date date2 = DateUtils.parse(DATE_RFC3339_2, DateUtils.FORMAT_RFC_3339);
        Date date3 = DateUtils.parse(DATE_RFC3339_3, DateUtils.FORMAT_RFC_3339);
        Date date4 = DateUtils.parse(DATE_RFC3339_4, DateUtils.FORMAT_RFC_3339);
        Date date5 = DateUtils.parse(DATE_RFC3339_5, DateUtils.FORMAT_RFC_3339);

        String dateFormat1 = DateUtils.format(date1,
                DateUtils.FORMAT_RFC_3339.get(0));
        String dateFormat2 = DateUtils.format(date2,
                DateUtils.FORMAT_RFC_3339.get(0));
        String dateFormat3 = DateUtils.format(date3,
                DateUtils.FORMAT_RFC_3339.get(0));
        String dateFormat4 = DateUtils.format(date4,
                DateUtils.FORMAT_RFC_3339.get(0));
        String dateFormat5 = DateUtils.format(date5,
                DateUtils.FORMAT_RFC_3339.get(0));

        assertEquals(DATE_RFC3339_1, dateFormat1);
        assertEquals("1996-12-20T00:39:57Z", dateFormat2);
        assertEquals("1991-01-01T00:00:00Z", dateFormat3);
        assertEquals("1991-01-01T00:00:00Z", dateFormat4);
        assertEquals("1937-01-01T11:40:27.87Z", dateFormat5);
    }
}
