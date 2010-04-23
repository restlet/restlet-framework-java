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
import java.nio.channels.SocketChannel;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Generic HTTP-like server connection.
 * 
 * @author Jerome Louvel
 */
public class ServerConnection extends Connection<Server> {

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @throws IOException
     */
    public ServerConnection(BaseHelper<Server> helper,
            SocketChannel socketChannel) throws IOException {
        super(helper, socketChannel);

    }

    /**
     * Adds the response headers.
     * 
     * @param response
     *            The response to inspect.
     * @param headers
     *            The headers series to update.
     */
    protected void addResponseHeaders(Response response,
            Series<Parameter> headers) {
        HeaderUtils.addResponseHeaders(response, headers);
    }

    /**
     * Asks the server connector to immediately commit the given response
     * associated to this request, making it ready to be sent back to the
     * client. Note that all server connectors don't necessarily support this
     * feature.
     * 
     * @param response
     *            The response to commit.
     */
    public void commit(Response response) {
        getHelper().getOutboundMessages().add(response);
    }

    /**
     * Creates a new request.
     * 
     * @param context
     *            The current context.
     * @param connection
     *            The associated connection.
     * @param methodName
     *            The method name.
     * @param resourceUri
     *            The target resource URI.
     * @param version
     *            The protocol version.
     * @return The created request.
     */
    protected ConnectedRequest createRequest(Context context,
            ServerConnection connection, String methodName, String resourceUri,
            String version) {
        return new ConnectedRequest(getHelper().getContext(), this, methodName,
                resourceUri, version);
    }

    /**
     * Reads the next request sent by the client if available. Note that the
     * optional entity is not fully read.
     * 
     * @throws IOException
     */
    @Override
    public void readMessage() throws IOException {
        if (getMessageState() == null) {
            setMessageState(WayMessageState.START_LINE);
            getBuilder().delete(0, getBuilder().length());
        }

        while (getBuffer().hasRemaining()) {
            if (getMessageState() == WayMessageState.START_LINE) {
                readMessageStart();
            } else if (getMessageState() == WayMessageState.HEADERS) {
                readMessageHeaders();
            }
        }
    }

    /**
     * Reads a message header.
     * 
     * @return The new message header or null.
     * @throws IOException
     */
    protected Parameter readMessageHeader() throws IOException {
        Parameter header = HeaderReader.readHeader(getBuilder());
        getBuilder().delete(0, getBuilder().length());
        return header;
    }

    @Override
    public void readMessageHeaders() throws IOException {
        if (readMessageLine()) {
            ConnectedRequest request = (ConnectedRequest) getMessage()
                    .getRequest();
            Series<Parameter> headers = request.getHeaders();
            Parameter header = readMessageHeader();

            while (header != null) {
                if (headers == null) {
                    headers = new Form();
                }

                headers.add(header);

                if (readMessageLine()) {
                    header = readMessageHeader();

                    // End of headers
                    if (header == null) {
                        // Check if the client wants to close the connection
                        if (HeaderUtils.isConnectionClose(headers)) {
                            setState(ConnectionState.CLOSING);
                        }

                        // Check if an entity is available
                        Representation entity = createInboundEntity(headers);

                        if (entity instanceof EmptyRepresentation) {
                            setMessageState(WayMessageState.END);
                        } else {
                            request.setEntity(entity);
                            setMessageState(WayMessageState.BODY);
                        }

                        // Update the response
                        getMessage().getServerInfo().setAddress(
                                getHelper().getHelped().getAddress());
                        getMessage().getServerInfo().setPort(
                                getHelper().getHelped().getPort());

                        if (request != null) {
                            if (request.isExpectingResponse()) {
                                // Add it to the connection queue
                                getMessages().add(getMessage());
                            }

                            // Add it to the helper queue
                            getHelper().getInboundMessages().add(
                                    getMessage());
                        }
                    }
                } else {
                    // Missing characters
                }
            }

            request.setHeaders(headers);
        }
    }

    @Override
    public void readMessageStart() throws IOException {
        if (readMessageLine()) {
            String requestMethod = null;
            String requestUri = null;
            String version = null;

            int i = 0;
            int start = 0;
            int size = getBuilder().length();
            char next;

            if (size == 0) {
                // Skip leading empty lines per HTTP specification
            } else {
                // Parse the request method
                for (i = start; (requestMethod == null) && (i < size); i++) {
                    next = getBuilder().charAt(i);

                    if (HeaderUtils.isSpace(next)) {
                        requestMethod = getBuilder().substring(start, i);
                        start = i + 1;
                    }
                }

                if ((requestMethod == null) || (i == size)) {
                    throw new IOException(
                            "Unable to parse the request method. End of line reached too early.");
                }

                // Parse the request URI
                for (i = start; (requestUri == null) && (i < size); i++) {
                    next = getBuilder().charAt(i);

                    if (HeaderUtils.isSpace(next)) {
                        requestUri = getBuilder().substring(start, i);
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
                    next = getBuilder().charAt(i);
                }

                if (i == size) {
                    version = getBuilder().substring(start, i);
                    start = i + 1;
                }

                if (version == null) {
                    throw new IOException(
                            "Unable to parse the protocol version. End of line reached too early.");
                }

                ConnectedRequest request = createRequest(getHelper()
                        .getContext(), this, requestMethod, requestUri, version);
                Response response = getHelper().createResponse(request);
                setMessage(response);

                setMessageState(WayMessageState.HEADERS);
                getBuilder().delete(0, getBuilder().length());
            }
        } else {
            // We need more characters before parsing
        }
    }

    /**
     * Write the given response on the socket.
     * 
     * @param response
     *            The response to write.
     */
    @Override
    protected void writeMessage(Response response) {
        if (getOutboundMessageState() == null) {
            setOutboundMessageState(WayMessageState.START_LINE);
            getOutboundBuilder().delete(0, getOutboundBuilder().length());
        }

        while (getOutboundBuffer().hasRemaining()) {
            if (getOutboundMessageState() == WayMessageState.START_LINE) {
                writeMessageStart();
            } else if (getMessageState() == WayMessageState.HEADERS) {
                readMessageHeaders();
            }
        }
    }

    @Override
    protected void writeMessageStart() throws IOException {
        getOutboundBuilder().delete(0, getOutboundBuilder().length());

        Protocol protocol = getOutboundMessage().getRequest().getProtocol();
        String protocolVersion = protocol.getVersion();
        String version = protocol.getTechnicalName() + '/'
                + ((protocolVersion == null) ? "1.1" : protocolVersion);
        getOutboundBuilder().append(
                version.getBytes(CharacterSet.ISO_8859_1.getName()));
        getOutboundBuilder().append(' ');
        getOutboundBuilder().append(
                StringUtils.getAsciiBytes(Integer.toString(getOutboundMessage()
                        .getStatus().getCode())));
        getOutboundBuilder().append(' ');

        if (getOutboundMessage().getStatus().getDescription() != null) {
            getOutboundBuilder().append(
                    StringUtils.getLatin1Bytes(getOutboundMessage().getStatus()
                            .getDescription()));
        } else {
            getOutboundBuilder().append(
                    StringUtils.getAsciiBytes(("Status " + getOutboundMessage()
                            .getStatus().getCode())));
        }

        getOutboundBuilder().append("\r\n");
    }
}
