/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import com.noelios.restlet.util.KeepAliveOutputStream;

/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class KeepAliveOutputStreamTestCase extends TestCase {

    static class MockOutputStream extends OutputStream {
        boolean closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public void write(int b) throws IOException {

        }
    }

    public void testClose() throws IOException {
        MockOutputStream stream = new MockOutputStream();
        OutputStream out = new KeepAliveOutputStream(stream);
        out.close();

        assertFalse(stream.closed);
        stream.close();
        assertTrue(stream.closed);
    }

    public void testWrite() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStream out = new KeepAliveOutputStream(stream);

        out.write('a');
        assertEquals("a", new String(stream.toByteArray()));

        out.write(new byte[] { 'b', 'c' });
        assertEquals("abc", new String(stream.toByteArray()));

        out.write(new byte[] { 'd', 'e', 'f', 'g' }, 0, 2);
        assertEquals("abcde", new String(stream.toByteArray()));
    }

}
