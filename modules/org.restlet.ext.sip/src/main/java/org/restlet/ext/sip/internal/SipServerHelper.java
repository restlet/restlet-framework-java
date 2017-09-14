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

package org.restlet.ext.sip.internal;

import java.util.ArrayList;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;
import org.restlet.ext.nio.ServerConnectionHelper;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.way.OutboundWay;
import org.restlet.ext.nio.internal.way.ServerInboundWay;

/**
 * Standalone SIP server helper.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SipServerHelper extends ServerConnectionHelper {

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public SipServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.SIP);
    }

    @Override
    protected boolean canHandle(Connection<Server> connection, Response response) {
        return (connection.getOutboundWay().getMessage() == null);
    }

    @Override
    public ServerInboundWay createInboundWay(Connection<Server> connection,
            int bufferSize) {
        return new SipServerInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Server> connection,
            int bufferSize) {
        return new SipServerOutboundWay(connection, bufferSize);
    }

    @Override
    public Request createRequest(Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {

        SipInboundRequest request = new SipInboundRequest(getContext(),
                connection, methodName, resourceUri, protocol);

        // The via header is linked with the sipRecipientsInfo attribute, due to
        // distinct formats.
        request.setRecipientsInfo(new ArrayList<RecipientInfo>());
        return request;
    }

    @Override
    public boolean isPipeliningConnections() {
        return true;
    }

}
