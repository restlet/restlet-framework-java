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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.restlet.Response;
import org.restlet.engine.io.NioUtils;

/**
 * A network connection way though which messages are either sent or received.
 * Messages can be either requests or responses.
 * 
 * @author Jerome Louvel
 */
public class ConnectionWay {

    /** The byte buffer. */
    private final ByteBuffer buffer;

    /** The line builder. */
    private final StringBuilder builder;

    /** The line builder index. */
    private volatile int builderIndex;

    /** The header index. */
    private volatile int headerIndex;

    /** The IO state. */
    private volatile IoState ioState;

    /** The current message read. */
    private volatile Response message;

    /** The queue of messages. */
    private final Queue<Response> messages;

    /** The message state. */
    private volatile MessageState messageState;

    /**
     * Constructor.
     */
    public ConnectionWay() {
        this.buffer = ByteBuffer.allocate(NioUtils.BUFFER_SIZE);
        this.builder = new StringBuilder();
        this.messageState = MessageState.NONE;
        this.ioState = IoState.IDLE;
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
     * Returns the header index.
     * 
     * @return The header index.
     */
    public int getHeaderIndex() {
        return headerIndex;
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
     * Returns the IO state.
     * 
     * @return The IO state.
     */
    public IoState getIoState() {
        return ioState;
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
    public MessageState getMessageState() {
        return messageState;
    }

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
     * @param ioState
     *            The IO state.
     */
    public void setIoState(IoState ioState) {
        this.ioState = ioState;
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
     * @param messageState
     *            The message state.
     */
    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

}
