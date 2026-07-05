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
    void write_versionedCookie_quotesValueAndAppendsPathAndDomain() {
        Cookie cookie = new Cookie(1, "name", "value", "/path", "example.com");
        String result = CookieWriter.write(cookie);
        assertEquals("name=\"value\"; $Path=\"/path\"; $Domain=\"example.com\"", result);
    }

    @Test
    void write_missingName_throwsIllegalArgumentException() {
        Cookie cookie = new Cookie(0, "", "value");
        assertThrows(IllegalArgumentException.class, () -> CookieWriter.write(cookie));
    }

    @Test
    void write_listOfCookies_prefixesVersionAndJoinsWithSemicolon() {
        List<Cookie> cookies = Arrays.asList(new Cookie(1, "a", "1"), new Cookie(1, "b", "2"));
        String result = CookieWriter.write(cookies);
        assertEquals("$Version=\"1\"; a=\"1\"; b=\"2\"", result);
    }

    @Test
    void write_listWithVersionZero_omitsVersionPrefix() {
        List<Cookie> cookies = Arrays.asList(new Cookie("a", "1"), new Cookie("b", "2"));
        assertEquals("a=1; b=2", CookieWriter.write(cookies));
    }

    @Test
    void write_emptyList_returnsEmptyString() {
        assertEquals("", CookieWriter.write(List.of()));
    }

    @Test
    void appendValue_versionZero_appendsUnquoted() {
        assertEquals("value", new CookieWriter().appendValue("value", 0).toString());
    }

    @Test
    void appendValue_nonZeroVersion_appendsQuoted() {
        assertEquals("\"value\"", new CookieWriter().appendValue("value", 1).toString());
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
