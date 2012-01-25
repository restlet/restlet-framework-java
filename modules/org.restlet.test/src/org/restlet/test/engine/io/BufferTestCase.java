/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine.io;

import java.io.IOException;

import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.BufferState;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the {@link Buffer} class.
 * 
 * @author Jerome Louvel
 */
public class BufferTestCase extends RestletTestCase {

    public void testFlip() throws IOException {
        Buffer buffer = new Buffer(8192);
        buffer.fill("abcdefghijklm");
        buffer.flip();

        for (int i = 0; i < 4; i++) {
            // Drain first characters
            buffer.drain();
        }

        buffer.flip();
        buffer.fill("nopqrst");
        buffer.flip();

        for (int i = 0; i < 4; i++) {
            // Drain first characters
            buffer.drain();
        }

        buffer.flip();
        buffer.fill("uvwxyz");
        buffer.flip();

        StringBuilder sb = new StringBuilder();
        buffer.drain(sb, BufferState.FILLING);
        assertEquals("Remaining buffer", "ijklmnopqrstuvwxyz", sb.toString());
    }
}
