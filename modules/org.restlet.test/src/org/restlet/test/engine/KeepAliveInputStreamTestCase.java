/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

import java.io.IOException;
import java.io.InputStream;

import org.restlet.engine.io.KeepAliveInputStream;
import org.restlet.test.RestletTestCase;


/**
 * Unit tests for the HTTP KeepAlive.
 * 
 * @author Kevin Conaway
 */
public class KeepAliveInputStreamTestCase extends RestletTestCase {

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
        final InputStream keepalive = new KeepAliveInputStream(mock);

        keepalive.close();
        assertFalse(mock.closed);
        mock.close();
        assertTrue(mock.closed);
    }
}
