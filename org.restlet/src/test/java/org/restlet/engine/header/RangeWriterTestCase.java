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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Range;

/** Unit tests for {@link RangeWriter}. */
class RangeWriterTestCase {

    @Test
    void write_nullList_returnsEmptyString() {
        assertEquals("", RangeWriter.write((List<Range>) null));
    }

    @Test
    void write_emptyList_returnsEmptyString() {
        assertEquals("", RangeWriter.write(List.of()));
    }

    @Test
    void write_singleRangeWithKnownEnd_formatsCorrectly() {
        Range range = new Range(0, 500);
        assertEquals("bytes=0-499", RangeWriter.write(List.of(range)));
    }

    @Test
    void write_multipleRanges_joinsWithComma() {
        Range range1 = new Range(0, 500);
        Range range2 = new Range(500, 500);
        assertEquals("bytes=0-499, 500-999", RangeWriter.write(List.of(range1, range2)));
    }

    @Test
    void write_rangeFromLastIndex_hasNoEnd() {
        Range range = new Range(Range.INDEX_LAST, 500);
        assertEquals("bytes=-500", RangeWriter.write(List.of(range)));
    }

    @Test
    void write_rangeWithSizeMax_hasNoUpperBound() {
        Range range = new Range(100, Range.SIZE_MAX);
        assertEquals("bytes=100-", RangeWriter.write(List.of(range)));
    }

    @Test
    void writeContentRange_indexFirstWithKnownSize_appendsEntitySize() {
        Range range = new Range(0, 500);
        assertEquals("bytes 0-499/1000", RangeWriter.write(range, 1000));
    }

    @Test
    void writeContentRange_indexFirstWithSizeMaxAndKnownEntitySize_usesEntitySize() {
        Range range = new Range(100, Range.SIZE_MAX);
        assertEquals("bytes 100-999/1000", RangeWriter.write(range, 1000));
    }

    @Test
    void writeContentRange_indexFirstWithSizeMaxAndUnknownEntitySize_throws() {
        Range range = new Range(100, Range.SIZE_MAX);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RangeWriter.write(
                                range, org.restlet.representation.Representation.UNKNOWN_SIZE));
    }

    @Test
    void writeContentRange_indexLastWithKnownSizeAndFittingRange_appendsRange() {
        Range range = new Range(Range.INDEX_LAST, 200);
        assertEquals("bytes 800-999/1000", RangeWriter.write(range, 1000));
    }

    @Test
    void writeContentRange_indexLastWithRangeSizeExceedingEntitySize_throws() {
        Range range = new Range(Range.INDEX_LAST, 2000);
        assertThrows(IllegalArgumentException.class, () -> RangeWriter.write(range, 1000));
    }

    @Test
    void writeContentRange_indexLastWithUnknownEntitySize_throws() {
        Range range = new Range(Range.INDEX_LAST, 200);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RangeWriter.write(
                                range, org.restlet.representation.Representation.UNKNOWN_SIZE));
    }

    @Test
    void writeContentRange_indexLastWithSizeMax_throwsInvalidRange() {
        Range range = new Range(Range.INDEX_LAST, Range.SIZE_MAX);
        assertThrows(IllegalArgumentException.class, () -> RangeWriter.write(range, 1000));
    }

    @Test
    void writeContentRange_unknownEntitySize_appendsWildcard() {
        Range range = new Range(0, 500);
        assertEquals(
                "bytes 0-499/*",
                RangeWriter.write(range, org.restlet.representation.Representation.UNKNOWN_SIZE));
    }
}
