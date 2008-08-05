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
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.util.WrapperRepresentation;

import com.noelios.restlet.util.KeepAliveOutputStream;

/**
 * HTTP client call based on streams.
 * 
 * @author Jerome Louvel
 */
public class StreamClientCall extends HttpClientCall {

    /**
     * Wrapper representation to close the associated socket when the
     * representation is released
     */
    private static class SocketWrapperRepresentation extends
            WrapperRepresentation {

        private final Logger log;

        private final Socket socket;

        public SocketWrapperRepresentation(
                Representation wrappedRepresentation, Socket socket, Logger log) {
            super(wrappedRepresentation);
            this.socket = socket;
            this.log = log;
        }

        @Override
        public void release() {
            try {
                if (!this.socket.isClosed()) {
                    this.socket.shutdownOutput();
                    this.socket.close();
                }
            } catch (final IOException ex) {
                this.log.log(Level.WARNING,
                        "An error occured closing the client socket", ex);
            }

            super.release();
        }
    }

    /** The request entity output stream. */
    private volatile OutputStream requestEntityStream;

    /** The request output stream. */
    private volatile OutputStream requestStream;

    /** The response input stream. */
    private volatile InputStream responseStream;

    /** The request socket */
    private volatile Socket socket;

    /**
     * Constructor.
     * 
     * @param helper
     *            The client connector helper.
     * @param request
     *            The request to send.
     */
    public StreamClientCall(StreamClientHelper helper, Request request) {
        // The path of the request uri must not be empty.
        super(helper, request.getMethod().toString(), (request.getResourceRef()
                .getPath() == null) ? request.getResourceRef().getIdentifier()
                + "/" : request.getResourceRef().getIdentifier());
        // Set the HTTP version
        setVersion("HTTP/1.1");
    }

    /**
     * Creates the socket that will be used to send the request and get the
     * response.
     * 
     * @param hostDomain
     *            The target host domain name.
     * @param hostPort
     *            The target host port.
     * @return The created socket.
     * @throws UnknownHostException
     * @throws IOException
     */
    public Socket createSocket(String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        return new Socket(hostDomain, hostPort);
    }

    @Override
    protected Representation getRepresentation(InputStream stream) {
        final Representation result = super.getRepresentation(stream);
        return new SocketWrapperRepresentation(result, this.socket, getHelper()
                .getLogger());
    }

    @Override
    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getRequestEntityStream() {
        if (this.requestEntityStream == null) {
            if (isRequestChunked() && isKeepAlive()) {
                this.requestEntityStream = new ChunkedOutputStream(
                        new KeepAliveOutputStream(getRequestHeadStream()));
            } else if (isRequestChunked()) {
                this.requestEntityStream = new ChunkedOutputStream(
                        getRequestHeadStream());
            } else if (isKeepAlive()) {
                this.requestEntityStream = new KeepAliveOutputStream(
                        getRequestHeadStream());
            } else {
                this.requestEntityStream = new KeepAliveOutputStream(
                        getRequestHeadStream());
            }
        }

        return this.requestEntityStream;
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return this.requestStream;
    }

    @Override
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        if (isResponseChunked()) {
            return new ChunkedInputStream(getResponseStream());
        } else if (size >= 0) {
            return new InputEntityStream(getResponseStream(), size);
        } else {
            return getResponseStream();
        }
    }

    /**
     * Returns the underlying HTTP response stream.
     * 
     * @return The underlying HTTP response stream.
     */
    private InputStream getResponseStream() {
        return this.responseStream;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return false;
    }

    /**
     * Parses the HTTP response.
     * 
     * @throws IOException
     */
    protected void parseResponse() throws IOException {
        final StringBuilder sb = new StringBuilder();

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
            final String hostDomain = request.getResourceRef().getHostDomain();
            int hostPort = request.getResourceRef().getHostPort();
            if (hostPort == -1) {
                if (request.getResourceRef().getSchemeProtocol() != null) {
                    hostPort = request.getResourceRef().getSchemeProtocol()
                            .getDefaultPort();
                } else {
                    hostPort = getProtocol().getDefaultPort();
                }
            }

            // Create the client socket
            this.socket = createSocket(hostDomain, hostPort);
            this.requestStream = this.socket.getOutputStream();
            this.responseStream = this.socket.getInputStream();

            // Write the request line
            getRequestHeadStream().write(getMethod().getBytes());
            getRequestHeadStream().write(' ');
            getRequestHeadStream().write(getRequestUri().getBytes());
            getRequestHeadStream().write(' ');
            getRequestHeadStream().write(getVersion().getBytes());
            HttpUtils.writeCRLF(getRequestHeadStream());

            if (shouldRequestBeChunked(request)) {
                getRequestHeaders().set(HttpConstants.HEADER_TRANSFER_ENCODING,
                        "chunked", true);
            }

            // We don't support persistent connections yet
            getRequestHeaders().set(HttpConstants.HEADER_CONNECTION, "close",
                    isClientKeepAlive());

            // Prepare the host header
            String host = hostDomain;
            if (request.getResourceRef().getHostPort() != -1) {
                host += ":" + request.getResourceRef().getHostPort();
            }
            getRequestHeaders().set(HttpConstants.HEADER_HOST, host, true);

            // Write the request headers
            for (final Parameter header : getRequestHeaders()) {
                HttpUtils.writeHeader(header, getRequestHeadStream());
            }

            // Write the end of the headers section
            HttpUtils.writeCRLF(getRequestHeadStream());

            // Write the request body
            result = super.sendRequest(request);

            if (result.equals(Status.CONNECTOR_ERROR_COMMUNICATION)) {
                return result;
            }

            // Parse the response
            parseResponse();

            // Build the result
            result = new Status(getStatusCode(), null, getReasonPhrase(), null);
        } catch (final IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.WARNING,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);
        }

        return result;
    }
}
