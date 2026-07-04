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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/** Unit tests for {@link BufferingRepresentation}. */
class BufferingRepresentationTestCase {

    @Test
    void getText_buffersAndReturnsWrappedText() throws Exception {
        BufferingRepresentation representation =
                new BufferingRepresentation(
                        new StringRepresentation("hello", MediaType.TEXT_PLAIN));

        assertTrue(representation.isAvailable());
        assertEquals("hello", representation.getText());
        assertEquals(5, representation.getSize());
        assertEquals(5, representation.getAvailableSize());
    }

    @Test
    void getStream_returnsBufferedBytes() throws Exception {
        BufferingRepresentation representation =
                new BufferingRepresentation(new StringRepresentation("hello"));

        assertEquals("hello", IoUtils.toString(representation.getStream()));
    }

    @Test
    void getReader_returnsBufferedCharacters() throws Exception {
        BufferingRepresentation representation =
                new BufferingRepresentation(new StringRepresentation("hello"));

        char[] buf = new char[5];
        representation.getReader().read(buf);
        assertEquals("hello", new String(buf));
    }

    @Test
    void write_outputStream_writesBufferedBytes() throws Exception {
        BufferingRepresentation representation =
                new BufferingRepresentation(new StringRepresentation("hello"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);
        assertEquals("hello", out.toString());
    }

    @Test
    void write_writer_writesBufferedText() throws Exception {
        BufferingRepresentation representation =
                new BufferingRepresentation(new StringRepresentation("hello"));

        StringWriter writer = new StringWriter();
        representation.write(writer);
        assertEquals("hello", writer.toString());
    }
}
