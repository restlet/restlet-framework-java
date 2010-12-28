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

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import javax.net.ssl.SSLEngine;

/**
 * Filter byte channel that enables secure communication using SSL/TLS
 * protocols. It is important to inherit from {@link SelectableChannel} as some
 * framework classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class SslChannel<T extends SelectionChannel> extends
        WrapperSelectionChannel<T> {

    /** The SSL engine to use of wrapping and unwrapping. */
    private SSLEngine engine;

    /** The secured byte buffer. */
    private final ByteBuffer packetBuffer;

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
    public SslChannel(T wrappedChannel, SSLEngine engine, ByteBuffer byteBuffer) {
        super(wrappedChannel);
        this.engine = engine;
        this.packetBuffer = byteBuffer;
    }

    /**
     * Returns the SSL engine to use of wrapping and unwrapping.
     * 
     * @return The SSL engine to use of wrapping and unwrapping.
     */
    public SSLEngine getEngine() {
        return this.engine;
    }

    /**
     * Returns the SSL/TLS packet byte buffer.
     * 
     * @return The SSL/TLS packet byte buffer.
     */
    protected ByteBuffer getPacketBuffer() {
        return packetBuffer;
    }

}
