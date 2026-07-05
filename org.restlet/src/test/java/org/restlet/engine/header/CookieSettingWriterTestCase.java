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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CookieSetting;

class CookieSettingWriterTestCase {

    @Test
    void write_simpleCookie_writesNameEqualsValue() {
        assertEquals("name=value", CookieSettingWriter.write(new CookieSetting("name", "value")));
    }

    @Test
    void write_missingName_throwsIllegalArgumentException() {
        CookieSetting cookieSetting = new CookieSetting(0, "", "value");
        assertThrows(
                IllegalArgumentException.class, () -> CookieSettingWriter.write(cookieSetting));
    }

    @Test
    void write_versionOne_appendsQuotedVersion() {
        CookieSetting cookieSetting = new CookieSetting(1, "name", "value");
        assertTrue(
                CookieSettingWriter.write(cookieSetting)
                        .startsWith("name=\"value\"; Version=\"1\""));
    }

    @Test
    void write_pathVersionZero_appendsUnquotedPath() {
        CookieSetting cookieSetting = new CookieSetting(0, "name", "value", "/path", null);
        assertEquals("name=value; Path=/path", CookieSettingWriter.write(cookieSetting));
    }

    @Test
    void write_pathVersionOne_appendsQuotedPath() {
        CookieSetting cookieSetting = new CookieSetting(1, "name", "value", "/path", null);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Path=\"/path\""));
    }

    @Test
    void write_maxAgeVersionZero_appendsExpiresDate() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setMaxAge(3600);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Expires="));
    }

    @Test
    void write_maxAgeVersionOne_appendsMaxAge() {
        CookieSetting cookieSetting = new CookieSetting(1, "name", "value");
        cookieSetting.setMaxAge(3600);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Max-Age=\"3600\""));
    }

    @Test
    void write_discardVersionOne_appendsDiscard() {
        CookieSetting cookieSetting = new CookieSetting(1, "name", "value");
        cookieSetting.setMaxAge(-1);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Discard"));
    }

    @Test
    void write_domain_isLowercased() {
        CookieSetting cookieSetting = new CookieSetting(0, "name", "value", null, "EXAMPLE.COM");
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Domain=example.com"));
    }

    @Test
    void write_secureFlag_appendsSecure() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setSecure(true);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Secure"));
    }

    @Test
    void write_accessRestrictedFlag_appendsHttpOnly() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setAccessRestricted(true);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; HttpOnly"));
    }

    @Test
    void write_commentVersionOne_appendsQuotedComment() {
        CookieSetting cookieSetting = new CookieSetting(1, "name", "value");
        cookieSetting.setComment("a comment");
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Comment=\"a comment\""));
    }

    @Test
    void write_commentVersionZero_isIgnored() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setComment("a comment");
        assertTrue(!CookieSettingWriter.write(cookieSetting).contains("Comment"));
    }

    @Test
    void write_listOfCookieSettings_joinsWithSemicolon() {
        List<CookieSetting> settings =
                Arrays.asList(new CookieSetting("a", "1"), new CookieSetting("b", "2"));
        String result = CookieSettingWriter.write(settings);
        assertTrue(result.contains("a=1"));
        assertTrue(result.contains("b=2"));
    }
}
