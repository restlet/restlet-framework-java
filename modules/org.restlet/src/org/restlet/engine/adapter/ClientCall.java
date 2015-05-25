/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Low-level HTTP client call.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientCall extends Call {

    /**
     * Returns the local IP address or 127.0.0.1 if the resolution fails.
     * 
     * @return The local IP address or 127.0.0.1 if the resolution fails.
     */
    public static String getLocalAddress() {
        // [ifndef gae,gwt]
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            // [enddef]
            return "127.0.0.1";
            // [ifndef gae,gwt]
        }
        // [enddef]
    }

    /** The parent HTTP client helper. */
    private volatile HttpClientHelper helper;

    /**
     * Constructor setting the request address to the local host.
     * 
     * @param helper
     *            The parent HTTP client helper.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     */
    public ClientCall(HttpClientHelper helper, String method, String requestUri) {
        this.helper = helper;
        setMethod(method);
        setRequestUri(requestUri);
        setClientAddress(getLocalAddress());
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    protected long getContentLength() {
        return HeaderUtils.getContentLength(getResponseHeaders());
    }

    /**
     * Returns the HTTP client helper.
     * 
     * @return The HTTP client helper.
     */
    public HttpClientHelper getHelper() {
        return this.helper;
    }

    // [ifndef gwt] member
    /**
     * Returns the request entity channel if it exists.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract java.nio.channels.WritableByteChannel getRequestEntityChannel();

    // [ifndef gwt] member
    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract OutputStream getRequestEntityStream();

    // [ifdef gwt] member uncomment
    // /**
    // * Returns the request entity string if it exists.
    // *
    // * @return The request entity string if it exists.
    // */
    // public abstract String getRequestEntityString();

    // [ifndef gwt] member
    /**
     * Returns the request head stream if it exists.
     * 
     * @return The request head stream if it exists.
     */
    public abstract OutputStream getRequestHeadStream();

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * @param response
     *            the Response to get the entity from
     * @return The response entity if available.
     */
    public Representation getResponseEntity(Response response) {
        Representation result = null;
        // boolean available = false;
        long size = Representation.UNKNOWN_SIZE;

        // Compute the content length
        Series<Header> responseHeaders = getResponseHeaders();
        String transferEncoding = responseHeaders.getFirstValue(
                HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        if ((transferEncoding != null)
                && !Encoding.IDENTITY.getName().equalsIgnoreCase(
                        transferEncoding)) {
            size = Representation.UNKNOWN_SIZE;
        } else {
            size = getContentLength();
        }

        if (!getMethod().equals(Method.HEAD.getName())
                && !response.getStatus().isInformational()
                && !response.getStatus()
                        .equals(Status.REDIRECTION_NOT_MODIFIED)
                && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                && !response.getStatus().equals(Status.SUCCESS_RESET_CONTENT)) {
            // Make sure that an InputRepresentation will not be instantiated
            // while the stream is closed.
            InputStream stream = getUnClosedResponseEntityStream(getResponseEntityStream(size));
            // [ifndef gwt] line
            java.nio.channels.ReadableByteChannel channel = getResponseEntityChannel(size);
            // [ifdef gwt] line uncomment
            // InputStream channel = null;

            if (stream != null) {
                result = getRepresentation(stream);
            } else if (channel != null) {
                result = getRepresentation(channel);
            }
        }

        if (result != null) {
            result.setSize(size);

            // Informs that the size has not been specified in the header.
            if (size == Representation.UNKNOWN_SIZE) {
                getLogger()
                        .fine("The length of the message body is unknown. The entity must be handled carefully and consumed entirely in order to surely release the connection.");
            }
        }
        result = HeaderUtils.extractEntityHeaders(responseHeaders, result);

        return result;
    }

    // [ifndef gwt] member
    /**
     * Returns the response channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * @return The response channel if it exists.
     */
    public abstract java.nio.channels.ReadableByteChannel getResponseEntityChannel(
            long size);

    /**
     * Returns the response entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * @return The response entity stream if it exists.
     */
    public abstract InputStream getResponseEntityStream(long size);

    /**
     * Checks if the given input stream really contains bytes to be read. If so,
     * returns the inputStream otherwise returns null.
     * 
     * @param inputStream
     *            the inputStream to check.
     * @return null if the given inputStream does not contain any byte, an
     *         inputStream otherwise.
     */
    private InputStream getUnClosedResponseEntityStream(InputStream inputStream) {
        InputStream result = null;

        if (inputStream != null) {
            try {
                if (inputStream.available() > 0) {
                    result = inputStream;
                    // [ifndef gwt]
                } else {
                    java.io.PushbackInputStream is = new java.io.PushbackInputStream(
                            inputStream);
                    int i = is.read();

                    if (i >= 0) {
                        is.unread(i);
                        result = is;
                    }
                    // [enddef]
                }
            } catch (IOException ioe) {
                getLogger().log(Level.FINER, "End of response entity stream.",
                        ioe);
            }

        }

        return result;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return true;
    }

    @Override
    protected boolean isServerKeepAlive() {
        return !HeaderUtils.isConnectionClose(getResponseHeaders());
    }

    // [ifndef gwt] method
    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     * @return the status of the communication
     */
    public Status sendRequest(Request request) {
        Status result = null;
        Representation entity = request.isEntityAvailable() ? request
                .getEntity() : null;

        // Get the connector service to callback
        org.restlet.service.ConnectorService connectorService = ConnectorHelper
                .getConnectorService();
        if (connectorService != null) {
            connectorService.beforeSend(entity);
        }

        try {
            if (entity != null) {

                // In order to workaround bug #6472250
                // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472250),
                // it is very important to reuse that exact same "requestStream"
                // reference when manipulating the request stream, otherwise
                // "insufficient data sent" exceptions will occur in
                // "fixedLengthMode"
                OutputStream requestStream = getRequestEntityStream();
                java.nio.channels.WritableByteChannel requestChannel = getRequestEntityChannel();

                if (requestChannel != null) {
                    entity.write(requestChannel);
                    requestChannel.close();
                } else if (requestStream != null) {
                    entity.write(requestStream);
                    requestStream.flush();
                    requestStream.close();
                }
            }

            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(Level.FINE,
                            "An error occurred during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);
        } finally {
            if (entity != null) {
                entity.release();
            }

            // Call-back after writing
            if (connectorService != null) {
                connectorService.afterSend(entity);
            }
        }

        return result;
    }

    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     * @param response
     *            The high-level response.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public void sendRequest(Request request, Response response,
            org.restlet.Uniform callback) throws Exception {
        Context.getCurrentLogger().warning(
                "Currently callbacks are not available for this connector.");
    }

    /**
     * Indicates if the request entity should be chunked.
     * 
     * @return True if the request should be chunked
     */
    protected boolean shouldRequestBeChunked(Request request) {
        return request.isEntityAvailable() && (request.getEntity() != null)
                && !request.getEntity().hasKnownSize();
    }
}
