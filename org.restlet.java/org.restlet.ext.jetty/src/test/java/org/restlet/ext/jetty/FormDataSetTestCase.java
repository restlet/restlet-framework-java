/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Unit tests for the Form class.
 *
 * @author Jerome Louvel
 */
public class FormDataSetTestCase {

    /**
     * Tests the cookies parsing.
     */
    @Test
    public void testParsing() throws IOException {
/*
TODO restore test of Form class
        FormDataSet form = new FormDataSet();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");
        String query = form.encode();

        Series<FormData> newFormData = new FormReader(query,
                CharacterSet.UTF_8, '&').read();

        FormDataSet newForm = new FormDataSet();
        newForm.getEntries().addAll(newFormData);
        String newQuery = newForm.encode();

        assertEquals(query, newQuery);
   */
    }

}
