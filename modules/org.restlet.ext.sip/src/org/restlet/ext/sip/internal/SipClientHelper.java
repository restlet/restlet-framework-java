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

package org.restlet.ext.sip.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientConnectionHelper;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.InboundWay;
import org.restlet.engine.connector.OutboundWay;
import org.restlet.ext.sip.SipResponse;
import org.restlet.ext.sip.Transaction;

/**
 * Standalone SIP client helper.
 * 
 * @author Jerome Louvel
 */
public class SipClientHelper extends ClientConnectionHelper {

    /** The map of managed transactions. */
    private final Map<String, Transaction> transactions;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public SipClientHelper(Client client) {
        super(client);
        this.transactions = new ConcurrentHashMap<String, Transaction>();
        getProtocols().add(Protocol.SIP);
        getProtocols().add(Protocol.SIPS);
    }

    @Override
    public InboundWay createInboundWay(Connection<Client> connection,
            int bufferSize) {
        return new SipClientInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Client> connection,
            int bufferSize) {
        return new SipClientOutboundWay(connection, bufferSize);
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

    /**
     * Returns the map of managed transactions.
     * 
     * @return The map of managed transactions.
     */
    protected Map<String, Transaction> getTransactions() {
        return transactions;
    }

}
