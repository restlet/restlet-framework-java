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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Response;

/**
 * HTTP server call based on streams.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StreamServerCall extends HttpServerCall {
    /** The request input stream. */
    private InputStream requestStream;

    /** The response output stream. */
    private OutputStream responseStream;

    /**
     * Constructor.
     * 
     * @param server
     *            The server connector.
     * @param requestStream
     *            The request input stream.
     * @param responseStream
     *            The response output stream.
     */
    public StreamServerCall(Server server, InputStream requestStream,
            OutputStream responseStream) {
        super(server);
        this.requestStream = requestStream;
        this.responseStream = responseStream;

        /** Parse the request until the optional request body. */
        try {
            parseRequest();
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
                    ioe);
        }
    }

    @Override
    public ReadableByteChannel getRequestChannel() {
        return null;
    }

    @Override
    public InputStream getRequestStream() {
        return this.requestStream;
    }

    @Override
    public WritableByteChannel getResponseChannel() {
        return null;
    }

    @Override
    public OutputStream getResponseStream() {
        return this.responseStream;
    }

    /**
     * Parsed the HTTP request.
     * 
     * @throws IOException
     */
    protected void parseRequest() throws IOException {
        StringBuilder sb = new StringBuilder();

        // Parse the request method
        int next = getRequestStream().read();
        while ((next != -1) && !HttpUtils.isSpace(next)) {
            sb.append((char) next);
            next = getRequestStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request method. End of stream reached too early.");
        } else {
            setMethod(sb.toString());
            sb.delete(0, sb.length());

            // Parse the request URI
            next = getRequestStream().read();
            while ((next != -1) && !HttpUtils.isSpace(next)) {
                sb.append((char) next);
                next = getRequestStream().read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the request URI. End of stream reached too early.");
            } else {
                setRequestUri(sb.toString());
                sb.delete(0, sb.length());

                // Parse the HTTP version
                next = getRequestStream().read();
                while ((next != -1) && !HttpUtils.isCarriageReturn(next)) {
                    sb.append((char) next);
                    next = getRequestStream().read();
                }

                if (next == -1) {
                    throw new IOException(
                            "Unable to parse the HTTP version. End of stream reached too early.");
                } else {
                    next = getRequestStream().read();

                    if (HttpUtils.isLineFeed(next)) {
                        setVersion(sb.toString());
                        sb.delete(0, sb.length());

                        // Parse the headers
                        Parameter header = HttpUtils.readHeader(
                                getRequestStream(), sb);
                        while (header != null) {
                            getRequestHeaders().add(header);
                            header = HttpUtils.readHeader(getRequestStream(),
                                    sb);
                        }
                    } else {
                        throw new IOException(
                                "Unable to parse the HTTP version. The carriage return must be followed by a line feed.");
                    }
                }
            }
        }
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        // Write the status line
        getResponseStream().write(getVersion().getBytes());
        getResponseStream().write(' ');
        getResponseStream().write(getStatusCode());
        getResponseStream().write(' ');
        getResponseStream().write(getReasonPhrase().getBytes());
        getResponseStream().write(13); // CR
        getResponseStream().write(10); // LF

        // We don't support persistent connections yet
        getResponseHeaders()
                .set(HttpConstants.HEADER_CONNECTION, "close", true);

        // Write the response headers
        for (Parameter header : getResponseHeaders()) {
            HttpUtils.writeHeader(header, getResponseStream());
        }

        // Write the end of the headers section
        getResponseStream().write(13); // CR
        getResponseStream().write(10); // LF

        // Write the response body
        super.sendResponse(response);
    }

}
