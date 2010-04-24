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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.restlet.Response;
import org.restlet.engine.io.NioUtils;

/**
 * A network connection way though which messages are either sent or received.
 * Messages can be either requests or responses.
 * 
 * @author Jerome Louvel
 */
public abstract class Way {

    /** The byte buffer. */
    private final ByteBuffer buffer;

    /** The line builder. */
    private final StringBuilder builder;

    /** The line builder index. */
    private volatile int builderIndex;

    /** The parent connection. */
    private final Connection<?> connection;

    /** The header index. */
    private volatile int headerIndex;

    /** The IO state. */
    private volatile WayIoState ioState;

    /** The current message read. */
    private volatile Response message;

    /** The queue of messages. */
    private final Queue<Response> messages;

    /** The message state. */
    private volatile WayMessageState messageState;

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public Way(Connection<?> connection) {
        this.buffer = ByteBuffer.allocate(NioUtils.BUFFER_SIZE);
        this.builder = new StringBuilder();
        this.connection = connection;
        this.messageState = WayMessageState.NONE;
        this.ioState = WayIoState.IDLE;
        this.message = null;
        this.messages = new ConcurrentLinkedQueue<Response>();
    }

    /**
     * Returns the byte buffer.
     * 
     * @return The byte buffer.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Returns the line builder.
     * 
     * @return The line builder.
     */
    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns the line builder index.
     * 
     * @return The line builder index.
     */
    public int getBuilderIndex() {
        return builderIndex;
    }

    /**
     * Returns the parent connection.
     * 
     * @return The parent connection.
     */
    public Connection<?> getConnection() {
        return connection;
    }

    /**
     * Returns the header index.
     * 
     * @return The header index.
     */
    public int getHeaderIndex() {
        return headerIndex;
    }

    /**
     * Returns the IO state.
     * 
     * @return The IO state.
     */
    public WayIoState getIoState() {
        return ioState;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return getConnection().getLogger();
    }

    /**
     * Returns the current message processed.
     * 
     * @return The current message processed.
     */
    public Response getMessage() {
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
    public WayMessageState getMessageState() {
        return messageState;
    }

    /**
     * Registers interest of this connection way for NIO operations with the
     * given selector. If called several times, it just update the selection key
     * with the new interest operations.
     * 
     * @param selector
     *            The selector to register with.
     */
    public abstract void registerInterest(Selector selector);

    public abstract void select(SelectionKey key);

    /**
     * Sets the line builder index.
     * 
     * @param builderIndex
     *            The line builder index.
     */
    public void setBuilderIndex(int builderIndex) {
        this.builderIndex = builderIndex;
    }

    /**
     * Sets the header index.
     * 
     * @param headerIndex
     *            The header index.
     */
    public void setHeaderIndex(int headerIndex) {
        this.headerIndex = headerIndex;
    }

    /**
     * Sets the IO state.
     * 
     * @param wayIoState
     *            The IO state.
     */
    public void setIoState(WayIoState wayIoState) {
        this.ioState = wayIoState;
    }

    /**
     * Sets the current message processed.
     * 
     * @param message
     *            The current message processed.
     */
    public void setMessage(Response message) {
        this.message = message;
    }

    /**
     * Sets the message state.
     * 
     * @param wayMessageState
     *            The message state.
     */
    public void setMessageState(WayMessageState wayMessageState) {
        this.messageState = wayMessageState;
    }

}
