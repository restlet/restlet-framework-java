/*
 * Copyright 2005-2008 Noelios Technologies.
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

import junit.framework.TestCase;

import com.noelios.restlet.http.HttpClientCall;
import com.noelios.restlet.http.HttpServerCall;

/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class HttpCallTestCase extends TestCase {

    public void testFormatContentDisposition() {
        assertEquals("attachment; filename=\"\"", HttpServerCall
                .formatContentDisposition(null));
        assertEquals("attachment; filename=\"\"", HttpServerCall
                .formatContentDisposition(""));
        assertEquals("attachment; filename=\"test.txt\"", HttpServerCall
                .formatContentDisposition("test.txt"));
        assertEquals("attachment; filename=\"file with space.txt\"",
                HttpServerCall.formatContentDisposition("file with space.txt"));
    }

    public void testParseContentDisposition() {
        assertEquals("file.txt", HttpClientCall
                .parseContentDisposition("attachment; fileName=\"file.txt\""));
        assertEquals("file.txt", HttpClientCall
                .parseContentDisposition("attachment; fileName=file.txt"));
        assertEquals(
                "file with space.txt",
                HttpClientCall
                        .parseContentDisposition("attachment; filename=\"file with space.txt\""));
        assertEquals(
                "file with space.txt",
                HttpClientCall
                        .parseContentDisposition("attachment; filename=file with space.txt"));
        assertEquals("", HttpClientCall
                .parseContentDisposition("attachment; fileName=\"\""));
        assertEquals("", HttpClientCall
                .parseContentDisposition("attachment; fileName="));
        assertNull(HttpClientCall
                .parseContentDisposition("attachment; fileNam"));
    }
}
