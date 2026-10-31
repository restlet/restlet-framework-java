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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.Cookie;

class CookieWriterTestCase {

    @Test
    void write_simpleCookie_writesNameEqualsValue() {
        assertEquals("name=value", CookieWriter.write(new Cookie("name", "value")));
    }

    @Test
    void write_cookieWithPathAndDomain_writesNameAndValueOnly() {
        // The obsolete $Path and $Domain pairs of RFC 2109 are not written anymore
        Cookie cookie = new Cookie("name", "value", "/path", "example.com");
        assertEquals("name=value", CookieWriter.write(cookie));
    }

    @Test
    void write_missingName_throwsIllegalArgumentException() {
        Cookie cookie = new Cookie("", "value");
        assertThrows(IllegalArgumentException.class, () -> CookieWriter.write(cookie));
    }

    @Test
    void write_listOfCookies_joinsWithSemicolon() {
        List<Cookie> cookies = Arrays.asList(new Cookie("a", "1"), new Cookie("b", "2"));
        assertEquals("a=1; b=2", CookieWriter.write(cookies));
    }

    @Test
    void write_emptyList_returnsEmptyString() {
        assertEquals("", CookieWriter.write(List.of()));
    }

    @Test
    void getCookies_matchingNameInDestinationMap_isUpdated() {
        Cookie cookie = new Cookie("name", "value");
        Map<String, Cookie> destination = new HashMap<>();
        destination.put("name", null);

        CookieWriter.getCookies(List.of(cookie), destination);

        assertEquals(cookie, destination.get("name"));
    }

    @Test
    void getCookies_nonMatchingName_isIgnored() {
        Cookie cookie = new Cookie("other", "value");
        Map<String, Cookie> destination = new HashMap<>();
        destination.put("name", null);

        CookieWriter.getCookies(List.of(cookie), destination);

        assertTrue(destination.containsKey("name"));
    }
}
