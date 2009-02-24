/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

import org.junit.Assert;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Test that the client address is available for all the connectors
 * 
 * @author Kevin Conaway
 */
public class RemoteClientAddressTestCase extends BaseConnectorsTestCase {

    public static class RemoteClientAddressResource extends Resource {

        public RemoteClientAddressResource(Context ctx, Request request,
                Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation represent(Variant variant) {
            Assert.assertEquals("127.0.0.1", getRequest().getClientInfo()
                    .getAddress());
            Assert.assertTrue(getRequest().getClientInfo().getPort() > 0);

            return new StringRepresentation("OK");
        }
    }

    @Override
    protected void call(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Response r = new Client(Protocol.HTTP).handle(request);

        assertEquals(Status.SUCCESS_OK, r.getStatus());
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", RemoteClientAddressResource.class);
                return router;
            }
        };

        return application;
    }
}
