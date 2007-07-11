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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Status;

/**
 * HTTP client call based on streams.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StreamClientCall extends HttpClientCall {
    /** The request to send. */
    private Request request;

    /** The request output stream. */
    private OutputStream requestStream;

    /** The response input stream. */
    private InputStream responseStream;

    /**
     * Constructor.
     * 
     * @param helper
     *                The client connector helper.
     * @param request
     *                The request to send.
     */
    public StreamClientCall(StreamClientHelper helper, Request request) {
        super(helper, request.getMethod().toString(), request.getResourceRef()
                .getIdentifier());

        // Set the HTTP version
        setVersion("HTTP/1.1");
    }

    /**
     * Creates the socket that will be used to send the request and get the
     * response.
     * 
     * @param hostDomain
     *                The target host domain name.
     * @param hostPort
     *                The target host port.
     * @return The created socket.
     * @throws UnknownHostException
     * @throws IOException
     */
    public Socket createSocket(String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        return new Socket(hostDomain, hostPort);
    }

    /**
     * Returns the request to send.
     * 
     * @return The request to send.
     */
    public Request getRequest() {
        return this.request;
    }

    @Override
    public WritableByteChannel getRequestChannel() {
        return null;
    }

    @Override
    public OutputStream getRequestStream() {
        return this.requestStream;
    }

    @Override
    public ReadableByteChannel getResponseChannel() {
        return null;
    }

    @Override
    public InputStream getResponseStream() {
        return this.responseStream;
    }

    /**
     * Parses the HTTP response.
     * 
     * @throws IOException
     */
    protected void parseResponse() throws IOException {
        StringBuilder sb = new StringBuilder();

        // Parse the HTTP version
        int next = getResponseStream().read();
        while ((next != -1) && !HttpUtils.isSpace(next)) {
            sb.append((char) next);
            next = getResponseStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the response HTTP version. End of stream reached too early.");
        } else {
            setVersion(sb.toString());
            sb.delete(0, sb.length());

            // Parse the status code
            next = getResponseStream().read();
            while ((next != -1) && !HttpUtils.isSpace(next)) {
                sb.append((char) next);
                next = getResponseStream().read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the response status. End of stream reached too early.");
            } else {
                setStatusCode(Integer.parseInt(sb.toString()));
                sb.delete(0, sb.length());

                // Parse the reason phrase
                next = getResponseStream().read();
                while ((next != -1) && !HttpUtils.isCarriageReturn(next)) {
                    sb.append((char) next);
                    next = getResponseStream().read();
                }

                if (next == -1) {
                    throw new IOException(
                            "Unable to parse the reason phrase. End of stream reached too early.");
                } else {
                    next = getResponseStream().read();

                    if (HttpUtils.isLineFeed(next)) {
                        setReasonPhrase(sb.toString());
                        sb.delete(0, sb.length());

                        // Parse the headers
                        Parameter header = HttpUtils.readHeader(
                                getResponseStream(), sb);
                        while (header != null) {
                            getResponseHeaders().add(header);
                            header = HttpUtils.readHeader(getResponseStream(),
                                    sb);
                        }
                    } else {
                        throw new IOException(
                                "Unable to parse the reason phrase. The carriage return must be followed by a line feed.");
                    }
                }
            }
        }
    }

    @Override
    public Status sendRequest(Request request) {
        Status result = null;

        try {
            // Extract the host info
            String hostDomain = request.getResourceRef().getHostDomain();
            int hostPort = request.getResourceRef().getHostPort();
            if (hostPort == -1) {
                hostPort = request.getResourceRef().getSchemeProtocol()
                        .getDefaultPort();
            }

            // Create the client socket
            Socket socket = createSocket(hostDomain, hostPort);
            this.requestStream = socket.getOutputStream();
            this.responseStream = socket.getInputStream();

            // Write the request line
            getRequestStream().write(getMethod().getBytes());
            getRequestStream().write(' ');
            getRequestStream().write(getRequestUri().getBytes());
            getRequestStream().write(' ');
            getRequestStream().write(getVersion().getBytes());
            getRequestStream().write(13); // CR
            getRequestStream().write(10); // LF

            // We don't support persistent connections yet
            getRequestHeaders().set(HttpConstants.HEADER_CONNECTION, "close",
                    true);

            // We don't support persistent connections yet
            String host = hostDomain;
            if (request.getResourceRef().getHostPort() != -1) {
                host += ":" + request.getResourceRef().getHostPort();
            }
            getRequestHeaders().set(HttpConstants.HEADER_HOST, host, true);

            // Write the request headers
            for (Parameter header : getRequestHeaders()) {
                HttpUtils.writeHeader(header, getRequestStream());
            }

            // Write the end of the headers section
            getRequestStream().write(13); // CR
            getRequestStream().write(10); // LF

            // Write the request body
            super.sendRequest(request);

            // Parse the response
            parseResponse();
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.FINE,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(
                    Status.CONNECTOR_ERROR_COMMUNICATION,
                    "Unable to complete the HTTP call due to a communication error with the remote server. "
                            + ioe.getMessage());
        }

        return result;
    }
}
