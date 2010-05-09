/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * A network connection way though which messages are received. Messages can be
 * either requests or responses.
 * 
 * @author Jerome Louvel
 */
public class InboundWay extends Way {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public InboundWay(Connection<?> connection) {
        super(connection);
        // this.inboundChannel = new InboundStream(getSocket()
        // .getInputStream());

    }

    /**
     * Returns the message entity if available.
     * 
     * @param headers
     *            The headers to use.
     * @return The inbound message if available.
     */
    public Representation createEntity(Series<Parameter> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);
        boolean chunkedEncoding = HeaderUtils.isChunkedEncoding(headers);

        // In some cases there is an entity without a content-length header
        boolean connectionClosed = HeaderUtils.isConnectionClose(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding || connectionClosed) {
            InputStream inboundEntityStream = getEntityStream(contentLength,
                    chunkedEncoding);
            ReadableByteChannel inboundEntityChannel = getEntityChannel(
                    contentLength, chunkedEncoding);

            if (inboundEntityStream != null) {
                result = new InputRepresentation(inboundEntityStream, null,
                        contentLength) {

                    @Override
                    public String getText() throws IOException {
                        try {
                            return super.getText();
                        } catch (IOException ioe) {
                            throw ioe;
                        } finally {
                            release();
                        }
                    }

                    @Override
                    public void release() {
                        super.release();
                        setMessageState(MessageState.START_LINE);
                        setIoState(IoState.IDLE);
                    }
                };
            } else if (inboundEntityChannel != null) {
                result = new ReadableRepresentation(inboundEntityChannel, null,
                        contentLength) {
                    @Override
                    public void release() {
                        super.release();
                        setMessageState(MessageState.START_LINE);
                        setIoState(IoState.IDLE);
                    }
                };
            }

            result.setSize(contentLength);
        } else {
            result = new EmptyRepresentation();

            // Mark the inbound as free so new messages can be read if possible
            setMessageState(MessageState.START_LINE);
            setIoState(IoState.IDLE);
        }

