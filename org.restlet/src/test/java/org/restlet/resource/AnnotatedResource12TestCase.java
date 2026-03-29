/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.data.Form;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource12TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

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
    void testPutGet() {
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
