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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/**
 * Unit tests for the {@link InputRepresentation} class.
 *
 * @author Jerome Louvel
 */
class InputRepresentationTestCase {

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void constructor_withStreamOnly_isTransientAndAvailable() {
        InputRepresentation representation = new InputRepresentation(stream("content"));

        assertTrue(representation.isTransient());
        assertTrue(representation.isAvailable());
        assertEquals(Representation.UNKNOWN_SIZE, representation.getSize());
    }

    @Test
    void constructor_withMediaType_setsMediaType() {
        InputRepresentation representation =
                new InputRepresentation(stream("content"), MediaType.TEXT_PLAIN);

        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    @Test
    void constructor_withExpectedSize_setsSize() {
        InputRepresentation representation =
                new InputRepresentation(stream("content"), MediaType.TEXT_PLAIN, 7L);

        assertEquals(7L, representation.getSize());
    }

    @Test
    void getStream_returnsStreamOnceThenNull() throws IOException {
        InputRepresentation representation = new InputRepresentation(stream("content"));

        InputStream first = representation.getStream();
        assertNotNull(first);

        InputStream second = representation.getStream();
        assertNull(second);
    }

    @Test
    void getStream_marksRepresentationUnavailableAfterConsumption() throws IOException {
        InputRepresentation representation = new InputRepresentation(stream("content"));

        representation.getStream();

        assertFalse(representation.isAvailable());
    }

    @Test
    void getText_readsFullStreamContent() throws IOException {
        InputRepresentation representation = new InputRepresentation(stream("hello world"));

        assertEquals("hello world", representation.getText());
    }

    @Test
    void write_copiesStreamContentToOutputStream() throws IOException {
        InputRepresentation representation = new InputRepresentation(stream("copied content"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        representation.write(out);

        assertEquals("copied content", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void release_closesStreamAndMarksUnavailable() {
        InputRepresentation representation = new InputRepresentation(stream("content"));

        representation.release();

        assertFalse(representation.isAvailable());
    }

    @Test
    void setStream_withNull_marksRepresentationUnavailable() {
        InputRepresentation representation = new InputRepresentation(stream("content"));

        representation.setStream(null);

        assertFalse(representation.isAvailable());
    }
}
