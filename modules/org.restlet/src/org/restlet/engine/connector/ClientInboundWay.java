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
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.io.IoState;
import org.restlet.util.Series;

/**
 * Client-side inbound way.
 * 
 * @author Jerome Louvel
 */
public class ClientInboundWay extends InboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public ClientInboundWay(Connection<?> connection) {
        super(connection);
    }

    /**
     * Copies headers into a response.
     * 
     * @param headers
     *            The headers to copy.
     * @param response
     *            The response to update.
     */
    protected void copyResponseTransportHeaders(Series<Parameter> headers,
            Response response) {
        HeaderUtils.copyResponseTransportHeaders(headers, response);
    }

    /**
     * Returns the status corresponding to a given status code.
     * 
     * @param code
     *            The status code.
     * @return The status corresponding to a given status code.
     */
    protected Status createStatus(int code) {
        return Status.valueOf(code);
    }

    @Override
    protected Message getActualMessage() {
        return getMessage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Connection<Client> getConnection() {
        return (Connection<Client>) super.getConnection();
    }

    @Override
    public ConnectionClientHelper getHelper() {
        return (ConnectionClientHelper) super.getHelper();
    }

    @Override
    protected int getSocketInterestOps() {
        int result = 0;

        if (getConnection().getState() == ConnectionState.OPENING) {
            result = SelectionKey.OP_CONNECT;
        } else {
            result = super.getSocketInterestOps();
        }

        return result;
    }

    @Override
    protected void onReceived() {
        // Update the response
        getMessage().setEntity(createEntity(getHeaders()));

        try {
            copyResponseTransportHeaders(getHeaders(), getMessage());
        } catch (Throwable t) {
            getLogger()
                    .log(Level.WARNING, "Error while parsing the headers", t);
        }

        // Put the headers in the response's attributes map
        if (getHeaders() != null) {
            getMessage().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    getHeaders());
        }

        if (!getMessage().getStatus().isInformational()) {
            getMessages().poll();
        }

        // Add it to the helper queue
        getHelper().getInboundMessages().add(getMessage());

        if (getMessage().isEntityAvailable()) {
            // Let's wait for the entity to be consumed by the caller
            setIoState(IoState.IDLE);
        } else {
            // The response has been completely read
            onCompleted();
        }
    }

    @Override
    protected void readStartLine() throws IOException {
        String version = null;
        int statusCode = -1;
        String reasonPhrase = null;

        int i = 0;
        int start = 0;
        int size = getLineBuilder().length();
        char next;

        if (size == 0) {
            // Skip leading empty lines per HTTP specification
        } else {
            // Parse the protocol version
            for (i = start; (version == null) && (i < size); i++) {
                next = getLineBuilder().charAt(i);

                if (HeaderUtils.isSpace(next)) {
                    version = getLineBuilder().substring(start, i);
                    start = i + 1;
                }
            }

            // Parse the status code
            for (i = start; (statusCode == -1) && (i < size); i++) {
                next = getLineBuilder().charAt(i);

                if (HeaderUtils.isSpace(next)) {
                    try {
                        statusCode = Integer.parseInt(getLineBuilder()
                                .substring(start, i));
                    } catch (NumberFormatException e) {
                        throw new IOException(
                                "Unable to parse the status code. Non numeric value: "
                                        + getLineBuilder().substring(start, i)
                                                .toString());
                    }

                    start = i + 1;
                }
            }

            if (statusCode == -1) {
                throw new IOException(
                        "Unable to parse the status code. End of line reached too early.");
            }

            // Parse the reason phrase
            for (i = start; (reasonPhrase == null) && (i < size); i++) {
                next = getLineBuilder().charAt(i);
            }

            if (i == size) {
                reasonPhrase = getLineBuilder().substring(start, i);
                start = i + 1;
            }

            if (reasonPhrase == null) {
                throw new IOException(
                        "Unable to parse the reason phrase. End of line reached too early.");
            }

            // Prepare the response
            Response finalResponse = getMessages().peek();
            Response response = null;
            Status status = createStatus(statusCode);

            if (status.isInformational()) {
                response = getHelper().createResponse(
                        finalResponse.getRequest());
            } else {
                response = finalResponse;
            }

            // Update the response
            response.setStatus(status, reasonPhrase);
            response.getServerInfo().setAddress(
                    getConnection().getSocket().getLocalAddress().toString());
            response.getServerInfo().setAgent(Engine.VERSION_HEADER);
            response.getServerInfo().setPort(
                    getConnection().getSocket().getPort());

            // Set the current message object
            setMessage(response);
            setMessageState(MessageState.HEADERS);
            clearLineBuilder();
        }
    }

    @Override
    public void updateState() {
        if (getIoState() == IoState.IDLE) {
            if (!getMessages().isEmpty()) {
                // Read the next response
                setIoState(IoState.INTEREST);
            }
        }

        // Update the registration
        super.updateState();
    }

}
