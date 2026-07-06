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
    void read_comment_setsComment() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; comment=hello");
        assertEquals("hello", cookieSetting.getComment());
    }

    @Test
    void read_discard_setsMaxAgeToNegativeOne() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; discard");
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
    void read_version_setsVersion() {
        CookieSetting cookieSetting = CookieSettingReader.read("name=value; version=1");
        assertEquals(1, cookieSetting.getVersion());
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
