/*
 * Copyright 2005-2007 Noelios Consulting.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a BIO input stream.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class InputRepresentation extends StreamRepresentation {
    /** Obtain a suitable logger. */
    private static Logger logger = Logger.getLogger(InputRepresentation.class
            .getCanonicalName());

    /** The representation's stream. */
    private InputStream stream;

    /**
     * Constructor.
     * 
     * @param inputStream
     *                The representation's stream.
     * @param mediaType
     *                The representation's media type.
     */
    public InputRepresentation(InputStream inputStream, MediaType mediaType) {
        this(inputStream, mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param inputStream
     *                The representation's stream.
     * @param mediaType
     *                The representation's media type.
     * @param expectedSize
     *                The expected input stream size.
     */
    public InputRepresentation(InputStream inputStream, MediaType mediaType,
            long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        setTransient(true);
        setStream(inputStream);
    }

    @Override
    public synchronized InputStream getStream() throws IOException {
        InputStream result = this.stream;
        setStream(null);
        return result;
    }

    @Override
    public String getText() throws IOException {
        return ByteUtils.toString(getStream(), this.getCharacterSet());
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
                logger.log(Level.WARNING,
                        "Error while releasing the representation.", e);
            }

            this.stream = null;
        }
    }

    /**
     * Sets the input stream to use.
     * 
     * @param stream
     *                The input stream to use.
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
