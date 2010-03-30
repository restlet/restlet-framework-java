package org.restlet.example.book.restlet.ch05.sec1;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.restlet.engine.io.BioUtils;
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
        BioUtils.copy(ar.getStream(), System.out);

        // Write its content to the console's writer
        Writer writer = new OutputStreamWriter(System.out);
        ar.write(writer);

        // Copy its content as a reader to the console
        BioUtils.copy(ar.getReader(), writer);
    }

}
