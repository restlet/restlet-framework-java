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

package org.restlet.representation;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.io.NioUtils;

/**
 * Transient representation based on a readable NIO byte channel.
 * 
 * @author Jerome Louvel
 */
public class ReadableRepresentation extends ChannelRepresentation {

    /** The representation's input stream. */
    private volatile ReadableByteChannel channel;

    /**
     * Constructor.
     * 
     * @param readableChannel
     *            The representation's channel.
     * @param mediaType
     *            The representation's media type.
     */
    public ReadableRepresentation(ReadableByteChannel readableChannel,
            MediaType mediaType) {
        this(readableChannel, mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param channel
     *            The representation's channel.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected stream size.
     */
    public ReadableRepresentation(ReadableByteChannel channel,
            MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        this.channel = channel;
        setAvailable(channel != null);
        setTransient(true);
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        ReadableByteChannel result = this.channel;
        setAvailable(false);
        return result;
    }

    /**
     * Closes and releases the readable channel.
     */
    @Override
    public void release() {
        if (this.channel != null) {
            try {
                this.channel.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Error while releasing the representation.", e);
            }

            this.channel = null;
        }

        super.release();
    }

    /**
     * Sets the readable channel.
     * 
     * @param channel
     *            The readable channel.
     */
    public void setChannel(ReadableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        NioUtils.copy(getChannel(), writableChannel);
    }

}
