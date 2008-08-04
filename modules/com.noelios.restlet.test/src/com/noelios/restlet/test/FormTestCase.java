/*
 * Copyright 2005-2007 Noelios Technologies.
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

import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;

import com.noelios.restlet.util.FormReader;

/**
 * Unit tests for the Form class.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FormTestCase extends TestCase {
    /**
     * Tests the cookies parsing.
     */
    public void testParsing() throws IOException {
        Form form = new Form();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");

        String query = form.encode(CharacterSet.UTF_8);
        Form newForm = new FormReader(Logger.getLogger(FormTestCase.class
                .getCanonicalName()), query, CharacterSet.UTF_8).read();
        String newQuery = newForm.encode(CharacterSet.UTF_8);
        assertEquals(query, newQuery);
    }
}
