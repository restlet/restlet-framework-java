/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.restlet.engine.io.ByteUtils;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the ByteUtils class.
 * 
 * @author Kevin Conaway
 */
public class ByteUtilsTestCase extends RestletTestCase {

    public void testGetStream() throws IOException {
        final StringWriter writer = new StringWriter();

        final OutputStream out = ByteUtils.getStream(writer);

        out.write("test".getBytes());
        out.flush();
        out.close();
        assertEquals("test", writer.toString());
    }

}
