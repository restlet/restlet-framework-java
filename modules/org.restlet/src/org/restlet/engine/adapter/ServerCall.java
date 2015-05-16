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
import java.io.PushbackInputStream;
import java.security.cert.Certificate;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Digest;
import org.restlet.data.Header;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.header.ContentType;
import org.restlet.engine.header.DispositionReader;
import org.restlet.engine.header.EncodingReader;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.header.LanguageReader;
import org.restlet.engine.header.RangeReader;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;

/**
 * Abstract HTTP server connector call.
 * 
 * @author Jerome Louvel
 */
public abstract class ServerCall extends Call {

    /** Indicates if the "host" header was already parsed. */
    private volatile boolean hostParsed;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server connector.
     */
    public ServerCall(Server server) {
        this((server == null) ? null : server.getAddress(),
                (server == null) ? 0 : server.getPort());
    }

    /**
     * Constructor.
     * 
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server port.
     */
    public ServerCall(String serverAddress, int serverPort) {
        setServerAddress(serverAddress);
        setServerPort(serverPort);
        this.hostParsed = false;
    }

    /**
     * Ask the connector to abort the related network connection, for example
     * immediately closing the socket.
     * 
     * @return True if the connection was aborted.
     */
    public abstract boolean abort();

    /**
     * Complete the response
     */
    public void complete() {

    }

    /**
     * Flushes the buffers onto the network so that for example you can force
     * headers to be written before the entity is becoming available.
     * 
     * @throws IOException
     */
    public void flushBuffers() throws IOException {
    }

    /**
     * Returns the chain of client SSL certificates, if available and
     * accessible.
     * 
     * @return The chain of client SSL certificates, if available and
     *         accessible.
     */
    public List<Certificate> getCertificates() {
        return null;
    }

