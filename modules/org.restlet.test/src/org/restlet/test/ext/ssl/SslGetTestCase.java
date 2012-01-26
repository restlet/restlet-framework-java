/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.ssl;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * Test that a simple get using SSL works for all the connectors.
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class SslGetTestCase extends SslBaseConnectorsTestCase {

    // [ifdef jse] method
    public static void main(String[] args) throws Exception {
        SslGetTestCase sgt = new SslGetTestCase();
        sgt.setUp();
        sgt.testSslInternalAndJdkNet();
    }

    public static class GetTestResource extends ServerResource {

        public GetTestResource() {

            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation get(Variant variant) {
            return new StringRepresentation("Hello world", MediaType.TEXT_PLAIN);
        }
    }

    @Override
    protected void call(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Client client = new Client(Protocol.HTTPS);
        client.setContext(new Context());
        configureSslClientParameters(client.getContext());
        final Response r = client.handle(request);

        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK,
                r.getStatus());
        assertEquals("Hello world", r.getEntity().getText());

        Thread.sleep(200);
        client.stop();
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", GetTestResource.class);
                return router;
            }
        };

        return application;
    }
}
