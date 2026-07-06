/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.engine.Engine;

/**
 * Test case for URI templates.
 *
 * @author Jerome Louvel
 */
class TemplateTestCase {

    @Test
    void testEncodedCharacters() {
        Template template = new Template("http://localhost/{token}/bookstore/{bookid}");
        String encodedToken =
                "FtDF91VSX%2F7AN6C39k51ZV510SW%2Fot6SIGstq8XGCcHfOfHbZOZLUD4u%2BGUNK0bBawVZ4GR5TgV7PtRbF%2Bnm9abYJN6AWycdj9J6CLyU4D7Zou36KEjkel%2B0LtlGGhFPVrCvpBuqPy8V8o5IZ9tDys0Py6sXXAtEVbXBYeRYzOvIBzOZkIviIyceVCU%2BlYv%2Fh9k7Fhlb1JGtKUCj3ZDg%2FvJ1Co7dOC1Ho3%2Fe0Fup7k9qgTuCvZRSHcpizaEFPNLp";
        String targetUri = "http://localhost/" + encodedToken + "/bookstore/1234";

        Map<String, Object> variables1 = new HashMap<>();
        int parsed1 = template.parse(targetUri, variables1);
        assertTrue(parsed1 >= 0);
        assertEquals(encodedToken, variables1.get("token"));
    }

    @Test
    void testPathMatching() {
        Template template = new Template("http://www.mydomain.com/abc/{v1}");
        template.setMatchingMode(Template.MODE_STARTS_WITH);
        template.getDefaultVariable().setType(Variable.TYPE_URI_PATH);

        Map<String, Object> variables1 = new HashMap<>();
        String string1 = "http://www.mydomain.com/abc/123/456";
        int parsed1 = template.parse(string1, variables1);
        assertTrue(parsed1 >= 0);
        assertEquals("123/456", variables1.get("v1"));

        Map<String, Object> variables2 = new HashMap<>();
        String string2 = "http://www.mydomain.com/abc/123/456?s=tuv";
        int parsed2 = template.parse(string2, variables2);
        assertTrue(parsed2 >= 0);
        assertEquals("123/456", variables2.get("v1"));

        Map<String, Object> variables3 = new HashMap<>();
        String string3 = "http://www.mydomain.com/abc/123/456#tuv";
        int parsed3 = template.parse(string3, variables3);
        assertTrue(parsed3 >= 0);
        assertEquals("123/456", variables3.get("v1"));
    }

    @Test
    void testVariableNames() {
        Template tpl = new Template("http://{userId}.restlet.com/invoices/{invoiceId}");
        tpl.setLogger(Engine.getAnonymousLogger());
        List<String> names = tpl.getVariableNames();

        assertEquals(2, names.size());
        assertEquals("userId", names.getFirst());
        assertEquals("invoiceId", names.get(1));
    }

    @Test
    void testWithPercentChars() {
        Template template = new Template("abc/{v1}");
        template.getDefaultVariable().setType(Variable.TYPE_URI_ALL);
        Map<String, Object> variables1 = new HashMap<>();
        String string1 = "abc/hff11kh";
        int parsed1 = template.parse(string1, variables1);
        assertTrue(parsed1 >= 0);
        assertEquals("hff11kh", variables1.get("v1"));

        Map<String, Object> variables2 = new HashMap<>();
        String string2 = "abc/hf%20kh";
        int parsed2 = template.parse(string2, variables2);
        assertTrue(parsed2 >= 0);
        assertEquals("hf%20kh", variables2.get("v1"));
    }
}
