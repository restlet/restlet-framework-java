/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import org.restlet.data.Form;

/**
 * Sample server resource.
 *
 * @author Jerome Louvel
 */
public class MyServerResource12 extends ServerResource implements MyResource12 {

    private static Form myForm = null;

    public void store(Form form) {
        myForm = form;
    }

    public Form represent() {
        return myForm;
    }
}
