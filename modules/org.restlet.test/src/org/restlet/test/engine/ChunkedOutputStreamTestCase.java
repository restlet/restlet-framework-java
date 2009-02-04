/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import org.restlet.engine.http.ChunkedOutputStream;
import org.restlet.test.RestletTestCase;


/**
 * Test cases for the chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class ChunkedOutputStreamTestCase extends RestletTestCase {

    public void testCallCloseTwice() throws IOException {
        final String data = "test data";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out);

        chunked.write(data.getBytes());
        chunked.close();
        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals("9\r\ntest data\r\n0\r\n\r\n", result);
    }

    public void testCallFlushAndClose() throws IOException {
        final String data = "test data";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out);

        chunked.write(data.getBytes());
        chunked.flush();
        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals("9\r\ntest data\r\n0\r\n\r\n", result);
    }

    public void testEmptyWrite() throws IOException {
        final String data = "";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out);

        chunked.write(data.getBytes());
        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals("0\r\n\r\n", result);
    }

    public void testNoWrite() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out);

        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals("0\r\n\r\n", result);
    }

    public void testWrite() throws IOException {
        final String data = "test data";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out);

        chunked.write(data.getBytes());
        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals("9\r\ntest data\r\n0\r\n\r\n", result);
    }

    public void testWriteSmallBuffer() throws IOException {
        final String data = "test data";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out, 2);

        chunked.write(data.getBytes());
        chunked.close();

        final String result = new String(out.toByteArray());

        assertEquals(
                "2\r\nte\r\n2\r\nst\r\n2\r\n d\r\n2\r\nat\r\n1\r\na\r\n0\r\n\r\n",
                result);
    }
}
