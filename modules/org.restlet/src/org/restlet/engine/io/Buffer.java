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
public class Buffer {

    /**
     * Creates a new byte buffer.
     * 
     * @param bufferSize
     *            The buffer size.
     * @param direct
     *            Indicates if a direct NIO buffer should be created.
     * @return The created byte buffer.
     */
    private static ByteBuffer createByteBuffer(int bufferSize, boolean direct) {
        ByteBuffer result = null;

        if (direct) {
            result = ByteBuffer.allocateDirect(bufferSize);
        } else {
            result = ByteBuffer.allocate(bufferSize);
        }

        return result;
    }

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
    public Buffer(ByteBuffer byteBuffer) {
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
    public Buffer(ByteBuffer byteBuffer, BufferState byteBufferState) {
        super();
        this.begin = 0;
        this.bytes = byteBuffer;
        this.state = byteBufferState;
    }

    /**
     * Constructor. Allocates a new non-direct byte buffer.
     * 
     * @param bufferSize
     *            The byte buffer size.
     */
    public Buffer(int bufferSize) {
        this(bufferSize, false);
    }

    /**
     * Constructor. Allocates a new byte buffer using
     * {@link ByteBuffer#allocate(int)} or
     * {@link ByteBuffer#allocateDirect(int)} methods.
     * 
     * @param bufferSize
     *            The byte buffer size.
     * @param direct
     *            Indicates if a direct NIO buffer should be created.
     */
    public Buffer(int bufferSize, boolean direct) {
        this(createByteBuffer(bufferSize, direct));
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
        return (isFilling() && (getBytes().position() > this.begin));
    }

    /**
     * Indicates if more bytes could be filled in.
     * 
     * @return True if more bytes could be filled in.
     */
    public boolean couldFill() {
        return (isDraining() && (!hasRemaining() || (getBytes().limit() < getBytes()
                .capacity())));
    }

    /**
     * Drains the next byte in the buffer and returns it as an integer.
     * 
     * @return The next byte in the buffer;
     */
    public int drain() {
        return getBytes().get() & 0xff;
    }

    /**
     * Drains the buffer into a byte array.
     * 
     * @param targetArray
     *            The target byte array.
     * @param offset
     *            The target offset.
     * @param length
     *            The number of bytes to drain.
     */
    public void drain(byte[] targetArray, int offset, int length) {
        getBytes().get(targetArray, offset, length);
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
     * Drains the buffer into a line builder (start line or header line).
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

        if (canFill()) {
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
        if (isFilling()) {
            setState(BufferState.DRAINING);
            getBytes().limit(getBytes().position());
            getBytes().position(this.begin);
            this.begin = 0;
        } else if (isDraining()) {
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
     * Processes as a loop the IO event by draining or filling the IO buffer.
     * Note that synchronization of the {@link #getLock()} object is
     * automatically made.
     * 
     * @param processor
     *            The IO processor to callback.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes drained or -1 if the filling source has
     *         ended.
     * @throws IOException
     */
    public int process(BufferProcessor processor, Object... args)
            throws IOException {
        int totalDrained = 0;
        int totalFilled = 0;

        synchronized (getLock()) {
            if (processor.couldFill()) {
                boolean tryAgain = true;
                int drained = 0;
                int filled = 0;
                boolean lastDrainFailed = false;
                boolean lastFillFailed = false;

                while (tryAgain && processor.canLoop()) {
                    if (isDraining()) {
                        drained = 0;

                        if (hasRemaining()) {
                            drained = processor.onDrain(this, args);
                        }

                        if (drained > 0) {
                            // Can attempt to drain again
                            totalDrained += drained;
                            lastDrainFailed = false;
                            lastFillFailed = false;
                        } else if (!lastFillFailed && couldFill()) {
                            // We may still be able to fill
                            lastDrainFailed = true;
                            beforeFill();
                        } else {
                            tryAgain = false;
                        }
                    } else if (isFilling()) {
                        filled = 0;

                        if (hasRemaining()) {
                            filled = processor.onFill(this, args);
                        }

                        if (filled > 0) {
                            // Can attempt to refill again
                            totalFilled += filled;
                            lastDrainFailed = false;
                            lastFillFailed = false;
                        } else if (!lastDrainFailed && couldDrain()) {
                            // We may still be able to drain
                            lastFillFailed = true;
                            beforeDrain();
                        } else {
                            tryAgain = false;
                        }
                    } else {
                        // Can't drain nor fill
                        tryAgain = false;
                    }
                }

                if ((totalDrained == 0) && !processor.couldFill()) {
                    // Nothing was drained and no hope to fill again
                    totalDrained = -1;
                }
            } else {
                totalDrained = -1;
            }
        }

        return totalDrained;
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
