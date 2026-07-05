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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Unit tests for the {@link CharacterRepresentation} abstract class, exercised through a minimal
 * local subclass.
 *
 * @author Jerome Louvel
 */
class CharacterRepresentationTestCase {

    /** Minimal concrete subclass backed by an in-memory string. */
    private static class SimpleCharacterRepresentation extends CharacterRepresentation {

        private final String content;

        SimpleCharacterRepresentation(MediaType mediaType, String content) {
            super(mediaType);
            this.content = content;
        }

        @Override
        public Reader getReader() {
            return new StringReader(content);
        }

        @Override
        public void write(Writer writer) throws IOException {
            writer.write(content);
        }
    }

    @Test
    void constructor_setsMediaTypeAndDefaultsToUtf8CharacterSet() {
        SimpleCharacterRepresentation representation =
                new SimpleCharacterRepresentation(MediaType.TEXT_PLAIN, "hello");

        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertEquals(CharacterSet.UTF_8, representation.getCharacterSet());
    }

    @Test
    void getStream_producesBytesMatchingReaderContent() throws IOException {
        SimpleCharacterRepresentation representation =
                new SimpleCharacterRepresentation(MediaType.TEXT_PLAIN, "some text");

        try (InputStream stream = representation.getStream()) {
            assertEquals("some text", IoUtils.toString(stream, representation.getCharacterSet()));
        }
    }

    @Test
    void writeToOutputStream_encodesUsingConfiguredCharacterSet() throws IOException {
        SimpleCharacterRepresentation representation =
                new SimpleCharacterRepresentation(MediaType.TEXT_PLAIN, "café");
        representation.setCharacterSet(CharacterSet.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        assertEquals("café", new String(out.toByteArray(), CharacterSet.UTF_8.getName()));
    }
}
