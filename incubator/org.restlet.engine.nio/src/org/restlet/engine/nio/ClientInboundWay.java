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

package org.restlet.engine.nio;

import java.io.IOException;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.HeaderUtils;

/**
 * Client-side inbound way.
 * 
 * @author Jerome Louvel
 */
public class ClientInboundWay extends InboundWay {

    public ClientInboundWay(Connection<?> connection) {
        super(connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Connection<Server> getConnection() {
        return (Connection<Server>) super.getConnection();
    }

    @Override
    public BaseServerHelper getHelper() {
        return (BaseServerHelper) super.getHelper();
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

            if (version == null) {
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
            getLineBuilder().delete(0, getLineBuilder().length());
        }
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
    public void updateState() {
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
