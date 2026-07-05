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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

/**
 * Unit tests for the {@link StreamRepresentation} abstract class, exercised through a minimal local
 * subclass.
 *
 * @author Jerome Louvel
 */
class StreamRepresentationTestCase {

    /** Minimal concrete subclass backed by an in-memory byte array. */
    private static class SimpleStreamRepresentation extends StreamRepresentation {

        private final String content;

        SimpleStreamRepresentation(MediaType mediaType, String content) {
            super(mediaType);
            this.content = content;
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void constructor_setsMediaType() {
        SimpleStreamRepresentation representation =
                new SimpleStreamRepresentation(MediaType.TEXT_PLAIN, "content");

        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    @Test
    void getReader_delegatesToStreamUsingConfiguredCharacterSet() throws IOException {
        SimpleStreamRepresentation representation =
                new SimpleStreamRepresentation(MediaType.TEXT_PLAIN, "héllo");
        representation.setCharacterSet(CharacterSet.UTF_8);

        try (Reader reader = representation.getReader()) {
            char[] buffer = new char[5];
            int read = reader.read(buffer);
            assertEquals("héllo", new String(buffer, 0, read));
        }
    }

    @Test
    void writeToWriter_delegatesToWriteOutputStream() throws IOException {
        SimpleStreamRepresentation representation =
                new SimpleStreamRepresentation(MediaType.TEXT_PLAIN, "writer content");
        representation.setCharacterSet(CharacterSet.UTF_8);
        StringWriter writer = new StringWriter();

        representation.write(writer);

        assertEquals("writer content", writer.toString());
    }

    @Test
    void writeToOutputStream_writesRawContent() throws IOException {
        SimpleStreamRepresentation representation =
                new SimpleStreamRepresentation(MediaType.TEXT_PLAIN, "raw");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        representation.write(out);

        assertEquals("raw", out.toString(StandardCharsets.UTF_8.name()));
    }
}
