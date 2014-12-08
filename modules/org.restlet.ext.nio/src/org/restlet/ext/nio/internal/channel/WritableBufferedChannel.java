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

import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;

// [excludes gwt]
/**
 * Writable byte channel based on a target socket channel. It is capable of
 * first filling a buffer before draining it to the target channel.
 */
public class WritableBufferedChannel extends
        BufferedSelectionChannel<WritableSelectionChannel> implements
        WritableSelectionChannel {

    /**
     * Constructor.
     * 
     * @param buffer
     *            The source byte buffer, typically remaining from previous read
     *            processing.
     * @param target
     *            The target channel.
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public WritableBufferedChannel(Buffer buffer,
            WritableSelectionChannel target, WakeupListener wakeupListener) {
        super(buffer, target, wakeupListener);
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
        return true;
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
    public final int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        return getBuffer().drain(getWrappedChannel());
    }

    /**
     * Fills the byte buffer by writing the current message.
     * 
     * @throws IOException
     */
    public int onFill(Buffer buffer, Object... args) throws IOException {
        return getBuffer().fill((ByteBuffer) args[0]);
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param sourceBuffer
     *            The source buffer.
     * @return The number of bytes written, possibly 0.
     */
    public int write(ByteBuffer sourceBuffer) throws IOException {
        return getBuffer().process(this, 0, sourceBuffer);
    }

}
