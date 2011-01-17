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

import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read. It is capable of first using the remaining buffer before
 * reading more.
 */
public class ReadableBufferedChannel extends
        WrapperSelectionChannel<ReadableSelectionChannel> implements
        ReadableSelectionChannel, Drainer {

    /** The buffer state. */
    private volatile BufferState byteBufferState;

    /** The completion callback. */
    private final CompletionListener completionListener;

    /** The byte buffer remaining from previous read processing. */
    private final ByteBuffer byteBuffer;

    /**
     * Constructor.
     * 
     * @param completionListener
     *            The listener to callback upon reading completion.
     * @param remainingBuffer
     *            The byte buffer remaining from previous read processing.
     * @param remainingBufferState
     *            The initial remaining byte buffer state.
     * @param source
     *            The source channel.
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            ByteBuffer remainingBuffer, BufferState remainingBufferState,
            ReadableSelectionChannel source) {
        super(source);
        setRegistration(new SelectionRegistration(0, null));
        this.completionListener = completionListener;
        this.byteBuffer = remainingBuffer;
        this.byteBufferState = remainingBufferState;
    }

    @Override
    public void close() throws IOException {
        // Don't actually close to protect the persistent connection
    }

    /**
     * Returns the byte buffer remaining from previous read processing.
     * 
     * @return The byte buffer remaining from previous read processing.
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /**
     * Returns the byte buffer state.
     * 
     * @return The byte buffer state.
     */
    public BufferState getByteBufferState() {
        return byteBufferState;
    }

    /**
     * Returns the completion callback.
     * 
     * @return The completion callback.
     */
    private CompletionListener getCompletionListener() {
        return completionListener;
    }

    /**
     * Callback invoked upon IO completion. Calls
     * {@link CompletionListener#onCompleted(boolean)} if the end has been
     * reached.
     * 
     * @param endDetected
     *            Indicates if the end of network connection was detected.
     */
    public void onCompleted(boolean endDetected) {
        if (getCompletionListener() != null) {
            getCompletionListener().onCompleted(endDetected,
                    getByteBufferState());
        }
    }

    /**
     * Drains the byte buffer. By default, it directly copies as many byte as
     * possible to the target buffer, with no modification.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes added to the target buffer.
     */
    public int drain(ByteBuffer targetBuffer) {
        int result = 0;

        if (getByteBuffer().remaining() >= targetBuffer.remaining()) {
            // Target buffer will be full
            result = targetBuffer.remaining();
        } else {
            // Target buffer will not be full
            result = getByteBuffer().remaining();
        }

        // Copy the byte to the target buffer
        for (int i = 0; i < result; i++) {
            targetBuffer.put(getByteBuffer().get());
        }

        return result;
    }

    public boolean canRetry(int lastRead, ByteBuffer targetBuffer) {
        return (lastRead > 0) && targetBuffer.hasRemaining();
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @param drainer
     *            The drain to callback to drain available bytes.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer targetBuffer, Drainer drainer)
            throws IOException {
        int result = 0;
        int lastRead = 0;
        boolean tryAgain = true;

        synchronized (getByteBuffer()) {
            while (tryAgain) {
                switch (getByteBufferState()) {
                case FILLED:
                    setByteBufferState(BufferState.DRAINING);
                case DRAINING:
                    if (getByteBuffer().remaining() > 0) {
                        lastRead = drainer.drain(targetBuffer);
                        result += lastRead;
                        tryAgain = drainer.canRetry(lastRead, targetBuffer);
                    }

                    if (getByteBuffer().remaining() == 0) {
                        setByteBufferState(BufferState.FILLING);
                        getByteBuffer().clear();
                    }
                    break;
                case IDLE:
                    setByteBufferState(BufferState.FILLING);
                case FILLING:
                    int refillCount = refill();

                    if (refillCount > 0) {
                        tryAgain = true;
                    } else if (refillCount == -1) {
                        result = -1;
                        tryAgain = false;
                    } else if (refillCount == 0) {
                        tryAgain = false;
                    }
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer targetBuffer) throws IOException {
        return read(targetBuffer, this);
    }

    /**
     * Refills the byte buffer.
     * 
     * @return The number of bytes read and added to the buffer or -1 if end of
     *         channel reached.
     * @throws IOException
     */
    public int refill() throws IOException {
        int result = 0;

        if (getByteBufferState() == BufferState.FILLING) {
            if (getWrappedChannel().isOpen()) {
                result = getWrappedChannel().read(getByteBuffer());

                if (result > 0) {
                    setByteBufferState(BufferState.DRAINING);
                    getByteBuffer().flip();
                }
            }
        }

        return result;
    }

    /**
     * Sets the buffer state.
     * 
     * @param bufferState
     *            The buffer state.
     */
    public void setByteBufferState(BufferState bufferState) {
        this.byteBufferState = bufferState;
    }
}
