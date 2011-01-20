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

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.restlet.Context;
import org.restlet.engine.header.HeaderUtils;

/**
 * Wrapper around a byte buffer and its state.
 * 
 * @author Jerome Louvel
 */
public class IoBuffer {

    /** The byte buffer. */
    private final ByteBuffer bytes;

    /** The byte buffer IO state. */
    private volatile BufferState state;

    /**
     * Constructor.
     * 
     * @param byteBuffer
     *            The byte buffer wrapped.
     */
    public IoBuffer(ByteBuffer byteBuffer) {
        this(byteBuffer, BufferState.FILLING);
    }

    /**
     * Constructor.
     * 
     * @param byteBuffer
     *            The byte buffer wrapped.
     * @param byteBufferState
     *            The initial byte buffer state.
     */
    public IoBuffer(ByteBuffer byteBuffer, BufferState byteBufferState) {
        super();
        this.bytes = byteBuffer;
        this.state = byteBufferState;
    }

    /**
     * Indicates if more bytes can be drained.
     * 
     * @return True if more bytes can be drained.
     */
    public boolean canDrain() {
        return (getState() == BufferState.DRAINING)
                && (getBytes().remaining() > 0);
    }

    /**
     * Indicates if more bytes can be filled in.
     * 
     * @return True if more bytes can be filled in.
     */
    public boolean canFill() {
        return (getState() == BufferState.FILLING)
                && (getBytes().hasRemaining());
    }

    /**
     * 
     * @param lastRead
     * @param targetBuffer
     * @return
     */
    public boolean canRetry(int lastRead, ByteBuffer targetBuffer) {
        return (lastRead > 0) && targetBuffer.hasRemaining();
    }

    /**
     * Recycles the buffer so it can be reused.
     */
    public void clear() {
        this.bytes.clear();
        this.state = BufferState.FILLING;
    }

    /**
     * Drains the byte buffer byte copying as many bytes as possible to the
     * target buffer, with no modification.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes added to the target buffer.
     */
    public int drain(ByteBuffer targetBuffer) {
        int result = 0;

        if (getBytes().remaining() >= targetBuffer.remaining()) {
            // Target buffer will be full
            result = targetBuffer.remaining();
        } else {
            // Target buffer will not be full
            result = getBytes().remaining();
        }

        // Copy the byte to the target buffer
        for (int i = 0; i < result; i++) {
            targetBuffer.put(getBytes().get());
        }

        return result;
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @param lineBuilder
     *            The line builder to fill.
     * @param builderState
     *            The builder state.
     * @param byteBuffer
     *            The byte buffer to read from.
     * @param completionListener
     * @return The new builder state.
     * @throws IOException
     */
    public BufferState fillLine(StringBuilder lineBuilder,
            BufferState builderState) throws IOException {
        int next;

        if (getState() == BufferState.DRAINING) {
            if (builderState == BufferState.IDLE) {
                builderState = BufferState.FILLING;
            }

            while ((builderState != BufferState.DRAINING)
                    && getBytes().hasRemaining()) {
                next = (int) getBytes().get();

                switch (builderState) {
                case FILLING:
                    if (HeaderUtils.isCarriageReturn(next)) {
                        builderState = BufferState.FILLED;
                    } else {
                        lineBuilder.append((char) next);
                    }

                    break;

                case FILLED:
                    if (HeaderUtils.isLineFeed(next)) {
                        builderState = BufferState.DRAINING;
                    } else {
                        throw new IOException(
                                "Missing line feed character at the end of the line. Found character \""
                                        + (char) next + "\" (" + next
                                        + ") instead");
                    }

                    break;
                }
            }

            // Have we drained all available bytes?
            if (!canDrain()) {
                setState(BufferState.FILLING);
            }
        }

        return builderState;
    }

    /**
     * Returns the byte buffer.
     * 
     * @return The byte buffer.
     */
    public ByteBuffer getBytes() {
        return bytes;
    }

    /**
     * Returns the byte buffer IO state.
     * 
     * @return The byte buffer IO state.
     */
    public BufferState getState() {
        return state;
    }

    /**
     * Indicates if the buffer state has the {@link BufferState#DRAINING} value.
     * 
     * @return True if the buffer state has the {@link BufferState#DRAINING}
     *         value.
     */
    public boolean isDraining() {
        return getState() == BufferState.DRAINING;
    }

    /**
     * Indicates if the buffer state has the {@link BufferState#FILLING} value.
     * 
     * @return True if the buffer state has the {@link BufferState#FILLING}
     *         value.
     */
    public boolean isFilling() {
        return getState() == BufferState.FILLING;
    }

    /**
     * Refills the byte buffer.
     * 
     * @param sourceChannel
     *            The byte channel to read from.
     * @return The number of bytes read and added to the buffer or -1 if end of
     *         channel reached.
     * @throws IOException
     */
    public int refill(ReadableByteChannel sourceChannel) throws IOException {
        int result = 0;

        if (getState() == BufferState.FILLING) {
            if (sourceChannel.isOpen()) {
                result = sourceChannel.read(getBytes());

                if (result > 0) {
                    getBytes().flip();
                    setState(BufferState.DRAINING);
                    Context.getCurrentLogger().fine(
                            "Refilled buffer with " + result + " byte(s)");
                }
            }
        }

        return result;
    }

    /**
     * Sets the byte buffer IO state.
     * 
     * @param byteBufferState
     *            The byte buffer IO state.
     */
    public void setState(BufferState byteBufferState) {
        this.state = byteBufferState;
    }

    @Override
    public String toString() {
        return getBytes().toString() + " | " + getState();
    }

}
