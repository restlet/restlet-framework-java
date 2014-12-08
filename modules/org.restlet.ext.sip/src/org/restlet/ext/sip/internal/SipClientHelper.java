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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.ext.nio.ClientConnectionHelper;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.ext.sip.SipStatus;

/**
 * Standalone SIP client helper.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SipClientHelper extends ClientConnectionHelper {

    /** The map of managed transactions represented by their initial request. */
    private final Map<String, SipRequest> requests;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public SipClientHelper(Client client) {
        super(client);
        this.requests = new ConcurrentHashMap<String, SipRequest>();
        getProtocols().add(Protocol.SIP);
        getProtocols().add(Protocol.SIPS);
    }

    @Override
    public boolean control() {
        boolean result = super.control();
        SipRequest request;

        // Control the transactions for timeouts
        for (Iterator<SipRequest> iter = getRequests().values().iterator(); iter
                .hasNext();) {
            request = iter.next();

            if (request.hasTimedOut()) {
                Level level;

                if (request.isHandled() || !request.isExpectingResponse()) {
                    level = Level.FINE;
                } else {
                    level = Level.INFO;

                    // Send an error response to the client
                    SipResponse response = new SipResponse(request);
                    response.setStatus(
                            SipStatus.CLIENT_ERROR_REQUEST_TIMEOUT,
                            "The SIP client connector has timeout due to lack of activity on this transaction: "
                                    + request.getTransaction());
                    handleInbound(response, false);
                }

                // Remove the transaction from the map
                getLogger().log(
                        level,
                        "This SIP transaction has timed out: "
                                + request.getTransaction());
                iter.remove();
            }
        }

        return result;
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
    public Request getRequest(Response response) {
        Request result = null;

        if (response != null) {
            result = response.getRequest();

            if (result == null) {
                SipResponse sipResponse = (SipResponse) response;

                // Lookup the parent request that initiated the SIP transaction
                String tid = sipResponse.getTransactionId();
                result = getRequests().get(tid);
            }
        }

        return result;
    }

    /**
     * Returns the map of managed transactions represented by their initial
     * request.
     * 
     * @return The map of managed transactions.
     */
    public Map<String, SipRequest> getRequests() {
        return requests;
    }

    @Override
    protected void unblock(Response response) {
        if (response.getRequest() != null) {
            SipRequest request = (SipRequest) response.getRequest();
            request.setHandled(true);
            CountDownLatch latch = (CountDownLatch) response.getRequest()
                    .getAttributes().get(CONNECTOR_LATCH);

            if (latch == null) {
                getLogger().warning("Final response ignored: " + response);
            }
        }

        super.unblock(response);
    }

}
