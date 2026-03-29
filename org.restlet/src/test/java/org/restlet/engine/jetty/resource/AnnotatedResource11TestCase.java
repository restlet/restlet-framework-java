/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.jetty.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

/**
 * Test annotated resource that reimplements of one the annotated method from its abstract super
 * class that implements several annotated interfaces.
 *
 * @author Thierry Boileau
 */
class AnnotatedResource11TestCase extends JettyConnectorTestCase {

    protected Application createApplication(final String path) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router(getContext());
                router.attach(path, MyResource11.class);
                return router;
            }
        };
    }

    /**
     * Test annotated methods.
     *
     * @throws IOException
     * @throws ResourceException
     */
    @Test
    void test() throws IOException, ResourceException {
        Request request = createRequest(Method.GET);
        Response response = handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("asText-txt", response.getEntity().getText());
        response.getEntity().release();

        request = createRequest(Method.POST);
        response = handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("accept", response.getEntity().getText());
        response.getEntity().release();
    }
}
