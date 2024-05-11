/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.data;

import org.junit.jupiter.api.Test;
import org.restlet.data.Tag;
import org.restlet.engine.header.TagReader;
import org.restlet.test.RestletTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link org.restlet.data.Tag}.
 *
 * @author Jerome Louvel
 */
public class TagTestCase extends RestletTestCase {

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
