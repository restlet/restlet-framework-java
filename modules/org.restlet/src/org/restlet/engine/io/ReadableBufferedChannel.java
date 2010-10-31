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
        ReadableSelectionChannel {

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
     * @param source
     *            The source channel.
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            ByteBuffer remainingBuffer, ReadableSelectionChannel source) {
        super(source);
        setRegistration(new SelectionRegistration(0, null));
        this.completionListener = completionListener;
        this.byteBuffer = remainingBuffer;
        this.byteBufferState = BufferState.DRAINING;
    }

    /**
     * Returns the byte buffer remaining from previous read processing.
     * 
     * @return The byte buffer remaining from previous read processing.
     */
    protected ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /**
     * Returns the byte buffer state.
     * 
     * @return The byte buffer state.
     */
    protected BufferState getByteBufferState() {
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
     * Post-read callback that calls {@link CompletionListener#onCompleted()} if
     * the end has been reached.
     * 
     * @param length
     */
    protected void postRead(int length) {
        if (length == -1) {
            getCompletionListener().onCompleted();
        }
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
        int totalRead = 0;
        int currentRead = 0;
        boolean tryAgain = true;

        synchronized (getByteBuffer()) {
            while (tryAgain) {
                switch (getByteBufferState()) {
                case FILLED:
                    setByteBufferState(BufferState.DRAINING);
                case DRAINING:
                    if (getByteBuffer().remaining() > 0) {
                        if (getByteBuffer().remaining() >= targetBuffer
                                .remaining()) {
                            // Target buffer will be full
                            currentRead = targetBuffer.remaining();
                            tryAgain = false;
                        } else {
                            // Target buffer will not be full
                            currentRead = getByteBuffer().remaining();
                        }

                        // Copy the byte to the target buffer
                        for (int i = 0; i < currentRead; i++) {
                            targetBuffer.put(getByteBuffer().get());
                        }

                        totalRead += currentRead;
                    }

                    if (getByteBuffer().remaining() == 0) {
                        setByteBufferState(BufferState.FILLING);
                        getByteBuffer().clear();
                    }
                    break;
                case IDLE:
                    setByteBufferState(BufferState.FILLING);
                case FILLING:
                    tryAgain = refill();
                    break;
                }
            }
        }

        return totalRead;
    }

    /**
     * Refills the byte buffer.
     * 
     * @return True if the refilling was successful.
     * @throws IOException
     */
    protected boolean refill() throws IOException {
        boolean result = false;

        if (getWrappedChannel().read(getByteBuffer()) > 0) {
            setByteBufferState(BufferState.DRAINING);
            getByteBuffer().flip();
            result = true;
        }

        return result;
    }

    /**
     * Sets the buffer state.
     * 
     * @param bufferState
     *            The buffer state.
     */
    protected void setByteBufferState(BufferState bufferState) {
        this.byteBufferState = bufferState;
    }
}
