/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.Test;
import org.restlet.engine.util.FormReader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the {@link Form} class.
 * 
 * @author Jerome Louvel
 */
public class FormTestCase {

    @Test
    public void testParsing() throws IOException {
        Form form = new Form();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");

        String query = form.encode(CharacterSet.UTF_8);
        Form newForm = new FormReader(query, CharacterSet.UTF_8, '&').read();
        String newQuery = newForm.encode(CharacterSet.UTF_8);

        assertEquals(query, newQuery);
    }

    @Test
    public void testEmptyParameter() {
        // Manual construction of form
        Form form = new Form();
        form.add("normalParam", "abcd");
        form.add("emptyParam", "");
        form.add("nullParam", null);

        assertEquals(3, form.size());
        assertEquals("abcd", form.getFirstValue("normalParam"));
        assertEquals("", form.getFirstValue("emptyParam"));
        assertNull(form.getFirstValue("nullParam"));
        assertNull(form.getFirstValue("unknownParam"));

        // Construction of form via URI query parsing
        form = new Form("normalParam=abcd&emptyParam=&nullParam");
        assertEquals(3, form.size());
        assertEquals("abcd", form.getFirstValue("normalParam"));
        assertEquals("", form.getFirstValue("emptyParam"));
        assertNull(form.getFirstValue("nullParam"));
        assertNull(form.getFirstValue("unknownParam"));
    }

}
