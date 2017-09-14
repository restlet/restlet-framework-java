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

package org.restlet.representation;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

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
        IoUtils.copy(getChannel(), writableChannel);
    }

}
