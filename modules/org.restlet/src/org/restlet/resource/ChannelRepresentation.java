/*
 * Copyright 2005-2008 Noelios Technologies.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a NIO byte channel.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class ChannelRepresentation extends Representation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public ChannelRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public Reader getReader() throws IOException {
        return ByteUtils.getReader(getStream(), getCharacterSet());
    }

    @Override
    public InputStream getStream() throws IOException {
        return ByteUtils.getStream(getChannel());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        write(ByteUtils.getChannel(outputStream));
    }

    @Override
    public void write(Writer writer) throws IOException {
        write(ByteUtils.getStream(writer));
    }

}
