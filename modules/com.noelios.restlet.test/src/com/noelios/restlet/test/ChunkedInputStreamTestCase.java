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
