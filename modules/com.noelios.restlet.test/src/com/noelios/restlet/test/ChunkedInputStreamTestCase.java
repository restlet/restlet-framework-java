/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import com.noelios.restlet.http.ChunkedInputStream;
import com.noelios.restlet.http.ChunkedOutputStream;

/**
 * Test cases for the chunked decoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class ChunkedInputStreamTestCase extends TestCase {

    private String read(InputStream input) throws IOException {
        final byte[] buffer = new byte[1024];
        final StringBuilder result = new StringBuilder();

        int bytesRead = input.read(buffer);
        while (bytesRead != -1) {
            result.append(new String(buffer, 0, bytesRead));
            bytesRead = input.read(buffer);
        }
        return result.toString();
    }

    public void testClose() throws IOException {
        final String data = "test data";
        InputStream input = write(data);
        InputStream chunked = new ChunkedInputStream(input);

        assertEquals('t', chunked.read());
        chunked.close();
        assertEquals(-1, chunked.read());

        input = write(data);
        chunked = new ChunkedInputStream(input);

        chunked.close();
        assertEquals(-1, chunked.read());
    }

    public void testRead() throws IOException {
        String data = "test data";
        InputStream input = write(data);
        InputStream chunked = new ChunkedInputStream(input);

        assertEquals(data, read(chunked));

        input = new ByteArrayInputStream(
                "1a; ignore-stuff-here\r\nabcdefghijklmnopqrstuvwxyz\r\n10; other stuff\r\n1234567890abcdef\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(input);

        assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef",
                read(chunked));

        input = new ByteArrayInputStream(
                "\r\n1a; ignore-stuff-here\r\nabcdefghijklmnopqrstuvwxyz\r\n10; other stuff\r\n1234567890abcdef\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(input);

        assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef",
                read(chunked));

        data = "";
        input = write(data);
        chunked = new ChunkedInputStream(input);

        assertEquals(data, read(chunked));

        data = "\r\n";
        input = write(data);
        chunked = new ChunkedInputStream(input);

        assertEquals(data, read(chunked));
    }

    public void testReadWithChunkSizeComments() throws IOException {
        InputStream input = new ByteArrayInputStream(
                "9; comment\r\ntest data\r\n0\r\n\r\n".getBytes());
        InputStream chunked = new ChunkedInputStream(input);

        assertEquals("test data", read(chunked));

        input = new ByteArrayInputStream(
                "9 ; comment\r\ntest data\r\n0\r\n\r\n".getBytes());
        chunked = new ChunkedInputStream(input);

        assertEquals("test data", read(chunked));

        input = new ByteArrayInputStream(
                "4; comment\r\ntest\r\n5; another comment\r\n data\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(input);

        assertEquals("test data", read(chunked));
    }

    private InputStream write(String data) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream chunked = new ChunkedOutputStream(out, 2);

        chunked.write(data.getBytes());
        chunked.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
