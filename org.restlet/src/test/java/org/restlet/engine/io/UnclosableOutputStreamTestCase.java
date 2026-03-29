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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the HTTP KeepAlive.
 *
 * @author Kevin Conaway
 */
class UnclosableOutputStreamTestCase {

    static class MockOutputStream extends OutputStream {
        boolean closed = false;

        @Override
        public void close() {
            this.closed = true;
        }

        @Override
        public void write(int b) {
            // Do nothing, only closing is relevant for this test
        }
    }

    @Test
    void testClose() throws IOException {
        final MockOutputStream stream = new MockOutputStream();
        final OutputStream out = new UnclosableOutputStream(stream);
        out.close();

        assertFalse(stream.closed);
        stream.close();
        assertTrue(stream.closed);
    }

    @Test
    void testWrite() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final OutputStream out = new UnclosableOutputStream(stream);

        out.write('a');
        assertEquals("a", stream.toString());

        out.write(new byte[] {'b', 'c'});
        assertEquals("abc", stream.toString());

        out.write(new byte[] {'d', 'e', 'f', 'g'}, 0, 2);
        assertEquals("abcde", stream.toString());

        out.close();
    }
}
