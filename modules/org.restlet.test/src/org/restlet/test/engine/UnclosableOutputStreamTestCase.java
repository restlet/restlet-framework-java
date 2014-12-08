/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.restlet.engine.io.UnclosableOutputStream;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class UnclosableOutputStreamTestCase extends RestletTestCase {

    static class MockOutputStream extends OutputStream {
        boolean closed = false;

        @Override
        public void close() throws IOException {
            this.closed = true;
        }

        @Override
        public void write(int b) throws IOException {

        }
    }

    public void testClose() throws IOException {
        final MockOutputStream stream = new MockOutputStream();
        final OutputStream out = new UnclosableOutputStream(stream);
        out.close();

        assertFalse(stream.closed);
        stream.close();
        assertTrue(stream.closed);
    }

    public void testWrite() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final OutputStream out = new UnclosableOutputStream(stream);

        out.write('a');
        assertEquals("a", new String(stream.toByteArray()));

        out.write(new byte[] { 'b', 'c' });
        assertEquals("abc", new String(stream.toByteArray()));

        out.write(new byte[] { 'd', 'e', 'f', 'g' }, 0, 2);
        assertEquals("abcde", new String(stream.toByteArray()));

        out.close();
    }

}
