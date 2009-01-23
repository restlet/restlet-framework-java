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

import org.restlet.engine.http.HttpClientCall;
import org.restlet.engine.http.HttpServerCall;

import junit.framework.TestCase;


/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class HttpCallTestCase extends TestCase {

    public void testFormatContentDisposition() {
        assertEquals("attachment; filename=\"\"", HttpServerCall
                .formatContentDisposition(null));
        assertEquals("attachment; filename=\"\"", HttpServerCall
                .formatContentDisposition(""));
        assertEquals("attachment; filename=\"test.txt\"", HttpServerCall
                .formatContentDisposition("test.txt"));
        assertEquals("attachment; filename=\"file with space.txt\"",
                HttpServerCall.formatContentDisposition("file with space.txt"));
    }

    public void testParseContentDisposition() {
        assertEquals("file.txt", HttpClientCall
                .parseContentDisposition("attachment; fileName=\"file.txt\""));
        assertEquals("file.txt", HttpClientCall
                .parseContentDisposition("attachment; fileName=file.txt"));
        assertEquals(
                "file with space.txt",
                HttpClientCall
                        .parseContentDisposition("attachment; filename=\"file with space.txt\""));
        assertEquals(
                "file with space.txt",
                HttpClientCall
                        .parseContentDisposition("attachment; filename=file with space.txt"));
        assertEquals("", HttpClientCall
                .parseContentDisposition("attachment; fileName=\"\""));
        assertEquals("", HttpClientCall
                .parseContentDisposition("attachment; fileName="));
        assertNull(HttpClientCall
                .parseContentDisposition("attachment; fileNam"));
    }
}
