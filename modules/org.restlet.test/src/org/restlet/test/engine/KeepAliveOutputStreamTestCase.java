/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.restlet.engine.io.KeepAliveOutputStream;
import org.restlet.test.RestletTestCase;


/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class KeepAliveOutputStreamTestCase extends RestletTestCase {

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
        final OutputStream out = new KeepAliveOutputStream(stream);
        out.close();

        assertFalse(stream.closed);
        stream.close();
        assertTrue(stream.closed);
    }

    public void testWrite() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final OutputStream out = new KeepAliveOutputStream(stream);

        out.write('a');
        assertEquals("a", new String(stream.toByteArray()));

        out.write(new byte[] { 'b', 'c' });
        assertEquals("abc", new String(stream.toByteArray()));

        out.write(new byte[] { 'd', 'e', 'f', 'g' }, 0, 2);
        assertEquals("abcde", new String(stream.toByteArray()));
    }

}
