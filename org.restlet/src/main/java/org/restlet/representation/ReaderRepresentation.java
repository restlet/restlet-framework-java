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
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Transient representation based on a BIO characters reader.
 * 
 * @author Jerome Louvel
 */
public class ReaderRepresentation extends CharacterRepresentation {

    /** The representation's reader. */
    private volatile Reader reader;

    /**
     * Constructor.
     * 
     * @param reader
     *            The representation's stream.
     */
    public ReaderRepresentation(Reader reader) {
        this(reader, null);
    }

    /**
     * Constructor.
     * 
     * @param reader
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     */
    public ReaderRepresentation(Reader reader, MediaType mediaType) {
        this(reader, mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param reader
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected reader size in bytes.
     */
    public ReaderRepresentation(Reader reader, MediaType mediaType,
            long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        setTransient(true);
        setReader(reader);
    }

    @Override
    public Reader getReader() throws IOException {
        final Reader result = this.reader;
        setReader(null);
        return result;
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
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Error while releasing the representation.", e);
            }

            this.reader = null;
        }

        super.release();
    }

    /**
     * Sets the reader to use.
     * 
     * @param reader
     *            The reader to use.
     */
    public void setReader(Reader reader) {
        this.reader = reader;
        setAvailable(reader != null);
    }

    @Override
    public void write(Writer writer) throws IOException {
        IoUtils.copy(getReader(), writer);
    }
}
