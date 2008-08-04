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

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a BIO input stream.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class InputRepresentation extends StreamRepresentation {
    /** The representation's stream. */
    private InputStream inputStream;

    /**
     * Constructor.
     * 
     * @param inputStream
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     */
    public InputRepresentation(InputStream inputStream, MediaType mediaType) {
        this(inputStream, mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param inputStream
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected input stream size.
     */
    public InputRepresentation(InputStream inputStream, MediaType mediaType,
            long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        this.inputStream = inputStream;
        setAvailable(inputStream != null);
        setTransient(true);
    }

    /**
     * Returns a stream with the representation's content.
     * 
     * @return A stream with the representation's content.
     */
	@Override
    public synchronized InputStream getStream() throws IOException {
        InputStream result = this.inputStream;
        this.inputStream = null;
        setAvailable(false);
        return result;
    }

    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @return The representation as a string value.
     */
	@Override
    public String getText() throws IOException {
        return ByteUtils.toString(getStream(), this.getCharacterSet());
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
	@Override
    public void write(OutputStream outputStream) throws IOException {
        ByteUtils.write(getStream(), outputStream);
    }

}
