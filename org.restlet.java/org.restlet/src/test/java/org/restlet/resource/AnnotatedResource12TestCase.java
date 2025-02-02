/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import org.junit.jupiter.api.Test;
import org.restlet.data.Form;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
public class AnnotatedResource12TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    private MyResource12 myResource;

    @Override
    protected void configureClientResource(ClientResource clientResource) {
        super.configureClientResource(clientResource);
        this.myResource = clientResource.wrap(MyResource12.class);
    }

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource12.class);
    }

    @Test
    public void testPutGet() {
        Form myForm = myResource.represent();
        assertNull(myForm);

        myForm = new Form();
        myForm.add("param1", "value1");
        myForm.add("param2", "value2");
        myResource.store(myForm);

        myForm = myResource.represent();
        assertNotNull(myForm);
        assertEquals("value1", myForm.getFirstValue("param1"));
        assertEquals("value2", myForm.getFirstValue("param2"));
    }

}
