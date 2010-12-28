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
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends SslChannel<ReadableSelectionChannel>
        implements ReadableSelectionChannel {

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param engine
     *            The SSL engine.
     * @param byteBuffer
     *            The byte buffer for SSL/TLS data.
     */
    public ReadableSslChannel(ReadableSelectionChannel wrappedChannel,
            SSLEngine sslEngine, ByteBuffer byteBuffer) {
        super(wrappedChannel, sslEngine, byteBuffer);
    }

    /**
     * Reads the available bytes from the wrapped channel to the destination
     * buffer while unwrapping them with the SSL/TLS protocols.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = 0;
        int remaining = getPacketBuffer().remaining();

        if (remaining == 0) {
            // Refill the packet buffer
            getPacketBuffer().clear();
            remaining = getWrappedChannel().read(getPacketBuffer());
        }

        if (remaining > 0) {
            // Unwrap the network data into application data
            getEngine().unwrap(getPacketBuffer(), dst);
            result = remaining - getPacketBuffer().remaining();
        }

        return result;
    }

}
