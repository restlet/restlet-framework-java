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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for the header.
 *
 * @author Thierry Boileau
 */
class RangeReaderTestCase {

    @Test
    void testUpdateRangeFirst9Bytes() {
        final String contentRangeHeaderValue = "bytes 1-9/10";
        final Representation representation =
                new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(9, representation.getAvailableSize());
        assertEquals(1, representation.getRange().getIndex());
        assertEquals(9, representation.getRange().getSize());
    }

    @Test
    void testUpdateRange0To100Bytes() {
        final String contentRangeHeaderValue = "bytes 0-100/10";
        final Representation representation =
                new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(10, representation.getAvailableSize());
        assertEquals(0, representation.getRange().getIndex());
        assertEquals(101, representation.getRange().getSize());
    }

    @Test
    void testUpdateRange1To9Bytes() {
        final String contentRangeHeaderValue = "bytes 1-9/*";
        final Representation representation =
                new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(9, representation.getAvailableSize());
        assertEquals(1, representation.getRange().getIndex());
        assertEquals(9, representation.getRange().getSize());
    }

    @Test
    void read_null_returnsEmptyList() {
        assertEquals(List.of(), RangeReader.read(null));
    }

    @Test
    void read_firstAndLastPosition_returnsRangeOfTheirSpan() {
        assertEquals(List.of(new Range(0, 500)), RangeReader.read("bytes=0-499"));
        assertEquals(List.of(new Range(0, 1)), RangeReader.read("bytes=0-0"));
    }

    @Test
    void read_openEndedRange_hasMaximumSize() {
        assertEquals(List.of(new Range(500, Range.SIZE_MAX)), RangeReader.read("bytes=500-"));
    }

    @Test
    void read_suffixRange_isRelativeToTheEnd() {
        assertEquals(List.of(new Range(Range.INDEX_LAST, 500)), RangeReader.read("bytes=-500"));
    }

    @Test
    void read_zeroLengthSuffix_isKeptSoThatItCanBeAnsweredAsUnsatisfiable() {
        assertEquals(List.of(new Range(Range.INDEX_LAST, 0)), RangeReader.read("bytes=-0"));
    }

    @Test
    void read_lastPositionAtLongMaxValue_isOpenEnded() {
        assertEquals(
                List.of(new Range(0, Range.SIZE_MAX)),
                RangeReader.read("bytes=0-" + Long.MAX_VALUE));
    }

    @Test
    void read_severalRanges_keepsTheirOrder() {
        assertEquals(
                List.of(new Range(0, 2), new Range(3, 2), new Range(Range.INDEX_LAST, 4)),
                RangeReader.read("bytes=0-1, 3-4,-4"));
    }

    @Test
    void read_emptyListElements_areIgnored() {
        assertEquals(List.of(new Range(0, 2), new Range(3, 2)), RangeReader.read("bytes=0-1,,3-4"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bytes=0-1", "BYTES=0-1", "bytes=0-1"})
    void read_unitName_isCaseInsensitive(String header) {
        assertEquals(List.of(new Range(0, 2)), RangeReader.read(header));
    }

    /**
     * RFC 9110 section 14.2: a server MAY ignore a Range header field, and MUST ignore one that
     * contains a range unit it does not understand. An invalid ranges-specifier (section 14.1.1)
     * may be ignored or rejected: it is ignored, as a single invalid range-spec invalidates the
     * whole header.
     */
    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                " ",
                "bytes",
                "bytes=",
                "bytes=,",
                "bytes=-",
                "bytes=a-b",
                "bytes=1-2-3",
                "bytes=5-3",
                "bytes=0-1,abc",
                "bytes=+1-2",
                "bytes=1 -2",
                "bytes =0-1",
                "bytes 0-1",
                "bytes=0-1;3-4",
                "bytes=99999999999999999999-",
                "bytes=-99999999999999999999",
                "0-1",
                "items=0-1"
            })
    void read_invalidOrUnknownRangeSpecifier_isIgnored(String header) {
        assertEquals(List.of(), RangeReader.read(header));
    }
}
