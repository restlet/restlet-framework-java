/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

/**
 * Test annotated resource that reimplements of one the annotated method from
 * its abstract super class that implements several annotated interfaces.
 * 
 * @author Thierry Boileau
 */
public class AnnotatedResource11TestCase extends InternalConnectorTestCase {

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
    public void test() throws IOException, ResourceException {
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
