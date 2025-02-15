/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.representation;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.restlet.client.Context;
import org.restlet.client.data.MediaType;
import org.restlet.client.engine.io.IoUtils;

/**
 * Transient representation based on a BIO input stream.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public class InputRepresentation extends StreamRepresentation {

    /** The representation's stream. */
    private volatile InputStream stream;

    /**
     * Constructor.
     * 
     * @param inputStream
     *            The representation's stream.
     */
    public InputRepresentation(InputStream inputStream) {
        this(inputStream, null);
    }

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
        return this.stream;
    }

    /**
     * Note that this method relies on {@link #getStream()}. This stream is
     * closed once fully read.
     */
    @Override
    public String getText() throws IOException {
        return IoUtils.toString(getStream(), getCharacterSet());
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


}
