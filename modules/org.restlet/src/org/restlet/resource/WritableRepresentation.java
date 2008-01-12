/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.resource;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a writable NIO byte channel. This class is a good
 * basis to write your own representations, especially for the dynamic and large
 * ones. For this you just need to create a subclass and override the abstract
 * Representation.write(WritableByteChannel) method. This method will later be
 * called back by the connectors when the actual representation's content is
 * needed.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class WritableRepresentation extends ChannelRepresentation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's media type.
     */
    public WritableRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return ByteUtils.getChannel(this);
    }

    @Override
    public abstract void write(WritableByteChannel writableChannel)
            throws IOException;

    /**
     * Default implementation with no behavior.
     */
    @Override
    public void release() {
    }

}
