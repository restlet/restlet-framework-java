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

package org.restlet.engine.connector;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.Representation;

/**
 * Server-side inbound way.
 * 
 * @author Jerome Louvel
 */
public class ServerInboundWay extends InboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public ServerInboundWay(Connection<?> connection) {
        super(connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Connection<Server> getConnection() {
        return (Connection<Server>) super.getConnection();
    }

    @Override
    public ConnectionServerHelper getHelper() {
        return (ConnectionServerHelper) super.getHelper();
    }

    @Override
    protected void onReceived() {
        InboundRequest request = (InboundRequest) getMessage().getRequest();

        if (getHeaders() != null) {
            request.setHeaders(getHeaders());
        }

        // Check if the client wants to close the
        // connection after the response is sent
        if (HeaderUtils.isConnectionClose(getHeaders())) {
            getConnection().setState(ConnectionState.CLOSING);
        }

        // Check if an entity is available
        Representation entity = createEntity(getHeaders());
        getMessage().getRequest().setEntity(entity);

        // Update the response
        getMessage().getServerInfo().setAddress(
                getConnection().getHelper().getHelped().getAddress());
        getMessage().getServerInfo().setPort(
                getConnection().getHelper().getHelped().getPort());

        if (request != null) {
            if (getMessage().getRequest().isExpectingResponse()) {
                // Add it to the inbound queue
                getMessages().add(getMessage());
            }

            // Add it to the helper queue
            getHelper().getInboundMessages().add(getMessage());

            // Wake up the controller if it is sleeping
            getHelper().getController().wakeup();
        }

        if (!getMessage().getRequest().isEntityAvailable()) {
            // The request has been completely read
            onCompleted();
        }
    }

    @Override
    protected void readStartLine() throws IOException {
        String requestMethod = null;
        String requestUri = null;
        String version = null;

        int i = 0;
        int start = 0;
        int size = getLineBuilder().length();
        char next;

        if (size == 0) {
            // Skip leading empty lines per HTTP specification
        } else {
            // Parse the request method
            for (i = start; (requestMethod == null) && (i < size); i++) {
                next = getLineBuilder().charAt(i);

                if (HeaderUtils.isSpace(next)) {
                    requestMethod = getLineBuilder().substring(start, i);
                    start = i + 1;
                }
            }

            if ((requestMethod == null) || (i == size)) {
                throw new IOException(
                        "Unable to parse the request method. End of line reached too early.");
            }

            // Parse the request URI
            for (i = start; (requestUri == null) && (i < size); i++) {
                next = getLineBuilder().charAt(i);

                if (HeaderUtils.isSpace(next)) {
                    requestUri = getLineBuilder().substring(start, i);
                    start = i + 1;
                }
            }

            if (i == size) {
                throw new IOException(
                        "Unable to parse the request URI. End of line reached too early.");
            }

            if ((requestUri == null) || (requestUri.equals(""))) {
                requestUri = "/";
            }

            // Parse the protocol version
            for (i = start; (version == null) && (i < size); i++) {
                next = getLineBuilder().charAt(i);
            }

            if (i == size) {
                version = getLineBuilder().substring(start, i);
                start = i + 1;
            }

            if (version == null) {
                throw new IOException(
                        "Unable to parse the protocol version. End of line reached too early.");
            }

            // Create a new request object
            Request request = getHelper().createRequest(getConnection(),
                    requestMethod, requestUri, version);
            Response response = getHelper().createResponse(request);
            setMessage(response);
            setMessageState(MessageState.HEADERS);
            getLineBuilder().delete(0, getLineBuilder().length());
        }
    }

    @Override
    public void updateState() {
        super.updateState();

        if (getIoState() == IoState.IDLE) {
            if (getConnection().isPipelining()) {
                // Read the next request
                setIoState(IoState.INTEREST);
            } else if (getMessages().isEmpty()
                    && (getConnection().getOutboundWay().getMessages()
                            .isEmpty())) {
                // Read the next request
                setIoState(IoState.INTEREST);
            }
        }
    }
}
