/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
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
     * @param socket
     *            The underlying BIO socket.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @throws IOException
     */
    public ServerConnection(BaseHelper<Server> helper, Socket socket,
            SocketChannel socketChannel) throws IOException {
        super(helper, socket, socketChannel);
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

    @Override
    public boolean canRead() {
        return super.canRead()
                && ((getInboundMessages().size() == 0) || isPipelining());
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
     * @param headers
     *            The request headers.
     * @param entity
     *            The request entity.
     * @param confidential
     *            True if received confidentially.
     * @param userPrincipal
     *            The user principal.
     * @return The created request.
     */
    protected ConnectedRequest createRequest(Context context,
            ServerConnection connection, String methodName, String resourceUri,
            String version, Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        return new ConnectedRequest(getHelper().getContext(), this, methodName,
                resourceUri, version, headers, createInboundEntity(headers),
                false, null);
    }

    /**
     * Reads the next request sent by the client if available. Note that the
     * optional entity is not fully read.
     * 
     * @throws IOException
     */
    @Override
    protected void readMessage() throws IOException {
        ConnectedRequest request = null;
        String requestMethod = null;
        String requestUri = null;
        String version = null;
        Series<Parameter> headers = null;

        // Parse the request method
        StringBuilder sb = new StringBuilder();
        int next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request method. End of stream reached too early.");
        }

        requestMethod = sb.toString();
        sb.delete(0, sb.length());

        // Parse the request URI
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request URI. End of stream reached too early.");
        }

        requestUri = sb.toString();
        if ((requestUri == null) || (requestUri.equals(""))) {
            requestUri = "/";
        }

        sb.delete(0, sb.length());

        // Parse the protocol version
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the protocol version. End of stream reached too early.");
        }
        next = getInboundStream().read();

        if (HeaderUtils.isLineFeed(next)) {
            version = sb.toString();
            sb.delete(0, sb.length());

            // Parse the headers
            Parameter header = HeaderUtils.readHeader(getInboundStream(), sb);
            while (header != null) {
                if (headers == null) {
                    headers = new Form();
                }

                headers.add(header);
                header = HeaderUtils.readHeader(getInboundStream(), sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the protocol version. The carriage return must be followed by a line feed.");
        }

        // Check if the client wants to close the connection
        if (HeaderUtils.isConnectionClose(headers)) {
            setState(ConnectionState.CLOSING);
        }

        // Create the request
        request = createRequest(getHelper().getContext(), this, requestMethod,
                requestUri, version, headers, createInboundEntity(headers),
                false, null);
        Response response = getHelper().createResponse(request);

        if (request != null) {
            if (request.isExpectingResponse()) {
                // Add it to the connection queue
                getInboundMessages().add(response);
            }

            // Add it to the helper queue
            getHelper().getInboundMessages().add(response);
        }
    }

    /**
     * Write the given response on the socket.
     * 
     * @param response
     *            The response to write.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeMessage(Response response) {
        // Prepare the headers
        Series<Parameter> headers = new Form();
        ConnectedRequest request = (ConnectedRequest) response.getRequest();

        try {
            if ((request.getMethod() != null)
                    && request.getMethod().equals(Method.HEAD)) {
                addEntityHeaders(response.getEntity(), headers);
                response.setEntity(null);
            } else if (Method.GET.equals(request.getMethod())
                    && Status.SUCCESS_OK.equals(response.getStatus())
                    && (!response.isEntityAvailable())) {
                addEntityHeaders(response.getEntity(), headers);
                getLogger()
                        .warning(
                                "A response with a 200 (Ok) status should have an entity. Make sure that resource \""
                                        + request.getResourceRef()
                                        + "\" returns one or sets the status to 204 (No content).");
            } else if (response.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
                addEntityHeaders(response.getEntity(), headers);

                if (response.isEntityAvailable()) {
                    getLogger()
                            .fine(
                                    "Responses with a 204 (No content) status generally don't have an entity. Only adding entity headers for resource \""
                                            + request.getResourceRef() + "\".");
                    response.setEntity(null);
                }
            } else if (response.getStatus()
                    .equals(Status.SUCCESS_RESET_CONTENT)) {
                if (response.isEntityAvailable()) {
                    getLogger()
                            .warning(
                                    "Responses with a 205 (Reset content) status can't have an entity. Ignoring the entity for resource \""
                                            + request.getResourceRef() + "\".");
                    response.setEntity(null);
                }
            } else if (response.getStatus().equals(
                    Status.REDIRECTION_NOT_MODIFIED)) {
                addEntityHeaders(response.getEntity(), headers);

                if (response.isEntityAvailable()) {
                    getLogger()
                            .warning(
                                    "Responses with a 304 (Not modified) status can't have an entity. Only adding entity headers for resource \""
                                            + request.getResourceRef() + "\".");
                    response.setEntity(null);
                }
            } else if (response.getStatus().isInformational()) {
                if (response.isEntityAvailable()) {
                    getLogger()
                            .warning(
                                    "Responses with an informational (1xx) status can't have an entity. Ignoring the entity for resource \""
                                            + request.getResourceRef() + "\".");
                    response.setEntity(null);
                }
            } else {
                addEntityHeaders(response.getEntity(), headers);

                if ((response.getEntity() != null)
                        && !response.getEntity().isAvailable()) {
                    // An entity was returned but isn't really available
                    getLogger()
                            .warning(
                                    "A response with an unavailable entity was returned. Ignoring the entity for resource \""
                                            + request.getResourceRef() + "\".");
                    response.setEntity(null);
                }
            }

            // Add the message headers
            try {
                addTransportHeaders(headers, response.getEntity());
                addResponseHeaders(response, headers);

                // Add user-defined extension headers
                Series<Parameter> additionalHeaders = (Series<Parameter>) response
                        .getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
                addAdditionalHeaders(headers, additionalHeaders);
            } catch (Exception e) {
                getLogger()
                        .log(
                                Level.INFO,
                                "Exception intercepted while adding the response headers",
                                e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

            // Write the response to the client
            writeMessage(response, headers);

            // Make sure that the optional request entity is released
            if (!response.getStatus().isInformational()
                    && (request.getEntity() != null)) {
                try {
                    request.getEntity().exhaust();
                } catch (IOException e) {
                    getLogger().log(Level.FINE,
                            "Unable to exhaust request entity", e);
                } finally {
                    request.getEntity().release();
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.INFO,
                    "An exception occured writing the response entity", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL,
                    "An exception occured writing the response entity");
            response.setEntity(null);

            try {
                writeMessage(response, headers);
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Unable to send error response",
                        ioe);
            }
        } finally {
            if (response.getOnSent() != null) {
                response.getOnSent().handle(request, response);
            }

            // Free the connection outbound for next responses
            getOutboundMessages().poll();
            setOutboundBusy(false);
        }
    }

    @Override
    protected void writeMessageHeadLine(Response response,
            OutputStream headStream) throws IOException {
        Protocol protocol = response.getRequest().getProtocol();
        String protocolVersion = protocol.getVersion();
        String version = protocol.getTechnicalName() + '/'
                + ((protocolVersion == null) ? "1.1" : protocolVersion);
        headStream.write(version.getBytes());
        headStream.write(' ');
        headStream.write(Integer.toString(response.getStatus().getCode())
                .getBytes());
        headStream.write(' ');

        if (response.getStatus().getDescription() != null) {
            headStream.write(response.getStatus().getDescription().getBytes());
        } else {
            headStream.write(("Status " + response.getStatus().getCode())
                    .getBytes());
        }

        headStream.write(13); // CR
        headStream.write(10); // LF
    }

}
