/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the HTTP KeepAlive.
 *
 * @author Kevin Conaway
 */
class UnclosableInputStreamTestCase {

    static class MockInputStream extends InputStream {
        boolean closed = false;

        @Override
        public void close() {
            this.closed = true;
        }

        @Override
        public int read() {
            return -1;
        }
    }

    @Test
    void testClose() throws IOException {
        final MockInputStream mock = new MockInputStream();
        final InputStream keepAlive = new UnclosableInputStream(mock);

        keepAlive.close();
        assertFalse(mock.closed);
        mock.close();
        assertTrue(mock.closed);
    }
}
