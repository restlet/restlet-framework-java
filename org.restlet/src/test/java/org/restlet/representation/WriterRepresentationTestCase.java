/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/** Unit tests for {@link WriterRepresentation}. */
class WriterRepresentationTestCase {

    private static class TestWriterRepresentation extends WriterRepresentation {
        TestWriterRepresentation(MediaType mediaType) {
            super(mediaType);
        }

        TestWriterRepresentation(MediaType mediaType, long expectedSize) {
            super(mediaType, expectedSize);
        }

        @Override
        public void write(Writer writer) throws IOException {
            writer.write("hello");
        }
    }

    @Test
    void constructor_withMediaType_hasUnknownSize() {
        TestWriterRepresentation representation =
                new TestWriterRepresentation(MediaType.TEXT_PLAIN);
        assertEquals(Representation.UNKNOWN_SIZE, representation.getSize());
    }

    @Test
    void constructor_withExpectedSize_setsSize() {
        TestWriterRepresentation representation =
                new TestWriterRepresentation(MediaType.TEXT_PLAIN, 5);
        assertEquals(5, representation.getSize());
    }

    @Test
    void getReader_readsWrittenContent() throws Exception {
        TestWriterRepresentation representation =
                new TestWriterRepresentation(MediaType.TEXT_PLAIN);
        char[] buf = new char[5];
        representation.getReader().read(buf);
        assertEquals("hello", new String(buf));
    }
}
