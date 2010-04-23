package org.restlet.engine.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

public class InboundWay extends Way {

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
            InputStream inboundEntityStream = getInboundEntityStream(
                    contentLength, chunkedEncoding);
            ReadableByteChannel inboundEntityChannel = getInboundEntityChannel(
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
                        if (getHelper().isTracing()) {
                            synchronized (System.out) {
                                System.out.println("\n");
                            }
                        }

                        super.release();
                        setInboundBusy(false);
                    }
                };
            } else if (inboundEntityChannel != null) {
                result = new ReadableRepresentation(inboundEntityChannel, null,
                        contentLength) {
                    @Override
                    public void release() {
                        super.release();
                        setInboundBusy(false);
                    }
                };
            }

            result.setSize(contentLength);
        } else {
            result = new EmptyRepresentation();

            // Mark the inbound as free so new messages can be read if possible
            setInboundBusy(false);
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

    // [ifndef gwt] method
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

    /**
     * Reads available bytes from the socket channel.
     * 
     * @return The number of bytes read.
     * @throws IOException
     */
    public int readBytes() throws IOException {
        getBuffer().clear();
        int result = getSocketChannel().read(getBuffer());
        getBuffer().flip();
        return result;
    }

    /**
     * Reads the next message received via the inbound stream or channel. Note
     * that the optional entity is not fully read.
     * 
     * @throws IOException
     */
    protected abstract void readMessage() throws IOException;

    /**
     * Reads the header lines of the current message received.
     * 
     * @throws IOException
     */
    protected abstract void readMessageHeaders() throws IOException;

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
     * Reads inbound messages from the socket. Only one message at a time if
     * pipelining isn't enabled.
     */
    public void readMessages() {
        try {
            if (canRead()) {
                int result = readBytes();

                while (getBuffer().hasRemaining()) {
                    readMessage();

                    if (!getBuffer().hasRemaining()) {
                        // Attempt to read more
                        result = readBytes();
                    }
                }

                if (result == -1) {
                    close(true);
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.INFO,
                            "Error while reading a message. Closing the connection.",
                            e);
            close(false);
        }
    }

    /**
     * Reads the start line of the current message received.
     * 
     * @throws IOException
     */
    protected abstract void readMessageStart() throws IOException;

}
