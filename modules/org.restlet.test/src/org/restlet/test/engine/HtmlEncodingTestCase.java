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

package org.restlet.test.engine;

import org.restlet.engine.util.StringUtils;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the HTML encoding/decoding.
 * 
 * @author Thierry Boileau
 */
public class HtmlEncodingTestCase extends RestletTestCase {

    public void testEncodingDecoding() {
        assertEquals("", StringUtils.htmlEscape(""));
        assertNull(StringUtils.htmlEscape(null));
        assertEquals("", StringUtils.htmlUnescape(""));
        assertNull(StringUtils.htmlUnescape(null));

        String str = "<test>azertyéè@à&%ù€®&#174;&reg;</test>&&&;&testest";
        String strEncoded = "&lt;test&gt;azerty&eacute;&egrave;@&agrave;&amp;%&ugrave;&euro;&reg;&amp;#174;&amp;reg;&lt;/test&gt;&amp;&amp;&amp;;&amp;testest";
        String strDecoded = "<test>azertyéè@à&%ù€®®®</test>&&&;&testest";

        assertEquals(strEncoded, StringUtils.htmlEscape(str));
        assertEquals(strDecoded, StringUtils.htmlUnescape(str));

        // Test encoding/decoding as identity tranformation
        assertEquals(str, StringUtils.htmlUnescape(StringUtils.htmlEscape(str)));
        // Test decoding twice
        assertEquals(strDecoded,
                StringUtils.htmlUnescape(StringUtils.htmlUnescape(str)));
    }
}
