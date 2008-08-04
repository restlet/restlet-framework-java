/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.resource;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a readable NIO byte channel.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ReadableRepresentation extends ChannelRepresentation {
    /** The representation's input stream. */
    private ReadableByteChannel readableChannel;

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
     * @param readableChannel
     *            The representation's channel.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected stream size.
     */
    public ReadableRepresentation(ReadableByteChannel readableChannel,
            MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        this.readableChannel = readableChannel;
        setAvailable(readableChannel != null);
        setTransient(true);
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
	@Override
    public synchronized ReadableByteChannel getChannel() throws IOException {
        ReadableByteChannel result = this.readableChannel;
        this.readableChannel = null;
        setAvailable(false);
        return result;
    }

    /**
     * Writes the representation to a byte channel.
     * 
     * @param writableChannel
     *            A writable byte channel.
     */
	@Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        ByteUtils.write(getChannel(), writableChannel);
    }

}
