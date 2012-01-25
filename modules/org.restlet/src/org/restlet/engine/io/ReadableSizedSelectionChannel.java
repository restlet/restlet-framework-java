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
import java.util.logging.Level;

import org.restlet.Context;

/**
 * Readable byte channel enforcing a maximum size and wrapping a selectable
 * channel.
 * 
 * @author Jerome Louvel
 */
public class ReadableSizedSelectionChannel extends ReadableBufferedChannel {

    /** The remaining size that should be read from the source channel. */
    private volatile long availableSize;

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
     * @param availableSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableSizedSelectionChannel(CompletionListener completionListener,
            Buffer buffer, ReadableSelectionChannel source, long availableSize) {
        super(completionListener, buffer, source);
        this.availableSize = availableSize;
    }

    /**
     * Returns the remaining size that should be read from the source channel.
     * 
     * @return The remaining size that should be read from the source channel.
     */
    protected long getAvailableSize() {
        return availableSize;
    }

    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        int result = 0;

        if (getAvailableSize() > 0) {
            result = super.onDrain(buffer, (int) getAvailableSize(), args);
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
            setEndReached(true);
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
        setEndReached(this.availableSize == 0);
    }

}
