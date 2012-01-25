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

package org.restlet.test.connector;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the RIAP Internal routing protocol.
 */
public class RiapConnectorsTestCase extends RestletTestCase {

    /**
     * Test the RIAP client and server connectors.
     */
    public void testRiapConnectors() {
        Component component = new Component();
        component.getServers().add(Protocol.RIAP);
        component.getClients().add(Protocol.RIAP);

        Application app = new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router(getContext());
                router.attach("/testA", new Restlet(getContext()) {

                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity("hello, world", MediaType.TEXT_PLAIN);
                    }

                });
                router.attach("/testB", new Restlet(getContext()) {
                    public void handle(Request request, Response response) {
                        ClientResource resource = new ClientResource(
                                "riap://component/app/testA");
                        try {
                            response.setEntity(resource.get().getText(),
                                    MediaType.TEXT_PLAIN);
                        } catch (Exception e) {
                        }
                    }

                });
                return router;
            }
        };

        // Attach the private application
        component.getInternalRouter().attach("/app", app);

        try {
            component.start();

            ClientResource res = new ClientResource(
                    "riap://component/app/testA");
            Representation rep = res.get();
            assertEquals("hello, world", rep.getText());

            rep = null;
            res = new ClientResource("riap://component/app/testB");
            rep = res.get();
            assertEquals("hello, world", rep.getText());

            component.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
