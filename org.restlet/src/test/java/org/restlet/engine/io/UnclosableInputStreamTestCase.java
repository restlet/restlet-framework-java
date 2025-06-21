/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the HTTP KeepAlive.
 *
 * @author Kevin Conaway
 */
public class UnclosableInputStreamTestCase {

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
    public void testClose() throws IOException {
        final MockInputStream mock = new MockInputStream();
        final InputStream keepAlive = new UnclosableInputStream(mock);

        keepAlive.close();
        assertFalse(mock.closed);
        mock.close();
        assertTrue(mock.closed);
    }
}