        if (headers != null) {
            try {
                result = HeaderUtils.copyResponseEntityHeaders(headers, result);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING,
                        "Error while parsing entity headers", t);
            }
        }

        return result;
    }

    /**
     * Creates the representation wrapping the given stream.
     * 
     * @param stream
     *            The response input stream.
     * @return The wrapping representation.
     */
    protected Representation createRepresentation(InputStream stream) {
        return new InputRepresentation(stream, null);
    }

    /**
     * Returns the representation wrapping the given channel.
     * 
     * @param channel
     *            The response channel.
     * @return The wrapping representation.
     */
    protected Representation createRepresentation(
            java.nio.channels.ReadableByteChannel channel) {
        return new org.restlet.representation.ReadableRepresentation(channel,
                null);
    }

    /**
     * Returns the inbound message entity channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The inbound message entity channel if it exists.
     */
    public ReadableByteChannel getEntityChannel(long size, boolean chunked) {
        return null; // getSocketChannel();
    }

    /**
     * Returns the inbound message entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The inbound message entity stream if it exists.
     */
    public InputStream getEntityStream(long size, boolean chunked) {
        // InputStream result = null;
        //
        // if (chunked) {
        // result = new ChunkedInputStream(this, getInboundStream());
        // } else if (size >= 0) {
        // result = new SizedInputStream(this, getInboundStream(), size);
        // } else {
        // result = new ClosingInputStream(this, getInboundStream());
        // }
        //
        // return result;
        return null;
    }

    @Override
    public int getSocketInterestOps() {
        int result = 0;

        if (getIoState() == IoState.READ_INTEREST) {
            result = SelectionKey.OP_READ;
        }

        return result;
    }

    @Override
    public void onSelected() {
        try {
            if (true) { // canRead()) {
                int result = readBytes();

                while (getBuffer().hasRemaining()) {
                    readMessage();

                    if (!getBuffer().hasRemaining()) {
                        // Attempt to read more
                        result = readBytes();
                    }
                }

                if (result == -1) {
                    getConnection().close(true);
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.INFO,
                            "Error while reading a message. Closing the connection.",
                            e);
            getConnection().close(false);
        }
    }

    /**
     * Reads available bytes from the socket channel.
     * 
     * @return The number of bytes read.
     * @throws IOException
     */
    public int readBytes() throws IOException {
        getBuffer().clear();
        int result = getConnection().getSocketChannel().read(getBuffer());
        getBuffer().flip();
        return result;
    }

    /**
     * Reads the next message received via the inbound stream or channel. Note
     * that the optional entity is not fully read.
     * 
     * @throws IOException
     */
    public void readMessage() throws IOException {
        if (getMessageState() == null) {
            setMessageState(MessageState.START_LINE);
            getBuilder().delete(0, getBuilder().length());
        }

        while (getBuffer().hasRemaining()) {
            if (getMessageState() == MessageState.START_LINE) {
                readMessageStart();
            } else if (getMessageState() == MessageState.HEADERS) {
                readMessageHeaders();
            }
        }
    }

    /**
     * Reads a message header.
     * 
     * @return The new message header or null.
     * @throws IOException
     */
    protected Parameter readMessageHeader() throws IOException {
        Parameter header = HeaderReader.readHeader(getBuilder());
        getBuilder().delete(0, getBuilder().length());
        return header;
    }

    /**
     * Reads the header lines of the current message received.
     * 
     * @throws IOException
     */
    public void readMessageHeaders() throws IOException {
        if (readMessageLine()) {
            ConnectedRequest request = (ConnectedRequest) getMessage()
                    .getRequest();
            Series<Parameter> headers = request.getHeaders();
            Parameter header = readMessageHeader();

            while (header != null) {
                if (headers == null) {
                    headers = new Form();
                }

                headers.add(header);

                if (readMessageLine()) {
                    header = readMessageHeader();

                    // End of headers
                    if (header == null) {
                        // Check if the client wants to close the connection
                        if (HeaderUtils.isConnectionClose(headers)) {
                            getConnection().setState(ConnectionState.CLOSING);
                        }

                        // Check if an entity is available
                        Representation entity = createEntity(headers);

                        if (entity instanceof EmptyRepresentation) {
                            setMessageState(MessageState.START_LINE);
                        } else {
                            request.setEntity(entity);
                            setMessageState(MessageState.BODY);
                        }

                        // Update the response
                        // getMessage().getServerInfo().setAddress(
                        // getConnection().getHelper().getHelped()
                        // .getAddress());
                        // getMessage().getServerInfo().setPort(
                        // getConnection().getHelper().getHelped()
                        // .getPort());

                        if (request != null) {
                            if (request.isExpectingResponse()) {
                                // Add it to the connection queue
                                getMessages().add(getMessage());
                            }

                            // Add it to the helper queue
                            getHelper().getInboundMessages().add(getMessage());
                        }
                    }
                } else {
                    // Missing characters
                }
            }

            request.setHeaders(headers);
        }
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @return True if the message line was fully read.
     * @throws IOException
     */
    protected boolean readMessageLine() throws IOException {
        boolean result = false;
        int next;

        while (!result && getBuffer().hasRemaining()) {
            next = (int) getBuffer().get();

            if (HeaderUtils.isCarriageReturn(next)) {
                next = (int) getBuffer().get();

                if (HeaderUtils.isLineFeed(next)) {
                    result = true;
                } else {
                    throw new IOException(
                            "Missing carriage return character at the end of HTTP line");
                }
            } else {
                getBuilder().append((char) next);
            }
        }

        return result;
    }

    /**
     * Reads the start line of the current message received.
     * 
     * @throws IOException
     */
    public void readMessageStart() throws IOException {
        if (readMessageLine()) {
            String requestMethod = null;
            String requestUri = null;
            String version = null;

            int i = 0;
            int start = 0;
            int size = getBuilder().length();
            char next;

            if (size == 0) {
                // Skip leading empty lines per HTTP specification
            } else {
                // Parse the request method
                for (i = start; (requestMethod == null) && (i < size); i++) {
                    next = getBuilder().charAt(i);

                    if (HeaderUtils.isSpace(next)) {
                        requestMethod = getBuilder().substring(start, i);
                        start = i + 1;
                    }
                }

                if ((requestMethod == null) || (i == size)) {
                    throw new IOException(
                            "Unable to parse the request method. End of line reached too early.");
                }

                // Parse the request URI
                for (i = start; (requestUri == null) && (i < size); i++) {
                    next = getBuilder().charAt(i);

                    if (HeaderUtils.isSpace(next)) {
                        requestUri = getBuilder().substring(start, i);
                        start = i + 1;
                    }
                }

                if (i == size) {
                    throw new IOException(
                            "Unable to parse the request URI. End of line reached too early.");
                }

                if ((requestUri == null) || (requestUri.equals(""))) {
                    requestUri = "/";
                }

                // Parse the protocol version
                for (i = start; (version == null) && (i < size); i++) {
                    next = getBuilder().charAt(i);
                }

                if (i == size) {
                    version = getBuilder().substring(start, i);
                    start = i + 1;
                }

                if (version == null) {
                    throw new IOException(
                            "Unable to parse the protocol version. End of line reached too early.");
                }

                // ConnectedRequest request = getHelper().createRequest(
                // getConnection(), requestMethod, requestUri, version);
                // Response response = getHelper().createResponse(request);
                // setMessage(response);

                setMessageState(MessageState.HEADERS);
                getBuilder().delete(0, getBuilder().length());
            }
        } else {
            // We need more characters before parsing
        }
    }

}
