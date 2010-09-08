/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import java.util.ArrayList;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;
import org.restlet.engine.nio.BaseServerHelper;
import org.restlet.engine.nio.Connection;
import org.restlet.engine.nio.OutboundWay;
import org.restlet.engine.nio.ServerInboundWay;
import org.restlet.ext.sip.internal.SipInboundRequest;
import org.restlet.ext.sip.internal.SipServerInboundWay;
import org.restlet.ext.sip.internal.SipServerOutboundWay;

/**
 * Standalone SIP server helper.
 * 
 * @author Jerome Louvel
 */
public class SipServerHelper extends BaseServerHelper {

    @Override
    protected Request createRequest(Connection<Server> connection,
            String methodName, String resourceUri, String version) {

        SipInboundRequest request = new SipInboundRequest(getContext(),
                connection, methodName, resourceUri, version);

        // The via header is linked with the sipRecipientsInfo attribute, due to
        // distinct formats.
        request.setRecipientsInfo(new ArrayList<RecipientInfo>());
        return request;
    }

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
    public ServerInboundWay createInboundWay(Connection<Server> connection) {
        return new SipServerInboundWay(connection);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Server> connection) {
        return new SipServerOutboundWay(connection);
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

}
