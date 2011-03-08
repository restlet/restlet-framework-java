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

import org.restlet.util.SelectionRegistration;

/**
 * Readable byte channel enforcing a maximum size and wrapping a selectable
 * channel.
 * 
 * @author Jerome Louvel
 */
public class ReadableSizedSelectionChannel extends ReadableSizedChannel
        implements ReadableSelectionChannel {

    /** The NIO registration. */
    private volatile SelectionRegistration registration;

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel to limit.
     * @param availableSize
     *            The available size to read.
     * @param registration
     *            The selection registration.
     */
    public ReadableSizedSelectionChannel(ReadableBufferedChannel source,
            long availableSize) {
        this(source, availableSize, source.getRegistration());
    }

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel to limit.
     * @param availableSize
     *            The available size to read.
     * @param registration
     *            The selection registration.
     */
    public ReadableSizedSelectionChannel(ReadableBufferedChannel source,
            long availableSize, SelectionRegistration registration) {
        super(source, availableSize);
        this.registration = registration;
    }

    /**
     * Returns the NIO registration.
     * 
     * @return The NIO registration.
     */
    public SelectionRegistration getRegistration() {
        return registration;
    }

    @Override
    protected ReadableBufferedChannel getWrappedChannel() {
        return (ReadableBufferedChannel) super.getWrappedChannel();
    }

    /**
     * Indicates if the wrapped channel is blocking.
     * 
     * @return True if the wrapped channel is blocking.
     */
    public boolean isBlocking() {
        return getWrappedChannel().isBlocking();
    }

    /**
     * Sets the NIO registration.
     * 
     * @param registration
     *            The NIO registration.
     */
    public void setRegistration(SelectionRegistration registration) {
        this.registration = registration;
    }

}
