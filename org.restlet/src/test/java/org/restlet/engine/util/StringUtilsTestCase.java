/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class StringUtilsTestCase {

    @Test
    void firstLower_lowercasesFirstCharacterOnly() {
        assertEquals("hELLO", StringUtils.firstLower("HELLO"));
    }

    @Test
    void firstLower_nullOrEmpty_returnsUnchanged() {
        assertNull(StringUtils.firstLower(null));
        assertEquals("", StringUtils.firstLower(""));
    }

    @Test
    void firstUpper_uppercasesFirstCharacterOnly() {
        assertEquals("Hello", StringUtils.firstUpper("hello"));
    }

    @Test
    void firstUpper_nullOrEmpty_returnsUnchanged() {
        assertNull(StringUtils.firstUpper(null));
        assertEquals("", StringUtils.firstUpper(""));
    }

    @Test
    void getAsciiBytes_encodesString() {
        assertArrayEquals(
                "abc".getBytes(StandardCharsets.US_ASCII), StringUtils.getAsciiBytes("abc"));
    }

    @Test
    void getAsciiBytes_null_returnsEmptyArray() {
        assertArrayEquals(new byte[0], StringUtils.getAsciiBytes(null));
    }

    @Test
    void getLatin1Bytes_encodesString() {
        assertArrayEquals(
                "abc".getBytes(StandardCharsets.ISO_8859_1), StringUtils.getLatin1Bytes("abc"));
    }

    @Test
    void getLatin1Bytes_null_returnsEmptyArray() {
        assertArrayEquals(new byte[0], StringUtils.getLatin1Bytes(null));
    }

    @Test
    void htmlEscape_nullOrEmpty_returnsUnchanged() {
        assertNull(StringUtils.htmlEscape(null));
        assertEquals("", StringUtils.htmlEscape(""));
    }

    @Test
    void htmlEscape_asciiWithoutEntities_isUnchanged() {
        assertEquals("hello world", StringUtils.htmlEscape("hello world"));
    }

    @Test
    void htmlEscape_knownEntities_areEscapedByName() {
        assertEquals("&quot;&amp;&lt;&gt;", StringUtils.htmlEscape("\"&<>"));
    }

    @Test
    void htmlEscape_nonAsciiKnownEntity_isEscapedByName() {
        // U+00FF (yuml) and U+20AC (euro) are both known HTML 4.0 entities.
        assertEquals("&yuml;", StringUtils.htmlEscape("ÿ"));
        assertEquals("&euro;", StringUtils.htmlEscape("€"));
    }

    @Test
    void htmlEscape_nonAsciiWithoutEntity_isEscapedNumerically() {
        // U+00FE (thorn) is a known entity, but codepoints beyond the 10000-entry table
        // (e.g. U+2603 SNOWMAN) fall back to numeric escaping.
        assertEquals("&#9731;", StringUtils.htmlEscape("☃"));
    }

    @Test
    void htmlUnescape_nullOrEmpty_returnsUnchanged() {
        assertNull(StringUtils.htmlUnescape(null));
        assertEquals("", StringUtils.htmlUnescape(""));
    }

    @Test
    void htmlUnescape_namedEntity_isDecoded() {
        assertEquals("\"&<>", StringUtils.htmlUnescape("&quot;&amp;&lt;&gt;"));
    }

    @Test
    void htmlUnescape_decimalNumericEntity_isDecoded() {
        assertEquals("€", StringUtils.htmlUnescape("&#8364;"));
    }

    @Test
    void htmlUnescape_hexNumericEntityLowerX_isDecoded() {
        assertEquals("€", StringUtils.htmlUnescape("&#x20AC;"));
    }

    @Test
    void htmlUnescape_hexNumericEntityUpperX_isDecoded() {
        assertEquals("€", StringUtils.htmlUnescape("&#X20AC;"));
    }

    @Test
    void htmlUnescape_invalidNumericEntity_isLeftAsIs() {
        assertEquals("&#notanumber;", StringUtils.htmlUnescape("&#notanumber;"));
    }

    @Test
    void htmlUnescape_unknownEntityName_isLeftAsIs() {
        assertEquals("&unknownentity;", StringUtils.htmlUnescape("&unknownentity;"));
    }

    @Test
    void htmlUnescape_emptyEntity_isReplacedWithAmpersandSemicolon() {
        assertEquals("&;", StringUtils.htmlUnescape("&;"));
    }

    @Test
    void htmlUnescape_ampersandWithoutSemicolon_isLeftAsIs() {
        assertEquals("a & b", StringUtils.htmlUnescape("a & b"));
    }

    @Test
    void htmlUnescape_consecutiveAmpersands_areHandled() {
        assertEquals("&&", StringUtils.htmlUnescape("&&amp;"));
    }

    @Test
    void htmlUnescape_trailingAmpersand_isLeftAsIs() {
        assertEquals("value&", StringUtils.htmlUnescape("value&"));
    }

    @Test
    void isNullOrEmpty_nullOrEmptyString_returnsTrue() {
        assertTrue(StringUtils.isNullOrEmpty(null));
        assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    void isNullOrEmpty_nonEmptyString_returnsFalse() {
        assertFalse(StringUtils.isNullOrEmpty("x"));
    }
}
