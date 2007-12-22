/*
 * Copyright 2005-2007 Noelios Consulting.
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

import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.restlet.util.Template;

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

}
