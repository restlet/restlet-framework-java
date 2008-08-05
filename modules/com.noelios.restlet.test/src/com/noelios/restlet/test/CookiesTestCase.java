/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.util.DateUtils;

import com.noelios.restlet.http.CookieReader;
import com.noelios.restlet.http.CookieUtils;

/**
 * Unit tests for the Cookie related classes.
 * 
 * @author Jerome Louvel
 */
public class CookiesTestCase extends TestCase {
    /**
     * Test one cookie header.
     * 
     * @param headerValue
     *            The cookie header value.
     * @throws IOException
     */
    private void testCookie(String headerValue) throws IOException {
        final CookieReader cr = new CookieReader(Logger
                .getLogger(CookiesTestCase.class.getCanonicalName()),
                headerValue);
        final List<Cookie> cookies = new ArrayList<Cookie>();
        Cookie cookie = cr.readCookie();

        while (cookie != null) {
            cookies.add(cookie);
            cookie = cr.readCookie();
        }

        // Rewrite the header
        final String newHeaderValue = CookieUtils.format(cookies);

        // Compare initial and new headers
        assertEquals(headerValue, newHeaderValue);
    }

    /**
     * Test a cookie date value.
     * 
     * @param headerValue
     *            The cookie date value.
     */
    private void testCookieDate(String dateValue) {
        final Date date = DateUtils.parse(dateValue, DateUtils.FORMAT_RFC_1036);

        // Rewrite the date
        final String newDateValue = DateUtils.format(date,
                DateUtils.FORMAT_RFC_1036.get(0));

        // Compare initial and new headers
        assertEquals(dateValue, newDateValue);
    }

    /**
     * Test one set cookie header.
     * 
     * @param headerValue
     *            The set cookie header value.
     * @param compare
     *            Indicates if the new header should be compared with the old
     *            one.
     * @throws IOException
     */
    private void testCookieSetting(String headerValue, boolean compare)
            throws IOException {
        final CookieReader cr = new CookieReader(Logger
                .getLogger(CookiesTestCase.class.getCanonicalName()),
                headerValue);
        final CookieSetting cookie = cr.readCookieSetting();

        // Rewrite the header
        final String newHeaderValue = CookieUtils.format(cookie);

        // Compare initial and new headers
        if (compare) {
            final boolean result = newHeaderValue.toLowerCase().startsWith(
                    headerValue.toLowerCase());
            assertTrue(result);
        }
    }

    /**
     * Tests the cookies parsing.
     */
    public void testParsing() throws IOException {
        // Netscape speficiation
        testCookie("CUSTOMER=WILE_E_COYOTE");
        testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001");
        testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001; SHIPPING=FEDEX");
        testCookie("NUMBER=RIDING_ROCKET_0023; PART_NUMBER=ROCKET_LAUNCHER_0001");

        testCookieSetting("CUSTOMER=WILE_E_COYOTE; path=/", true);
        testCookieSetting("PART_NUMBER=ROCKET_LAUNCHER_0001; path=/", true);
        testCookieSetting("SHIPPING=FEDEX; path=/foo", true);
        testCookieSetting("NUMBER=RIDING_ROCKET_0023; path=/ammo", true);

        testCookieDate("Tuesday, 09-Nov-99 23:12:40 GMT");

        // RFC 2109
        testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"");
        testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"");
        testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"; Shipping=\"FedEx\"; $Path=\"/acme\"");
        testCookie("$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"");

        testCookieSetting(
                "Customer=\"WILE_E_COYOTE\"; Version=\"1\"; Path=\"/acme\"",
                true);
        testCookieSetting(
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\"",
                true);
        testCookieSetting("Shipping=\"FedEx\"; Version=\"1\"; Path=\"/acme\"",
                true);
        testCookieSetting(
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\"",
                true);
        testCookieSetting(
                "Part_Number=\"Riding_Rocket_0023\"; Version=\"1\"; Path=\"/acme/ammo\"",
                true);

        // Bug #49
        testCookieSetting(
                "RMS_ADMETA_VISITOR_RMS=27756847%3A240105; expires=Thu, 02 Mar 2006 21:09:00 GMT; path=/; domain=.admeta.com",
                false);
    }

}
