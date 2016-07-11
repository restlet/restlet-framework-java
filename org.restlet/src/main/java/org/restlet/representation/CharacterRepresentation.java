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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

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
        return IoUtils.getChannel(getStream());
    }

    // [ifndef gwt] method
    @Override
    public InputStream getStream() throws IOException {
        return IoUtils.getStream(getReader(), getCharacterSet());
    }

    // [ifndef gwt] method
    @Override
    public void write(OutputStream outputStream) throws IOException {
        Writer writer = IoUtils.getWriter(outputStream, getCharacterSet());
        write(writer);
        writer.flush();
    }

    // [ifndef gwt] method
    @Override
    public void write(java.nio.channels.WritableByteChannel writableChannel)
            throws IOException {
        OutputStream os = IoUtils.getStream(writableChannel);
        write(os);
        os.flush();
    }

}
