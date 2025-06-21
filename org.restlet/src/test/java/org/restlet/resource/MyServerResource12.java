/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
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
