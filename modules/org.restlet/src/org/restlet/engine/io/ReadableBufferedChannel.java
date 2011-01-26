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

    /** The completion callback. */
    private final CompletionListener completionListener;

    /** The source IO buffer. */
    private final IoBuffer sourceBuffer;

    /**
     * Constructor.
     * 
     * @param completionListener
     *            The listener to callback upon reading completion.
     * @param sourceBuffer
     *            The source byte buffer, typically remaining from previous read
     *            processing.
     * @param source
     *            The source channel.
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            IoBuffer sourceBuffer, ReadableSelectionChannel source) {
        super(source);
        setRegistration(new SelectionRegistration(0, null));
        this.completionListener = completionListener;
        this.sourceBuffer = sourceBuffer;
    }

    @Override
    public void close() throws IOException {
        // Don't actually close to protect the persistent connection
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
     * Returns the source buffer.
     * 
     * @return The source buffer.
     */
    public IoBuffer getSourceBuffer() {
        return sourceBuffer;
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
            getCompletionListener().onCompleted(endDetected);
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
        return read(targetBuffer, getSourceBuffer());
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @param sourceBuffer
     *            The buffer to drain for available bytes.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer targetBuffer, IoBuffer sourceBuffer)
            throws IOException {
        int result = 0;
        int lastRead = 0;
        boolean tryAgain = true;

        synchronized (getSourceBuffer().getLock()) {
            while (tryAgain) {
                switch (getSourceBuffer().getState()) {
                case FILLED:
                    getSourceBuffer().setState(BufferState.DRAINING);
                case DRAINING:
                    if (getSourceBuffer().remaining() > 0) {
                        lastRead = sourceBuffer.drain(targetBuffer);
                        result += lastRead;
                        tryAgain = sourceBuffer
                                .canRetry(lastRead, targetBuffer);
                    }

                    if (!getSourceBuffer().hasRemaining()) {
                        getSourceBuffer().clear();
                    }
                    break;
                case IDLE:
                    getSourceBuffer().setState(BufferState.FILLING);
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
     * Refills the IO buffer with the wrapped channel.
     * 
     * @return The number of bytes refilled.
     * @throws IOException
     */
    public int refill() throws IOException {
        return getSourceBuffer().fill(getWrappedChannel());
    }

}
