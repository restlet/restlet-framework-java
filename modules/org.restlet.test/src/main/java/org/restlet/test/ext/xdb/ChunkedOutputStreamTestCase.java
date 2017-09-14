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

package org.restlet.test.ext.xdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.restlet.ext.xdb.internal.ChunkedOutputStream;
import org.restlet.test.RestletTestCase;

/**
 * Test cases for the chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
@Deprecated
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
