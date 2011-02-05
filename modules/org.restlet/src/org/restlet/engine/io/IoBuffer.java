/**
 * Copyright 2005-2011 Noelios Technologies.
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
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.header.HeaderUtils;

/**
 * Wrapper around a byte buffer and its state.
 * 
 * @author Jerome Louvel
 */
public class IoBuffer {

    /** The index of the buffer's beginning while filling. */
    private volatile int begin;

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
        this.begin = 0;
        this.bytes = byteBuffer;
        this.state = byteBufferState;
    }

    /**
     * Ensure that the buffer is ready to be drained, flipping it if necessary
     * only.
     */
    public void beforeDrain() {
        if (isFilling()) {
            flip();
        }
    }

    /**
     * Ensure that the buffer is ready to be filled, flipping it if necessary
     * only.
     */
    public void beforeFill() {
        if (isDraining()) {
            flip();
        }
    }

    /**
     * Indicates if a compacting operation can be beneficial.
     * 
     * @return True if a compacting operation can be beneficial.
     */
    public boolean canCompact() {
        return this.begin > 0;
    }

    /**
     * Indicates if more bytes can be drained.
     * 
     * @return True if more bytes can be drained.
     */
    public boolean canDrain() {
        return isDraining() && hasRemaining();
    }

    /**
     * Indicates if more bytes can be filled in.
     * 
     * @return True if more bytes can be filled in.
     */
    public boolean canFill() {
        return isFilling() && hasRemaining();
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
     * Returns the maximum capacity of this buffer.
     * 
     * @return The maximum capacity of this buffer.
     */
    public final int capacity() {
        return getBytes().capacity();
    }

    /**
     * Recycles the buffer so it can be reused.
     */
    public void clear() {
        this.begin = 0;
        this.bytes.clear();
        this.state = BufferState.FILLING;
    }

    /**
     * Compacts the bytes to be drained at the beginning of the buffer.
     */
    public void compact() {
        if (isDraining()) {
            getBytes().compact();
            getBytes().flip();
        }
    }

    /**
     * Indicates if bytes could be drained by flipping the buffer.
     * 
     * @return True if bytes could be drained.
     */
    public boolean couldDrain() {
        return canDrain()
                || (isFilling() && (getBytes().position() > this.begin));
    }

    /**
     * Indicates if more bytes could be filled in.
     * 
     * @return True if more bytes could be filled in.
     */
    public boolean couldFill() {
        return canFill()
                || (isDraining() && (getBytes().limit() < getBytes().capacity()));
    }

    /**
     * Drains the byte buffer by copying as many bytes as possible to the target
     * buffer, with no modification.
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
     * Drain the buffer into a line builder (start line or header line).
     * 
     * @param lineBuilder
     *            The line builder to fill.
     * @param builderState
     *            The builder state.
     * @return The new builder state.
     * @throws IOException
     */
    public BufferState drain(StringBuilder lineBuilder, BufferState builderState)
            throws IOException {
        int next;

        if (isDraining()) {
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
     * Drains the byte buffer by attempting to write as much as possible on the
     * given channel.
     * 
     * @param wbc
     *            The byte channel to write to.
     * @return The number of bytes written.
     * @throws IOException
     */
    public int drain(WritableByteChannel wbc) throws IOException {
        return wbc.write(getBytes());
    }

    /**
     * Fills the byte buffer by copying as many bytes as possible to the target
     * buffer, with no modification.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes added to the target buffer.
     */
    public void fill(byte[] targetBuffer) {
        getBytes().put(targetBuffer);
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
    public int fill(ReadableByteChannel sourceChannel) throws IOException {
        int result = 0;

        if (isFilling()) {
            if (sourceChannel.isOpen()) {
                result = sourceChannel.read(getBytes());

                if (result > 0) {
                    flip();

                    if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                        Context.getCurrentLogger().finer(
                                "Refilled buffer with " + result + " byte(s)");
                    }
                } else {
                    if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                        Context.getCurrentLogger().finer(
                                "Coudn't refill buffer : " + toString());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Flip from draining to filling or the other way around.
     */
    public void flip() {
        if (getState() == BufferState.FILLING) {
            setState(BufferState.DRAINING);
            getBytes().limit(getBytes().position());
            getBytes().position(this.begin);
            this.begin = 0;
        } else if (getState() == BufferState.DRAINING) {
            if (hasRemaining()) {
                setState(BufferState.FILLING);
                this.begin = getBytes().position();
                getBytes().position(getBytes().limit());
                getBytes().limit(getBytes().capacity());
            } else {
                clear();
            }
        }
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
     * Returns the lock on which multiple thread can synchronize to ensure safe
     * access to the underlying byte buffer which isn't thread safe.
     * 
     * @return The lock on which multiple thread can synchronize.
     */
    public Object getLock() {
        return this.bytes;
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
     * Indicates if the buffer has remaining bytes to be read or written.
     * 
     * @return True if the buffer has remaining bytes to be read or written.
     */
    public final boolean hasRemaining() {
        return getBytes().hasRemaining();
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
     * Returns the number of bytes that can be read or written in the byte
     * buffer.
     * 
     * @return The number of bytes that can be read or written.
     */
    public final int remaining() {
        return getBytes().remaining();
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
