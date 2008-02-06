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

package org.restlet.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.restlet.util.Template;
import org.restlet.util.Variable;

/**
 * Test case for URI templates.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateTestCase extends TestCase {

    private static String TEMPLATE1 = "http://{userId}.noelios.com/invoices/{invoiceId}";

    public void testVariableNames() throws Exception {
        Template tpl = new Template(Logger.getAnonymousLogger(), TEMPLATE1);
        List<String> names = tpl.getVariableNames();

        assertEquals(2, names.size());
        assertEquals("userId", names.get(0));
        assertEquals("invoiceId", names.get(1));
    }

    public void testPathMatching() {
        Template template = new Template("http://www.mydomain.com/abc/{v1}");
        template.setMatchingMode(Template.MODE_STARTS_WITH);
        template.getDefaultVariable().setType(Variable.TYPE_URI_PATH);
        Map<String, Object> variables1 = new HashMap<String, Object>();

        String string1 = "http://www.mydomain.com/abc/123/456";
        int parsed1 = template.parse(string1, variables1);
        assertTrue("parsing of " + string1
                + " not successful, but it should be.", parsed1 >= 0);
        assertEquals("123/456", variables1.get("v1"));

        Map<String, Object> variables2 = new HashMap<String, Object>();
        String string2 = "http://www.mydomain.com/abc/123/456?s=tuv";
        int parsed2 = template.parse(string2, variables2);
        assertTrue("parsing of " + string2
                + " not successful, but it should be.", parsed2 >= 0);
        assertEquals("123/456", variables2.get("v1"));

        Map<String, Object> variables3 = new HashMap<String, Object>();
        String string3 = "http://www.mydomain.com/abc/123/456#tuv";
        int parsed3 = template.parse(string3, variables3);
        assertTrue("parsing of " + string3
                + " not successful, but it should be.", parsed3 >= 0);
        assertEquals("123/456", variables3.get("v1"));
    }

    public void testWithPercentChars() {
        Template template = new Template("abc/{v1}");
        template.getDefaultVariable().setType(Variable.TYPE_URI_ALL);
        Map<String, Object> variables1 = new HashMap<String, Object>();
        String string1 = "abc/hff11kh";
        int parsed1 = template.parse(string1, variables1);
        assertTrue("parsing of " + string1
                + " not successful, but it should be.", parsed1 >= 0);
        assertEquals("hff11kh", variables1.get("v1"));

        Map<String, Object> variables2 = new HashMap<String, Object>();
        String string2 = "abc/hf%20kh";
        int parsed2 = template.parse(string2, variables2);
        assertTrue("parsing of " + string2
                + " not successful, but it should be.", parsed2 >= 0);
        assertEquals("hf%20kh", variables2.get("v1"));
    }
}