    /**
     * Returns the SSL Cipher Suite, if available and accessible.
     * 
     * @return The SSL Cipher Suite, if available and accessible.
     */
    public String getCipherSuite() {
        return null;
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    protected long getContentLength() {
        return HeaderUtils.getContentLength(getRequestHeaders());
    }

    /**
     * Returns the host domain name.
     * 
     * @return The host domain name.
     */
    @Override
    public String getHostDomain() {
        if (!this.hostParsed) {
            parseHost();
        }
        return super.getHostDomain();
    }

    /**
     * Returns the host port.
     * 
     * @return The host port.
     */
    @Override
    public int getHostPort() {
        if (!this.hostParsed) {
            parseHost();
        }
        return super.getHostPort();
    }

    /**
     * Returns the request entity if available.
     * 
     * @return The request entity if available.
     */
    public Representation getRequestEntity() {
        Representation result = null;
        long contentLength = getContentLength();
        boolean chunkedEncoding = HeaderUtils
                .isChunkedEncoding(getRequestHeaders());
        // In some cases there is an entity without a content-length header
        boolean connectionClosed = HeaderUtils
                .isConnectionClose(getRequestHeaders());

        // Create the representation
        if (((contentLength != Representation.UNKNOWN_SIZE) && (contentLength != 0))
                || chunkedEncoding || connectionClosed) {
            // Create the result representation
            InputStream requestStream = getRequestEntityStream(contentLength);

            if (connectionClosed) {
                // We need to detect if there is really an entity or not as only
                // the end of connection can let us know at this point
                PushbackInputStream pbi = new PushbackInputStream(requestStream);

                try {
                    int next = pbi.read();

                    if (next != -1) {
                        pbi.unread(next);
                        requestStream = pbi;
                    } else {
                        requestStream = null;
                    }
                } catch (IOException e) {
                    getLogger().fine("Unable to read request entity");
                }
            }

            if (requestStream != null) {
                result = new InputRepresentation(requestStream, null,
                        contentLength);
            } else {
                result = new EmptyRepresentation();
            }

            result.setSize(contentLength);
        } else {
            result = new EmptyRepresentation();
        }

        // Extract some interesting header values
        for (Header header : getRequestHeaders()) {
            if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_ENCODING)) {
                new EncodingReader(header.getValue()).addValues(result
                        .getEncodings());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_LANGUAGE)) {
                new LanguageReader(header.getValue()).addValues(result
                        .getLanguages());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_TYPE)) {
                ContentType contentType = new ContentType(header.getValue());
                result.setMediaType(contentType.getMediaType());
                result.setCharacterSet(contentType.getCharacterSet());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_RANGE)) {
                RangeReader.update(header.getValue(), result);
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_MD5)) {
                result.setDigest(new Digest(Digest.ALGORITHM_MD5, Base64
                        .decode(header.getValue())));
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_DISPOSITION)) {
                try {
                    result.setDisposition(new DispositionReader(header
                            .getValue()).readValue());
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Error during Content-Disposition header parsing. Header: "
                                    + header.getValue(), ioe);
                }
            }
        }

        return result;
    }

    /**
     * Returns the request entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract InputStream getRequestEntityStream(long size);

    /**
     * Returns the request head stream if it exists.
     * 
     * @return The request head stream if it exists.
     */
    public abstract InputStream getRequestHeadStream();

    /**
     * Returns the response entity stream if it exists.
     * 
     * @return The response entity stream if it exists.
     */
    public abstract OutputStream getResponseEntityStream();

    /**
     * Returns the SSL key size, if available and accessible.
     * 
     * @return The SSL key size, if available and accessible.
     */
    public Integer getSslKeySize() {
        return null;
    }

    /**
     * Returns the SSL session ID, in hexadecimal encoding, if available and
     * accessible.
     * 
     * @return The SSL session ID, in hexadecimal encoding, if available and
     *         accessible.
     */
    public String getSslSessionId() {
        byte[] byteArray = getSslSessionIdBytes();

        if (byteArray != null) {
            return IoUtils.toHexString(byteArray);
        } else {
            return null;
        }
    }

    /**
     * Returns the SSL session ID, as a byte array, if available and accessible
     * in that format (to be used by getSslSessionId).
     * 
     * @return The SSL session ID, as a byte array, if available and accessible
     *         in that format.
     */
    protected byte[] getSslSessionIdBytes() {
        return null;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return !HeaderUtils.isConnectionClose(getRequestHeaders());
    }

    @Override
    protected boolean isServerKeepAlive() {
        return true;
    }

    /**
     * Parses the "host" header to set the server host and port properties.
     */
    private void parseHost() {
        String host = getRequestHeaders().getFirstValue(
                HeaderConstants.HEADER_HOST, true);

        if (host != null) {
            // Take care of IPV6 addresses
            int colonIndex = host.indexOf(':', host.indexOf(']'));

            if (colonIndex != -1) {
                super.setHostDomain(host.substring(0, colonIndex));
                super.setHostPort(Integer.valueOf(host
                        .substring(colonIndex + 1)));
            } else {
                super.setHostDomain(host);
                super.setHostPort(getProtocol().getDefaultPort());
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
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = headStream.read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request method. End of stream reached too early.");
        }

        setMethod(sb.toString());
        sb.delete(0, sb.length());

        // Parse the request URI
        next = headStream.read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = headStream.read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request URI. End of stream reached too early.");
        }
        setRequestUri(sb.toString());
        sb.delete(0, sb.length());

        // Parse the HTTP version
        next = headStream.read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = headStream.read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the HTTP version. End of stream reached too early.");
        }
        next = headStream.read();

        if (HeaderUtils.isLineFeed(next)) {
            setVersion(sb.toString());
            sb.delete(0, sb.length());

            // Parse the headers
            Header header = HeaderReader.readHeader(headStream, sb);

            while (header != null) {
                getRequestHeaders().add(header);
                header = HeaderReader.readHeader(headStream, sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the HTTP version. The carriage return must be followed by a line feed.");
        }
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them over the network. The default
     * implementation only writes the response entity on the response stream or
     * channel. Subclasses will probably also copy the response headers and
     * status.
     * 
     * @param response
     *            The high-level response.
     * @throws IOException
     *             if the Response could not be written to the network.
     */
    public void sendResponse(Response response) throws IOException {
        if (response != null) {
            // Get the connector service to callback
            Representation responseEntity = response.getEntity();
            ConnectorService connectorService = ConnectorHelper
                    .getConnectorService();

            if (connectorService != null) {
                connectorService.beforeSend(responseEntity);
            }

            OutputStream responseEntityStream = null;
            try {
                writeResponseHead(response);

                if (responseEntity != null) {

                    responseEntityStream = getResponseEntityStream();
                    writeResponseBody(responseEntity, responseEntityStream);

                }
            } finally {
                if (responseEntityStream != null) {
                    try {
                        responseEntityStream.flush();
                        responseEntityStream.close();
                    } catch (IOException ioe) {
                        // The stream was probably already closed by the
                        // connector. Probably OK, low message priority.
                        getLogger()
                                .log(Level.FINE,
                                        "Exception while flushing and closing the entity stream.",
                                        ioe);
                    }
                }
                if (responseEntity != null) {
                    responseEntity.release();
                }

                if (connectorService != null) {
                    connectorService.afterSend(responseEntity);
                }
            }
        }
    }

    /**
     * Indicates if the response should be chunked because its length is
     * unknown.
     * 
     * @param response
     *            The response to analyze.
     * @return True if the response should be chunked.
     */
    public boolean shouldResponseBeChunked(Response response) {
        return (response.getEntity() != null)
                && !response.getEntity().hasKnownSize();
    }

    /**
     * Effectively writes the response body. The entity to write is guaranteed
     * to be non null. Attempts to write the entity on the response channel or
     * response stream by default.
     * 
     * @param entity
     *            The representation to write as entity of the body.
     * @param responseEntityStream
     *            The response entity stream or null if a channel is used.
     * @throws IOException
     */
    protected void writeResponseBody(Representation entity,
            OutputStream responseEntityStream) throws IOException {
        // Send the entity to the client
        if (responseEntityStream != null) {
            entity.write(responseEntityStream);
            responseEntityStream.flush();
        }
    }

    /**
     * Writes the response status line and headers. Does nothing by default.
     * 
     * @param response
     *            The response.
     * @throws IOException
     */
    protected void writeResponseHead(Response response) throws IOException {
        // Do nothing by default
    }

    /**
     * Writes the response head to the given output stream.
     * 
     * @param response
     *            The response.
     * @param headStream
     *            The output stream to write to.
     * @throws IOException
     */
    protected void writeResponseHead(Response response, OutputStream headStream)
            throws IOException {
        // Write the status line
        String version = (getVersion() == null) ? "1.1" : getVersion();
        headStream.write(StringUtils.getAsciiBytes(version));
        headStream.write(' ');
        headStream.write(StringUtils.getAsciiBytes(Integer
                .toString(getStatusCode())));
        headStream.write(' ');

        if (getReasonPhrase() != null) {
            headStream.write(StringUtils.getLatin1Bytes(getReasonPhrase()));
        } else {
            headStream.write(StringUtils
                    .getAsciiBytes(("Status " + getStatusCode())));
        }

        headStream.write(13); // CR
        headStream.write(10); // LF

        // We don't support persistent connections yet
        getResponseHeaders().set(HeaderConstants.HEADER_CONNECTION, "close",
                true);

        // Check if 'Transfer-Encoding' header should be set
        if (shouldResponseBeChunked(response)) {
            getResponseHeaders().add(HeaderConstants.HEADER_TRANSFER_ENCODING,
                    "chunked");
        }

        // Write the response headers
        for (Header header : getResponseHeaders()) {
            HeaderUtils.writeHeaderLine(header, headStream);
        }

        // Write the end of the headers section
        headStream.write(13); // CR
        headStream.write(10); // LF
        headStream.flush();
    }
}
