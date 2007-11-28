/*
 * Copyright 2005-2007 Noelios Consulting.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import junit.framework.TestCase;

import com.noelios.restlet.http.HttpClientCall;
import com.noelios.restlet.http.HttpServerCall;

/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class HttpCallTestCase extends TestCase {

    public void testParseContentDisposition() {
        HttpClientCall call = new HttpClientCall(null, null, null);

        assertEquals("file.txt", call
                .parseContentDisposition("attachment; fileName=\"file.txt\""));
        assertEquals("file.txt", call
                .parseContentDisposition("attachment; fileName=file.txt"));
        assertEquals(
                "file with space.txt",
                call
                        .parseContentDisposition("attachment; filename=\"file with space.txt\""));
        assertEquals(
                "file with space.txt",
                call
                        .parseContentDisposition("attachment; filename=file with space.txt"));
        assertEquals("", call
                .parseContentDisposition("attachment; fileName=\"\""));
        assertEquals("", call.parseContentDisposition("attachment; fileName="));
        assertNull(call.parseContentDisposition("attachment; fileNam"));
        assertNull(null);
    }

    public void testFormatContentDisposition() {
        HttpServerCall call = new HttpServerCall(null, null, 0) {

            @Override
            public ReadableByteChannel getRequestEntityChannel(long size) {
                return null;
            }

            @Override
            public InputStream getRequestEntityStream(long size) {
                return null;
            }

            @Override
            public ReadableByteChannel getRequestHeadChannel() {
                return null;
            }

            @Override
            public InputStream getRequestHeadStream() {
                return null;
            }

            @Override
            public WritableByteChannel getResponseEntityChannel() {
                return null;
            }

            @Override
            public OutputStream getResponseEntityStream() {
                return null;
            }

        };

        assertEquals("attachment; filename=\"\"", call
                .formatContentDisposition(null));
        assertEquals("attachment; filename=\"\"", call
                .formatContentDisposition(""));
        assertEquals("attachment; filename=\"test.txt\"", call
                .formatContentDisposition("test.txt"));
        assertEquals("attachment; filename=\"file with space.txt\"", call
                .formatContentDisposition("file with space.txt"));
    }
}
