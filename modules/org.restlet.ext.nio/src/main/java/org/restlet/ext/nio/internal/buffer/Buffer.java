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

package org.restlet.ext.nio.internal.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.ext.nio.internal.util.NioUtils;

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

    /** The byte buffer. */
    private final ByteBuffer bytes;

    /** The index of the buffer's beginning while filling. */
    private volatile int fillBegin;

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
        this.fillBegin = 0;
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
        return isFilling() ? (this.fillBegin > 0) : (getBytes().position() > 0);
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
        this.fillBegin = 0;
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
        } else {
            flip();
            compact();
            flip();
        }
    }

    /**
     * Indicates if bytes could be drained by flipping the buffer.
     * 
     * @return True if bytes could be drained.
     */
    public boolean couldDrain() {
        return (isFilling() && (getBytes().position() > this.fillBegin));
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
        return drain(targetBuffer, 0);
    }

    /**
     * Drains the byte buffer by copying as many bytes as possible to the target
     * buffer, with no modification.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @param maxDrained
     *            The maximum number of bytes drained by this call or 0 for
     *            unlimited length.
     * @return The number of bytes added to the target buffer.
     */
    public int drain(ByteBuffer targetBuffer, long maxDrained) {
        return NioUtils.copy(getBytes(), targetBuffer, maxDrained);
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
                                    + (char) next + "\" (" + next + ") instead");
                }

                break;

            default:
                // Nothing to do
                break;
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
     * Fills the byte buffer by copying as many bytes as possible from the
     * source buffer, with no modification.
     * 
     * @param sourceBuffer
     *            The source buffer.
     */
    public void fill(byte[] sourceBuffer) {
        getBytes().put(sourceBuffer);
    }

    /**
     * Fills the byte buffer by copying as many bytes as possible from the
     * source buffer, with no modification.
     * 
     * @param sourceBuffer
     *            The source buffer.
     * @return The number of bytes added from the source buffer.
     */
    public int fill(ByteBuffer sourceBuffer) {
        return fill(sourceBuffer, 0);
    }

    /**
     * Fills the byte buffer by copying as many bytes as possible from the
     * source buffer, with no modification.
     * 
     * @param sourceBuffer
     *            The source buffer.
     * @param maxFilled
     *            The maximum number of bytes filled by this call or 0 for
     *            unlimited length.
     * @return The number of bytes added from the source buffer.
     */
    public int fill(ByteBuffer sourceBuffer, long maxFilled) {
        return NioUtils.copy(sourceBuffer, getBytes(), maxFilled);
    }

    /**
     * Refills the byte buffer.
     * 
     * @param sourceChannel
     *            The byte channel to read from.
     * @return The number of bytes read and added or -1 if end of the source
     *         channel reached.
     * @throws IOException
     */
    public int fill(ReadableByteChannel sourceChannel) throws IOException {
        int result = 0;

        if (sourceChannel.isOpen()) {
            result = sourceChannel.read(getBytes());
        }

        return result;
    }

    /**
     * Fills the byte buffer by copying as many bytes as possible from the
     * source string, using the default platform encoding.
     * 
     * @param source
     *            The source string.
     */
    public void fill(String source) {
        fill(source.getBytes());
    }

    /**
     * Flip from draining to filling or the other way around.
     */
    public void flip() {
        if (isFilling()) {
            setState(BufferState.DRAINING);
            getBytes().limit(getBytes().position());
            getBytes().position(this.fillBegin);
            this.fillBegin = 0;
        } else if (isDraining()) {
            if (hasRemaining()) {
                setState(BufferState.FILLING);
                this.fillBegin = getBytes().position();
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
     * Indicates if the buffer is empty in either filling or draining state.
     * 
     * 
     * @return True if the buffer is empty.
     */
    public boolean isEmpty() {
        return isFilling() ? (capacity() == remaining()) : !hasRemaining();
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
     * @param maxDrained
     *            The maximum number of bytes drained by this call or 0 for
     *            unlimited length.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes drained or -1 if the filling source has
     *         ended.
     * @throws IOException
     */
    public int process(BufferProcessor processor, int maxDrained,
            Object... args) throws IOException {
        int result = 0;

        synchronized (getLock()) {
            int totalFilled = 0;
            int drained = 0;
            int filled = 0;
            boolean lastDrainFailed = false;
            boolean lastFillFailed = false;
            boolean fillEnded = false;
            boolean tryAgain = true;

            if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                Context.getCurrentLogger().log(Level.FINEST,
                        "Beginning process of buffer " + this);
            }

            // Calling back the processor for preparation work, such as
            // initiating a SSL handshake
            result += processor.preProcess(maxDrained, args);

            if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                Context.getCurrentLogger()
                        .log(Level.FINEST,
                                result
                                        + " bytes drained from buffer at pre-processing, "
                                        + remaining() + " remaining bytes");
            }

            while (tryAgain && processor.canLoop(this, args)) {
                if (isDraining()) {
                    if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                        Context.getCurrentLogger().log(Level.FINEST,
                                "Draining buffer " + this);
                    }

                    drained = 0;

                    if (hasRemaining()) {
                        if (maxDrained <= 0) {
                            drained = processor.onDrain(this, 0, args);
                        } else if (maxDrained > result) {
                            drained = processor.onDrain(this, maxDrained
                                    - result, args);
                        }
                    }

                    if (drained > 0) {
                        // Can attempt to drain again
                        result += drained;
                        lastDrainFailed = false;
                        lastFillFailed = false;

                        if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                            Context.getCurrentLogger().log(
                                    Level.FINEST,
                                    drained + " bytes drained from buffer, "
                                            + remaining() + " remaining bytes");
                        }
                    } else {
                        if (!lastFillFailed) {
                            if (couldFill()) {
                                // We may still be able to fill
                                beforeFill();
                            } else if (canCompact()) {
                                compact();
                            } else {
                                tryAgain = false;
                            }
                        } else {
                            tryAgain = false;
                        }

                        lastDrainFailed = true;
                    }
                } else if (isFilling()) {
                    if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                        Context.getCurrentLogger().log(Level.FINEST,
                                "Filling buffer " + this);
                    }

                    filled = 0;

                    if (hasRemaining() && processor.couldFill(this, args)) {
                        filled = processor.onFill(this, args);
                    }

                    if (filled > 0) {
                        // Can attempt to refill again
                        totalFilled += filled;
                        lastDrainFailed = false;
                        lastFillFailed = false;

                        if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                            Context.getCurrentLogger().log(Level.FINEST,
                                    filled + " bytes filled into buffer");
                        }
                    } else {
                        if (!lastDrainFailed && couldDrain()) {
                            // We may still be able to drain
                            beforeDrain();
                        } else {
                            tryAgain = false;
                        }

                        if (filled == -1) {
                            fillEnded = true;
                            processor.onFillEof();
                        }

                        lastFillFailed = true;
                    }
                } else {
                    // Can't drain nor fill
                    tryAgain = false;
                }
            }

            if ((result == 0)
                    && (!processor.couldFill(this, args) || fillEnded)) {
                // Nothing was drained and no hope to fill again
                result = -1;
            }

            if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                Context.getCurrentLogger().log(
                        Level.FINEST,
                        "Ending process of buffer " + this + ". Result: "
                                + result + ", try again: " + tryAgain
                                + ", can loop: "
                                + processor.canLoop(this, args)
                                + ", total filled: " + totalFilled);
            }

            processor.postProcess(result);
        }

        return result;
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
        return getBytes().toString() + ", " + getState() + ", " + isEmpty();
    }

}
