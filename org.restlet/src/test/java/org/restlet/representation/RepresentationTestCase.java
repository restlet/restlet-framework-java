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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.data.Digest;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.data.Tag;

/**
 * Unit tests for the {@link Representation} abstract class, exercised through a minimal local
 * subclass.
 *
 * @author Jerome Louvel
 */
class RepresentationTestCase {

    /** Minimal concrete subclass backed by an in-memory string. */
    private static class SimpleRepresentation extends Representation {

        private final String content;

        SimpleRepresentation(String content) {
            super(MediaType.TEXT_PLAIN);
            this.content = content;
            setSize(content.getBytes(StandardCharsets.UTF_8).length);
        }

        @Override
        public Reader getReader() {
            return new StringReader(content);
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void write(Writer writer) throws IOException {
            writer.write(content);
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void constructor_withMediaTypeOnly_setsSensibleDefaults() {
        SimpleRepresentation representation = new SimpleRepresentation("");
        representation.setSize(Representation.UNKNOWN_SIZE);

        assertTrue(representation.isAvailable());
        assertFalse(representation.isTransient());
        assertNull(representation.getDisposition());
        assertNull(representation.getDigest());
        assertNull(representation.getExpirationDate());
        assertNull(representation.getRange());
    }

    @Test
    void isAvailable_isFalseWhenSizeIsZero() {
        SimpleRepresentation representation = new SimpleRepresentation("");

        assertEquals(0, representation.getSize());
        assertTrue(representation.isEmpty());
        assertFalse(representation.isAvailable());
    }

    @Test
    void hasKnownSize_isTrueWhenSizeIsNonNegative() {
        SimpleRepresentation representation = new SimpleRepresentation("content");

        assertTrue(representation.hasKnownSize());

        representation.setSize(Representation.UNKNOWN_SIZE);
        assertFalse(representation.hasKnownSize());
    }

    @Test
    void exhaust_consumesStreamAndReturnsByteCount() throws IOException {
        SimpleRepresentation representation = new SimpleRepresentation("0123456789");

        long consumed = representation.exhaust();

        assertEquals(10L, consumed);
    }

    @Test
    void getText_usesWriteWhenAvailable() throws IOException {
        SimpleRepresentation representation = new SimpleRepresentation("some text");

        assertEquals("some text", representation.getText());
    }

    @Test
    void getText_returnsEmptyStringWhenEmpty() throws IOException {
        SimpleRepresentation representation = new SimpleRepresentation("");

        assertEquals("", representation.getText());
    }

    @Test
    void append_writesTextToAppendable() throws IOException {
        SimpleRepresentation representation = new SimpleRepresentation("appended");
        StringBuilder builder = new StringBuilder();

        representation.append(builder);

        assertEquals("appended", builder.toString());
    }

    @Test
    void setDigestThenGetDigest_roundTrips() {
        SimpleRepresentation representation = new SimpleRepresentation("data");
        Digest digest = new Digest(Digest.ALGORITHM_MD5, new byte[] {1, 2, 3});

        representation.setDigest(digest);

        assertEquals(digest, representation.getDigest());
    }

    @Test
    void setDispositionThenGetDisposition_roundTrips() {
        SimpleRepresentation representation = new SimpleRepresentation("data");
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);

        representation.setDisposition(disposition);

        assertEquals(disposition, representation.getDisposition());
    }

    @Test
    void setExpirationDateThenGetExpirationDate_roundTrips() {
        SimpleRepresentation representation = new SimpleRepresentation("data");
        Date date = new Date(0);

        representation.setExpirationDate(date);

        assertEquals(date, representation.getExpirationDate());
    }

    @Test
    void setRangeThenGetRange_roundTrips() {
        SimpleRepresentation representation = new SimpleRepresentation("data");
        Range range = new Range(0, 10);

        representation.setRange(range);

        assertEquals(range, representation.getRange());
    }

    @Test
    void setTransientThenIsTransient_roundTrips() {
        SimpleRepresentation representation = new SimpleRepresentation("data");

        representation.setTransient(true);

        assertTrue(representation.isTransient());
    }

    @Test
    void release_marksRepresentationUnavailable() {
        SimpleRepresentation representation = new SimpleRepresentation("data");

        representation.release();

        assertFalse(representation.isAvailable());
    }

    @Test
    void constructorFromVariant_copiesMetadata() {
        Variant variant = new Variant(MediaType.APPLICATION_JSON);
        variant.setCharacterSet(org.restlet.data.CharacterSet.UTF_8);

        // Use a dedicated subclass constructed from the variant to verify copy semantics.
        Representation fromVariant = new StreamRepresentationFromVariant(variant, new Tag("abc"));

        assertEquals(MediaType.APPLICATION_JSON, fromVariant.getMediaType());
        assertEquals(org.restlet.data.CharacterSet.UTF_8, fromVariant.getCharacterSet());
        assertEquals(new Tag("abc"), fromVariant.getTag());
    }

    /** Helper subclass used solely to exercise the Representation(Variant, Tag) constructor. */
    private static class StreamRepresentationFromVariant extends Representation {

        StreamRepresentationFromVariant(Variant variant, Tag tag) {
            super(variant, tag);
        }

        @Override
        public Reader getReader() {
            return new StringReader("");
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void write(Writer writer) {
            // no-op
        }

        @Override
        public void write(OutputStream outputStream) {
            // no-op
        }
    }
}
