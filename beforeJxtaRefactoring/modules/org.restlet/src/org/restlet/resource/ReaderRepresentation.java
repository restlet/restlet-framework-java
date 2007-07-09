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
import java.io.Reader;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a BIO characters reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ReaderRepresentation extends CharacterRepresentation {
    /** The representation's reader. */
    private Reader reader;

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

    /**
     * Sets the reader to use.
     * 
     * @param reader
     *            The reader to use.
     */
    private void setReader(Reader reader) {
        this.reader = reader;
        setAvailable(reader != null);
    }

    @Override
    public synchronized Reader getReader() throws IOException {
        Reader result = this.reader;
        setReader(null);
        return result;
    }

    @Override
    public String getText() throws IOException {
        return ByteUtils.toString(getStream(), this.getCharacterSet());
    }

    @Override
    public void write(Writer writer) throws IOException {
        ByteUtils.write(getReader(), writer);
    }
}
