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

import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.buffer.BufferProcessor;
import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Byte channel wrapping a byte buffer.
 */
public abstract class BufferedSelectionChannel<T extends SelectionChannel>
        extends WrapperSelectionChannel<T> implements BufferProcessor {

    /** The source IO buffer. */
    private final Buffer buffer;

    /**
     * Constructor.
     * 
     * @param buffer
     *            The source byte buffer, typically remaining from previous read
     *            processing.
     * @param source
     *            The source channel.
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public BufferedSelectionChannel(Buffer buffer, T source,
            WakeupListener wakeupListener) {
        super(source);
        setRegistration(new SelectionRegistration(0, null, wakeupListener));
        this.buffer = buffer;
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
     * Returns the source buffer.
     * 
     * @return The source buffer.
     */
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * Called back when a fill operation returns with an EOF status.
     */
    public void onFillEof() {
    }

    /**
     * Does nothing by default.
     */
    public void postProcess(int drained) throws IOException {
    }

    /**
     * Does nothing by default.
     */
    public int preProcess(int maxDrained, Object... args) throws IOException {
        return 0;
    }

}
