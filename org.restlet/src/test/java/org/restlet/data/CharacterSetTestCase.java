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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.CharacterSet}. */
class CharacterSetTestCase {

    @Test
    void constructor_withName_uppercasesName() {
        CharacterSet characterSet = new CharacterSet("utf-8");
        assertEquals("UTF-8", characterSet.getName());
    }

    @Test
    void constructor_withNameAndDescription() {
        CharacterSet characterSet = new CharacterSet("MY-CHARSET", "a description");
        assertEquals("MY-CHARSET", characterSet.getName());
        assertEquals("a description", characterSet.getDescription());
    }

    @Test
    void constructor_withJavaCharset_usesCharsetNameAndDisplayName() {
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        CharacterSet characterSet = new CharacterSet(charset);
        assertEquals(charset.name(), characterSet.getName());
        assertEquals(charset.displayName(), characterSet.getDescription());
    }

    @Test
    void valueOf_returnsSharedConstant() {
        assertSame(CharacterSet.UTF_8, CharacterSet.valueOf("UTF-8"));
        assertSame(CharacterSet.ISO_8859_1, CharacterSet.valueOf("ISO-8859-1"));
        assertSame(CharacterSet.US_ASCII, CharacterSet.valueOf("US-ASCII"));
        assertSame(CharacterSet.ALL, CharacterSet.valueOf("*"));
    }

    @Test
    void valueOf_resolvesAliasNames() {
        assertSame(CharacterSet.US_ASCII, CharacterSet.valueOf("ASCII"));
        assertSame(CharacterSet.ISO_8859_1, CharacterSet.valueOf("latin1"));
        assertSame(CharacterSet.MACINTOSH, CharacterSet.valueOf("MACROMAN"));
        // "arabic" resolves (via the IANA name mapping) to the ISO-8859-6 name,
        // but valueOf() only returns the shared singleton for a subset of
        // constants, so a new (but equals()-equivalent) instance is created.
        assertEquals(CharacterSet.ISO_8859_6, CharacterSet.valueOf("arabic"));
    }

    @Test
    void valueOf_unknownName_createsNewInstance() {
        CharacterSet characterSet = CharacterSet.valueOf("CUSTOM-CHARSET");
        assertEquals("CUSTOM-CHARSET", characterSet.getName());
    }

    @Test
    void valueOf_nullOrEmptyName_returnsNull() {
        assertNull(CharacterSet.valueOf(null));
        assertNull(CharacterSet.valueOf(""));
    }

    @Test
    void equals_isCaseInsensitive() {
        CharacterSet cs1 = new CharacterSet("utf-8");
        CharacterSet cs2 = new CharacterSet("UTF-8");
        assertEquals(cs1, cs2);
        assertEquals(cs1.hashCode(), cs2.hashCode());
    }

    @Test
    void getParent_forAll_returnsNull() {
        assertNull(CharacterSet.ALL.getParent());
    }

    @Test
    void getParent_forSpecificCharset_returnsAll() {
        assertEquals(CharacterSet.ALL, CharacterSet.UTF_8.getParent());
    }

    @Test
    void includes_allIncludesEverything() {
        assertTrue(CharacterSet.ALL.includes(CharacterSet.UTF_8));
        assertFalse(CharacterSet.UTF_8.includes(CharacterSet.ALL));
    }

    @Test
    void includes_nullIsAlwaysIncluded() {
        assertTrue(CharacterSet.UTF_8.includes(null));
    }

    @Test
    void includes_sameCharacterSet_returnsTrue() {
        assertTrue(CharacterSet.UTF_8.includes(CharacterSet.UTF_8));
    }

    @Test
    void isCompatible_reflexiveAndSymmetricViaAll() {
        assertTrue(CharacterSet.UTF_8.isCompatible(CharacterSet.ALL));
        assertTrue(CharacterSet.ALL.isCompatible(CharacterSet.UTF_8));
        assertFalse(CharacterSet.UTF_8.isCompatible(CharacterSet.ISO_8859_1));
    }

    @Test
    void toCharset_returnsMatchingNioCharset() {
        assertEquals(java.nio.charset.StandardCharsets.UTF_8, CharacterSet.UTF_8.toCharset());
    }

    @Test
    void toString_returnsName() {
        assertEquals("UTF-8", CharacterSet.UTF_8.toString());
    }
}
