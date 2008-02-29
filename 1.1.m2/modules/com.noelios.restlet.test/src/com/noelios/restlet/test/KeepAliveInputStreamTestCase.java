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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.noelios.restlet.util.KeepAliveInputStream;

/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class KeepAliveInputStreamTestCase extends TestCase {

    static class MockInputStream extends InputStream {
        boolean closed = false;

        @Override
        public int read() throws IOException {
            return -1;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    public void testClose() throws IOException {
        MockInputStream mock = new MockInputStream();
        InputStream keepalive = new KeepAliveInputStream(mock);

        keepalive.close();
        assertFalse(mock.closed);
        mock.close();
        assertTrue(mock.closed);
    }
}
