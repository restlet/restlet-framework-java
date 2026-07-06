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

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.restlet.data.Tag;

class TagWriterTestCase {

    @Test
    void write_singleTag_delegatesToTagFormat() {
        Tag tag = new Tag("abc");
        assertEquals(tag.format(), TagWriter.write(tag));
    }

    @Test
    void write_listOfTags_joinsWithSeparator() {
        Tag tag1 = new Tag("abc");
        Tag tag2 = new Tag("def");
        String result = TagWriter.write(Arrays.asList(tag1, tag2));
        assertEquals(tag1.format() + ", " + tag2.format(), result);
    }

    @Test
    void append_singleTag_writesFormattedValue() {
        Tag tag = new Tag("abc");
        TagWriter writer = new TagWriter();
        writer.append(tag);
        assertEquals(tag.format(), writer.toString());
    }
}
