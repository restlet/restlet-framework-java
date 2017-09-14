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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.ext.xdb.internal.ChunkedInputStream;
import org.restlet.ext.xdb.internal.ChunkedOutputStream;
import org.restlet.test.RestletTestCase;

/**
 * Test cases for the chunked decoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
@Deprecated
public class ChunkedInputStreamTestCase extends RestletTestCase {

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
        InputStream chunked = new ChunkedInputStream(null, input);

        assertEquals('t', chunked.read());
        chunked.close();
        assertEquals(-1, chunked.read());

        input = write(data);
        chunked = new ChunkedInputStream(null, input);

        chunked.close();
        assertEquals(-1, chunked.read());
    }

    public void testRead() throws IOException {
        String data = "test data";
        InputStream input = write(data);
        InputStream chunked = new ChunkedInputStream(null, input);

        assertEquals(data, read(chunked));

        input = new ByteArrayInputStream(
                "1a; ignore-stuff-here\r\nabcdefghijklmnopqrstuvwxyz\r\n10; other stuff\r\n1234567890abcdef\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(null, input);

        assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef",
                read(chunked));

        input = new ByteArrayInputStream(
                "\r\n1a; ignore-stuff-here\r\nabcdefghijklmnopqrstuvwxyz\r\n10; other stuff\r\n1234567890abcdef\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(null, input);

        assertEquals("abcdefghijklmnopqrstuvwxyz1234567890abcdef",
                read(chunked));

        data = "";
        input = write(data);
        chunked = new ChunkedInputStream(null, input);

        assertEquals(data, read(chunked));

        data = "\r\n";
        input = write(data);
        chunked = new ChunkedInputStream(null, input);

        assertEquals(data, read(chunked));
    }

    public void testReadWithChunkSizeComments() throws IOException {
        InputStream input = new ByteArrayInputStream(
                "9; comment\r\ntest data\r\n0\r\n\r\n".getBytes());
        InputStream chunked = new ChunkedInputStream(null, input);

        assertEquals("test data", read(chunked));

        input = new ByteArrayInputStream(
                "9 ; comment\r\ntest data\r\n0\r\n\r\n".getBytes());
        chunked = new ChunkedInputStream(null, input);

        assertEquals("test data", read(chunked));

        input = new ByteArrayInputStream(
                "4; comment\r\ntest\r\n5; another comment\r\n data\r\n0\r\n\r\n"
                        .getBytes());
        chunked = new ChunkedInputStream(null, input);

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
