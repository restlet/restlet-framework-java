/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import java.util.Arrays;

import junit.framework.TestCase;

import com.noelios.restlet.util.Base64;

/**
 * Base64 test case.
 * 
 * @author Ray Waldin.
 */
public class Base64TestCase extends TestCase {

    public Base64TestCase(String name) {
        super(name);
    }

    public void roundTrip(byte[] bytes, int options) throws Exception {
        assert (Arrays.equals(
                Base64.decode(Base64.encodeBytes(bytes, options)), bytes));
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
        String base64 = Base64.encodeBytes(b, Base64.DONT_BREAK_LINES);
        assertEquals(s, base64);
        roundTrip(b, Base64.DONT_BREAK_LINES);
        assertEquals(new String(b), new String(Base64.decode(base64)));

        assertEquals("scott:tiger", new String(Base64
                .decode("c2NvdHQ6dGlnZXI=")));
    }
}
