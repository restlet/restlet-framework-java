/**
 * Copyright 2005-2011 Noelios Technologies.
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
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.util.Series;

/**
 * Generic HTTP-like client connection.
 * 
 * @author Jerome Louvel
 */
public class ClientConnection extends Connection<Client> {

    /**
     * Returns the request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @param isProxied
     *            Indicates if the request goes through a proxy and requires an
     *            absolute URI.
     * @return The absolute request URI.
     */
    private static String getRequestUri(Reference resourceRef, boolean isProxied) {
        String result = null;
        Reference requestRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();

        if (isProxied) {
            result = requestRef.getIdentifier();
        } else {
            if (requestRef.hasQuery()) {
                result = requestRef.getPath() + "?" + requestRef.getQuery();
            } else {
                result = requestRef.getPath();
            }

            if ((result == null) || (result.equals(""))) {
                result = "/";
            }
        }

        return result;
    }

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
    public ClientConnection(BaseHelper<Client> helper, Socket socket,
            SocketChannel socketChannel) throws IOException {
        super(helper, socket, socketChannel);
    }

    /**
     * Adds the request headers.
     * 
     * @param request
     *            The request to inspect.
     * @param headers
     *            The headers series to update.
     */
    protected void addRequestHeaders(Request request, Series<Parameter> headers) {
        HeaderUtils.addRequestHeaders(request, headers);
    }

    /**
     * Indicates whether the client connection can accept a new message.
     * 
     * @return True if the client connection can accept a new message.
     */
    public boolean canEnqueue() {
        return !isBusy() && getOutboundMessages().isEmpty()
                && getInboundMessages().isEmpty();
    }

    @Override
    public boolean canRead() {
        // There should be at least one call to read/update
        return super.canRead() && (getInboundMessages().size() > 0);
    }

    @Override
    public boolean canWrite() {
        return super.canWrite()
                && ((getInboundMessages().size() == 0) || isPipelining());
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
    protected void readMessage() throws IOException {
        @SuppressWarnings("unused")
        String version = null;
        Series<Parameter> headers = null;
        int statusCode = 0;
        String reasonPhrase = null;

        // Parse the HTTP version
        StringBuilder sb = new StringBuilder();
        int next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the response HTTP version. End of stream reached too early.");
        }

        version = sb.toString();
        sb.delete(0, sb.length());

        // Parse the status code
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the response status. End of stream reached too early.");
        }

        try {
            statusCode = Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Unable to parse the status code. Non numeric value: "
                            + sb.toString());
        }
        sb.delete(0, sb.length());

        // Parse the reason phrase
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the reason phrase. End of stream reached too early.");
        }

        next = getInboundStream().read();

        if (HeaderUtils.isLineFeed(next)) {
            reasonPhrase = sb.toString();
            sb.delete(0, sb.length());

            // Parse the headers
            Parameter header = HeaderReader.readHeader(getInboundStream(), sb);
            while (header != null) {
                if (headers == null) {
                    headers = new Form();
                }

                headers.add(header);
                header = HeaderReader.readHeader(getInboundStream(), sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the reason phrase. The carriage return must be followed by a line feed.");
        }

        // Check if the server wants to close the connection
        if (HeaderUtils.isConnectionClose(headers)) {
            setState(ConnectionState.CLOSING);
        }

        // Prepare the response
        Response finalResponse = getInboundMessages().peek();
        Response response = null;
        Status status = createStatus(statusCode);

        if (status.isInformational()) {
            response = getHelper().createResponse(finalResponse.getRequest());
        } else {
            response = finalResponse;
        }

        // Update the response
        response.setStatus(status, reasonPhrase);
        response.getServerInfo().setAddress(
                getSocket().getLocalAddress().toString());
        response.getServerInfo().setAgent(Engine.VERSION_HEADER);
        response.getServerInfo().setPort(getSocket().getPort());
        response.setEntity(createInboundEntity(headers));

        try {
            copyResponseTransportHeaders(headers, response);
        } catch (Throwable t) {
            getLogger()
                    .log(Level.WARNING, "Error while parsing the headers", t);
        }

        // Put the headers in the response's attributes map
        if (headers != null) {
            response.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    headers);
        }

        if (!response.getStatus().isInformational()) {
            getInboundMessages().poll();
        }

        // Add it to the helper queue
        getHelper().getInboundMessages().add(response);
    }

    /**
     * Write the given response on the socket.
     * 
     * @param response
     *            The response to write.
     */
    @Override
    protected void writeMessage(Response response) {
        // Prepare the headers
        Series<Parameter> headers = new Form();
        Request request = response.getRequest();

        try {
            addGeneralHeaders(request, headers);
            addRequestHeaders(request, headers);
            addEntityHeaders(request.getEntity(), headers);
            writeMessage(response, headers);
        } catch (IOException e) {
            getLogger().log(Level.INFO,
                    "An exception occured writing the request", e);
            response.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION,
                    "An exception occured writing the request");
            response.setEntity(null);

            try {
                writeMessage(response, headers);
            } catch (IOException ee) {
                getLogger().log(Level.WARNING, "Unable to send error request",
                        ee);
            }
        } finally {
            if (request.getOnSent() != null) {
                request.getOnSent().handle(request, response);
            }

            // The request has been written
            getOutboundMessages().poll();

            if (request.isExpectingResponse()) {
                getInboundMessages().add(response);
            }

            // Indicate that we are done with writing the request
            setOutboundBusy(false);
        }
    }

    @Override
    protected void writeMessageHeadLine(Response message,
            OutputStream headStream) throws IOException {
        Request request = message.getRequest();
        headStream.write(StringUtils.getAsciiBytes(request.getMethod()
                .getName()));
        headStream.write(' ');
        headStream.write(StringUtils.getAsciiBytes(getRequestUri(
                request.getResourceRef(), getHelper().isProxying())));
        headStream.write(' ');
        headStream.write(StringUtils.getAsciiBytes(request.getProtocol()
                .getTechnicalName()));
        headStream.write('/');
        headStream.write(StringUtils.getAsciiBytes(request.getProtocol()
                .getVersion()));
        HeaderUtils.writeCRLF(getOutboundStream());
    }

}
