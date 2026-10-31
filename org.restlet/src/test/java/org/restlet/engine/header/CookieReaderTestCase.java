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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.engine.util.DateUtils;

/**
 * Unit tests for the Cookie related classes.
 *
 * @author Jerome Louvel
 */
class CookieReaderTestCase {
    /**
     * Test one cookie header.
     *
     * @param headerValue The cookie header value.
     * @throws IOException
     */
    private void testCookie(String headerValue) throws IOException {
        CookieReader cr = new CookieReader(headerValue);
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie = cr.readValue();

        while (cookie != null) {
            cookies.add(cookie);
            cookie = cr.readValue();
        }

        // Rewrite the header
        String newHeaderValue = CookieWriter.write(cookies);

        // Compare initial and new headers
        assertEquals(headerValue, newHeaderValue);
    }

    /**
     * Test one cookie header.
     *
     * @param headerValue The cookie header value.
     * @throws IOException
     */
    private void testCookieValues(String headerValue) throws IOException {
        CookieReader cr = new CookieReader(headerValue);
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie = cr.readValue();
        while (cookie != null) {
            cookies.add(cookie);
            cookie = cr.readValue();
        }

        // Rewrite the header
        String newHeaderValue = CookieWriter.write(cookies);

        // Reparse
        List<Cookie> cookies2 = new ArrayList<>();
        cr = new CookieReader(newHeaderValue);
        cookie = cr.readValue();
        while (cookie != null) {
            cookies2.add(cookie);
            cookie = cr.readValue();
        }

        // Compare initial and new cookies
        assertEquals(cookies.size(), cookies2.size());
        for (int i = 0; i < cookies.size(); i++) {
            assertEquals(cookies.get(i).getName(), cookies2.get(i).getName());
            assertEquals(cookies.get(i).getValue(), cookies2.get(i).getValue());
        }
    }

    /**
     * Test a cookie date value.
     *
     * @param dateValue The cookie date value.
     */
    private void testCookieDate(String dateValue) {
        final Date date = DateUtils.parse(dateValue, DateUtils.FORMAT_RFC_1036);

        // Rewrite the date
        final String newDateValue = DateUtils.format(date, DateUtils.FORMAT_RFC_1036.getFirst());

        // Compare initial and new headers
        assertEquals(dateValue, newDateValue);
    }

    /**
     * Test one set cookie header.
     *
     * @param headerValue The set cookie header value.
     * @param compare Indicates if the new header should be compared with the old one.
     * @throws IOException
     */
    private void testCookieSetting(String headerValue, boolean compare) throws IOException {
        CookieSettingReader cr = new CookieSettingReader(headerValue);
        CookieSetting cookie = cr.readValue();

        // Rewrite the header
        String newHeaderValue = CookieSettingWriter.write(cookie);

        // Compare initial and new headers
        if (compare) {
            boolean result = newHeaderValue.toLowerCase().startsWith(headerValue.toLowerCase());
            assertTrue(result);
        }
    }

    /**
     * Test the lenient reading of a cookie header in the obsolete RFC 2109 format.
     *
     * @param headerValue The legacy cookie header value.
     * @param expected The expected RFC 6265 header once rewritten.
     */
    private void testLegacyCookie(String headerValue, String expected) throws IOException {
        CookieReader cr = new CookieReader(headerValue);
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie = cr.readValue();

        while (cookie != null) {
            assertNull(cookie.getPath(), "The legacy $Path must be ignored");
            assertNull(cookie.getDomain(), "The legacy $Domain must be ignored");
            cookies.add(cookie);
            cookie = cr.readValue();
        }

        assertEquals(expected, CookieWriter.write(cookies));
    }

    /**
     * Test a set cookie header in the obsolete RFC 2109 format.
     *
     * @param headerValue The legacy set cookie header value.
     * @param expected The expected RFC 6265 header once rewritten.
     */
    private void testCookieSetting(String headerValue, String expected) throws IOException {
        CookieSetting cookie = new CookieSettingReader(headerValue).readValue();
        assertEquals(expected, CookieSettingWriter.write(cookie));
    }

    @Test
    void read_commaIsNotASeparator() throws IOException {
        // RFC 6265 section 4.2.1: cookies are separated by semicolons only
        Cookie cookie = new CookieReader("a=1, b=2; c=3").readValue();
        assertEquals("a", cookie.getName());
        assertEquals("1, b=2", cookie.getValue());
    }

    /** Tests the parsing of cookies. */
    @Test
    void testParsing() throws IOException {
        // Netscape specification
        testCookie("CUSTOMER=WILE_E_COYOTE");
        testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001");
        testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001; SHIPPING=FEDEX");
        testCookie("NUMBER=RIDING_ROCKET_0023; PART_NUMBER=ROCKET_LAUNCHER_0001");

        testCookieSetting("CUSTOMER=WILE_E_COYOTE; path=/", true);
        testCookieSetting("PART_NUMBER=ROCKET_LAUNCHER_0001; path=/", true);
        testCookieSetting("SHIPPING=FEDEX; path=/foo", true);
        testCookieSetting("NUMBER=RIDING_ROCKET_0023; path=/ammo", true);

        testCookieDate("Tuesday, 09-Nov-99 23:12:40 GMT");

        // Obsolete RFC 2109 samples: read leniently, written back as RFC 6265
        testLegacyCookie(
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"",
                "Customer=WILE_E_COYOTE");
        testLegacyCookie(
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"",
                "Customer=WILE_E_COYOTE; Part_Number=Rocket_Launcher_0001");
        testLegacyCookie(
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"; Shipping=\"FedEx\"; $Domain=\"example.com\"",
                "Customer=WILE_E_COYOTE; Part_Number=Rocket_Launcher_0001; Shipping=FedEx");
        testLegacyCookie(
                "$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"",
                "Part_Number=Riding_Rocket_0023; Part_Number=Rocket_Launcher_0001");

        testCookieSetting(
                "Customer=\"WILE_E_COYOTE\"; Version=\"1\"; Path=\"/acme\"; Comment=\"x\"; Discard",
                "Customer=WILE_E_COYOTE; Path=/acme");
        testCookieSetting(
                "Part_Number=\"Riding_Rocket_0023\"; Version=\"1\"; Path=\"/acme/ammo\"",
                "Part_Number=Riding_Rocket_0023; Path=/acme/ammo");

        // Bug #49
        testCookieSetting(
                "RMS_ADMETA_VISITOR_RMS=27756847%3A240105; expires=Thu, 02 Mar 2006 21:09:00 GMT; path=/; domain=.admeta.com",
                false);
        testCookieValues(
                "Cookie 1=One; Cookie 2=Two; Cookie 3=Three; Cookie 4=Four; Cookie 5=\"Five\"; Cookie 6=\"Six\"");
    }

    @Test
    void testParsingTooLongMaxAgeShouldBeCapedToIntegerMAX_VALUE() throws IOException {
        CookieSettingReader cr =
                new CookieSettingReader(
                        "RMS_ADMETA_VISITOR_RMS=27756847%3A240105; max-age=31536000000; path=/; domain=.admeta.com");
        CookieSetting cookie = cr.readValue();
        assertEquals(Integer.MAX_VALUE, cookie.getMaxAge());
    }
}
