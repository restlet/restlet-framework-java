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
    private volatile boolean endReached;

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
        this.endReached = false;
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
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = -1;

        if (getAvailableSize() > 0) {
            if (getAvailableSize() < dst.remaining()) {
                dst.limit((int) (getAvailableSize() + dst.position()));
            }

            result = getWrappedChannel().read(dst);
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
            this.endReached = true;
        }

        if ((result == -1)
                && (getWrappedChannel() instanceof CompletionListener)) {
            ((CompletionListener) getWrappedChannel())
                    .onCompleted(this.endReached);
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
}
