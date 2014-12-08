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

package org.restlet.ext.nio;

import java.io.IOException;
import java.util.Iterator;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.request.HttpInboundRequest;
import org.restlet.ext.nio.internal.way.HttpServerInboundWay;
import org.restlet.ext.nio.internal.way.HttpServerOutboundWay;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;

/**
 * HTTP server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends ServerConnectionHelper {

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        this(server, Protocol.HTTP);
    }

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     * @param protocol
     *            The protocol supported.
     */
    public HttpServerHelper(Server server, Protocol protocol) {
        super(server);
        getProtocols().add(protocol);
    }

    @Override
    protected boolean canHandle(Connection<Server> connection, Response response)
            throws IOException {
        boolean result = false;

        // Check if the response is indeed the next one to be written
        // for this connection
        HttpServerInboundWay inboundWay = (HttpServerInboundWay) connection
                .getInboundWay();
        Response nextResponse = inboundWay.getMessages().peek();

        if (nextResponse != null) {
            if (nextResponse.getRequest() == response.getRequest()) {
                result = true;
            } else {
                boolean found = false;

                for (Iterator<Response> iterator = inboundWay.getMessages()
                        .iterator(); iterator.hasNext() && !found;) {
                    Response next = iterator.next();
                    found = next.getRequest() == response.getRequest();
                }

                if (!found) {
                    throw new IOException(
                            "Can't find the parent request in the list of inbound messages.");
                }
            }
        } else {
            throw new IOException(
                    "Can't find the parent request in the empty list of inbound messages.");
        }

        return result;
    }

    @Override
    public InboundWay createInboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpServerInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpServerOutboundWay(connection, bufferSize);
    }

    @Override
    public Request createRequest(Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {
        return new HttpInboundRequest(getContext(), connection, methodName,
                resourceUri, protocol);
    }

}
