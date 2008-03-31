/*
 * Copyright 2005-2008 Noelios Consulting.
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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.data.Response;
import org.restlet.util.ByteUtils;

import com.noelios.restlet.util.ChunkedInputStream;
import com.noelios.restlet.util.ChunkedOutputStream;
import com.noelios.restlet.util.KeepAliveOutputStream;

/**
 * HTTP server call based on streams.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StreamServerCall extends HttpServerCall {

    /** The request entity stream */
    private volatile InputStream requestEntityStream;

    /** The request input stream. */
    private final InputStream requestStream;

    /** The response entity output stream. */
    private volatile OutputStream responseEntityStream;

    /** The response output stream. */
    private final OutputStream responseStream;

    /** The connecting user */
    private final Socket socket;

    /**
     * Constructor.
     * 
     * @param server
     *                The server connector.
     * @param requestStream
     *                The request input stream.
     * @param responseStream
     *                The response output stream.
     * @param socket
     *                The request socket
     */
    public StreamServerCall(Server server, InputStream requestStream,
            OutputStream responseStream, Socket socket) {
        super(server);
        this.requestStream = requestStream;
        this.responseStream = responseStream;
        this.responseEntityStream = null;
        this.requestEntityStream = null;
        this.socket = socket;

        try {
            readRequestHead(getRequestHeadStream());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
                    ioe);
        }
    }

    @Override
    public void complete() {
        try {
            socket.getOutputStream().flush();

            // Exhaust the input stream before closing in case
            // the client is still writing to it
            ByteUtils.exhaust(getRequestEntityStream(getContentLength()));

            if (!socket.isClosed()) {
                socket.shutdownOutput();
                socket.close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Unable to shutdown server socket",
                    ex);
        }
    }

    @Override
    public String getClientAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        return socket.getPort();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (requestEntityStream == null) {
            if (isRequestChunked()) {
                requestEntityStream = new ChunkedInputStream(getRequestStream());
            } else {
                requestEntityStream = new InputEntityStream(getRequestStream(),
                        size);
            }
        }
        return requestEntityStream;
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
        if (responseEntityStream == null) {
            if (isResponseChunked()) {
                responseEntityStream = new ChunkedOutputStream(
                        getResponseStream());
            } else {
                responseEntityStream = new KeepAliveOutputStream(
                        getResponseStream());
            }
        }
        return responseEntityStream;
    }

    private OutputStream getResponseStream() {
        return responseStream;
    }

    @Override
    protected boolean isServerKeepAlive() {
        return false;
    }

    @Override
    public void writeResponseHead(Response response) throws IOException {
        if (shouldResponseBeChunked(response)) {
            getResponseHeaders().add(HttpConstants.HEADER_TRANSFER_ENCODING,
                    "chunked");
        }

        writeResponseHead(getResponseStream());
    }
}
