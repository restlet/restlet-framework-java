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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Encoding;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link DecodeRepresentation}. */
class DecodeRepresentationTestCase {

    private static Representation gzip(String text) throws Exception {
        EncodeRepresentation encoded =
                new EncodeRepresentation(Encoding.GZIP, new StringRepresentation(text));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encoded.write(out);
        ByteArrayRepresentation result = new ByteArrayRepresentation(out.toByteArray());
        result.setEncodings(List.of(Encoding.GZIP));
        return result;
    }

    @Test
    void getSupportedEncodings_containsExpectedEncodings() {
        assertTrue(DecodeRepresentation.getSupportedEncodings().contains(Encoding.GZIP));
        assertTrue(DecodeRepresentation.getSupportedEncodings().contains(Encoding.IDENTITY));
    }

    @Test
    void isDecoding_forSupportedEncoding_isTrue() throws Exception {
        DecodeRepresentation representation = new DecodeRepresentation(gzip("hello"));
        assertTrue(representation.isDecoding());
    }

    @Test
    void isDecoding_forPlainRepresentation_isTrueBecauseNoEncoding() {
        DecodeRepresentation representation =
                new DecodeRepresentation(new StringRepresentation("hello"));
        assertTrue(representation.isDecoding());
        assertEquals(0, representation.getEncodings().size());
    }

    @Test
    void getStreamAndText_decodeGzippedContent() throws Exception {
        DecodeRepresentation representation = new DecodeRepresentation(gzip("hello world"));
        assertEquals("hello world", representation.getText());
    }

    @Test
    void getReader_decodesGzippedContent() throws Exception {
        DecodeRepresentation representation = new DecodeRepresentation(gzip("hello"));
        assertEquals("hello", IoUtils.toString(representation.getReader()));
    }

    @Test
    void write_decodesGzippedContentToOutputStream() throws Exception {
        DecodeRepresentation representation = new DecodeRepresentation(gzip("hello"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);
        assertEquals("hello", out.toString());
    }

    @Test
    void getAvailableSize_delegatesToIoUtils() throws Exception {
        DecodeRepresentation representation =
                new DecodeRepresentation(new StringRepresentation("hello"));
        assertEquals(5, representation.getAvailableSize());
    }

    @Test
    void getSize_forIdentityEncoding_delegatesToWrapped() {
        StringRepresentation wrapped = new StringRepresentation("hello");
        DecodeRepresentation representation = new DecodeRepresentation(wrapped);
        assertEquals(wrapped.getSize(), representation.getSize());
    }

    @Test
    void equals_delegatesToSuper() {
        StringRepresentation wrapped = new StringRepresentation("hello");
        DecodeRepresentation representation = new DecodeRepresentation(wrapped);
        assertFalse(representation.equals("not a representation"));
    }
}
