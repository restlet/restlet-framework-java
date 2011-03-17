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

import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read. It is capable of first using the remaining buffer before
 * reading more.
 */
public class ReadableBufferedChannel extends
        WrapperSelectionChannel<ReadableSelectionChannel> implements
        ReadableSelectionChannel, BufferProcessor, CompletionListener {

    /** The source IO buffer. */
    private final Buffer buffer;

    /** The completion callback. */
    private final CompletionListener completionListener;

    /** Indicates if the end of the channel has been reached. */
    private volatile boolean endReached;

    /**
     * Constructor.
     * 
     * @param completionListener
     *            The listener to callback upon reading completion.
     * @param buffer
     *            The source byte buffer, typically remaining from previous read
     *            processing.
     * @param source
     *            The source channel.
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            Buffer buffer, ReadableSelectionChannel source) {
        super(source);
        setRegistration(new SelectionRegistration(0, null));
        this.completionListener = completionListener;
        this.buffer = buffer;
        this.endReached = false;
    }

    /**
     * Indicates if the processing loop can continue.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the processing loop can continue.
     */
    public boolean canLoop(Buffer buffer, Object... args) {
        return true;
    }

    @Override
    public void close() throws IOException {
        // Don't actually close to protect the persistent connection
    }

    /**
     * Indicates if the buffer could be filled again.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the buffer could be filled again.
     */
    public boolean couldFill(Buffer buffer, Object... args) {
        return !isEndReached();
    }

    /**
     * Returns the source buffer.
     * 
     * @return The source buffer.
     */
    public Buffer getBuffer() {
        return buffer;
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
     * Indicates if the end of the channel has been reached.
     * 
     * @return True if the end of the channel has been reached.
     */
    protected boolean isEndReached() {
        return endReached;
    }

    /**
     * Callback invoked upon IO completion. Calls
     * {@link CompletionListener#onCompleted(boolean)} if the end has been
     * reached.
     * 
     * @param eofDetected
     *            Indicates if the end of network connection was detected.
     */
    public void onCompleted(boolean eofDetected) {
        if (getCompletionListener() != null) {
            getCompletionListener().onCompleted(eofDetected);
        }
    }

    /**
     * Called back when a fill operation returns with an EOF status.
     */
    public void onFillEof() {
    }

    /**
     * Drains the byte buffer.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param maxDrained
     *            The maximum number of bytes drained by this call.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @throws IOException
     */
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        return getBuffer().drain((ByteBuffer) args[0], maxDrained);
    }

    /**
     * Fills the byte buffer by writing the current message.
     * 
     * @throws IOException
     */
    public final int onFill(Buffer buffer, Object... args) throws IOException {
        int result = refill();

        if (result == -1) {
            setEndReached(true);
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
        return getBuffer().process(this, targetBuffer);
    }

    /**
     * Refills the IO buffer with the wrapped channel.
     * 
     * @return The number of bytes refilled.
     * @throws IOException
     */
    public int refill() throws IOException {
        return getBuffer().fill(getWrappedChannel());
    }

    /**
     * Indicates if the end of the channel has been reached.
     * 
     * @param endReached
     *            True if the end of the channel has been reached.
     */
    protected void setEndReached(boolean endReached) {
        this.endReached = endReached;

        if (this.endReached) {
            onCompleted(false);
        }
    }

}
