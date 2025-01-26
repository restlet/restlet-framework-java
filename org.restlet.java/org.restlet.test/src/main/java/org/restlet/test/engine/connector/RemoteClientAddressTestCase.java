/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    @Override
    protected void doTestUri(String uri) throws Exception {
        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, uri);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus());
        } finally {
            response.release();
            client.stop();
        }
    }

    @Override
    protected Application createApplication(Component component) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", RemoteClientAddressResource.class);
                return router;
            }
        };
    }

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
            assertTrue(localAddress);
            assertTrue(getRequest().getClientInfo().getPort() > 0);

            return new StringRepresentation("OK");
        }
    }
}
