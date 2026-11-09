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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.restlet.engine.header.TagReader;

/**
 * Test {@link org.restlet.data.Tag}.
 *
 * @author Jerome Louvel
 */
class TagTestCase {

    @Test
    void testSimpleTag() {
        assertEquals("my-tag", Tag.parse("\"my-tag\"").getName());
        assertFalse(Tag.parse("\"my-tag\"").isWeak());
    }

    @Test
    void testInvalidTag() {
        assertNull(Tag.parse("my-tag"));
        assertNull(Tag.parse("\"my-tag"));
    }

    @Test
    void testAllTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("*").getName());
    }

    @Test
    void testWeakTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("W/*").getName());
        assertTrue(Tag.parse("W/*").isWeak());

        assertEquals("my-tag", Tag.parse("W/\"my-tag\"").getName());
        assertTrue(Tag.parse("W/\"my-tag\"").isWeak());
    }

    @Test
    void testListOfValidTags() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", \"c3pio\", *").addValues(tags);
        assertEquals("xyz", tags.getFirst().getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals("c3pio", tags.get(2).getName());
        assertEquals(Tag.ALL.getName(), tags.get(3).getName());
        assertEquals(4, tags.size());
    }

    @Test
    void testListOfTagsWithInvalidTag() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", c3pio, *").addValues(tags);
        assertEquals("xyz", tags.getFirst().getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals(Tag.ALL.getName(), tags.get(2).getName());
        assertEquals(3, tags.size());
    }

    // RFC 9110 section 8.8.3.2: strong and weak comparison of entity tags

    private static final Tag WEAK_1 = new Tag("1", true);

    private static final Tag WEAK_2 = new Tag("2", true);

    private static final Tag STRONG_1 = new Tag("1", false);

    private static final Tag STRONG_2 = new Tag("2", false);

    @Test
    void strongMatch_followsTheTableOfRfc9110() {
        assertFalse(WEAK_1.strongMatch(WEAK_1));
        assertFalse(WEAK_1.strongMatch(WEAK_2));
        assertFalse(WEAK_1.strongMatch(STRONG_1));
        assertFalse(STRONG_1.strongMatch(WEAK_1));
        assertTrue(STRONG_1.strongMatch(new Tag("1", false)));
        assertFalse(STRONG_1.strongMatch(STRONG_2));
    }

    @Test
    void weakMatch_followsTheTableOfRfc9110() {
        assertTrue(WEAK_1.weakMatch(WEAK_1));
        assertFalse(WEAK_1.weakMatch(WEAK_2));
        assertTrue(WEAK_1.weakMatch(STRONG_1));
        assertTrue(STRONG_1.weakMatch(WEAK_1));
        assertTrue(STRONG_1.weakMatch(new Tag("1", false)));
        assertFalse(STRONG_1.weakMatch(STRONG_2));
    }

    @Test
    void strongMatchAndWeakMatch_withNull_areFalse() {
        assertFalse(STRONG_1.strongMatch(null));
        assertFalse(STRONG_1.weakMatch(null));
    }

    @Test
    void equals_comparesTheNameAndTheWeakness() {
        assertEquals(STRONG_1, new Tag("1", false));
        assertEquals(WEAK_1, new Tag("1", true));
        assertNotEquals(WEAK_1, STRONG_1);
        assertNotEquals(STRONG_1, WEAK_1);
        assertNotEquals(STRONG_1, STRONG_2);
        assertNotEquals(STRONG_1, null);
    }

    @Test
    void equals_isConsistentWithHashCode() {
        List<Tag> tags = List.of(WEAK_1, WEAK_2, STRONG_1, STRONG_2, Tag.ALL, new Tag());

        for (Tag a : tags) {
            for (Tag b : tags) {
                if (a.equals(b)) {
                    assertEquals(a.hashCode(), b.hashCode(), a.format() + " / " + b.format());
                }
            }
        }
    }

    @Test
    void hashSet_distinguishesWeakAndStrongTags() {
        Set<Tag> tags = new HashSet<>(List.of(STRONG_1));

        assertTrue(tags.contains(new Tag("1", false)));
        assertFalse(tags.contains(WEAK_1));
    }
}
