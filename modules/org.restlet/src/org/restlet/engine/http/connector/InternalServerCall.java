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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.restlet.Server;

/**
 * HTTP server call based on streams.
 * 
 * @author Jerome Louvel
 */
public class InternalServerCall {

    /** The request entity stream */
    private volatile InputStream requestEntityStream;

    /** The response entity output stream. */
    private volatile OutputStream responseEntityStream;

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
    public InternalServerCall(Server server, InputStream requestStream,
            OutputStream responseStream, Socket socket) {
        this.responseEntityStream = null;
        this.requestEntityStream = null;

//        try {
//            readRequestHead(getRequestHeadStream());
//        } catch (IOException ioe) {
//            getLogger().log(Level.WARNING, "Unable to parse the HTTP request",
//                    ioe);
//        }
    }

    public void complete() {
//        try {
//            if (!this.socket.isClosed()) {
//                // Exhaust the input stream before closing in case
//                // the client is still writing to it
//                BioUtils.exhaust(getRequestEntityStream(getContentLength()));
//                if (!(this.socket instanceof SSLSocket)) {
//                    this.socket.shutdownInput();
//                }
//
//                // Flush the output stream
//                this.socket.getOutputStream().flush();
//                if (!(this.socket instanceof SSLSocket)) {
//                    this.socket.shutdownOutput();
//                }
//            }
//        } catch (IOException ex) {
//            getLogger().log(Level.WARNING, "Unable to shutdown server socket",
//                    ex);
//        }
//        try {
//            if (!this.socket.isClosed()) {
//                // As we don't support persistent connections,
//                // we must call this method to make sure sockets
//                // are properly released.
//                this.socket.close();
//            }
//        } catch (IOException ex) {
//            getLogger().log(Level.WARNING, "Unable to close server socket", ex);
//        }
    }

    public InputStream getRequestEntityStream(long size) {
//        if (this.requestEntityStream == null) {
//            if (isRequestChunked()) {
//                this.requestEntityStream = new ChunkedInputStream(
//                        getInboundStream());
//            } else {
//                this.requestEntityStream = new InputEntityStream(
//                        getInboundStream(), size);
//            }
//        }
        return this.requestEntityStream;
    }

    public OutputStream getResponseEntityStream() {
//        if (this.responseEntityStream == null) {
//            this.responseEntityStream = getOutboundStream();
//            if (isKeepAlive()) {
//                this.responseEntityStream = new KeepAliveOutputStream(
//                        this.responseEntityStream);
//            }
//            if (isResponseChunked()) {
//                this.responseEntityStream = new ChunkedOutputStream(
//                        this.responseEntityStream);
//            }
//        }
        return this.responseEntityStream;
    }
}
