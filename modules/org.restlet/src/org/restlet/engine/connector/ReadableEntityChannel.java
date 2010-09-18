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

package org.restlet.engine.connector;

import java.io.IOException;
import java.nio.ByteBuffer;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read.
 */
public class ReadableEntityChannel extends
        WrapperSelectionChannel<ReadableSelectionChannel> implements
        ReadableSelectionChannel {

    /** The parent inbound way. */
    private final InboundWay inboundWay;

    /** The byte buffer remaining from previous read processing. */
    private volatile ByteBuffer remainingBuffer;

    /** The total available size that should be read from the source channel. */
    private volatile long availableSize;

    /**
     * Constructor.
     * 
     * @param inboundWay
     *            The parent inbound way.
     * @param remainingBuffer
     *            The byte buffer remaining from previous read processing.
     * @param source
     *            The source channel.
     * @param availableSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableEntityChannel(InboundWay inboundWay,
            ByteBuffer remainingBuffer, ReadableSelectionChannel source,
            long availableSize) {
        super(source);
        this.inboundWay = inboundWay;
        this.remainingBuffer = remainingBuffer;
        this.availableSize = availableSize;
    }

    /**
     * Returns the parent inbound way.
     * 
     * @return The parent inbound way.
     */
    public InboundWay getInboundWay() {
        return inboundWay;
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

        if (this.availableSize > 0) {
            if ((this.remainingBuffer != null)
                    && (this.remainingBuffer.hasRemaining())) {
                int limit = this.remainingBuffer.limit();
                int position = this.remainingBuffer.position();
                this.remainingBuffer.limit(limit);
                this.remainingBuffer.position(position);

                // First make sure that the remaining buffer is empty
                result = Math.min(limit - position, dst.remaining());
                byte[] src = new byte[result];
                this.remainingBuffer.get(src);
                dst.put(src);
            } else {
                // Otherwise, read data from the source channel
                result = getWrappedChannel().read(dst);
            }

            if (result > 0) {
                this.availableSize -= result;
            }
        }

        if (result == -1) {
            getInboundWay().onCompleted();
        }

        return result;
    }

}
