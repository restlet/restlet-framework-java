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
