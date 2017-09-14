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

package org.restlet.ext.nio.internal.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.util.CompletionListener;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read. It is capable of first using the remaining buffer before
 * reading more.
 */
public class ReadableBufferedChannel extends
        BufferedSelectionChannel<ReadableSelectionChannel> implements
        ReadableSelectionChannel, CompletionListener {

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
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            Buffer buffer, ReadableSelectionChannel source,
            WakeupListener wakeupListener) {
        super(buffer, source, wakeupListener);
        this.completionListener = completionListener;
        this.endReached = false;
    }

    /**
     * Indicates if the buffer could be drained again.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the buffer could be drained again.
     */
    public boolean couldDrain(Buffer buffer, Object... args) {
        return false;
    }

    /**
     * Indicates if the buffer could be filled again.
     * 
     * @param buffer
     *            The IO buffer to fill.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the buffer could be filled again.
     */
    public boolean couldFill(Buffer buffer, Object... args) {
        return !isEndReached();
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
        int result = getBuffer().fill(getWrappedChannel());

        if (result == -1) {
            setEndReached(true);
        }

        return result;
    }

    /**
     * Callback invoked upon IO completion. Calls
     * {@link CompletionListener#onMessageCompleted(boolean)} if the end has
     * been reached.
     * 
     * @param eofDetected
     *            Indicates if the end of network connection was detected.
     */
    public void onMessageCompleted(boolean eofDetected) throws IOException {
        if (getCompletionListener() != null) {
            getCompletionListener().onMessageCompleted(eofDetected);
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
        return getBuffer().process(this, 0, targetBuffer);
    }

    /**
     * Indicates if the end of the channel has been reached.
     * 
     * @param endReached
     *            True if the end of the channel has been reached.
     */
    protected void setEndReached(boolean endReached) throws IOException {
        if (this.endReached != endReached) {
            this.endReached = endReached;

            if (this.endReached) {
                onMessageCompleted(false);
            }
        }
    }

}
