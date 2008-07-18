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

import com.noelios.restlet.http.ChunkedOutputStream;

/**
 * Test cases for the chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class ChunkedOutputStreamTestCase extends TestCase {

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
