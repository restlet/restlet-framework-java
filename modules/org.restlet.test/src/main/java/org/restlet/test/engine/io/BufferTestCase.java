/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.io;

import java.io.IOException;

import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.buffer.BufferState;
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
