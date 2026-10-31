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

import org.junit.jupiter.api.Test;
import org.restlet.data.CookieSetting;
import org.restlet.data.SameSite;

class CookieSettingReaderTestCase {

    @Test
    void read_nameAndValue_parsesBoth() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value");
        assertEquals("name", cookieSetting.getName());
        assertEquals("value", cookieSetting.getValue());
    }

    @Test
    void read_emptyHeader_returnsNull() {
        assertNull(CookieSettingReader.read(""));
    }

    @Test
    void read_dollarPrefixedAttribute_isSkipped() {
        CookieSetting cookieSetting = CookieSettingReader.read("$Version=1; name=value");
        assertEquals("name", cookieSetting.getName());
    }

    @Test
    void read_path_setsPath() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; path=/app");
        assertEquals("/app", cookieSetting.getPath());
    }

    @Test
    void read_domain_setsDomain() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; domain=example.com");
        assertEquals("example.com", cookieSetting.getDomain());
    }

    @Test
    void read_legacyAttributes_areIgnored() {
        CookieSetting cookieSetting =
                CookieSettingReader.read(
                        "name=\"value\"; Version=\"1\"; Comment=hello; CommentURL=http://x; Discard; Port=80; Path=\"/app\"");
        assertEquals("name", cookieSetting.getName());
        assertEquals("value", cookieSetting.getValue());
        assertEquals("/app", cookieSetting.getPath());
        assertEquals(-1, cookieSetting.getMaxAge());
    }

    @Test
    void read_validMaxAge_setsMaxAge() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; max-age=3600");
        assertEquals(3600, cookieSetting.getMaxAge());
    }

    @Test
    void read_invalidMaxAge_defaultsToIntegerMax() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; max-age=notanumber");
        assertEquals(Integer.MAX_VALUE, cookieSetting.getMaxAge());
    }

    @Test
    void read_secureFlag_setsSecureTrue() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; secure");
        assertTrue(cookieSetting.isSecure());
    }

    @Test
    void read_httpOnlyFlag_setsAccessRestrictedTrue() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; httpOnly");
        assertTrue(cookieSetting.isAccessRestricted());
    }

    @Test
    void read_sameSite_setsSameSite() {
        assertEquals(
                SameSite.STRICT,
                CookieSettingReader.read("name=value; SameSite=Strict").getSameSite());
        assertEquals(
                SameSite.NONE, CookieSettingReader.read("name=value; samesite=none").getSameSite());
        assertNull(CookieSettingReader.read("name=value; SameSite=bogus").getSameSite());
        assertNull(CookieSettingReader.read("name=value").getSameSite());
    }

    @Test
    void read_maxAgeAndExpires_maxAgeWinsWhateverTheOrder() {
        String expires =
                org.restlet.engine.util.DateUtils.format(
                        new java.util.Date(System.currentTimeMillis() + 7_200_000L),
                        org.restlet.engine.util.DateUtils.FORMAT_RFC_1123.getFirst());
        assertEquals(
                60,
                CookieSettingReader.read("name=value; Max-Age=60; Expires=" + expires).getMaxAge());
        assertEquals(
                60,
                CookieSettingReader.read("name=value; Expires=" + expires + "; Max-Age=60")
                        .getMaxAge());
    }

    @Test
    void roundTrip_writtenCookieSetting_isReadBack() {
        CookieSetting written =
                new CookieSetting("id", "123", "/app", "example.com", 600, true, true);
        written.setSameSite(SameSite.LAX);
        CookieSetting read = CookieSettingReader.read(CookieSettingWriter.write(written));
        assertEquals(written, read);
    }

    @Test
    void read_unknownAttribute_isIgnored() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; unknown=foo");
        assertEquals("name", cookieSetting.getName());
    }

    @Test
    void read_validExpiresDate_setsPositiveMaxAge() {
        java.util.Date future = new java.util.Date(System.currentTimeMillis() + 3_600_000L);
        String expires =
                org.restlet.engine.util.DateUtils.format(
                        future, org.restlet.engine.util.DateUtils.FORMAT_RFC_1123.getFirst());
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; expires=" + expires);
        assertTrue(cookieSetting.getMaxAge() > 0);
    }

    @Test
    void read_invalidExpiresDate_leavesMaxAgeAtDefault() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; expires=not-a-date");
        assertEquals(-1, cookieSetting.getMaxAge());
    }

    @Test
    void read_quotedValue_isUnquoted() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=\"quoted value\"");
        assertEquals("quoted value", cookieSetting.getValue());
    }
}
