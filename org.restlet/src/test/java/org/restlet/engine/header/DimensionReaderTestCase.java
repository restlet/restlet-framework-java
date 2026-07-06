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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Dimension;
import org.restlet.data.Header;

class DimensionReaderTestCase {

    @Test
    void readValue_accept_returnsMediaType() throws IOException {
        assertEquals(
                Dimension.MEDIA_TYPE,
                new DimensionReader(HeaderConstants.HEADER_ACCEPT).readValue());
    }

    @Test
    void readValue_acceptCharset_returnsCharacterSet() throws IOException {
        assertEquals(
                Dimension.CHARACTER_SET,
                new DimensionReader(HeaderConstants.HEADER_ACCEPT_CHARSET).readValue());
    }

    @Test
    void readValue_acceptEncoding_returnsEncoding() throws IOException {
        assertEquals(
                Dimension.ENCODING,
                new DimensionReader(HeaderConstants.HEADER_ACCEPT_ENCODING).readValue());
    }

    @Test
    void readValue_acceptLanguage_returnsLanguage() throws IOException {
        assertEquals(
                Dimension.LANGUAGE,
                new DimensionReader(HeaderConstants.HEADER_ACCEPT_LANGUAGE).readValue());
    }

    @Test
    void readValue_authorization_returnsAuthorization() throws IOException {
        assertEquals(
                Dimension.AUTHORIZATION,
                new DimensionReader(HeaderConstants.HEADER_AUTHORIZATION).readValue());
    }

    @Test
    void readValue_userAgent_returnsClientAgent() throws IOException {
        assertEquals(
                Dimension.CLIENT_AGENT,
                new DimensionReader(HeaderConstants.HEADER_USER_AGENT).readValue());
    }

    @Test
    void readValue_origin_returnsOrigin() throws IOException {
        assertEquals(
                Dimension.ORIGIN, new DimensionReader(HeaderConstants.HEADER_ORIGIN).readValue());
    }

    @Test
    void readValue_star_returnsUnspecified() throws IOException {
        assertEquals(Dimension.UNSPECIFIED, new DimensionReader("*").readValue());
    }

    @Test
    void readValue_unknownValue_returnsNull() throws IOException {
        assertNull(new DimensionReader("something-else").readValue());
    }

    @Test
    void addValuesStatic_fromHeader_populatesCollection() {
        List<Dimension> dimensions = new ArrayList<>();
        Header header = new Header("Vary", HeaderConstants.HEADER_ACCEPT);
        DimensionReader.addValues(header, dimensions);
        assertEquals(1, dimensions.size());
        assertEquals(Dimension.MEDIA_TYPE, dimensions.getFirst());
    }
}
