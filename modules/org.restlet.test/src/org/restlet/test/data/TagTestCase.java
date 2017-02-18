/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Tag;
import org.restlet.engine.header.TagReader;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.Tag}.
 * 
 * @author Jerome Louvel
 */
public class TagTestCase extends RestletTestCase {

    public void testSimpleTag() {
        assertEquals("my-tag", Tag.parse("\"my-tag\"").getName());
        assertFalse(Tag.parse("\"my-tag\"").isWeak());
    }

    public void testInvalidTag() {
        assertNull(Tag.parse("my-tag"));
        assertNull(Tag.parse("\"my-tag"));
    }

    public void testAllTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("*").getName());
    }

    public void testWeakTag() {
        assertEquals(Tag.ALL.getName(), Tag.parse("W/*").getName());
        assertTrue(Tag.parse("W/*").isWeak());

        assertEquals("my-tag", Tag.parse("W/\"my-tag\"").getName());
        assertTrue(Tag.parse("W/\"my-tag\"").isWeak());
    }

    public void testListOfValidTags() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", \"c3pio\", *").addValues(tags);
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals("c3pio", tags.get(2).getName());
        assertEquals(Tag.ALL.getName(), tags.get(3).getName());
        assertEquals(4, tags.size());
    }

    public void testListOfTagsWithInvalidTag() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"xyz\", \"r2d2\", c3pio, *").addValues(tags);
        assertEquals("xyz", tags.get(0).getName());
        assertEquals("r2d2", tags.get(1).getName());
        assertEquals(Tag.ALL.getName(), tags.get(2).getName());
        assertEquals(3, tags.size());
    }

}
