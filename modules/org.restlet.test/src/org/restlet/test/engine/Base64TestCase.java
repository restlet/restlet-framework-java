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

import java.util.Arrays;

import org.restlet.engine.util.Base64;
import org.restlet.test.RestletTestCase;

/**
 * Base64 test case.
 * 
 * @author Ray Waldin.
 */
public class Base64TestCase extends RestletTestCase {

    public void roundTrip(byte[] bytes, boolean newlines) throws Exception {
        assert (Arrays.equals(Base64.decode(Base64.encode(bytes, newlines)),
                bytes));
    }

    public void test() throws Exception {
        final byte[] b = ("Man is distinguished, not only by his reason, but by this singular passion from "
                + "other animals, which is a lust of the mind, that by a perseverance of delight "
                + "in the continued and indefatigable generation of knowledge, exceeds the short "
                + "vehemence of any carnal pleasure.").getBytes();
        final String s = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n"
                + "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n"
                + "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n"
                + "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n"
                + "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
        final String base64 = Base64.encode(b, true);
        assertEquals(s, base64);
        roundTrip(b, true);
        assertEquals(new String(b), new String(Base64.decode(base64)));

        assertEquals("scott:tiger", new String(Base64
                .decode("c2NvdHQ6dGlnZXI=")));
    }
}
