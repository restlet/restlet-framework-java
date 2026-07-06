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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Unit tests for the {@link OutputRepresentation} abstract class, exercised through a minimal local
 * subclass.
 *
 * @author Jerome Louvel
 */
class OutputRepresentationTestCase {

    /** Minimal concrete subclass writing a fixed piece of content. */
    private static class SimpleOutputRepresentation extends OutputRepresentation {

        private final String content;

        SimpleOutputRepresentation(MediaType mediaType, String content) {
            super(mediaType);
            this.content = content;
        }

        SimpleOutputRepresentation(MediaType mediaType, long expectedSize, String content) {
            super(mediaType, expectedSize);
            this.content = content;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void constructor_withMediaTypeOnly_hasUnknownSize() {
        SimpleOutputRepresentation representation =
                new SimpleOutputRepresentation(MediaType.TEXT_PLAIN, "data");

        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertEquals(Representation.UNKNOWN_SIZE, representation.getSize());
    }

    @Test
    void constructor_withExpectedSize_setsSize() {
        SimpleOutputRepresentation representation =
                new SimpleOutputRepresentation(MediaType.TEXT_PLAIN, 4L, "data");

        assertEquals(4L, representation.getSize());
    }

    @Test
    void getStream_producesContentWrittenByWrite() throws IOException {
        SimpleOutputRepresentation representation =
                new SimpleOutputRepresentation(MediaType.TEXT_PLAIN, "streamed content");

        try (InputStream stream = representation.getStream()) {
            assertEquals(
                    "streamed content", IoUtils.toString(stream, representation.getCharacterSet()));
        }
    }
}
