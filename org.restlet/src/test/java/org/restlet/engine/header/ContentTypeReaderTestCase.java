/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

class ContentTypeReaderTestCase {

    @Test
    void readValue_mediaTypeOnly_parsesMediaType() throws IOException {
        ContentType result = new ContentTypeReader("text/plain").readValue();
        assertEquals(MediaType.TEXT_PLAIN, result.getMediaType());
        assertNull(result.getCharacterSet());
    }

    @Test
    void readValue_withCharsetParameter_extractsCharacterSet() throws IOException {
        ContentType result = new ContentTypeReader("text/plain; charset=UTF-8").readValue();
        assertEquals(MediaType.TEXT_PLAIN, result.getMediaType());
        assertEquals(CharacterSet.UTF_8, result.getCharacterSet());
    }

    @Test
    void readValue_withOtherParameter_keepsItOnMediaType() throws IOException {
        ContentType result =
                new ContentTypeReader("multipart/form-data; boundary=abc123").readValue();
        assertEquals("abc123", result.getMediaType().getParameters().getFirstValue("boundary"));
    }

    @Test
    void readValue_withQuotedParameterValue_unquotesIt() throws IOException {
        ContentType result =
                new ContentTypeReader("multipart/form-data; boundary=\"abc 123\"").readValue();
        assertEquals("abc 123", result.getMediaType().getParameters().getFirstValue("boundary"));
    }

    @Test
    void readValue_emptyMediaTypeName_throws() {
        ContentTypeReader reader = new ContentTypeReader("; charset=UTF-8");
        assertThrows(IOException.class, reader::readValue);
    }

    @Test
    void readValue_emptyParameterName_throws() {
        ContentTypeReader reader = new ContentTypeReader("text/plain; =UTF-8");
        assertThrows(IOException.class, reader::readValue);
    }

    @Test
    void readValue_parameterWithoutValue_isRecordedWithNullValue() throws IOException {
        ContentType result = new ContentTypeReader("text/plain; charset").readValue();
        assertNull(result.getMediaType().getParameters().getFirstValue("charset"));
    }

    @Test
    void readValue_emptyHeader_returnsNull() throws IOException {
        assertNull(new ContentTypeReader("").readValue());
    }
}
