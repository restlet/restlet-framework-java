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
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * Generic HTTP-like client connection.
 * 
 * @author Jerome Louvel
 */
public class ClientConnection extends Connection<Client> {

    /**
     * Returns the absolute request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @return The absolute request URI.
     */
    private static String getRequestUri(Reference resourceRef) {
        String result = null;
        Reference absoluteRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();
        if (absoluteRef.hasQuery()) {
            result = absoluteRef.getPath() + "?" + absoluteRef.getQuery();
        }

        result = absoluteRef.getPath();

        if ((result == null) || (result.equals(""))) {
            result = "/";
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

        statusCode = Integer.parseInt(sb.toString());
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
                    "Unable to parse the reason phrase. The carriage return must be followed by a line feed.");
        }

        // Check if the server wants to close the connection
        if (HeaderUtils.isConnectionClose(headers)) {
            setState(ConnectionState.CLOSING);
        }

        // Update the response
        Response response = getOutboundMessages().peek();
        response.setStatus(Status.valueOf(statusCode), reasonPhrase);
        response.getServerInfo().setAddress(
                getSocket().getLocalAddress().toString());
        response.getServerInfo().setAgent(Engine.VERSION_HEADER);
        response.getServerInfo().setPort(getSocket().getPort());
        response.setEntity(createInboundEntity(headers));

        if (!response.getStatus().isInformational()) {
            getOutboundMessages().poll();
        }

        // Add it to the helper queue
        getHelper().getInboundMessages().add(response);
    }

    @Override
    public boolean canRead() throws IOException {
        return super.canRead() && (getOutboundMessages().size() > 0);
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
        Request request = response.getRequest();

        try {
            try {
                addTransportHeaders(headers, request.getEntity());
                addRequestHeaders(request, headers);

                // Prepare the host header
                // String host = hostDomain;
                //
                // if (resourceRef.getHostPort() != -1) {
                // host += ":" + resourceRef.getHostPort();
                // }
                //
                // headers.set(HeaderConstants.HEADER_HOST, host, true);

                // TODO may be replaced by an attribute on the Method class
                // telling that a method requires an entity.
                // Actually, since such classes are used in the context of
                // clients and servers, there could be two attributes
                // if ((request.getEntity() == null ||
                // !request.isEntityAvailable() ||
                // request
                // .getEntity().getSize() == 0)
                // && (Method.POST.equals(request.getMethod()) || Method.PUT
                // .equals(request.getMethod()))) {
                // HeaderUtils.writeHeader(new Parameter(
                // HeaderConstants.HEADER_CONTENT_LENGTH, "0"),
                // getOutboundStream());
                // }

                // if (result.equals(Status.CONNECTOR_ERROR_COMMUNICATION)) {
                // return result;
                // }

                // Add user-defined extension headers
                Series<Parameter> additionalHeaders = (Series<Parameter>) request
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

            // Write the request to the server
            writeMessage(response, headers);
        } catch (Exception e) {
            getLogger().log(Level.INFO,
                    "An exception occured writing the request entity", e);
            response.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION,
                    "An exception occured writing the request entity");
            response.setEntity(null);

            try {
                writeMessage(response, headers);
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Unable to send error response",
                        ioe);
            }
        } finally {
            if (request.getOnSent() != null) {
                request.getOnSent().handle(request, response);
            }

            if (!request.isExpectingResponse()) {
                // Don't wait for a response
                getOutboundMessages().remove(response);
            }
        }
    }

    @Override
    protected void writeMessageHeadLine(Response message,
            OutputStream headStream) throws IOException {
        Request request = message.getRequest();
        headStream.write(request.getMethod().getName().getBytes());
        headStream.write(' ');
        headStream.write(getRequestUri(request.getResourceRef()).getBytes());
        headStream.write(' ');
        headStream.write(request.getProtocol().getName().getBytes());
        headStream.write('/');
        headStream.write(request.getProtocol().getVersion().getBytes());
        HeaderUtils.writeCRLF(getOutboundStream());
    }

}
