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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * Unit tests for the {@link StringRepresentation} class.
 *
 * @author Jerome Louvel
 */
class StringRepresentationTestCase {

    @Test
    void constructor_withCharSequence_usesTextPlainAndUtf8() {
        StringRepresentation representation = new StringRepresentation("hello");

        assertEquals("hello", representation.getText());
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertEquals(CharacterSet.UTF_8, representation.getCharacterSet());
    }

    @Test
    void constructor_withCharArray_convertsToString() {
        StringRepresentation representation = new StringRepresentation("hello".toCharArray());

        assertEquals("hello", representation.getText());
    }

    @Test
    void constructor_withLanguage_addsLanguageToList() {
        StringRepresentation representation = new StringRepresentation("hello", Language.ENGLISH);

        assertTrue(representation.getLanguages().contains(Language.ENGLISH));
    }

    @Test
    void constructor_withMediaType_setsMediaType() {
        StringRepresentation representation =
                new StringRepresentation("{}", MediaType.APPLICATION_JSON);

        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
    }

    @Test
    void constructor_withFullSignature_setsAllMetadata() {
        StringRepresentation representation =
                new StringRepresentation(
                        "text", MediaType.TEXT_PLAIN, Language.FRENCH, CharacterSet.ISO_8859_1);

        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
        assertTrue(representation.getLanguages().contains(Language.FRENCH));
        assertEquals(CharacterSet.ISO_8859_1, representation.getCharacterSet());
    }

    @Test
    void getSize_reflectsUtf8ByteLength() {
        StringRepresentation representation = new StringRepresentation("café");

        assertEquals(
                "café".getBytes(CharacterSet.UTF_8.toCharset()).length, representation.getSize());
    }

    @Test
    void setText_updatesSize() {
        StringRepresentation representation = new StringRepresentation("abc");

        representation.setText("abcdef");

        assertEquals(6, representation.getSize());
        assertEquals("abcdef", representation.getText());
    }

    @Test
    void setText_withNull_setsUnknownSize() {
        StringRepresentation representation = new StringRepresentation("abc");

        representation.setText((String) null);

        assertNull(representation.getText());
        assertEquals(Representation.UNKNOWN_SIZE, representation.getSize());
    }

    @Test
    void getReader_returnsFreshReaderWithFullContent() throws IOException {
        StringRepresentation representation = new StringRepresentation("reader content");

        try (Reader reader = representation.getReader()) {
            char[] buffer = new char[32];
            int read = reader.read(buffer);
            assertEquals("reader content", new String(buffer, 0, read));
        }
    }

    @Test
    void getStream_encodesTextUsingCharacterSet() throws IOException {
        StringRepresentation representation = new StringRepresentation("stream content");

        String result =
                new String(
                        representation.getStream().readAllBytes(), CharacterSet.UTF_8.toCharset());

        assertEquals("stream content", result);
    }

    @Test
    void write_writesTextToWriter() throws IOException {
        StringRepresentation representation = new StringRepresentation("written content");
        StringWriter writer = new StringWriter();

        representation.write(writer);

        assertEquals("written content", writer.toString());
    }

    @Test
    void toString_returnsText() {
        StringRepresentation representation = new StringRepresentation("as string");

        assertEquals("as string", representation.toString());
    }

    @Test
    void release_clearsTextAndMarksUnavailable() {
        StringRepresentation representation = new StringRepresentation("to release");

        representation.release();

        assertNull(representation.getText());
        assertFalse(representation.isAvailable());
    }

    @Test
    void setCharacterSet_updatesSize() {
        StringRepresentation representation = new StringRepresentation("café");

        representation.setCharacterSet(CharacterSet.ISO_8859_1);

        assertEquals(
                "café".getBytes(CharacterSet.ISO_8859_1.toCharset()).length,
                representation.getSize());
    }

    @Test
    void write_withNullText_writesNothing() throws IOException {
        StringRepresentation representation = new StringRepresentation("abc");
        representation.setText(null);
        StringWriter writer = new StringWriter();

        representation.write(writer);

        assertEquals("", writer.toString());
    }
}
