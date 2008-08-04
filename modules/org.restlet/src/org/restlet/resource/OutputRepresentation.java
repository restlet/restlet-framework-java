/*
 * Copyright 2005-2007 Noelios Technologies.
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

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a BIO output stream. This class is a good basis to write 
 * your own representations, especially for the dynamic and large ones. For this you 
 * just need to create a subclass and override the abstract 
 * Representation.write(OutputStream) method. This method will later be called back
 * by the connectors when the actual representation's content is needed.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class OutputRepresentation extends StreamRepresentation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     */
    public OutputRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     * @param expectedSize
     *            The expected input stream size.
     */
    public OutputRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
    }

    /**
     * Returns a stream with the representation's content. Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * @return A stream with the representation's content.
     */
	@Override
    public InputStream getStream() throws IOException {
        return ByteUtils.getStream(this);
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
	@Override
    public abstract void write(OutputStream outputStream) throws IOException;

}
