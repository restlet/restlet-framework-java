/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.engine.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;

import org.restlet.data.CharacterSet;

// [excludes gwt]
/**
 * Input stream based on a reader.
 * 
 * @author Jerome Louvel
 */
public class ReaderInputStream extends InputStream {
    /**
     * Writer to an output stream that converts characters according to a given
     * character set.
     */
    private final OutputStreamWriter outputStreamWriter;

    /** Input stream that gets its content from the piped output stream. */
    private final PipedInputStream pipedInputStream;

    /** Output stream that sends its content to the piped input stream. */
    private final PipedOutputStream pipedOutputStream;

    /** The wrapped reader. */
    private final BufferedReader reader;

    /**
     * Constructor.
     * 
     * @param reader
     *            The reader to wrap as an input stream.
     * @param characterSet
     *            The character set to use for encoding.
     * @throws IOException
     */
    public ReaderInputStream(Reader reader, CharacterSet characterSet)
            throws IOException {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                : new BufferedReader(reader, IoUtils.getBufferSize());
        this.pipedInputStream = new PipedInputStream();
        this.pipedOutputStream = new PipedOutputStream(this.pipedInputStream);

        if (characterSet != null) {
            this.outputStreamWriter = new OutputStreamWriter(
                    this.pipedOutputStream, characterSet.getName());
        } else {
            this.outputStreamWriter = new OutputStreamWriter(
                    this.pipedOutputStream, CharacterSet.ISO_8859_1.getName());
        }
    }

    @Override
    public int available() throws IOException {
        return this.pipedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
        this.outputStreamWriter.close();
        this.pipedInputStream.close();
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        if (this.pipedInputStream.available() == 0) {
            int character = this.reader.read();

            if (character != -1) {
                this.outputStreamWriter.write(character);
                this.outputStreamWriter.flush();
                this.pipedOutputStream.flush();
                result = this.pipedInputStream.read();
            }
        } else {
            result = this.pipedInputStream.read();
        }

        return result;
    }

}