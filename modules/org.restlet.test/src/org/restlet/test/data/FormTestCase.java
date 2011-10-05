/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.test.data;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.engine.util.FormReader;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the {@link Form} class.
 * 
 * @author Jerome Louvel
 */
public class FormTestCase extends RestletTestCase {

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

    public void testEmptyParameter() {
        Form form = new Form();
        form.add("normalParam", "abcd");
        form.add("emptyParam", "");
        form.add("nullParam", null);

        assertEquals("abcd", form.getFirstValue("normalParam"));
        assertEquals("", form.getFirstValue("emptyParam"));
        assertNull(form.getFirstValue("nullParam"));
    }

}
