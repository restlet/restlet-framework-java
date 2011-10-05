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
     */
    public BufferedSelectionChannel(Buffer buffer, T source) {
        super(source);
        setRegistration(new SelectionRegistration(0, null));
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
