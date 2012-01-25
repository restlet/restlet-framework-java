/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.connector;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Status;
import org.restlet.engine.io.IoState;

/**
 * HTTP server inbound way.
 * 
 * @author Jerome Louvel
 */
public class HttpServerInboundWay extends ServerInboundWay {

    /** The queue of messages. */
    private final Queue<Response> messages;

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public HttpServerInboundWay(Connection<Server> connection, int bufferSize) {
        super(connection, bufferSize);
        this.messages = new ConcurrentLinkedQueue<Response>();
    }

    @Override
    public void clear() {
        super.clear();
        this.messages.clear();
    }

    @Override
    protected Response createResponse(Request request) {
        return new Response(request);
    }

    @Override
    public int getLoadScore() {
        return getMessages().size();
    }

    /**
     * Returns the queue of messages.
     * 
     * @return The queue of messages.
     */
    public Queue<Response> getMessages() {
        return messages;
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && getMessages().isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && getMessages().isEmpty();
    }

    @Override
    public void onError(Status status) {
        for (Response rsp : getMessages()) {
            if (rsp != getMessage()) {
                getMessages().remove(rsp);
                getHelper().onInboundError(status, rsp);
            }
        }

        super.onError(status);
    }

    @Override
    protected void onReceived(Response message) {
        if ((message.getRequest() != null)
                && message.getRequest().isExpectingResponse()) {
            // Add it to the inbound queue
            getMessages().add(message);
        }

        super.onReceived(message);
    }

    @Override
    public void onTimeOut() {
        for (Response rsp : getMessages()) {
            if (rsp != getMessage()) {
                getMessages().remove(rsp);
                getHelper().onInboundError(
                        Status.CONNECTOR_ERROR_COMMUNICATION, rsp);
            }
        }

        super.onTimeOut();
    }

    @Override
    public void updateState() {
        Queue<Response> outboundMessages = ((HttpServerOutboundWay) getConnection()
                .getOutboundWay()).getMessages();

        if ((getIoState() == IoState.IDLE) && getMessages().isEmpty()
                && outboundMessages.isEmpty()) {
            // Read the next request
            setIoState(IoState.INTEREST);
        }

        super.updateState();
    }

}
