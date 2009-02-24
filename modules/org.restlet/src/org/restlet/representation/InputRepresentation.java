/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.io.ByteUtils;

/**
 * Transient representation based on a BIO input stream.
 * 
 * @author Jerome Louvel
 */
public class InputRepresentation extends StreamRepresentation {

    /** The representation's stream. */
    private volatile InputStream stream;

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
        setTransient(true);
        setStream(inputStream);
    }

    @Override
    public InputStream getStream() throws IOException {
        final InputStream result = this.stream;
        setStream(null);
        return result;
    }

    @Override
    public String getText() throws IOException {
        return ByteUtils.toString(getStream(), getCharacterSet());
    }

    /**
     * Closes and releases the input stream.
     */
    @Override
    public void release() {
        if (this.stream != null) {
            try {
                this.stream.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Error while releasing the representation.", e);
            }
            this.stream = null;
        }
        super.release();
    }

    /**
     * Sets the input stream to use.
     * 
     * @param stream
     *            The input stream to use.
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
        setAvailable(stream != null);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        ByteUtils.write(getStream(), outputStream);
    }

}
