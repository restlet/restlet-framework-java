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

package org.restlet.ext.nio.internal.way;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.ext.nio.ClientConnectionHelper;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.state.ConnectionState;
import org.restlet.ext.nio.internal.state.IoState;
import org.restlet.ext.nio.internal.state.MessageState;
import org.restlet.util.Series;

/**
 * Client-side inbound way.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientInboundWay extends InboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public ClientInboundWay(Connection<?> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    /**
     * Copies headers into a response.
     * 
     * @param headers
     *            The headers to copy.
     * @param response
     *            The response to update.
     */
    protected void copyResponseTransportHeaders(Series<Header> headers,
            Response response) {
        HeaderUtils.copyResponseTransportHeaders(headers, response);
    }

    /**
     * Creates a response object for the given status.
     * 
     * @param status
     *            The response status.
     * @return The new response object.
     */
    protected abstract Response createResponse(Status status);

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
    public Message getActualMessage() {
        return getMessage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Connection<Client> getConnection() {
        return (Connection<Client>) super.getConnection();
    }

    @Override
    public ClientConnectionHelper getHelper() {
        return (ClientConnectionHelper) super.getHelper();
    }

    @Override
    public int getInterestOperations() {
        int result = 0;

        if (getConnection().getState() == ConnectionState.OPENING) {
            result = SelectionKey.OP_CONNECT;
        } else {
            result = super.getInterestOperations();
        }

        return result;
    }

    @Override
    protected void onHeadersCompleted() throws IOException {
        super.onHeadersCompleted();

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

        onReceived(getMessage());
    }

    @Override
    public void onMessageCompleted(boolean endDetected) throws IOException {
        // Check if we need to close the connection
        if (endDetected || !getConnection().isPersistent()
                || HeaderUtils.isConnectionClose(getHeaders())) {
            getConnection().close(true);
        }

        super.onMessageCompleted(endDetected);
    }

    @Override
    protected void onReceived(Response message) throws IOException {
        // Add it to the helper queue
        getHelper().getInboundMessages().add(getMessage());

        if (getMessage().isEntityAvailable()) {
            // Let's wait for the entity to be consumed by the caller
            setIoState(IoState.IDLE);
        } else {
            // The response has been completely read
            onMessageCompleted(false);
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
            Status status = createStatus(statusCode);
            Response response = createResponse(status);

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
}
