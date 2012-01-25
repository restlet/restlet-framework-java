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

package org.restlet.test.ext.xdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.restlet.engine.io.BioUtils;
import org.restlet.ext.xdb.internal.InputEntityStream;
import org.restlet.ext.xdb.internal.SizedInputStream;
import org.restlet.test.RestletTestCase;

/**
 * Test cases for the input entity stream.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * @author Jerome Louvel
 */
public class InputEntityStreamTestCase extends RestletTestCase {

    public void testRead() {
        String data = "test data";
        InputStream input = new ByteArrayInputStream(data.getBytes());
        assertEquals("test", BioUtils
                .toString(new SizedInputStream(null, input, 4)));
    }

    public void testReset() throws IOException {
        String data = "12345678";
        InputStream input = new ByteArrayInputStream(data.getBytes());
        InputEntityStream ies = new SizedInputStream(null, input, 4);

        assertEquals(true, ies.markSupported());

        ies.mark(10);

        assertEquals('1', (char) ies.read());
        assertEquals('2', (char) ies.read());
        assertEquals('3', (char) ies.read());
        assertEquals('4', (char) ies.read());
        assertEquals(-1, ies.read());
        assertEquals(-1, ies.read());

        ies.reset();

        assertEquals('1', (char) ies.read());
        assertEquals('2', (char) ies.read());

        ies.mark(10);

        assertEquals('3', (char) ies.read());
        assertEquals('4', (char) ies.read());

        ies.reset();

        assertEquals('3', (char) ies.read());
        assertEquals('4', (char) ies.read());
    }

}
