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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/** Unit tests for {@link ReaderRepresentation}. */
class ReaderRepresentationTestCase {

    @Test
    void constructorWithReaderOnly_isAvailable() {
        ReaderRepresentation representation = new ReaderRepresentation(new StringReader("text"));
        assertTrue(representation.isAvailable());
        assertTrue(representation.isTransient());
    }

    @Test
    void getText_readsFromWrappedReader() throws Exception {
        ReaderRepresentation representation =
                new ReaderRepresentation(new StringReader("hello"), MediaType.TEXT_PLAIN);
        assertEquals("hello", representation.getText());
    }

    @Test
    void getReader_returnsReaderOnceThenNull() throws Exception {
        ReaderRepresentation representation =
                new ReaderRepresentation(new StringReader("hello"), MediaType.TEXT_PLAIN, 5);
        assertEquals(5, representation.getSize());
        assertTrue(representation.getReader() != null);
        assertNull(representation.getReader());
    }

    @Test
    void write_writer_copiesReaderContent() throws Exception {
        ReaderRepresentation representation = new ReaderRepresentation(new StringReader("hello"));
        StringWriter writer = new StringWriter();
        representation.write(writer);
        assertEquals("hello", writer.toString());
    }

    @Test
    void setReader_null_marksUnavailable() {
        ReaderRepresentation representation = new ReaderRepresentation(new StringReader("hello"));
        representation.setReader(null);
        assertFalse(representation.isAvailable());
    }

    @Test
    void release_closesReader() {
        ReaderRepresentation representation = new ReaderRepresentation(new StringReader("hello"));
        representation.release();
    }
}
