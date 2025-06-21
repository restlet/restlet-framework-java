/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.Test;
import org.restlet.engine.header.TagReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test {@link org.restlet.data.Tag}.
 *
 * @author Jerome Louvel
 */
public class TagTestCase {

    @Test
    public void testSimpleTag() {
        assertEquals("my-tag", Tag.parse("\"my-tag\"").getName());
        assertFalse(Tag.parse("\"my-tag\"").isWeak());
    }

    @Test
    public void testInvalidTag() {
        assertNull(Tag.parse("my-tag"));
        assertNull(Tag.parse("\"my-tag"));
    }

    @Test
    public void testAllTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("*").getName());
    }

    @Test
    public void testWeakTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("W/*").getName());
        assertTrue(Tag.parse("W/*").isWeak());

        assertEquals("my-tag", Tag.parse("W/\"my-tag\"").getName());
        assertTrue(Tag.parse("W/\"my-tag\"").isWeak());
    }

    @Test
    public void testListOfValidTags() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", \"c3pio\", *").addValues(tags);
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals("c3pio", tags.get(2).getName());
        assertEquals(Tag.ALL.getName(), tags.get(3).getName());
        assertEquals(4, tags.size());
    }

    @Test
    public void testListOfTagsWithInvalidTag() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", c3pio, *").addValues(tags);
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals(Tag.ALL.getName(), tags.get(2).getName());
        assertEquals(3, tags.size());
    }

}
