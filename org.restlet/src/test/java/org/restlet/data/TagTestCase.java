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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals("c3pio", tags.get(2).getName());
        assertEquals(Tag.ALL.getName(), tags.get(3).getName());
        assertEquals(4, tags.size());
    }

    @Test
    void testListOfTagsWithInvalidTag() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", c3pio, *").addValues(tags);
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals(Tag.ALL.getName(), tags.get(2).getName());
        assertEquals(3, tags.size());
    }
}
