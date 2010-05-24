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

package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.BioUtils;

/**
 * Representation based on a BIO character stream.
 * 
 * @author Jerome Louvel
 */
public abstract class CharacterRepresentation extends Representation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public CharacterRepresentation(MediaType mediaType) {
        super(mediaType);
        setCharacterSet(CharacterSet.UTF_8);
    }

    // [ifndef gwt] method
    @Override
    public java.nio.channels.ReadableByteChannel getChannel()
            throws IOException {
        return org.restlet.engine.io.NioUtils.getChannel(getStream());
    }

    // [ifndef gwt] method
    @Override
    public InputStream getStream() throws IOException {
        return BioUtils.getStream(getReader(), getCharacterSet());
    }

    // [ifndef gwt] method
    @Override
    public void write(OutputStream outputStream) throws IOException {
        BioUtils.copy(getStream(), outputStream);
    }

    // [ifndef gwt] method
    @Override
    public void write(java.nio.channels.WritableByteChannel writableChannel)
            throws IOException {
        write(org.restlet.engine.io.NioUtils.getStream(writableChannel));
    }

}
