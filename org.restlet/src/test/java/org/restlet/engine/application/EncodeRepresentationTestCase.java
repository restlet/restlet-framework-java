/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.restlet.data.Disposition;
import org.restlet.data.Encoding;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link EncodeRepresentation}. */
class EncodeRepresentationTestCase {

    @Test
    void canEncode_forSupportedEncoding_isTrue() {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("text"));
        assertTrue(representation.canEncode());
    }

    @Test
    void canEncode_forUnsupportedEncoding_isFalse() {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("text"));
        assertFalse(representation.canEncode());
    }

    @Test
    void getSupportedEncodings_containsExpectedEncodings() {
        assertTrue(EncodeRepresentation.getSupportedEncodings().contains(Encoding.GZIP));
        assertTrue(EncodeRepresentation.getSupportedEncodings().contains(Encoding.DEFLATE));
        assertTrue(EncodeRepresentation.getSupportedEncodings().contains(Encoding.ZIP));
        assertTrue(EncodeRepresentation.getSupportedEncodings().contains(Encoding.IDENTITY));
    }

    @Test
    void getEncodings_whenCanEncode_appendsOwnEncoding() {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("text"));
        assertTrue(representation.getEncodings().contains(Encoding.GZIP));
    }

    @Test
    void getEncodings_whenCannotEncode_onlyContainsWrappedEncodings() {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("text"));
        assertFalse(representation.getEncodings().contains(Encoding.COMPRESS));
    }

    @Test
    void identityEncoding_availableSizeAndSize_delegateToWrapped() {
        StringRepresentation wrapped = new StringRepresentation("text");
        EncodeRepresentation representation = new EncodeRepresentation(Encoding.IDENTITY, wrapped);
        assertEquals(wrapped.getAvailableSize(), representation.getAvailableSize());
        assertEquals(wrapped.getSize(), representation.getSize());
    }

    @Test
    void gzipEncoding_sizeAndAvailableSize_unknown() {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("text"));
        assertEquals(Representation.UNKNOWN_SIZE, representation.getSize());
        assertEquals(Representation.UNKNOWN_SIZE, representation.getAvailableSize());
    }

    @Test
    void unsupportedEncoding_sizeAndAvailableSize_delegateToWrapped() {
        StringRepresentation wrapped = new StringRepresentation("text");
        EncodeRepresentation representation = new EncodeRepresentation(Encoding.COMPRESS, wrapped);
        assertEquals(wrapped.getSize(), representation.getSize());
        assertEquals(wrapped.getAvailableSize(), representation.getAvailableSize());
    }

    @Test
    void write_gzip_producesGzipStream() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("hello world"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        try (GZIPInputStream gzip =
                new GZIPInputStream(new java.io.ByteArrayInputStream(out.toByteArray()))) {
            assertEquals("hello world", IoUtils.toString(gzip));
        }
    }

    @Test
    void write_deflate_producesDeflateStream() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.DEFLATE, new StringRepresentation("hello"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        try (InflaterInputStream inflater =
                new InflaterInputStream(new java.io.ByteArrayInputStream(out.toByteArray()))) {
            assertEquals("hello", IoUtils.toString(inflater));
        }
    }

    @Test
    void write_deflateNowrap_producesDeflateNowrapStream() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(
                        Encoding.DEFLATE_NOWRAP, new StringRepresentation("hello"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        try (InflaterInputStream inflater =
                new InflaterInputStream(
                        new java.io.ByteArrayInputStream(out.toByteArray()),
                        new java.util.zip.Inflater(true))) {
            assertEquals("hello", IoUtils.toString(inflater));
        }
    }

    @Test
    void write_zip_producesZipEntryNamedFromDisposition() throws Exception {
        StringRepresentation wrapped = new StringRepresentation("hello");
        Disposition disposition = new Disposition();
        disposition.getParameters().add(Disposition.NAME_FILENAME, "myFile");
        wrapped.setDisposition(disposition);

        EncodeRepresentation representation = new EncodeRepresentation(Encoding.ZIP, wrapped);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        try (ZipInputStream zip =
                new ZipInputStream(new java.io.ByteArrayInputStream(out.toByteArray()))) {
            var entry = zip.getNextEntry();
            assertEquals("myFile", entry.getName());
            assertEquals("hello", IoUtils.toString(zip));
        }
    }

    @Test
    void write_unsupportedEncoding_delegatesToWrapped() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("hello"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);
        assertEquals("hello", out.toString());
    }

    @Test
    void getReader_whenCanEncode_returnsDecompressibleReader() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("hello"));
        assertNotNull(representation.getReader());
    }

    @Test
    void getReader_whenCannotEncode_delegatesToWrapped() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("hello"));
        assertEquals("hello", IoUtils.toString(representation.getReader()));
    }

    @Test
    void getStream_whenCannotEncode_delegatesToWrapped() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("hello"));
        assertEquals("hello", IoUtils.toString(representation.getStream()));
    }

    @Test
    void getText_whenCannotEncode_delegatesToWrapped() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("hello"));
        assertEquals("hello", representation.getText());
    }

    @Test
    void write_writerWithEncoding_producesEncodedContent() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("hello"));
        StringWriter writer = new StringWriter();
        representation.write(writer);
        assertFalse(writer.toString().isEmpty());
    }

    @Test
    void write_writerWithoutEncoding_delegatesToWrapped() throws Exception {
        EncodeRepresentation representation =
                new EncodeRepresentation(Encoding.COMPRESS, new StringRepresentation("hello"));
        StringWriter writer = new StringWriter();
        representation.write(writer);
        assertEquals("hello", writer.toString());
    }
}
