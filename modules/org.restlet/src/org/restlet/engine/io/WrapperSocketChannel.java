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

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read.
 */
public class WrapperSocketChannel extends WrapperChannel<SocketChannel>
        implements SelectionChannel {

    /** The NIO registration. */
    private SelectionRegistration registration;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The source channel.
     * @param registration
     *            The NIO registration.
     */
    public WrapperSocketChannel(SocketChannel wrappedChannel,
            SelectionRegistration registration) {
        super(wrappedChannel);
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

    /**
     * Registers the given listener with the underlying selector and socket
     * channel for the operations of interest.
     * 
     * @param interestOperations
     *            The operations of interest.
     * @param listener
     *            The listener to notify.
     * @return The created registration.
     */
    public SelectionRegistration register(int interestOperations,
            SelectionListener listener) throws IOException {
        getRegistration().setInterestOperations(interestOperations);
        getRegistration().setListener(listener);
        return getRegistration();
    }

}
