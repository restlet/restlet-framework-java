/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.Range;

class RangeInputStreamTestCase {

    private static byte[] source() {
        return "0123456789".getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    void readByteArray_boundedRange_returnsOnlyThatSlice() throws IOException {
        RangeInputStream in =
                new RangeInputStream(new ByteArrayInputStream(source()), 10, new Range(2, 5));

        byte[] buffer = new byte[10];
        int read = in.read(buffer, 0, buffer.length);

        assertEquals("23456", new String(buffer, 0, read, StandardCharsets.US_ASCII));
    }

    @Test
    void readByteArray_unboundedRangeFromIndex_readsToEndOfStream() throws IOException {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()), 10, new Range(5, Range.SIZE_MAX));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertEquals("56789", out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void readByteArray_lastRangeWithKnownSize_readsFromComputedStart() throws IOException {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()), 10, new Range(Range.INDEX_LAST, 3));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[10];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertEquals("789", out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void readSingleByte_skipsBytesOutsideRange() throws IOException {
        RangeInputStream in =
                new RangeInputStream(new ByteArrayInputStream(source()), 10, new Range(2, 3));

        assertEquals('2', (char) in.read());
        assertEquals('3', (char) in.read());
        assertEquals('4', (char) in.read());
        assertEquals(-1, in.read());
    }

    @Test
    void available_returnsConfiguredRangeSize() throws IOException {
        RangeInputStream in =
                new RangeInputStream(new ByteArrayInputStream(source()), 10, new Range(2, 5));
        assertEquals(5, in.available());
    }

    @Test
    void mark_withRegularIndex_doesNotThrow() {
        RangeInputStream in =
                new RangeInputStream(new ByteArrayInputStream(source()), 10, new Range(2, 5));
        in.mark(10);
    }

    @Test
    void mark_withLastIndex_doesNotThrow() {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()), 10, new Range(Range.INDEX_LAST, 3));
        in.mark(10);
    }

    @Test
    void constructor_unknownSizeWithLastIndexAndNonMaxSize_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new RangeInputStream(
                                new ByteArrayInputStream(source()),
                                org.restlet.representation.Representation.UNKNOWN_SIZE,
                                new Range(Range.INDEX_LAST, 3)));
    }

    @Test
    void constructor_unknownSizeWithLastIndexAndMaxSize_readsWholeStream() throws IOException {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()),
                        org.restlet.representation.Representation.UNKNOWN_SIZE,
                        new Range(Range.INDEX_LAST, Range.SIZE_MAX));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[10];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertEquals("0123456789", out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void constructor_unknownSizeWithIndexAndMaxSize_readsFromIndexToEnd() throws IOException {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()),
                        org.restlet.representation.Representation.UNKNOWN_SIZE,
                        new Range(3, Range.SIZE_MAX));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[10];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertEquals("3456789", out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void constructor_unknownSizeWithIndexAndBoundedSize_readsExactSlice() throws IOException {
        RangeInputStream in =
                new RangeInputStream(
                        new ByteArrayInputStream(source()),
                        org.restlet.representation.Representation.UNKNOWN_SIZE,
                        new Range(3, 4));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[10];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertEquals("3456", out.toString(StandardCharsets.US_ASCII));
    }
}
