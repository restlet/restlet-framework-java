/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.test.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.test.RestletTestCase;
import org.restlet.util.Template;
import org.restlet.util.Variable;

/**
 * Test case for URI templates.
 * 
 * @author Jerome Louvel
 */
public class TemplateTestCase extends RestletTestCase {

    private static String TEMPLATE1 = "http://{userId}.noelios.com/invoices/{invoiceId}";

    public void testPathMatching() {
        final Template template = new Template(
                "http://www.mydomain.com/abc/{v1}");
        template.setMatchingMode(Template.MODE_STARTS_WITH);
        template.getDefaultVariable().setType(Variable.TYPE_URI_PATH);
        final Map<String, Object> variables1 = new HashMap<String, Object>();

        final String string1 = "http://www.mydomain.com/abc/123/456";
        final int parsed1 = template.parse(string1, variables1);
        assertTrue("parsing of " + string1
                + " not successful, but it should be.", parsed1 >= 0);
        assertEquals("123/456", variables1.get("v1"));

        final Map<String, Object> variables2 = new HashMap<String, Object>();
        final String string2 = "http://www.mydomain.com/abc/123/456?s=tuv";
        final int parsed2 = template.parse(string2, variables2);
        assertTrue("parsing of " + string2
                + " not successful, but it should be.", parsed2 >= 0);
        assertEquals("123/456", variables2.get("v1"));

        final Map<String, Object> variables3 = new HashMap<String, Object>();
        final String string3 = "http://www.mydomain.com/abc/123/456#tuv";
        final int parsed3 = template.parse(string3, variables3);
        assertTrue("parsing of " + string3
                + " not successful, but it should be.", parsed3 >= 0);
        assertEquals("123/456", variables3.get("v1"));
    }

    public void testVariableNames() throws Exception {
        final Template tpl = new Template(TEMPLATE1);
        tpl.setLogger(Logger.getAnonymousLogger());
        final List<String> names = tpl.getVariableNames();

        assertEquals(2, names.size());
        assertEquals("userId", names.get(0));
        assertEquals("invoiceId", names.get(1));
    }

    public void testWithPercentChars() {
        final Template template = new Template("abc/{v1}");
        template.getDefaultVariable().setType(Variable.TYPE_URI_ALL);
        final Map<String, Object> variables1 = new HashMap<String, Object>();
        final String string1 = "abc/hff11kh";
        final int parsed1 = template.parse(string1, variables1);
        assertTrue("parsing of " + string1
                + " not successful, but it should be.", parsed1 >= 0);
        assertEquals("hff11kh", variables1.get("v1"));

        final Map<String, Object> variables2 = new HashMap<String, Object>();
        final String string2 = "abc/hf%20kh";
        final int parsed2 = template.parse(string2, variables2);
        assertTrue("parsing of " + string2
                + " not successful, but it should be.", parsed2 >= 0);
        assertEquals("hf%20kh", variables2.get("v1"));
    }
}
