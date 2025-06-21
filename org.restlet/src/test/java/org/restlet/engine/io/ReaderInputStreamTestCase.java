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
import org.restlet.representation.InputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the conversion from {@link Reader} to {@link InputStream} and the other
 * way around.
 *
 * @author Jerome Louvel
 */
public class ReaderInputStreamTestCase {

    @Test
    public void testConversion() throws IOException {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < 5000; i++) {
            buf.append(i).append('-');
        }

        String s = buf.toString();
        Reader r = new StringReader(s);
        InputStream is = new ReaderInputStream(r);

        InputRepresentation ir = new InputRepresentation(is);
        ir.setCharacterSet(CharacterSet.ISO_8859_1);

        assertEquals(s, ir.getText());
    }

}
