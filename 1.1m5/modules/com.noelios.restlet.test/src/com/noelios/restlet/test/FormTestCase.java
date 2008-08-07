/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
 * @author Jerome Louvel
 */
public class FormTestCase extends TestCase {
    /**
     * Tests the cookies parsing.
     */
    public void testParsing() throws IOException {
        final Form form = new Form();
        form.add("name", "John D. Mitchell");
        form.add("email", "john@bob.net");
        form.add("email2", "joe@bob.net");

        final String query = form.encode(CharacterSet.UTF_8);
        final Form newForm = new FormReader(Logger.getLogger(FormTestCase.class
                .getCanonicalName()), query, CharacterSet.UTF_8, '&').read();
        final String newQuery = newForm.encode(CharacterSet.UTF_8);
        assertEquals(query, newQuery);
    }
}
