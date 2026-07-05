/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link EmptyRepresentation} class.
 *
 * @author Jerome Louvel
 */
class EmptyRepresentationTestCase {

    @Test
    void constructor_setsUnavailableTransientAndZeroSize() {
        EmptyRepresentation representation = new EmptyRepresentation();

        assertFalse(representation.isAvailable());
        assertTrue(representation.isTransient());
        assertEquals(0, representation.getSize());
        assertTrue(representation.isEmpty());
    }

    @Test
    void getReaderGetStreamAndGetText_returnNull() throws Exception {
        EmptyRepresentation representation = new EmptyRepresentation();

        assertNull(representation.getReader());
        assertNull(representation.getStream());
        assertNull(representation.getText());
    }

    @Test
    void writeToWriter_doesNothingAndDoesNotThrow() throws Exception {
        EmptyRepresentation representation = new EmptyRepresentation();
        StringWriter writer = new StringWriter();

        representation.write(writer);

        assertEquals("", writer.toString());
    }

    @Test
    void writeToOutputStream_doesNothingAndDoesNotThrow() throws Exception {
        EmptyRepresentation representation = new EmptyRepresentation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        representation.write(out);

        assertEquals(0, out.size());
    }
}
