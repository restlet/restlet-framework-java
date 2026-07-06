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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;

class WriterOutputStreamTestCase {

    @Test
    void writeByteArray_decodesUsingGivenCharacterSet() throws IOException {
        StringWriter writer = new StringWriter();
        WriterOutputStream out = new WriterOutputStream(writer, CharacterSet.UTF_8);

        out.write("hello".getBytes(StandardCharsets.UTF_8));

        assertEquals("hello", writer.toString());
    }

    @Test
    void writeByteArray_nullCharacterSet_defaultsToIso88591() throws IOException {
        StringWriter writer = new StringWriter();
        WriterOutputStream out = new WriterOutputStream(writer, null);

        out.write("abc".getBytes(StandardCharsets.ISO_8859_1));

        assertEquals("abc", writer.toString());
    }

    @Test
    void writeSingleByte_delegatesToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        WriterOutputStream out = new WriterOutputStream(writer, CharacterSet.UTF_8);

        out.write('a');

        assertEquals("a", writer.toString());
    }

    @Test
    void writeByteArrayWithOffsetAndLength_writesOnlyThatSlice() throws IOException {
        StringWriter writer = new StringWriter();
        WriterOutputStream out = new WriterOutputStream(writer, CharacterSet.UTF_8);
        byte[] data = "xxhelloxx".getBytes(StandardCharsets.UTF_8);

        out.write(data, 2, 5);

        assertEquals("hello", writer.toString());
    }

    @Test
    void flush_flushesUnderlyingWriter() throws IOException {
        java.util.concurrent.atomic.AtomicBoolean flushed =
                new java.util.concurrent.atomic.AtomicBoolean();
        java.io.Writer tracker =
                new java.io.FilterWriter(new StringWriter()) {
                    @Override
                    public void flush() throws IOException {
                        flushed.set(true);
                        super.flush();
                    }
                };
        WriterOutputStream out = new WriterOutputStream(tracker, CharacterSet.UTF_8);

        out.flush();

        assertTrue(flushed.get());
    }

    @Test
    void close_closesUnderlyingWriter() throws IOException {
        java.util.concurrent.atomic.AtomicBoolean closed =
                new java.util.concurrent.atomic.AtomicBoolean();
        java.io.Writer tracker =
                new java.io.FilterWriter(new StringWriter()) {
                    @Override
                    public void close() throws IOException {
                        closed.set(true);
                        super.close();
                    }
                };
        WriterOutputStream out = new WriterOutputStream(tracker, CharacterSet.UTF_8);

        out.close();

        assertTrue(closed.get());
    }
}
