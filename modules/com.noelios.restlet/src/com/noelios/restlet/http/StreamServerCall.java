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

import com.noelios.restlet.util.ChunkedInputStream;

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
     *                The server connector.
     * @param requestStream
     *                The request input stream.
     * @param responseStream
     *                The response output stream.
     */
    public StreamServerCall(Server server, InputStream requestStream,
            OutputStream responseStream) {
        super(server);
        this.requestStream = requestStream;
        this.responseStream = responseStream;

        try {
            readRequestHead(getRequestHeadStream());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
                    ioe);
        }
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (isRequestChunked()) {
            return new ChunkedInputStream(getRequestStream());
        } else {
            return new InputEntityStream(getRequestStream(), size);
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        return null;
    }

    @Override
    public InputStream getRequestHeadStream() {
        return getRequestStream();
    }

    private InputStream getRequestStream() {
        return requestStream;
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return getResponseStream();
    }

    private OutputStream getResponseStream() {
        return responseStream;
    }

    /**
     * Indicates if the request entity is chunked.
     * 
     * @return True if the request entity is chunked.
     */
    private boolean isRequestChunked() {
        for (Parameter header : getRequestHeaders()) {
            if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_TRANSFER_ENCODING)) {
                if (header.getValue().equalsIgnoreCase("chunked")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void writeResponseHead(Response response) throws IOException {
        writeResponseHead(getResponseEntityStream());
    }

}
