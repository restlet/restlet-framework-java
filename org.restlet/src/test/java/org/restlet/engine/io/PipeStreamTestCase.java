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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

class PipeStreamTestCase {

    @Test
    void writeThenReadThenClose_roundTripsBytesAndSignalsEndOfStream() throws IOException {
        PipeStream pipe = new PipeStream();
        OutputStream out = pipe.getOutputStream();
        InputStream in = pipe.getInputStream();

        out.write('a');
        out.write('b');
        out.close();

        assertEquals('a', in.read());
        assertEquals('b', in.read());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
    }

    @Test
    void write_masksToUnsignedByte() throws IOException {
        PipeStream pipe = new PipeStream();
        OutputStream out = pipe.getOutputStream();
        InputStream in = pipe.getInputStream();

        out.write(0xFF);
        out.close();

        assertEquals(0xFF, in.read());
    }
}
