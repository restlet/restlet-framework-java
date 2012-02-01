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

package org.restlet.test.engine.util;

import java.io.UnsupportedEncodingException;
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
        byte[] b = ("Man is distinguished, not only by his reason, but by this singular passion from "
                + "other animals, which is a lust of the mind, that by a perseverance of delight "
                + "in the continued and indefatigable generation of knowledge, exceeds the short "
                + "vehemence of any carnal pleasure.").getBytes();
        String s = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n"
                + "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n"
                + "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n"
                + "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n"
                + "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
        String base64 = Base64.encode(b, true);

        assertEquals(s, base64);
        roundTrip(b, true);
        assertEquals(new String(b), new String(Base64.decode(base64)));
        assertEquals("scott:tiger",
                new String(Base64.decode("c2NvdHQ6dGlnZXI=")));
    }

    public void testParsing() throws UnsupportedEncodingException {
        String header = "MGRjM2VhZWQtOWRiNi00NGQ0LWI3NDktNjI5MzgyMDdiNWIwOjBiYWU3MmFiLWFmZjYtNGFhZS1iYmU1LTkxxNjNmNjBkMQ==";

        try {
            Base64.decode(header);
            fail("Values that aren't multiple of 4 are not allowed");
        } catch (IllegalArgumentException iae) {
            // OK
        }
    }
}
