package org.restlet.test;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.restlet.util.Template;

public class TemplateTestCase extends TestCase {

    private static String TEMPLATE1 = "http://{userId}.noelios.com/invoices/{invoiceId}";

    public void testVariableNames() throws Exception {
        Template tpl = new Template(Logger.getAnonymousLogger(), TEMPLATE1);
        List<String> names = tpl.getVariableNames();

        assertEquals(2, names.size());
        assertEquals("userId", names.get(0));
        assertEquals("invoiceId", names.get(1));
    }

}
