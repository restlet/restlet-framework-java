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

package org.restlet.test.engine.connector;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.junit.Assert;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
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
 * Test that the client address is available for all the connectors
 * 
 * @author Kevin Conaway
 */
public class RemoteClientAddressTestCase extends BaseConnectorsTestCase {

    public static class RemoteClientAddressResource extends ServerResource {

        public RemoteClientAddressResource() {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation get(Variant variant) {
            boolean localAddress = false;
            try {
                Enumeration<NetworkInterface> n = NetworkInterface
                        .getNetworkInterfaces();
                for (; n.hasMoreElements();) {
                    NetworkInterface e = n.nextElement();
                    Enumeration<InetAddress> a = e.getInetAddresses();
                    for (; a.hasMoreElements();) {
                        InetAddress addr = a.nextElement();
                        if (addr.getHostAddress().equals(
                                getRequest().getClientInfo().getAddress())) {
                            localAddress = true;
                        }
                    }
                }
            } catch (SocketException e1) {
                // nothing
            }
            Assert.assertTrue(localAddress);
            Assert.assertTrue(getRequest().getClientInfo().getPort() > 0);

            return new StringRepresentation("OK");
        }
    }

    @Override
    protected void call(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        Client c = new Client(Protocol.HTTP);
        final Response r = c.handle(request);
        assertEquals(Status.SUCCESS_OK, r.getStatus());
        c.stop();
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", RemoteClientAddressResource.class);
                return router;
            }
        };

        return application;
    }
}
