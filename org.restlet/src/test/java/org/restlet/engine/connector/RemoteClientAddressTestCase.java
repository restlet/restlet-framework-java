/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.restlet.Application;
import org.restlet.Client;
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
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, uri);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("OK", response.getEntityAsText());
        } finally {
            client.stop();
        }
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attachDefault(RemoteClientAddressResource.class);
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
                Enumeration<NetworkInterface> networkInterfaces =
                        NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        final InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress
                                .getHostAddress()
                                .equals(getRequest().getClientInfo().getAddress())) {
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
