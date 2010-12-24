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

    /** The line builder to parse chunk size or trailer. */
    private final StringBuilder lineBuilder;

    /** The line builder state. */
    private volatile BufferState lineBuilderState;

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
        this.lineBuilder = new StringBuilder();
        this.lineBuilderState = BufferState.IDLE;
    }

    /**
     * Clears the line builder and adjust its state.
     */
    public void clearLineBuilder() {
        getLineBuilder().delete(0, getLineBuilder().length());
        setLineBuilderState(BufferState.IDLE);
    }

    @Override
    public void close() throws IOException {
        // Don't actually close to protect the persistent connection
    }

    /**
     * Read the current line builder (start line or header line).
     * 
     * @return True if the message line was fully read.
     * @throws IOException
     */
    public boolean fillLineBuilder() throws IOException {
        boolean result = false;

        if (getLineBuilderState() != BufferState.DRAINING) {
            int byteBufferSize = 0;

            if (getByteBufferState() == BufferState.DRAINING) {
                byteBufferSize = getByteBuffer().remaining();
            }

            if (byteBufferSize == 0) {
                setByteBufferState(BufferState.FILLING);
                getByteBuffer().clear();

                if (refill() > 0) {
                    byteBufferSize = getByteBuffer().remaining();
                }
            }

            if (byteBufferSize > 0) {
                // Some bytes are available, fill the line builder
                setLineBuilderState(NioUtils.fillLine(getLineBuilder(),
                        getLineBuilderState(), getByteBuffer()));

                if (getByteBuffer().remaining() == 0) {
                    setByteBufferState(BufferState.FILLING);
                    getByteBuffer().clear();
                }

                return getLineBuilderState() == BufferState.DRAINING;
            }
        } else {
            result = true;
        }

        return result;
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
     * Returns the line builder to parse chunk size or trailer.
     * 
     * @return The line builder to parse chunk size or trailer.
     */
    public StringBuilder getLineBuilder() {
        return lineBuilder;
    }

    /**
     * Returns the line builder state.
     * 
     * @return The line builder state.
     */
    protected BufferState getLineBuilderState() {
        return lineBuilderState;
    }

    /**
     * Post-read callback that calls {@link CompletionListener#onCompleted(boolean)} if
     * the end has been reached.
     * 
     * @param bytesRead
     *            The number of bytes read, or -1 if the end of the channel has
     *            been reached.
     */
    public void postRead(int bytesRead) {
        if ((bytesRead == -1) && (getCompletionListener() != null)) {
            getCompletionListener().onCompleted((bytesRead == -1));
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
        int result = 0;
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

                        result += currentRead;
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

                    if (refillCount == -1) {
                        result = -1;
                        tryAgain = false;
                    } else if (refillCount > 0) {
                        tryAgain = true;
                    }
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Refills the byte buffer.
     * 
     * @return The number of bytes read and added to the buffer or -1 if end of
     *         channel reached.
     * @throws IOException
     */
    protected int refill() throws IOException {
        int readCount = getWrappedChannel().read(getByteBuffer());

        if (readCount > 0) {
            setByteBufferState(BufferState.DRAINING);
            getByteBuffer().flip();
        }

        return readCount;
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

    /**
     * Sets the line builder state.
     * 
     * @param lineBuilderState
     *            The line builder state.
     */
    protected void setLineBuilderState(BufferState lineBuilderState) {
        this.lineBuilderState = lineBuilderState;
    }
}
