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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.data.MediaType;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource20TestCase extends AbstractAnnotatedResourceTestCase {

    private MyResource20 myResource;

    @Override
    void configureClientResource(ClientResource clientResource) {
        // Hosts resources into an Application because we need some services for
        // handling content negotiation, conversion of exceptions, etc.
        Application application = new Application();
        application.setInboundRoot(MyServerResource20.class);

        this.clientResource.accept(MediaType.APPLICATION_JSON);
        this.clientResource.setNext(application);
        this.myResource = clientResource.wrap(MyResource20.class);
    }

    @Test
    void testGet() {
        assertThrows(MyException01.class, () -> myResource.represent());
        assertEquals(400, clientResource.getStatus().getCode());
    }

    @Test
    void testGetAndSerializeException() {
        MyException02 e =
                assertThrows(
                        MyException02.class, () -> myResource.representAndSerializeException());
        assertEquals("my custom error", e.getCustomProperty());
        assertEquals(400, clientResource.getStatus().getCode());
    }
}
