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

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/** Unit tests for {@link ByteArrayRepresentation}. */
class ByteArrayRepresentationTestCase {

    private static final byte[] DATA = "hello world".getBytes();

    @Test
    void constructor_byteArray() throws Exception {
        assertEquals("hello world", new ByteArrayRepresentation(DATA).getText());
    }

    @Test
    void constructor_byteArrayWithOffsetAndLength() throws Exception {
        assertEquals("hello", new ByteArrayRepresentation(DATA, 0, 5).getText());
    }

    @Test
    void constructor_byteArrayWithOffsetLengthAndMediaType() throws Exception {
        ByteArrayRepresentation representation =
                new ByteArrayRepresentation(DATA, 6, 5, MediaType.TEXT_PLAIN);
        assertEquals("world", representation.getText());
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    @Test
    void constructor_byteArrayWithOffsetLengthMediaTypeAndExpectedSize() throws Exception {
        ByteArrayRepresentation representation =
                new ByteArrayRepresentation(DATA, 0, 5, MediaType.TEXT_PLAIN, 5);
        assertEquals("hello", representation.getText());
        assertEquals(5, representation.getSize());
    }

    @Test
    void constructor_byteArrayWithMediaType() throws Exception {
        ByteArrayRepresentation representation =
                new ByteArrayRepresentation(DATA, MediaType.TEXT_PLAIN);
        assertEquals("hello world", representation.getText());
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    @Test
    void constructor_byteArrayWithMediaTypeAndExpectedSize() throws Exception {
        ByteArrayRepresentation representation =
                new ByteArrayRepresentation(DATA, MediaType.TEXT_PLAIN, DATA.length);
        assertEquals("hello world", representation.getText());
        assertEquals(DATA.length, representation.getSize());
    }
}
