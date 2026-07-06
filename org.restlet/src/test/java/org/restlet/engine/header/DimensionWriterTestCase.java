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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;
import org.restlet.data.Dimension;

class DimensionWriterTestCase {

    @Test
    void write_nullOrEmpty_returnsEmptyString() {
        assertEquals("", DimensionWriter.write((Collection<Dimension>) null));
        assertEquals("", DimensionWriter.write(Collections.emptyList()));
    }

    @Test
    void write_clientAddress_returnsStar() {
        assertEquals("*", DimensionWriter.write(Collections.singleton(Dimension.CLIENT_ADDRESS)));
    }

    @Test
    void write_time_returnsStar() {
        assertEquals("*", DimensionWriter.write(Collections.singleton(Dimension.TIME)));
    }

    @Test
    void write_unspecified_returnsStar() {
        assertEquals("*", DimensionWriter.write(Collections.singleton(Dimension.UNSPECIFIED)));
    }

    @Test
    void write_characterSet_writesAcceptCharsetHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_ACCEPT_CHARSET,
                DimensionWriter.write(Collections.singleton(Dimension.CHARACTER_SET)));
    }

    @Test
    void write_clientAgent_writesUserAgentHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_USER_AGENT,
                DimensionWriter.write(Collections.singleton(Dimension.CLIENT_AGENT)));
    }

    @Test
    void write_encoding_writesAcceptEncodingHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_ACCEPT_ENCODING,
                DimensionWriter.write(Collections.singleton(Dimension.ENCODING)));
    }

    @Test
    void write_language_writesAcceptLanguageHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_ACCEPT_LANGUAGE,
                DimensionWriter.write(Collections.singleton(Dimension.LANGUAGE)));
    }

    @Test
    void write_mediaType_writesAcceptHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_ACCEPT,
                DimensionWriter.write(Collections.singleton(Dimension.MEDIA_TYPE)));
    }

    @Test
    void write_authorization_writesAuthorizationHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_AUTHORIZATION,
                DimensionWriter.write(Collections.singleton(Dimension.AUTHORIZATION)));
    }

    @Test
    void write_origin_writesOriginHeaderName() {
        assertEquals(
                HeaderConstants.HEADER_ORIGIN,
                DimensionWriter.write(Collections.singleton(Dimension.ORIGIN)));
    }

    @Test
    void write_multipleDimensions_joinsWithSeparator() {
        LinkedHashSet<Dimension> dimensions =
                new LinkedHashSet<>(Arrays.asList(Dimension.MEDIA_TYPE, Dimension.ENCODING));
        assertEquals(
                HeaderConstants.HEADER_ACCEPT + ", " + HeaderConstants.HEADER_ACCEPT_ENCODING,
                DimensionWriter.write(dimensions));
    }
}
