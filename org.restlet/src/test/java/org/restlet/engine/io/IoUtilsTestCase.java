/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.io;

import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for the ByteUtils class.
 * 
 * @author Kevin Conaway
 */
public class IoUtilsTestCase {

    @Test
    public void testGetStream() throws IOException {
        StringWriter writer = new StringWriter();
        OutputStream out = IoUtils.getStream(writer, CharacterSet.UTF_8);
        out.write("testé".getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        assertEquals("testé", writer.toString());
    }

    @Test
    public void testPipe() throws IOException {
        final byte[] content = new byte[] { 1, 2, 3, -1, -2, -3, 4, 5, 6 };
        ByteArrayInputStream bais = new ByteArrayInputStream(content);

        OutputRepresentation or = new OutputRepresentation(
                MediaType.APPLICATION_OCTET_STREAM) {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                outputStream.write(content);
            }
        };

        InputStream is = or.getStream();
        int result = 0;

        while (result != -1) {
            result = is.read();
            assertEquals(bais.read(), result);
            System.out.println(result);
        }
    }

}
