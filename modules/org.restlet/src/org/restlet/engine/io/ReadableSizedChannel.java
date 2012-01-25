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
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;

// [excludes gwt]
/**
 * Readable byte channel enforcing a maximum size.
 */
public class ReadableSizedChannel extends WrapperChannel<ReadableByteChannel>
        implements ReadableByteChannel {

    /** The total available size that should be read from the source channel. */
    private volatile long availableSize;

    /** Indicates if the end of the wrapped channel has been reached. */
    private volatile boolean endDetected;

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel.
     * @param availableSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableSizedChannel(ReadableByteChannel source, long availableSize) {
        super(source);
        this.availableSize = availableSize;
        this.endDetected = false;
    }

    /**
     * Returns the remaining size that should be read from the source channel.
     * 
     * @return The remaining size that should be read from the source channel.
     */
    protected long getAvailableSize() {
        return availableSize;
    }

    /**
     * Indicates if the end of the channel has been detected.
     * 
     * @return True if the end of the channel has been detected.
     */
    protected boolean isEndDetected() {
        return endDetected;
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = 0;

        if (getAvailableSize() > 0) {
            if (getAvailableSize() < dst.remaining()) {
                dst.limit((int) (getAvailableSize() + dst.position()));
            }

            result = getWrappedChannel().read(dst);
        } else {
            result = -1;
        }

        if (result > 0) {
            setAvailableSize(getAvailableSize() - result);

            if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                Context.getCurrentLogger().finer(
                        "Bytes (read | available) : " + result + " | "
                                + getAvailableSize());
            }

            if (getAvailableSize() == 0) {
                if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                    Context.getCurrentLogger().finer("Channel fully read.");
                }
            }
        } else if (result == -1) {
            setEndDetected(true);
        }

        return result;
    }

    /**
     * Sets the remaining size that should be read from the source channel.
     * 
     * @param availableSize
     *            The remaining size that should be read from the source
     *            channel.
     */
    protected void setAvailableSize(long availableSize) {
        this.availableSize = availableSize;
    }

    /**
     * Indicates if the end of the channel has been detected.
     * 
     * @param endDetected
     *            True if the end of the channel has been detected.
     */
    protected void setEndDetected(boolean endDetected) {
        this.endDetected = endDetected;

        if (endDetected && (getWrappedChannel() instanceof CompletionListener)) {
            ((CompletionListener) getWrappedChannel())
                    .onCompleted(isEndDetected());
        }

    }
}
