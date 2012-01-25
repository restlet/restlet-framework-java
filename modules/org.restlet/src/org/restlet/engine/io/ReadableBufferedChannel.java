/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.ByteBuffer;

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
     */
    public ReadableBufferedChannel(CompletionListener completionListener,
            Buffer buffer, ReadableSelectionChannel source) {
        super(buffer, source);
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
