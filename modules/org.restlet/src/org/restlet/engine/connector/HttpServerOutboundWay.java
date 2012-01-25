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

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Status;
import org.restlet.engine.io.IoState;

/**
 * HTTP server outbound way.
 * 
 * @author Jerome Louvel
 */
public class HttpServerOutboundWay extends ServerOutboundWay {

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
    public HttpServerOutboundWay(Connection<Server> connection, int bufferSize) {
        super(connection, bufferSize);
        this.messages = new ConcurrentLinkedQueue<Response>();
    }

    @Override
    public void clear() {
        super.clear();
        this.messages.clear();
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
    protected void handle(Response response) {
        getMessages().add(response);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && getMessages().isEmpty();
    }

    @Override
    public void onCompleted(boolean endDetected) {
        getMessages().remove(getMessage());

        if (!getMessage().getStatus().isInformational()) {
            Queue<Response> inboundMessages = ((HttpServerInboundWay) getConnection()
                    .getInboundWay()).getMessages();

            // Attempt to read additional inbound messages
            Response inboundMessage = inboundMessages.peek();

            if (inboundMessage.getRequest() == getMessage().getRequest()) {
                // As we are supporting provisional responses and
                // asynchronous responses, it is possible that the final
                // response object is not the original one blocked in the
                // inbound queue
                inboundMessages.remove(inboundMessage);
            }
        }

        super.onCompleted(endDetected);
    }

    @Override
    public void onError(Status status) {
        for (Response rsp : getMessages()) {
            if (rsp != getMessage()) {
                getMessages().remove(rsp);
                getHelper().onOutboundError(status, rsp);
            }
        }

        super.onError(status);
    }

    @Override
    public void onTimeOut() {
        for (Response rsp : getMessages()) {
            if (rsp != getMessage()) {
                getMessages().remove(rsp);
                getHelper().onOutboundError(
                        Status.CONNECTOR_ERROR_COMMUNICATION, rsp);
            }
        }

        super.onTimeOut();
    }

    @Override
    public void updateState() {
        // Update the IO state if necessary
        if ((getIoState() == IoState.IDLE) && !isEmpty()) {
            if (getMessage() == null) {
                setIoState(IoState.INTEREST);
                setMessage(getMessages().peek());
            }
        }

        super.updateState();
    }

}
