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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.http.io.ChunkedInputStream;
import org.restlet.engine.http.io.ChunkedOutputStream;
import org.restlet.engine.http.io.InboundStream;
import org.restlet.engine.http.io.InputEntityStream;
import org.restlet.engine.http.io.OutboundStream;
import org.restlet.util.Series;

/**
 * An internal HTTP server connection.
 * 
 * @author Jerome Louvel
 */
public class BaseServerConnection extends ServerConnection {

    /** The inbound stream. */
    private final InputStream inboundStream;

    /** The outbound stream. */
    private final OutputStream outboundStream;

    /** Indicates if idempotent sequences of requests should be pipelined. */
    private volatile boolean pipelining;

    /** Queue of inbound requests. */
    private final Queue<Request> inboundRequests;

    /** Queue of outbound response. */
    private final Queue<Response> outboundResponses;

    /**
     * Constructor.
     * 
     * @param helper
     * @param socket
     * @throws IOException
     */
    public BaseServerConnection(BaseServerHelper helper, Socket socket)
            throws IOException {
        super(helper, socket);
        this.inboundStream = new InboundStream(socket.getInputStream());
        this.outboundStream = new OutboundStream(socket.getOutputStream());
        this.pipelining = false;
        this.inboundRequests = new ConcurrentLinkedQueue<Request>();
        this.outboundResponses = new ConcurrentLinkedQueue<Response>();
        setPersistent(true);
    }

    @Override
    public void close(boolean graceful) {
        super.close(graceful);
    }

    @Override
    public void commit(Response response) {
        getHelper().getPendingResponses().add(response);
    }

    /**
     * Controls the connection for actions that needs to be done, such as
     * reading more requests or writing pending responses.
     */
    public void control() {
        if (!getHandlerService().isShutdown()) {
            try {
                // Attempts to read requests
                if (!isInboundBusy()
                        && (getInboundRequests().isEmpty() || isPipelining())) {
                    getHandlerService().execute(new Runnable() {
                        public void run() {
                            readRequests();
                        }
                    });
                }

                // Attempts to write responses
                if (!isOutboundBusy()) {
                    getHandlerService().execute(new Runnable() {
                        public void run() {
                            writeResponses();
                        }
                    });
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING,
                        "Error while controlling an HTTP connection: ",
                        e.getMessage());
                getLogger().log(Level.INFO,
                        "Error while controlling an HTTP connection", e);
            }
        }
    }

    /**
     * Returns the connection handler service.
     * 
     * @return The connection handler service.
     */
    protected ExecutorService getHandlerService() {
        return getHelper().getHandlerService();
    }

    @Override
    public BaseServerHelper getHelper() {
        return (BaseServerHelper) super.getHelper();
    }

    public Queue<Request> getInboundRequests() {
        return inboundRequests;
    }

    @Override
    public InputStream getInboundStream() {
        return this.inboundStream;
    }

    public Queue<Response> getOutboundResponses() {
        return outboundResponses;
    }

    @Override
    public OutputStream getOutboundStream() {
        return this.outboundStream;
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size,
            boolean chunked) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size, boolean chunked) {
        InputStream result = null;

        if (chunked) {
            result = new ChunkedInputStream(getInboundStream());
        } else {
            result = new InputEntityStream(getInboundStream(), size);
        }

        return result;
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        return null;
    }

    @Override
    public InputStream getRequestHeadStream() {
        return getInboundStream();
    }

    @Override
    public WritableByteChannel getResponseEntityChannel(boolean chunked) {
        return null;
    }

    @Override
    public OutputStream getResponseEntityStream(boolean chunked) {
        OutputStream result = getOutboundStream();

        if (chunked) {
            result = new ChunkedOutputStream(result);
        }

        return result;
    }

    public boolean isPipelining() {
        return pipelining;
    }

    @Override
    public void open() {
        super.open();

        if (!getHandlerService().isShutdown()) {
            try {
                getHandlerService().execute(new Runnable() {
                    public void run() {
                        readRequests();
                    }
                });
            } catch (Exception e) {
                getLogger().log(Level.WARNING,
                        "Error while handling an HTTP server call: ",
                        e.getMessage());
                getLogger().log(Level.INFO,
                        "Error while handling an HTTP server call", e);
            }
        }
    }

    /**
     * Reads the next requests. Only one request at a time if pipelining isn't
     * enabled.
     */
    public void readRequests() {
        try {
            if (isPipelining()) {
                // TODO
                // boolean idempotentSequence = true;
            } else if (!isInboundBusy()) {
                // Ensure that no request is pending for this connection
                if (getInboundRequests().size() == 0) {
                    // Read the request on the socket
                    ConnectedRequest request = readRequest();

                    if (request != null) {
                        // Add it to the connection queue
                        getInboundRequests().add(request);

                        // Add it to the helper queue
                        getHelper().getPendingRequests().add(request);
                    }
                }
            }

            // Offer some workforce to the helper
            getHelper().control();
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error while reading an HTTP request: ", e.getMessage());
            getLogger().log(Level.INFO, "Error while reading an HTTP request",
                    e);
        }
    }

    public void setPipelining(boolean pipelining) {
        this.pipelining = pipelining;
    }

    @Override
    public void writeResponseHead(Response response, Series<Parameter> headers)
            throws IOException {
        writeResponseHead(response, getOutboundStream(), headers);
    }

    /**
     * Writes the next responses. Only one response at a time if pipelining
     * isn't enabled.
     */
    public void writeResponses() {
        try {
            if (isPipelining()) {
                // TODO
            } else {
                if (getOutboundResponses().size() > 0) {
                    Response nextResponse = getOutboundResponses().poll();
                    writeResponse(nextResponse);
                }
            }

            // Check if some new requests can be read
            readRequests();
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error while writing an HTTP response: ", e.getMessage());
            getLogger().log(Level.INFO, "Error while writing an HTTP response",
                    e);
        }
    }

}
