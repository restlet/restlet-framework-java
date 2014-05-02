/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.nio;

import org.restlet.Request;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.request.HttpsInboundRequest;
import org.restlet.ext.nio.internal.way.HttpsServerInboundWay;
import org.restlet.ext.nio.internal.way.HttpsServerOutboundWay;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;

/**
 * HTTPS server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends HttpServerHelper {

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server, Protocol.HTTPS);
    }

    @Override
    public InboundWay createInboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpsServerInboundWay(connection, bufferSize);
    }
    
    @Override
    public OutboundWay createOutboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpsServerOutboundWay(connection, bufferSize);
    }

    @Override
    public Request createRequest(Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {
        return new HttpsInboundRequest(getContext(), connection, methodName,
                resourceUri, protocol);
    }

    @Override
    public boolean isConfidential() {
        return true;
    }

}
