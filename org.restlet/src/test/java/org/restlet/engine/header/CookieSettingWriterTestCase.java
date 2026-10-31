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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CookieSetting;
import org.restlet.data.SameSite;

class CookieSettingWriterTestCase {

    @Test
    void write_simpleCookie_writesNameEqualsValue() {
        assertEquals("name=value", CookieSettingWriter.write(new CookieSetting("name", "value")));
    }

    @Test
    void write_missingName_throwsIllegalArgumentException() {
        CookieSetting cookieSetting = new CookieSetting("", "value");
        assertThrows(
                IllegalArgumentException.class, () -> CookieSettingWriter.write(cookieSetting));
    }

    @Test
    void write_neverWritesLegacyAttributes() {
        CookieSetting cookieSetting = new CookieSetting("name", "value", "/path", "example.com");
        String result = CookieSettingWriter.write(cookieSetting);
        assertFalse(result.contains("Version"));
        assertFalse(result.contains("Comment"));
        assertFalse(result.contains("Discard"));
        assertFalse(result.contains("\""));
    }

    @Test
    void write_path_isNotQuoted() {
        CookieSetting cookieSetting = new CookieSetting("name", "value", "/path", null);
        assertEquals("name=value; Path=/path", CookieSettingWriter.write(cookieSetting));
    }

    @Test
    void write_maxAge_appendsExpiresAndUnquotedMaxAge() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setMaxAge(3600);
        String result = CookieSettingWriter.write(cookieSetting);
        assertTrue(result.contains("; Expires="));
        assertTrue(result.endsWith("; Max-Age=3600"));
    }

    @Test
    void write_maxAgeZero_appendsMaxAgeZero() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setMaxAge(0);
        assertTrue(CookieSettingWriter.write(cookieSetting).contains("; Max-Age=0"));
    }

    @Test
    void write_sessionCookie_appendsNoExpirationAttribute() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setMaxAge(-1);
        assertEquals("name=value", CookieSettingWriter.write(cookieSetting));
    }

    @Test
    void write_domain_isLowercased() {
        CookieSetting cookieSetting = new CookieSetting("name", "value", null, "EXAMPLE.COM");
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
    void write_sameSite_appendsSameSite() {
        CookieSetting cookieSetting = new CookieSetting("name", "value");
        cookieSetting.setSameSite(SameSite.LAX);
        assertEquals("name=value; SameSite=Lax", CookieSettingWriter.write(cookieSetting));
        cookieSetting.setSameSite(SameSite.NONE);
        assertEquals("name=value; SameSite=None", CookieSettingWriter.write(cookieSetting));
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
