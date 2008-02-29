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

package org.restlet.test.jaxrs.util;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.util.EncodeOrCheck;

@SuppressWarnings("unchecked")
public class EncodeOrCheckTests extends TestCase {

    void checkForHexDigitAllowd(String string) {
        int indexOfPercent = string.indexOf('%');
        if (indexOfPercent < 0)
            throw new IllegalArgumentException(
                    "The percent position must be at least 0");
        EncodeOrCheck.checkForHexDigit(string, indexOfPercent);
    }

    void checkForHexDigitAllowd(String string, int percentPos) {
        EncodeOrCheck.checkForHexDigit(string, percentPos);
    }

    void checkForHexDigitReject(String string) {
        int indexOfPercent = string.indexOf('%');
        if (indexOfPercent < 0)
            throw new IllegalArgumentException(
                    "The percent position must be at least 0");
        try {
            EncodeOrCheck.checkForHexDigit(string, indexOfPercent);
            fail("\"" + string + "\" is not allowed");
        } catch (IllegalArgumentException e) {
            // 
        }
    }

    void checkForHexDigitReject(String string, int percentPos) {
        try {
            EncodeOrCheck.checkForHexDigit(string, percentPos);
            fail("\"" + string + "\" is not allowed");
        } catch (IllegalArgumentException e) {
            // 
        }
    }

    private void checkForInvalidCharFail(String uriPart) {
        try {
            EncodeOrCheck.checkForInvalidUriChars(uriPart, -1, "");
            fail("\"" + uriPart
                    + "\" contains an invalid char. The test must fail");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
    }

    public void testCheckForHexDigit() {
        checkForHexDigitReject("fh%");
        checkForHexDigitReject("fh%5");
        checkForHexDigitAllowd("fh%5f");
        checkForHexDigitAllowd("fh%5d");
        checkForHexDigitReject("fh%5J");
    }

    public void testCheckForInvalidUriChars() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890{}";
        EncodeOrCheck.checkForInvalidUriChars(allowed, -1, "");
        EncodeOrCheck.checkForInvalidUriChars("aaaaa", -1, "");
        EncodeOrCheck.checkForInvalidUriChars("\\\\\\", -1, "");
        checkForInvalidCharFail("a:a");
        checkForInvalidCharFail("a:1");
        checkForInvalidCharFail("/a:");
        checkForInvalidCharFail("a:");
        checkForInvalidCharFail("/");
        checkForInvalidCharFail(" ");
        checkForInvalidCharFail("\0");
    }

    public void testFragment() {
        EncodeOrCheck.fragment(EncodeOrCheck.RESERVED, false);
        EncodeOrCheck.fragment(EncodeOrCheck.UNRESERVED, false);
        EncodeOrCheck.fragment(EncodeOrCheck.TEMPL_PARAMS, false);
        EncodeOrCheck.fragment("%20%27HH", false);
        String forbidden = EncodeOrCheck.FRAGMENT_FORBIDDEN;
        for (int i = 0; i < forbidden.length(); i++) {
            try {
                String substring = forbidden.substring(i, i + 1);
                System.out.print(substring);
                EncodeOrCheck.fragment(substring, false);
                fail("The char " + substring.charAt(0) + " ("
                        + ((int) substring.charAt(0))
                        + ") must not be allwed in a fragment");
            } catch (IllegalArgumentException e) {
                // 
            }
        }
    }
}