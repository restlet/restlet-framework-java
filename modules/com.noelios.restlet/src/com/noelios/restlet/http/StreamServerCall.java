/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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

import com.noelios.restlet.util.KeepAliveOutputStream;

/**
 * HTTP server call based on streams.
 * 
 * @author Jerome Louvel
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
     *            The server connector.
     * @param requestStream
     *            The request input stream.
     * @param responseStream
     *            The response output stream.
     * @param socket
     *            The request socket
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
        } catch (final IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
                    ioe);
        }
    }

    @Override
    public void complete() {
        try {
            this.socket.getOutputStream().flush();

            // Exhaust the input stream before closing in case
            // the client is still writing to it
            ByteUtils.exhaust(getRequestEntityStream(getContentLength()));

            if (!this.socket.isClosed()) {
                this.socket.shutdownOutput();
            }
        } catch (final IOException ex) {
            getLogger().log(Level.WARNING, "Unable to shutdown server socket",
                    ex);
        }
    }

    @Override
    public String getClientAddress() {
        return this.socket.getInetAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        return this.socket.getPort();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (this.requestEntityStream == null) {
            if (isRequestChunked()) {
                this.requestEntityStream = new ChunkedInputStream(
                        getRequestStream());
            } else {
                this.requestEntityStream = new InputEntityStream(
                        getRequestStream(), size);
            }
        }
        return this.requestEntityStream;
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
        return this.requestStream;
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getResponseEntityStream() {
        if (this.responseEntityStream == null) {
            if (isResponseChunked()) {
                this.responseEntityStream = new ChunkedOutputStream(
                        getResponseStream());
            } else {
                this.responseEntityStream = new KeepAliveOutputStream(
                        getResponseStream());
            }
        }
        return this.responseEntityStream;
    }

    private OutputStream getResponseStream() {
        return this.responseStream;
    }

    @Override
    protected boolean isServerKeepAlive() {
        return false;
    }

    @Override
    public void writeResponseHead(Response response) throws IOException {
        writeResponseHead(response, getResponseStream());
    }
}
