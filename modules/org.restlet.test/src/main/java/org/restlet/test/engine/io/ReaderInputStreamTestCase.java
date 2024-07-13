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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.engine.io.ReaderInputStream;
import org.restlet.representation.InputRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test the conversion from {@link Reader} to {@link InputStream} and the other
 * way around.
 *
 * @author Jerome Louvel
 */
public class ReaderInputStreamTestCase extends RestletTestCase {

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
