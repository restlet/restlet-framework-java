/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.representation.InputRepresentation;

/**
 * Test the conversion from {@link Reader} to {@link InputStream} and the other way around.
 *
 * @author Jerome Louvel
 */
class ReaderInputStreamTestCase {

    @Test
    void testConversion() throws IOException {
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
