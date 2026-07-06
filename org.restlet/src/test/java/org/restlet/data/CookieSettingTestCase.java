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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.CookieSetting}. */
class CookieSettingTestCase {

    @Test
    void defaultConstructor_hasDefaultValues() {
        CookieSetting cookie = new CookieSetting();
        assertEquals(0, cookie.getVersion());
        assertNull(cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(-1, cookie.getMaxAge());
        assertFalse(cookie.isSecure());
        assertFalse(cookie.isAccessRestricted());
    }

    @Test
    void preferredConstructor_setsNameAndValue() {
        CookieSetting cookie = new CookieSetting("session", "abc123");
        assertEquals("session", cookie.getName());
        assertEquals("abc123", cookie.getValue());
        assertEquals(0, cookie.getVersion());
    }

    @Test
    void constructor_withVersionNameValue() {
        CookieSetting cookie = new CookieSetting(1, "session", "abc123");
        assertEquals(1, cookie.getVersion());
        assertEquals("session", cookie.getName());
        assertEquals("abc123", cookie.getValue());
        assertEquals(-1, cookie.getMaxAge());
    }

    @Test
    void constructor_withPathAndDomain() {
        CookieSetting cookie = new CookieSetting(1, "session", "abc123", "/app", "example.com");
        assertEquals("/app", cookie.getPath());
        assertEquals("example.com", cookie.getDomain());
    }

    @Test
    void constructor_withCommentMaxAgeSecure() {
        CookieSetting cookie =
                new CookieSetting(
                        1, "session", "abc123", "/app", "example.com", "a comment", 3600, true);
        assertEquals("a comment", cookie.getComment());
        assertEquals(3600, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
        assertFalse(cookie.isAccessRestricted());
    }

    @Test
    void constructor_withAccessRestricted() {
        CookieSetting cookie =
                new CookieSetting(
                        1,
                        "session",
                        "abc123",
                        "/app",
                        "example.com",
                        "a comment",
                        3600,
                        true,
                        true);
        assertTrue(cookie.isAccessRestricted());
    }

    @Test
    void settersUpdateState() {
        CookieSetting cookie = new CookieSetting();
        cookie.setComment("comment");
        cookie.setMaxAge(60);
        cookie.setSecure(true);
        cookie.setAccessRestricted(true);

        assertEquals("comment", cookie.getComment());
        assertEquals(60, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isAccessRestricted());
    }

    @Test
    void getDescription_returnsFixedText() {
        assertEquals("Cookie setting", new CookieSetting().getDescription());
    }

    @Test
    void equals_sameFields_returnsTrue() {
        CookieSetting c1 =
                new CookieSetting(
                        1, "session", "abc123", "/app", "example.com", "comment", 3600, true);
        CookieSetting c2 =
                new CookieSetting(
                        1, "session", "abc123", "/app", "example.com", "comment", 3600, true);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void equals_differentMaxAge_returnsFalse() {
        CookieSetting c1 = new CookieSetting(0, "session", "abc123", null, null, null, 10, false);
        CookieSetting c2 = new CookieSetting(0, "session", "abc123", null, null, null, 20, false);
        assertNotEquals(c1, c2);
    }

    @Test
    void equals_differentComment_returnsFalse() {
        CookieSetting c1 = new CookieSetting(0, "session", "abc123", null, null, "a", 10, false);
        CookieSetting c2 = new CookieSetting(0, "session", "abc123", null, null, "b", 10, false);
        assertNotEquals(c1, c2);
    }

    @Test
    void toString_containsFields() {
        CookieSetting cookie =
                new CookieSetting(
                        1, "session", "abc123", "/app", "example.com", "comment", 3600, true, true);
        String result = cookie.toString();
        assertTrue(result.contains("session"));
        assertTrue(result.contains("abc123"));
        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("/app"));
        assertTrue(result.contains("comment"));
        assertTrue(result.contains("secure=true"));
        assertTrue(result.contains("accessRestricted=true"));
    }
}
