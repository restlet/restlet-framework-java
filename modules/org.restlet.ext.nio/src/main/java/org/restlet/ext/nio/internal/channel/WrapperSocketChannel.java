/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.nio.internal.channel;

import java.nio.channels.SocketChannel;

import org.restlet.engine.io.SelectionChannel;
import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read.
 * @deprecated Will be removed to favor lower-level network extensions allowing more control at the Restlet API level.
 */
@Deprecated
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
     * Indicates if the channel is blocking.
     * 
     * @return True if the channel is blocking.
     */
    public boolean isBlocking() {
        return getWrappedChannel().isBlocking();
    }

}
