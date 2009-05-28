/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;

/**
 * Unit test case for the RIAP Internal routing protocol.
 * 
 * @author Marc Portier (mpo@outerthought.org)
 */
public class RiapConnectorsTestCase extends TestCase {

    /**
     * Test the RIAP client and server connectors.
     */
    public void testRiapConnectors() {
        Component component = new Component();
        component.getServers().add(Protocol.RIAP);
        component.getClients().add(Protocol.RIAP);

        Application app = new Application() {
            @Override
            public Restlet createRoot() {
                Router router = new Router(getContext());
                router.attach("/test", new Restlet(getContext()) {

                    @Override
                    public void handle(Request request, Response response) {
                        response
                                .setEntity("hello, world", MediaType.TEXT_PLAIN);
                    }

                });
                return router;
            }
        };

        // Attach the private application
        component.getInternalRouter().attach(app);

        try {
            component.start();
            ClientResource res = new ClientResource("riap://component/test");
            Representation rep = res.get();
            assertEquals("hello, world", rep.getText());
            component.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
