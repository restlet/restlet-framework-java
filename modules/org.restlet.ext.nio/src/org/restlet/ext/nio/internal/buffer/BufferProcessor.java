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

package org.restlet.ext.nio.internal.buffer;

import java.io.IOException;

/**
 * Interface called back when IO buffer draining or filling can actually be
 * done.
 * 
 * @author Jerome Louvel
 */
public interface BufferProcessor {

    /**
     * Indicates if the processing loop can continue.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the processing loop can continue.
     */
    boolean canLoop(Buffer buffer, Object... args);

    /**
     * Indicates if the buffer could be filled again.
     * 
     * @param buffer
     *            The IO buffer to fill.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return True if the buffer could be filled again.
     */
    boolean couldFill(Buffer buffer, Object... args);

    /**
     * Drains the byte buffer.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param maxDrained
     *            The maximum number of bytes drained by this call.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes drained.
     * @throws IOException
     */
    int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException;

    /**
     * Fills the byte buffer.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes filled.
     * @throws IOException
     */
    int onFill(Buffer buffer, Object... args) throws IOException;

    /**
     * Called back when a fill operation returns with an EOF status.
     */
    void onFillEof();

    /**
     * Called back after a complete processing pass.
     * 
     * @param drained
     *            The number of bytes drained or -1 if the filling source has
     *            ended.
     */
    void postProcess(int drained) throws IOException;

    /**
     * Called back before a processing pass.
     * 
     * @param maxDrained
     *            The maximum number of bytes drained by this call or 0 for
     *            unlimited length.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes drained or -1 if the filling source has
     *         ended.
     */
    int preProcess(int maxDrained, Object... args) throws IOException;

}
