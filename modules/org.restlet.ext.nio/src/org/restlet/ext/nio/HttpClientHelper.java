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

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.way.HttpClientInboundWay;
import org.restlet.ext.nio.internal.way.HttpClientOutboundWay;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;

/**
 * HTTP client helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpClientHelper extends ClientConnectionHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public InboundWay createInboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpClientInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpClientOutboundWay(connection, bufferSize);
    }

}
