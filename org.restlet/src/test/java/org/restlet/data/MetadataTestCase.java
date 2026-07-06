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

/**
 * Test {@link org.restlet.data.Metadata}, exercised through the concrete {@link CharacterSet} and
 * {@link Encoding} subclasses since Metadata is abstract.
 */
class MetadataTestCase {

    @Test
    void getName_returnsConfiguredValue() {
        Metadata metadata = new CharacterSet("UTF-8", "description");
        assertEquals("UTF-8", metadata.getName());
    }

    @Test
    void getDescription_returnsConfiguredValue() {
        Metadata metadata = new CharacterSet("UTF-8", "description");
        assertEquals("description", metadata.getDescription());
    }

    @Test
    void constructor_withNameOnly_hasNullDescription() {
        Encoding encoding = new Encoding("custom", null);
        assertNull(encoding.getDescription());
    }

    @Test
    void equals_acrossDifferentMetadataSubtypes_returnsFalse() {
        // Although the abstract Metadata.equals() only compares names, every
        // concrete subclass (CharacterSet, Encoding, ...) overrides equals()
        // with its own "instanceof" check, so two different Metadata subtypes
        // sharing the same name are never considered equal.
        Metadata characterSet = new CharacterSet("SAME-NAME");
        Metadata encoding = new Encoding("SAME-NAME");
        assertNotEquals(characterSet, encoding);
        assertNotEquals(encoding, characterSet);
    }

    @Test
    void equals_differentName_returnsFalse() {
        Metadata cs1 = new CharacterSet("A");
        Metadata cs2 = new CharacterSet("B");
        assertNotEquals(cs1, cs2);
    }

    @Test
    void hashCode_matchesNameHashCode() {
        // CharacterSet overrides hashCode() to be case-insensitive (lowercases
        // the name), so this exercises that override rather than the base
        // Metadata implementation.
        Metadata metadata = new CharacterSet("UTF-8");
        assertEquals("utf-8".hashCode(), metadata.hashCode());
    }

    @Test
    void hashCode_forNullName_returnsZero() {
        Metadata metadata = new CharacterSet((String) null);
        assertEquals(0, metadata.hashCode());
    }

    @Test
    void toString_returnsName() {
        Metadata metadata = new Encoding("gzip");
        assertEquals("gzip", metadata.toString());
    }

    @Test
    void isCompatible_withNull_returnsFalse() {
        Metadata metadata = CharacterSet.UTF_8;
        assertFalse(metadata.isCompatible(null));
    }

    @Test
    void isCompatible_delegatesToIncludes() {
        assertTrue(CharacterSet.ALL.isCompatible(CharacterSet.UTF_8));
        assertTrue(CharacterSet.UTF_8.isCompatible(CharacterSet.ALL));
        assertFalse(CharacterSet.UTF_8.isCompatible(CharacterSet.ISO_8859_1));
    }

    @Test
    void getParent_isAbstractButImplementedBySubclasses() {
        assertNull(CharacterSet.ALL.getParent());
        assertEquals(CharacterSet.ALL, CharacterSet.UTF_8.getParent());
    }
}
