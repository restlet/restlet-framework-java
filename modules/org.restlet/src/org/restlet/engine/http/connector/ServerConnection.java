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
import java.nio.channels.WritableByteChannel;
import java.security.Principal;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

/**
 * Generic HTTP server connection.
 * 
 * @author Jerome Louvel
 */
public abstract class ServerConnection extends Connection<Server> {

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socket
     *            The underlying socket.
     * @throws IOException
     */
    public ServerConnection(BaseHelper<Server> helper, Socket socket)
            throws IOException {
        super(helper, socket);
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
        getHelper().getPendingResponses().add(response);
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
    protected abstract ConnectedRequest createRequest(Context context,
            ServerConnection connection, String methodName, String resourceUri,
            String version, Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal);

    @Override
    protected void handleNextMessage() {
        getHelper().handleNextRequest();
    }

    /**
     * Reads the next request sent by the client if available. Note that the
     * optional entity is not fully read.
     * 
     * @return The next request sent by the client if available.
     * @throws IOException
     */
    @Override
    protected ConnectedRequest readMessage() throws IOException {
        ConnectedRequest result = null;
        String requestMethod = null;
        String requestUri = null;
        String version = null;
        Series<Parameter> requestHeaders = null;

        // Mark the inbound as busy
        setInboundBusy(true);

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

        // Parse the HTTP version
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the HTTP version. End of stream reached too early.");
        }
        next = getInboundStream().read();

        if (HeaderUtils.isLineFeed(next)) {
            version = sb.toString();
            sb.delete(0, sb.length());

            // Parse the headers
            Parameter header = HeaderUtils.readHeader(getInboundStream(), sb);
            while (header != null) {
                if (requestHeaders == null) {
                    requestHeaders = new Form();
                }

                requestHeaders.add(header);
                header = HeaderUtils.readHeader(getInboundStream(), sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the HTTP version. The carriage return must be followed by a line feed.");
        }

        if (HeaderUtils.isConnectionClose(requestHeaders)) {
            setState(ConnectionState.CLOSING);
        }

        // Create the HTTP request
        result = createRequest(getHelper().getContext(), this, requestMethod,
                requestUri, version, requestHeaders,
                createInboundEntity(requestHeaders), false, null);

        if (result != null) {
            if (result.isExpectingResponse()) {
                // Add it to the connection queue
                getInboundMessages().add(result);
            }

            // Add it to the helper queue
            getHelper().getPendingRequests().add(result);
        }

        return result;
    }

    /**
     * Writes the entity headers for the given response.
     * 
     * @param entity
     *            The entity to inspect.
     */
    protected void writeEntityHeaders(Representation entity,
            Series<Parameter> headers) {
        HeaderUtils.addEntityHeaders(entity, headers);
    }

    /**
     * Write the given response on the socket.
     * 
     * @param response
     *            The response to write.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeMessage(Message message) {
        if (message instanceof Response) {
            Response response = (Response) message;

            // Prepare the headers
            Series<Parameter> headers = new Form();

            try {
                if ((response.getRequest().getMethod() != null)
                        && response.getRequest().getMethod()
                                .equals(Method.HEAD)) {
                    writeEntityHeaders(response.getEntity(), headers);
                    response.setEntity(null);
                } else if (Method.GET.equals(response.getRequest().getMethod())
                        && Status.SUCCESS_OK.equals(response.getStatus())
                        && (!response.isEntityAvailable())) {
                    writeEntityHeaders(response.getEntity(), headers);
                    getLogger()
                            .warning(
                                    "A response with a 200 (Ok) status should have an entity. Make sure that resource \""
                                            + response.getRequest()
                                                    .getResourceRef()
                                            + "\" returns one or sets the status to 204 (No content).");
                } else if (response.getStatus().equals(
                        Status.SUCCESS_NO_CONTENT)) {
                    writeEntityHeaders(response.getEntity(), headers);

                    if (response.isEntityAvailable()) {
                        getLogger()
                                .fine(
                                        "Responses with a 204 (No content) status generally don't have an entity. Only adding entity headers for resource \""
                                                + response.getRequest()
                                                        .getResourceRef()
                                                + "\".");
                        response.setEntity(null);
                    }
                } else if (response.getStatus().equals(
                        Status.SUCCESS_RESET_CONTENT)) {
                    if (response.isEntityAvailable()) {
                        getLogger()
                                .warning(
                                        "Responses with a 205 (Reset content) status can't have an entity. Ignoring the entity for resource \""
                                                + response.getRequest()
                                                        .getResourceRef()
                                                + "\".");
                        response.setEntity(null);
                    }
                } else if (response.getStatus().equals(
                        Status.REDIRECTION_NOT_MODIFIED)) {
                    writeEntityHeaders(response.getEntity(), headers);

                    if (response.isEntityAvailable()) {
                        getLogger()
                                .warning(
                                        "Responses with a 304 (Not modified) status can't have an entity. Only adding entity headers for resource \""
                                                + response.getRequest()
                                                        .getResourceRef()
                                                + "\".");
                        response.setEntity(null);
                    }
                } else if (response.getStatus().isInformational()) {
                    if (response.isEntityAvailable()) {
                        getLogger()
                                .warning(
                                        "Responses with an informational (1xx) status can't have an entity. Ignoring the entity for resource \""
                                                + response.getRequest()
                                                        .getResourceRef()
                                                + "\".");
                        response.setEntity(null);
                    }
                } else {
                    writeEntityHeaders(response.getEntity(), headers);

                    if ((response.getEntity() != null)
                            && !response.getEntity().isAvailable()) {
                        // An entity was returned but isn't really available
                        getLogger()
                                .warning(
                                        "A response with an unavailable entity was returned. Ignoring the entity for resource \""
                                                + response.getRequest()
                                                        .getResourceRef()
                                                + "\".");
                        response.setEntity(null);
                    }
                }

                // Add the response headers
                try {
                    addResponseHeaders(response, headers);

                    // Add user-defined extension headers
                    Series<Parameter> additionalHeaders = (Series<Parameter>) response
                            .getAttributes().get(
                                    HeaderConstants.ATTRIBUTE_HEADERS);
                    addAdditionalHeaders(headers, additionalHeaders);

                    // Set the server name again
                    headers.add(HeaderConstants.HEADER_SERVER, response
                            .getServerInfo().getAgent());
                } catch (Exception e) {
                    getLogger()
                            .log(
                                    Level.INFO,
                                    "Exception intercepted while adding the response headers",
                                    e);
                    response.setStatus(Status.SERVER_ERROR_INTERNAL);
                }

                // Write the response to the client
                writeResponse(response, headers);
            } catch (Exception e) {
                getLogger().log(Level.INFO,
                        "An exception occured writing the response entity", e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL,
                        "An exception occured writing the response entity");
                response.setEntity(null);

                try {
                    writeResponse(response, headers);
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to send error response", ioe);
                }
            } finally {
                if (response.getOnSent() != null) {
                    response.getOnSent()
                            .handle(response.getRequest(), response);
                }
            }
        }
    }

    /**
     * Writes the response back to the client. Commits the status, headers and
     * optional entity and send them over the network. The default
     * implementation only writes the response entity on the response stream or
     * channel. Subclasses will probably also copy the response headers and
     * status.
     * 
     * @param response
     *            The high-level response.
     * @throws IOException
     *             if the Response could not be written to the network.
     */
    protected void writeResponse(Response response, Series<Parameter> headers)
            throws IOException {
        if (response != null) {
            // Get the connector service to callback
            Representation responseEntity = response.getEntity();
            ConnectorService connectorService = ConnectorHelper
                    .getConnectorService(response.getRequest());

            if (connectorService != null) {
                connectorService.beforeSend(responseEntity);
            }

            try {
                writeResponseHead(response, headers);

                if (responseEntity != null) {
                    boolean chunked = HeaderUtils.isChunkedEncoding(headers);
                    WritableByteChannel responseEntityChannel = getOutboundEntityChannel(chunked);
                    OutputStream responseEntityStream = getOutboundEntityStream(chunked);
                    writeResponseBody(responseEntity, responseEntityChannel,
                            responseEntityStream);

                    if (responseEntityStream != null) {
                        responseEntityStream.flush();
                        responseEntityStream.close();
                    }
                }
            } catch (IOException ioe) {
                // The stream was probably already closed by the
                // connector. Probably OK, low message priority.
                getLogger()
                        .log(
                                Level.FINE,
                                "Exception while flushing and closing the entity stream.",
                                ioe);
            } finally {
                setOutboundBusy(false);

                if (responseEntity != null) {
                    responseEntity.release();
                }

                if (connectorService != null) {
                    connectorService.afterSend(responseEntity);
                }
            }
        }
    }

    /**
     * Effectively writes the response body. The entity to write is guaranteed
     * to be non null. Attempts to write the entity on the response channel or
     * response stream by default.
     * 
     * @param entity
     *            The representation to write as entity of the body.
     * @param responseEntityChannel
     *            The response entity channel or null if a stream is used.
     * @param responseEntityStream
     *            The response entity stream or null if a channel is used.
     * @throws IOException
     */
    protected void writeResponseBody(Representation entity,
            WritableByteChannel responseEntityChannel,
            OutputStream responseEntityStream) throws IOException {
        // Send the entity to the client
        if (responseEntityChannel != null) {
            entity.write(responseEntityChannel);
        } else if (responseEntityStream != null) {
            entity.write(responseEntityStream);
            responseEntityStream.flush();
        }
    }

    /**
     * Writes the response head to the given output stream.
     * 
     * @param response
     *            The response.
     * @param headStream
     *            The output stream to write to.
     * @throws IOException
     */
    protected void writeResponseHead(Response response,
            OutputStream headStream, Series<Parameter> headers)
            throws IOException {
        // Write the status line
        Protocol protocol = response.getRequest().getProtocol();
        String requestVersion = protocol.getVersion();
        String version = protocol.getTechnicalName() + '/'
                + ((requestVersion == null) ? "1.1" : requestVersion);
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

        if (!isPersistent()) {
            headers.set(HeaderConstants.HEADER_CONNECTION, "close", true);
        }

        // Check if 'Transfer-Encoding' header should be set
        if (shouldBeChunked(response.getEntity())) {
            headers.add(HeaderConstants.HEADER_TRANSFER_ENCODING, "chunked");
        }

        // Write the response headers
        for (Parameter header : headers) {
            HeaderUtils.writeHeader(header, headStream);
        }

        // Write the end of the headers section
        headStream.write(13); // CR
        headStream.write(10); // LF
        headStream.flush();
    }

    /**
     * Writes the response status line and headers. Does nothing by default.
     * 
     * @param response
     *            The response.
     * @throws IOException
     */
    protected void writeResponseHead(Response response,
            Series<Parameter> headers) throws IOException {
        writeResponseHead(response, getOutboundStream(), headers);
    }

}
