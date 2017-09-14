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
import java.nio.channels.SocketChannel;

import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read.
 */
public class WritableSocketChannel extends WrapperSocketChannel implements
        WritableSelectionChannel {

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The source channel.
     * @param registration
     *            The NIO registration.
     */
    public WritableSocketChannel(SocketChannel wrappedChannel,
            SelectionRegistration registration) {
        super(wrappedChannel, registration);
    }

    /**
     * Writes the given bytes to the wrapped socket channel.
     * 
     * @param src
     *            The source byte buffer.
     * @return The number of bytes written.
     */
    public int write(ByteBuffer src) throws IOException {
        int count = 0;
        // Limit the number of loops
        int nbLoops = 0;
        while (src.hasRemaining() && nbLoops < src.capacity()) {
            count += getWrappedChannel().write(src);
            nbLoops++;
        }

        return count;
    }

}
