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

package org.restlet.example.book.restlet.ch04.sec1;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.restlet.engine.io.IoUtils;
import org.restlet.representation.AppendableRepresentation;

/**
 * Test the appendable representation.
 */
public class AppendableTestCase extends TestCase {

    public void testAppendable() throws IOException {
        // Create the representation
        AppendableRepresentation ar = new AppendableRepresentation();
        ar.append("abcd");
        ar.append("1234");

        // Get its content as text
        assertEquals("abcd1234", ar.getText());

        // Append a new line character
        ar.append('\n');

        // Write its content to the console's output stream
        ar.write(System.out);

        // Copy its content as an input stream to the console
        IoUtils.copy(ar.getStream(), System.out);

        // Write its content to the console's writer
        Writer writer = new OutputStreamWriter(System.out);
        ar.write(writer);

        // Copy its content as a reader to the console
        IoUtils.copy(ar.getReader(), writer);
    }

}
