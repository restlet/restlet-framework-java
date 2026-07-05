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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;
import org.restlet.data.Tag;

class TagReaderTestCase {

    @Test
    void readValue_singleTag_parsesTag() throws IOException {
        TagReader reader = new TagReader("\"abc\"");
        Tag tag = reader.readValue();
        assertEquals(new Tag("abc"), tag);
    }

    @Test
    void readValue_weakTag_parsesWeakFlag() throws IOException {
        TagReader reader = new TagReader("W/\"abc\"");
        Tag tag = reader.readValue();
        assertTrue(tag.isWeak());
    }

    @Test
    void addValues_multipleTags_populatesCollection() {
        List<Tag> tags = new ArrayList<>();
        new TagReader("\"abc\", \"def\"").addValues(tags);
        assertEquals(2, tags.size());
        assertEquals(new Tag("abc"), tags.get(0));
        assertEquals(new Tag("def"), tags.get(1));
    }

    @Test
    void addValuesStatic_fromHeader_populatesCollection() {
        List<Tag> tags = new ArrayList<>();
        Header header = new Header("ETag", "\"abc\"");
        TagReader.addValues(header, tags);
        assertEquals(1, tags.size());
        assertEquals(new Tag("abc"), tags.get(0));
    }
}
