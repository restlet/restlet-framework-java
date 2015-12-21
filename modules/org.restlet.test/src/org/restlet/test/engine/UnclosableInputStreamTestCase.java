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

package org.restlet.test.engine;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.engine.io.UnclosableInputStream;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class UnclosableInputStreamTestCase extends RestletTestCase {

    static class MockInputStream extends InputStream {
        boolean closed = false;

        @Override
        public void close() throws IOException {
            this.closed = true;
        }

        @Override
        public int read() throws IOException {
            return -1;
        }
    }

    public void testClose() throws IOException {
        final MockInputStream mock = new MockInputStream();
        final InputStream keepalive = new UnclosableInputStream(mock);

        keepalive.close();
        assertFalse(mock.closed);
        mock.close();
        assertTrue(mock.closed);
    }
}
