/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.http;

import com.noelios.restlet.util.HeaderReader;
import org.restlet.Server;
import org.restlet.data.*;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.ReadableRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

/**
 * Abstract HTTP server connector call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpServerCall extends HttpCall {
    /** Indicates if the "host" header was already parsed. */
    private boolean hostParsed;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server connector.
     */
    public HttpServerCall(Server server) {
        this(server.getLogger(), server.getAddress(), server.getPort());
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger.
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server port.
     */
    public HttpServerCall(Logger logger, String serverAddress, int serverPort) {
        setLogger(logger);
        setServerAddress(serverAddress);
        setServerPort(serverPort);
        this.hostParsed = false;
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract ReadableByteChannel getRequestChannel();

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract InputStream getRequestStream();

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public abstract WritableByteChannel getResponseChannel();

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    public abstract OutputStream getResponseStream();

    /**
     * Returns the request entity if available.
     * 
     * @return The request entity if available.
     */
    public Representation getRequestEntity() {
        Representation result = null;
        InputStream requestStream = getRequestStream();
        ReadableByteChannel requestChannel = getRequestChannel();

        if (((requestStream != null) || (requestChannel != null))) {
            // Extract the header values
            MediaType contentMediaType = null;
            long contentLength = Representation.UNKNOWN_SIZE;

            if (requestStream != null) {
                result = new InputRepresentation(requestStream,
                        contentMediaType, contentLength);
            } else {
                result = new ReadableRepresentation(requestChannel,
                        contentMediaType, contentLength);
            }

            for (Parameter header : getRequestHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_ENCODING)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    while (value != null) {
                        Encoding encoding = Encoding.valueOf(value);
                        if (!encoding.equals(Encoding.IDENTITY)) {
                            result.getEncodings().add(encoding);
                        }
                        value = hr.readValue();
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LANGUAGE)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    while (value != null) {
                        result.getLanguages().add(Language.valueOf(value));
                        value = hr.readValue();
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_TYPE)) {
                    ContentType contentType = new ContentType(header.getValue());
                    result.setMediaType(contentType.getMediaType());
                    result.setCharacterSet(contentType.getCharacterSet());

                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LENGTH)) {
                    try {
                        contentLength = Long.parseLong(header.getValue());
                    } catch (NumberFormatException e) {
                        contentLength = Representation.UNKNOWN_SIZE;
                    }
                    result.setSize(contentLength);
                }
            }
        }

        return result;
    }

    /**
     * Returns the host domain name.
     * 
     * @return The host domain name.
     */
    public String getHostDomain() {
        if (!hostParsed)
            parseHost();
        return super.getHostDomain();
    }

    /**
     * Returns the host port.
     * 
     * @return The host port.
     */
    public int getHostPort() {
        if (!hostParsed)
            parseHost();
        return super.getHostPort();
    }

    /**
     * Parses the "host" header to set the server host and port properties.
     */
    private void parseHost() {
        String host = getRequestHeaders().getFirstValue(
                HttpConstants.HEADER_HOST, true);
        getLogger().warning(
                "Host header : " + host + ".");
        if (host != null) {
            int colonIndex = host.indexOf(':');

            if (colonIndex != -1) {
                super.setHostDomain(host.substring(0, colonIndex));
                super.setHostPort(Integer.valueOf(host
                        .substring(colonIndex + 1)));
            } else {
                super.setHostDomain(host);

                if (isConfidential()) {
                    super.setServerPort(Protocol.HTTPS.getDefaultPort());
                } else {
                    super.setServerPort(Protocol.HTTP.getDefaultPort());
                }
            }
        } else {
            getLogger().info(
                    "Couldn't find the mandatory \"Host\" HTTP header.");
        }

        this.hostParsed = true;
    }

    /**
     * Reads the HTTP request head (request line and headers).
     * 
     * @throws IOException
     */
    protected void readRequestHead(InputStream headStream) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Parse the request method
        int next = headStream.read();
        while ((next != -1) && !HttpUtils.isSpace(next)) {
            sb.append((char) next);
            next = headStream.read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request method. End of stream reached too early.");
        } else {
            setMethod(sb.toString());
            sb.delete(0, sb.length());

            // Parse the request URI
            next = headStream.read();
            while ((next != -1) && !HttpUtils.isSpace(next)) {
                sb.append((char) next);
                next = headStream.read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the request URI. End of stream reached too early.");
            } else {
                setRequestUri(sb.toString());
                sb.delete(0, sb.length());

                // Parse the HTTP version
                next = headStream.read();
                while ((next != -1) && !HttpUtils.isCarriageReturn(next)) {
                    sb.append((char) next);
                    next = headStream.read();
                }

                if (next == -1) {
                    throw new IOException(
                            "Unable to parse the HTTP version. End of stream reached too early.");
                } else {
                    next = headStream.read();

                    if (HttpUtils.isLineFeed(next)) {
                        setVersion(sb.toString());
                        sb.delete(0, sb.length());

                        // Parse the headers
                        Parameter header = HttpUtils.readHeader(headStream, sb);
                        while (header != null) {
                            getRequestHeaders().add(header);
                            header = HttpUtils.readHeader(headStream, sb);
                        }
                    } else {
                        throw new IOException(
                                "Unable to parse the HTTP version. The carriage return must be followed by a line feed.");
                    }
                }
            }
        }
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them over the network. The default
     * implementation only writes the response entity on the reponse stream or
     * channel. Subclasses will probably also copy the response headers and
     * status.
     * 
     * @param response
     *            The high-level response.
     */
    public void sendResponse(Response response) throws IOException {
        if (response != null) {
            writeResponseHead(response);
            Representation entity = response.getEntity();

            if ((entity != null)
                    && !response.getRequest().getMethod().equals(Method.HEAD)
                    && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                    && !response.getStatus().equals(
                            Status.SUCCESS_RESET_CONTENT)) {
                // Get the connector service to callback
                ConnectorService connectorService = getConnectorService(response
                        .getRequest());
                if (connectorService != null)
                    connectorService.beforeSend(entity);

                writeResponseBody(entity);

                if (connectorService != null)
                    connectorService.afterSend(entity);
            }

            if (getResponseStream() != null) {
                getResponseStream().flush();
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
     * @throws IOException
     */
    public void writeResponseBody(Representation entity) throws IOException {
        // Send the entity to the client
        if (getResponseChannel() != null) {
            entity.write(getResponseChannel());
        } else if (getResponseStream() != null) {
            entity.write(getResponseStream());
        }
    }

    /**
     * Writes the response status line and headers. Does nothing by default.
     * 
     * @param response
     *            The response.
     * @throws IOException
     */
    public void writeResponseHead(Response response) throws IOException {
        // Do nothing by default
    }

    /**
     * Writes the response head to the given output stream.
     * 
     * @param headStream
     *            The output stream to write to.
     * @throws IOException
     */
    protected void writeResponseHead(OutputStream headStream)
            throws IOException {
        // Write the status line
        headStream.write(getVersion().getBytes());
        headStream.write(' ');
        headStream.write(getStatusCode());
        headStream.write(' ');
        headStream.write(getReasonPhrase().getBytes());
        headStream.write(13); // CR
        headStream.write(10); // LF

        // We don't support persistent connections yet
        getResponseHeaders()
                .set(HttpConstants.HEADER_CONNECTION, "close", true);

        // Write the response headers
        for (Parameter header : getResponseHeaders()) {
            HttpUtils.writeHeader(header, headStream);
        }

        // Write the end of the headers section
        headStream.write(13); // CR
        headStream.write(10); // LF
    }

}
