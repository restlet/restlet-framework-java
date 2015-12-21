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

package org.restlet.test.engine.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the ByteUtils class.
 * 
 * @author Kevin Conaway
 */
public class BioUtilsTestCase extends RestletTestCase {

    public void testGetStream() throws IOException {
        StringWriter writer = new StringWriter();
        OutputStream out = IoUtils.getStream(writer, CharacterSet.UTF_8);
        out.write("testé".getBytes("UTF-8"));
        out.flush();
        out.close();
        assertEquals("testé", writer.toString());
    }

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
