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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a BIO characters reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ReaderRepresentation extends InputRepresentation {
    /**
     * Constructor.
     * 
     * @param reader
     *            The representation's reader.
     * @param mediaType
     *            The representation's media type.
     */
    public ReaderRepresentation(Reader reader, MediaType mediaType) {
        super(null, mediaType);
        setInputStream(new ReaderInputStream(reader));
    }

    /**
     * Constructor.
     * 
     * @param reader
     *            The representation's reader.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected input stream size.
     */
    public ReaderRepresentation(Reader reader, MediaType mediaType,
            long expectedSize) {
        super(null, mediaType, expectedSize);
        setInputStream(new ReaderInputStream(reader));
    }

    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @return The representation as a string value.
     */
    public String getText() throws IOException {
        return ByteUtils.toString(getStream(), this.getCharacterSet());
    }

    /**
     * Input stream based on a reader.
     * 
     * @param reader
     *            The characters reader.
     * @param source
     *            The source representation with the target character set.
     * @return The encoding input stream.
     */
    class ReaderInputStream extends InputStream {
        private BufferedReader localReader;

        private byte[] buffer;

        private int index;

        public ReaderInputStream(Reader reader) {
            this.localReader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                    : new BufferedReader(reader);
            this.buffer = null;
            this.index = -1;
        }

        @Override
        public int read() throws IOException {
            int result = -1;

            // If the buffer is empty, read a new line
            if (this.buffer == null) {
                String line = localReader.readLine();

                if (line != null) {
                    this.buffer = line.getBytes(getCharacterSet().getName());
                    this.index = 0;
                }
            }

            if (this.buffer != null) {
                // Read the next byte and increment the index
                result = this.buffer[index++];

                // Check if the buffer has been fully read
                if (this.index == this.buffer.length) {
                    this.buffer = null;
                }
            }

            return result;
        }
    }

}
