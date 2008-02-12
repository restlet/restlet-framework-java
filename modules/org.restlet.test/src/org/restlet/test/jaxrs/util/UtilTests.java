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

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.util.Util;

@SuppressWarnings("unchecked")
public class UtilTests extends TestCase {

    public void testCheckForInvalidUriChars() {
        Util
                .checkForInvalidUriChars(
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890{}",
                        -1, "");
        checkForInvalidCharFail("a:a");
        checkForInvalidCharFail("a:1");
        checkForInvalidCharFail("/a:");
        checkForInvalidCharFail("a:");
        Util.checkForInvalidUriChars("a", -1, "");
        checkForInvalidCharFail("/");
        checkForInvalidCharFail(" ");
        checkForInvalidCharFail("\0");
        Util.checkForInvalidUriChars("\\", -1, "");
    }

    private void checkForInvalidCharFail(String uriPart) {
        try {
            Util.checkForInvalidUriChars(uriPart, -1, "");
            fail("\"" + uriPart
                    + "\" contains an invalid char. The test must fail");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
    }

    public void testIsSameOrSubType() {
        assertTrue(Util.isSameOrSubType(MediaType.ALL, MediaType.ALL));
        assertTrue(Util.isSameOrSubType(MediaType.TEXT_ALL, MediaType.ALL));
        assertTrue(Util.isSameOrSubType(MediaType.TEXT_ALL, MediaType.TEXT_ALL));
        assertTrue(Util.isSameOrSubType(MediaType.TEXT_PLAIN,
                MediaType.TEXT_ALL));
        assertTrue(Util.isSameOrSubType(MediaType.TEXT_PLAIN,
                MediaType.TEXT_PLAIN));

        assertFalse(Util.isSameOrSubType(MediaType.ALL, MediaType.TEXT_ALL));
        assertFalse(Util.isSameOrSubType(MediaType.TEXT_ALL,
                MediaType.TEXT_PLAIN));

        assertFalse(Util.isSameOrSubType(MediaType.APPLICATION_ALL,
                MediaType.TEXT_ALL));
    }

    public void testMostSpecificMediaType() {
        assertEquals(MediaType.TEXT_ALL, Util.mostSpecific(MediaType.ALL,
                MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_ALL, Util.mostSpecific(MediaType.TEXT_ALL,
                MediaType.ALL));

        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(MediaType.ALL,
                MediaType.TEXT_ALL, MediaType.TEXT_PLAIN));
        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(MediaType.ALL,
                MediaType.TEXT_PLAIN, MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(
                MediaType.TEXT_ALL, MediaType.ALL, MediaType.TEXT_PLAIN));
        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(
                MediaType.TEXT_ALL, MediaType.TEXT_PLAIN, MediaType.ALL));
        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(
                MediaType.TEXT_PLAIN, MediaType.ALL, MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_PLAIN, Util.mostSpecific(
                MediaType.TEXT_PLAIN, MediaType.TEXT_ALL, MediaType.ALL));
    }
}