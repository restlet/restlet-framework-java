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
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a BIO character stream.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return ByteUtils.getChannel(getStream());
    }

    @Override
    public InputStream getStream() throws IOException {
        return new ReaderInputStream(getReader());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        ByteUtils.write(getStream(), outputStream);
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        write(ByteUtils.getStream(writableChannel));
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
