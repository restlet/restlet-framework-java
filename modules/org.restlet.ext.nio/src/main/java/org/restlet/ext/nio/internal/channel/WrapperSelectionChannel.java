/**
 * Copyright 2005-2020 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.nio.internal.channel;

import org.restlet.engine.io.SelectionChannel;
import org.restlet.util.SelectionRegistration;

/**
 * Wrapper selection channel.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions allowing more control at the Restlet API level.
 */
@Deprecated
public class WrapperSelectionChannel<T extends SelectionChannel> extends
        WrapperChannel<T> implements SelectionChannel {

    /** The NIO registration. */
    private volatile SelectionRegistration registration;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     */
    public WrapperSelectionChannel(T wrappedChannel) {
        this(wrappedChannel, wrappedChannel.getRegistration());
    }

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param registration
     *            The selection registration.
     */
    public WrapperSelectionChannel(T wrappedChannel,
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

    @Override
    public String toString() {
        return "WrapperSelectionChannel [toString()=" + super.toString()
                + ", registration=" + registration + "]";
    }

}
