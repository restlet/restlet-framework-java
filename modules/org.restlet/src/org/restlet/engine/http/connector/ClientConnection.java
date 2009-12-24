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

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
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
        Reference absoluteRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();
        if (absoluteRef.hasQuery()) {
            return absoluteRef.getPath() + "?" + absoluteRef.getQuery();
        }

        return absoluteRef.getPath();
    }

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socket
     *            The underlying socket.
     * @throws IOException
     */
    public ClientConnection(BaseHelper<Client> helper, Socket socket)
            throws IOException {
        super(helper, socket);
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
     * Creates a new response.
     * 
     * @param context
     *            The context of the parent connector.
     * @param connection
     *            The parent network connection.
     * @param request
     *            The associated request.
     * @param version
     *            The protocol version.
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server IP port number.
     * @return The created response.
     */
    protected ConnectedResponse createResponse(Context context,
            ClientConnection connection, Request request, String version,
            int statusCode, String reasonPhrase, String serverAddress,
            int serverPort) {
        return new ConnectedResponse(context, connection, request, version,
                statusCode, reasonPhrase, serverAddress, serverPort);
    }

    @Override
    protected ConnectedResponse readMessage() throws IOException {
        ConnectedResponse result = null;
        String version = null;
        Series<Parameter> headers = null;
        int statusCode = 0;
        String reasonPhrase = null;

        // Mark the inbound as busy
        setInboundBusy(true);

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

        // Create the response
        result = createResponse(getHelper().getContext(), this, null, version,
                statusCode, reasonPhrase, getSocket().getLocalAddress()
                        .toString(), getSocket().getPort());

        if (result != null) {
            if (!result.getStatus().isInformational()) {
                // Add it to the connection queue
                getInboundMessages().add(result);
            }

            // Add it to the helper queue
            getHelper().getOutboundMessages().add(result);
        }

        return result;
    }

    @Override
    protected void writeMessage(Response message) {
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
        // if ((request.getEntity() == null || !request.isEntityAvailable() ||
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
    }

    @Override
    protected void writeMessageHeadLine(Response message,
            OutputStream headStream) throws IOException {
        Request request = message.getRequest();
        headStream.write(request.getMethod().getName().getBytes());
        headStream.write(' ');
        headStream.write(getRequestUri(request.getResourceRef()).getBytes());
        headStream.write(' ');
        headStream.write(request.getProtocol().getVersion().getBytes());
        HeaderUtils.writeCRLF(getOutboundStream());
    }

}
