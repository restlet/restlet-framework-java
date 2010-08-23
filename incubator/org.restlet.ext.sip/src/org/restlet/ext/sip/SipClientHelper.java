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

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.nio.BaseClientHelper;
import org.restlet.engine.nio.Connection;
import org.restlet.engine.nio.InboundWay;
import org.restlet.engine.nio.OutboundWay;
import org.restlet.ext.sip.internal.SipClientInboundWay;
import org.restlet.ext.sip.internal.SipClientOutboundWay;

/**
 * Standalone SIP client helper.
 * 
 * @author Jerome Louvel
 */
public class SipClientHelper extends BaseClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public SipClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.SIP);
        getProtocols().add(Protocol.SIPS);
    }

    @Override
    public InboundWay createInboundWay(Connection<Client> connection) {
        return new SipClientInboundWay(connection);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Client> connection) {
        return new SipClientOutboundWay(connection);
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

}
