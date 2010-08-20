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

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.Series;

/**
 * A network connection way though which messages are exchanged. Messages can be
 * either sent or received, requests or responses.
 * 
 * @author Jerome Louvel
 */
public abstract class Way {

    /** The message headers. */
    private volatile Series<Parameter> headers;

    /** The byte buffer. */
    private final ByteBuffer byteBuffer;

    /** The parent connection. */
    private final Connection<?> connection;

    /** The IO state. */
    private volatile IoState ioState;

    /** The line builder. */
    private final StringBuilder lineBuilder;

    /** The current message exchanged. */
    private volatile Response message;

    /** The queue of messages. */
    private final Queue<Response> messages;

    /** The message state. */
    private volatile MessageState messageState;

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public Way(Connection<?> connection) {
        this.byteBuffer = ByteBuffer.allocate(IoUtils.BUFFER_SIZE);
        this.headers = null;
        this.lineBuilder = new StringBuilder();
        this.connection = connection;
        this.messageState = MessageState.IDLE;
        this.ioState = IoState.IDLE;
        this.message = null;
        this.messages = new ConcurrentLinkedQueue<Response>();
    }

    /**
     * Returns the byte buffer.
     * 
     * @return The byte buffer.
     */
    protected ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /**
     * Returns the parent connection.
     * 
     * @return The parent connection.
     */
    protected Connection<?> getConnection() {
        return connection;
    }

    /**
     * Returns the entity channel, chunked if necessary.
     * 
     * @return The entity channel, chunked if necessary.
     */
    public WritableByteChannel getEntityChannel(boolean chunked) {
        return getConnection().getWritableSelectionChannel();
    }

    /**
     * Returns the response headers.
     * 
     * @return The response headers to be written.
     */
    public Series<Parameter> getHeaders() {
        return headers;
    }

    /**
     * Returns the parent connector helper.
     * 
     * @return The parent connector helper.
     */
    protected BaseHelper<?> getHelper() {
        return getConnection().getHelper();
    }

    /**
     * Returns the IO state.
     * 
     * @return The IO state.
     */
    protected IoState getIoState() {
        return ioState;
    }

    /**
     * Returns the line builder.
     * 
     * @return The line builder.
     */
    protected StringBuilder getLineBuilder() {
        return lineBuilder;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    protected Logger getLogger() {
        return getConnection().getLogger();
    }

    /**
     * Returns the current message processed.
     * 
     * @return The current message processed.
     */
    protected Response getMessage() {
        return message;
    }

    /**
     * Returns the queue of messages.
     * 
     * @return The queue of messages.
     */
    public Queue<Response> getMessages() {
        return messages;
    }

    /**
     * Returns the message state.
     * 
     * @return The message state.
     */
    protected MessageState getMessageState() {
        return messageState;
    }

    /**
     * Registers interest of this way for socket NIO operations.
     * 
     * @return The operations of interest.
     */
    protected abstract int getSocketInterestOps();

    /**
     * Indicates if we are filling the byte buffer.
     * 
     * @return True if we are filling the byte buffer.
     */
    protected boolean isFilling() {
        return (getMessageState() != MessageState.IDLE)
                && getByteBuffer().hasRemaining();
    }

    /**
     * Callback method invoked when the current message has been completely
     * received or sent.
     */
    protected void onCompleted() {
        setIoState(IoState.IDLE);
        setMessageState(MessageState.IDLE);
        setMessage(null);
    }

    /**
     * Callback method invoked when the way has been selected for IO operations
     * it registered interest in. By default it call
     * {@link Connection#onSelected()}.
     */
    public void onSelected() {
        if (getIoState() == IoState.INTEREST) {
            setIoState(IoState.PROCESSING);

            if (getMessageState() == MessageState.IDLE) {
                setMessageState(MessageState.START_LINE);
            }
        } else if (getIoState() == IoState.CANCELING) {
            setIoState(IoState.CANCELLED);
        }

        if (!getByteBuffer().hasRemaining()) {
            getByteBuffer().clear();
        }
    }

    /**
     * Registers interest of this connection way for NIO operations with the
     * given selector. If called several times, it just updates the selection
     * keys with the new interest operations. By default, it does nothing.
     * 
     * @param selector
     *            The selector to register with.
     * @throws ClosedChannelException
     */
    public void registerInterest(Selector selector) {
        updateState();
    }

    /**
     * Sets the response headers to be written.
     * 
     * @param headers
     *            The response headers.
     */
    public void setHeaders(Series<Parameter> headers) {
        this.headers = headers;
    }

    /**
     * Sets the IO state.
     * 
     * @param ioState
     *            The IO state.
     */
    protected void setIoState(IoState ioState) {
        this.ioState = ioState;
    }

    /**
     * Sets the current message processed.
     * 
     * @param message
     *            The current message processed.
     */
    protected void setMessage(Response message) {
        this.message = message;
    }

    /**
     * Sets the message state.
     * 
     * @param messageState
     *            The message state.
     */
    protected void setMessageState(MessageState messageState) {
        if (this.messageState != messageState) {
            this.messageState = messageState;

            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().finer(
                        "New message state: " + messageState + " ("
                                + hashCode() + ")");
            }
        }
    }

    @Override
    public String toString() {
        return getIoState() + ", " + getMessageState();
    }

    /**
     * Updates the way IO and message states.
     */
    public abstract void updateState();

}
